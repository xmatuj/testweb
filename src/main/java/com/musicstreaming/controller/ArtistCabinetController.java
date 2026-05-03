package com.musicstreaming.controller;

import com.musicstreaming.dto.ArtistProfileDTO;
import com.musicstreaming.model.Moderation;
import com.musicstreaming.model.User;
import com.musicstreaming.repository.ModerationRepository;
import com.musicstreaming.service.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/artist")
public class ArtistCabinetController {

    private static final Logger logger = LoggerFactory.getLogger(ArtistCabinetController.class);

    @Autowired
    private ArtistService artistService;

    @Autowired
    private TrackService trackService;

    @Autowired
    private AuthService authService;

    @Autowired
    private GenreService genreService;

    @Autowired
    private AlbumService albumService;

    @Autowired
    private AudioMetadataService audioMetadataService;

    @Autowired
    private ModerationRepository moderationRepository;

    // Определяет статус трека на основе записей в Moderations
    private String getTrackStatus(com.musicstreaming.model.Track track) {
        // Если трек одобрен (isModerated = true)
        if (track.isModerated()) {
            return "approved";
        }

        // Проверяем последнюю запись в Moderations
        Optional<Moderation> latestModeration = moderationRepository.findLatestByTrackId(track.getId());

        // Если есть запись и статус Rejected - трек отклонен
        if (latestModeration.isPresent() && latestModeration.get().getStatus() == Moderation.ModerationStatus.Rejected) {
            return "rejected";
        }

        // Во всех остальных случаях - на модерации
        return "pending";
    }

    @GetMapping("/cabinet")
    public String artistCabinet(Model model, HttpServletRequest request,
                                RedirectAttributes redirectAttributes) {
        User currentUser = authService.getCurrentUser(request);

        if (currentUser == null) {
            return "redirect:/account/login";
        }

        if (currentUser.isAdmin() && !currentUser.isMusician()) {
            return "redirect:/account/profile";
        }

        if (!currentUser.isMusician()) {
            redirectAttributes.addFlashAttribute("error", "Доступ запрещен. Только для музыкантов.");
            return "redirect:/account/profile";
        }

        ArtistProfileDTO artistProfile = artistService.getArtistProfile(currentUser.getId());

        // статусы модерации для треков
        Map<Integer, String> trackStatuses = new HashMap<>();
        if (artistProfile.getPopularTracks() != null) {
            for (com.musicstreaming.model.Track track : artistProfile.getPopularTracks()) {
                trackStatuses.put(track.getId(), getTrackStatus(track));
            }
        }
        if (artistProfile.getTracks() != null) {
            for (com.musicstreaming.model.Track track : artistProfile.getTracks()) {
                trackStatuses.put(track.getId(), getTrackStatus(track));
            }
        }

        model.addAttribute("artist", artistProfile);
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("isAuthenticated", true);
        model.addAttribute("isAdmin", currentUser.isAdmin());
        model.addAttribute("isMusician", currentUser.isMusician());
        model.addAttribute("genres", genreService.findAll());
        model.addAttribute("artists", artistService.findAll());
        model.addAttribute("trackStatuses", trackStatuses); // ДОБАВЛЕНО

        return "artist/cabinet";
    }

    @GetMapping("/api/albums-by-artist")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getAlbumsByArtist(@RequestParam Integer artistId) {
        Map<String, Object> response = new HashMap<>();
        try {
            List<com.musicstreaming.model.Album> albums = albumService.findByArtistId(artistId);
            List<Map<String, Object>> albumList = albums.stream().map(album -> {
                Map<String, Object> map = new HashMap<>();
                map.put("id", album.getId());
                map.put("title", album.getTitle());
                return map;
            }).collect(Collectors.toList());

            response.put("success", true);
            response.put("albums", albumList);
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
        }
        return ResponseEntity.ok(response);
    }

    @PostMapping("/upload-track")
    public String uploadTrack(@RequestParam String title,
                              @RequestParam Integer artistId,
                              @RequestParam(required = false) Integer albumId,
                              @RequestParam Integer genreId,
                              @RequestParam("audioFile") MultipartFile audioFile,
                              @RequestParam(value = "coverFile", required = false) MultipartFile coverFile,
                              HttpServletRequest request,
                              RedirectAttributes redirectAttributes) {
        User currentUser = authService.getCurrentUser(request);

        if (currentUser == null || !currentUser.isMusician()) {
            redirectAttributes.addFlashAttribute("error", "Доступ запрещен");
            return "redirect:/account/profile";
        }

        try {
            com.musicstreaming.model.Track track = new com.musicstreaming.model.Track();
            track.setTitle(title);
            track.setDuration(180);
            track.setUploadedByUser(currentUser);
            track.setModerated(false);

            if (artistId != null && artistId > 0) {
                artistService.findById(artistId).ifPresent(track::setArtist);
            }

            if (albumId != null && albumId > 0) {
                albumService.findById(albumId).ifPresent(track::setAlbum);
            }

            genreService.findById(genreId).ifPresent(track::setGenre);

            if (audioFile != null && !audioFile.isEmpty()) {
                String userDir = System.getProperty("user.dir");
                Path uploadDir = Paths.get(userDir, "uploads", "music");

                if (!Files.exists(uploadDir)) {
                    Files.createDirectories(uploadDir);
                }

                String originalFilename = audioFile.getOriginalFilename();
                String extension = ".mp3";
                if (originalFilename != null && originalFilename.contains(".")) {
                    extension = originalFilename.substring(originalFilename.lastIndexOf("."));
                }
                String filename = UUID.randomUUID().toString() + extension;

                Path destFile = uploadDir.resolve(filename);
                audioFile.transferTo(destFile.toFile());

                track.setFilePath(filename);

                int duration = audioMetadataService.getDurationInSeconds(destFile.toString());
                if (duration > 0) {
                    track.setDuration(duration);
                    logger.info("Auto-detected duration for track '{}': {} seconds", title, duration);
                } else {
                    logger.warn("Could not detect duration for track '{}', using default 180 seconds", title);
                    track.setDuration(180);
                }
            }

            trackService.save(track);
            redirectAttributes.addFlashAttribute("success",
                    "Трек успешно загружен и отправлен на модерацию! Длительность: " + track.getFormattedDuration());

        } catch (Exception e) {
            logger.error("Error uploading track", e);
            redirectAttributes.addFlashAttribute("error", "Ошибка при загрузке трека: " + e.getMessage());
        }

        return "redirect:/artist/cabinet";
    }

    @GetMapping("/tracks")
    public String myTracks(Model model, HttpServletRequest request,
                           @RequestParam(required = false) String status,
                           RedirectAttributes redirectAttributes) {
        User currentUser = authService.getCurrentUser(request);

        if (currentUser == null || !currentUser.isMusician()) {
            redirectAttributes.addFlashAttribute("error", "Доступ запрещен");
            return "redirect:/account/profile";
        }

        List<com.musicstreaming.model.Track> allTracks = trackService.findByUploaderId(currentUser.getId());

        // Фильтрация по статусу
        List<com.musicstreaming.model.Track> filteredTracks;
        if (status != null && !status.isEmpty()) {
            switch (status.toLowerCase()) {
                case "approved":
                    filteredTracks = allTracks.stream()
                            .filter(t -> "approved".equals(getTrackStatus(t)))
                            .collect(Collectors.toList());
                    break;
                case "rejected":
                    filteredTracks = allTracks.stream()
                            .filter(t -> "rejected".equals(getTrackStatus(t)))
                            .collect(Collectors.toList());
                    break;
                case "pending":
                    filteredTracks = allTracks.stream()
                            .filter(t -> "pending".equals(getTrackStatus(t)))
                            .collect(Collectors.toList());
                    break;
                default:
                    filteredTracks = allTracks;
                    break;
            }
        } else {
            filteredTracks = allTracks;
        }

        // Собираем статусы и комментарии
        Map<Integer, String> trackStatuses = new HashMap<>();
        Map<Integer, String> moderationComments = new HashMap<>();

        for (com.musicstreaming.model.Track track : filteredTracks) {
            String trackStatus = getTrackStatus(track);
            trackStatuses.put(track.getId(), trackStatus);

            // Получаем комментарий из последней записи модерации
            Optional<Moderation> moderationOpt = moderationRepository.findLatestByTrackId(track.getId());
            moderationOpt.ifPresent(moderation -> {
                if (moderation.getComment() != null && !moderation.getComment().isEmpty()) {
                    moderationComments.put(track.getId(), moderation.getComment());
                }
            });
        }

        model.addAttribute("tracks", filteredTracks);
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("isAuthenticated", true);
        model.addAttribute("isAdmin", currentUser.isAdmin());
        model.addAttribute("isMusician", currentUser.isMusician());
        model.addAttribute("currentStatus", status);
        model.addAttribute("trackStatuses", trackStatuses);
        model.addAttribute("moderationComments", moderationComments);

        // Статистика
        model.addAttribute("totalTracks", allTracks.size());
        model.addAttribute("approvedCount", allTracks.stream().filter(t -> "approved".equals(getTrackStatus(t))).count());
        model.addAttribute("pendingCount", allTracks.stream().filter(t -> "pending".equals(getTrackStatus(t))).count());
        model.addAttribute("rejectedCount", allTracks.stream().filter(t -> "rejected".equals(getTrackStatus(t))).count());

        return "artist/tracks";
    }
}
package com.musicstreaming.controller;

import com.musicstreaming.dto.ArtistProfileDTO;
import com.musicstreaming.model.User;
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

    @GetMapping("/cabinet")
    public String artistCabinet(Model model, HttpServletRequest request,
                                RedirectAttributes redirectAttributes) {
        User currentUser = authService.getCurrentUser(request);

        if (currentUser == null) {
            return "redirect:/account/login";
        }

        // Если админ, показываем обычный профиль или админ-панель
        if (currentUser.isAdmin() && !currentUser.isMusician()) {
            // Админ без статуса музыканта - показываем обычный профиль
            return "redirect:/account/profile";
        }

        // Проверяем, является ли пользователь музыкантом
        if (!currentUser.isMusician()) {
            redirectAttributes.addFlashAttribute("error", "Доступ запрещен. Только для музыкантов.");
            return "redirect:/account/profile";
        }

        // Загружаем профиль артиста с полной статистикой
        ArtistProfileDTO artistProfile = artistService.getArtistProfile(currentUser.getId());

        model.addAttribute("artist", artistProfile);
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("isAuthenticated", true);
        model.addAttribute("isAdmin", currentUser.isAdmin());
        model.addAttribute("isMusician", currentUser.isMusician());

        // Загружаем все жанры для формы
        model.addAttribute("genres", genreService.findAll());

        // Загружаем всех исполнителей для выбора в форме
        model.addAttribute("artists", artistService.findAll());

        return "artist/cabinet";
    }

    /**
     * API для получения альбомов конкретного исполнителя
     */
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
            track.setDuration(180); // По умолчанию 3 минуты
            track.setUploadedByUser(currentUser);
            track.setModerated(false);

            // Установка исполнителя
            if (artistId != null && artistId > 0) {
                artistService.findById(artistId).ifPresent(track::setArtist);
            }

            // Установка альбома (если выбран)
            if (albumId != null && albumId > 0) {
                albumService.findById(albumId).ifPresent(track::setAlbum);
            }

            // Установка жанра
            genreService.findById(genreId).ifPresent(track::setGenre);

            // Загрузка аудиофайла
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
            }

            trackService.save(track);
            redirectAttributes.addFlashAttribute("success", "Трек успешно загружен и отправлен на модерацию!");

        } catch (Exception e) {
            logger.error("Error uploading track", e);
            redirectAttributes.addFlashAttribute("error", "Ошибка при загрузке трека: " + e.getMessage());
        }

        return "redirect:/artist/cabinet";
    }

    @GetMapping("/tracks")
    public String myTracks(Model model, HttpServletRequest request,
                           RedirectAttributes redirectAttributes) {
        User currentUser = authService.getCurrentUser(request);

        if (currentUser == null || !currentUser.isMusician()) {
            redirectAttributes.addFlashAttribute("error", "Доступ запрещен");
            return "redirect:/account/profile";
        }

        List<com.musicstreaming.model.Track> tracks = trackService.findByUploaderId(currentUser.getId());
        model.addAttribute("tracks", tracks);
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("isAuthenticated", true);
        model.addAttribute("isAdmin", currentUser.isAdmin());
        model.addAttribute("isMusician", currentUser.isMusician());

        return "artist/tracks";
    }

    @GetMapping("/analytics")
    public String analytics(Model model, HttpServletRequest request,
                            RedirectAttributes redirectAttributes) {
        User currentUser = authService.getCurrentUser(request);

        if (currentUser == null || !currentUser.isMusician()) {
            redirectAttributes.addFlashAttribute("error", "Доступ запрещен");
            return "redirect:/account/profile";
        }

        ArtistProfileDTO artistProfile = artistService.getArtistProfile(currentUser.getId());
        model.addAttribute("artist", artistProfile);
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("isAuthenticated", true);
        model.addAttribute("isAdmin", currentUser.isAdmin());
        model.addAttribute("isMusician", currentUser.isMusician());

        return "artist/analytics";
    }
}
package com.musicstreaming.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import com.musicstreaming.repository.ModerationRepository;
import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import com.musicstreaming.dto.AdminUserDTO;
import com.musicstreaming.dto.TrackDTO;
import com.musicstreaming.model.*;
import com.musicstreaming.service.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private static final Logger logger = LoggerFactory.getLogger(AdminController.class);

    private final UserService userService;
    private final TrackService trackService;
    private final AdminService adminService;
    private final AuthService authService;
    private final ArtistService artistService;
    private final AlbumService albumService;
    private final GenreService genreService;
    private final PlaylistService playlistService;
    private final ModerationRepository moderationRepository;

    @Autowired
    private AudioMetadataService audioMetadataService;

    @Autowired
    public AdminController(UserService userService, TrackService trackService,
                           AdminService adminService, AuthService authService,
                           ArtistService artistService, AlbumService albumService,
                           GenreService genreService, PlaylistService playlistService,
                           ModerationRepository moderationRepository,  AudioMetadataService audioMetadataService) {
        this.userService = userService;
        this.trackService = trackService;
        this.adminService = adminService;
        this.authService = authService;
        this.artistService = artistService;
        this.albumService = albumService;
        this.genreService = genreService;
        this.playlistService = playlistService;
        this.moderationRepository = moderationRepository;
        this.audioMetadataService = audioMetadataService;
    }

    @GetMapping
    public String index(Model model, HttpServletRequest request, RedirectAttributes redirectAttributes) {
        logger.info("Admin index accessed");

        if (!authService.isAdmin(request)) {
            redirectAttributes.addFlashAttribute("error", "Access denied");
            return "redirect:/";
        }

        User currentUser = authService.getCurrentUser(request);
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("pageTitle", "Панель управления");
        model.addAttribute("activePage", "dashboard");

        try {
            List<User> allUsers = userService.findAll();
            model.addAttribute("totalUsers", allUsers.size());
            model.addAttribute("totalAdmins", (int) allUsers.stream().filter(User::isAdmin).count());
            model.addAttribute("totalMusicians", (int) allUsers.stream().filter(User::isMusician).count());

            int totalTracks = trackService.findAll().size();
            int pendingTracks = trackService.findPendingModeration().size();

            model.addAttribute("totalTracks", totalTracks);
            model.addAttribute("pendingCount", pendingTracks);
            model.addAttribute("totalArtists", artistService.findAll().size());
            model.addAttribute("totalAlbums", albumService.findAll().size());
            model.addAttribute("totalGenres", genreService.findAll().size());

        } catch (Exception e) {
            logger.error("Error loading admin statistics", e);
        }

        return "admin/index";
    }

    // ==================== USER MANAGEMENT ====================

    @GetMapping("/users")
    public String users(@RequestParam(required = false) String search,
                        Model model, HttpServletRequest request,
                        RedirectAttributes redirectAttributes) {

        if (!authService.isAdmin(request)) {
            redirectAttributes.addFlashAttribute("error", "Access denied");
            return "redirect:/";
        }

        User currentUser = authService.getCurrentUser(request);
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("pageTitle", "Управление пользователями");
        model.addAttribute("activePage", "users");

        List<User> users;
        if (search != null && !search.isEmpty()) {
            users = userService.search(search);
        } else {
            users = userService.findAll();
        }

        // Конвертируем в DTO с количеством плейлистов
        List<AdminUserDTO> userDTOs = users.stream()
                .map(user -> {
                    int playlistCount = playlistService.countByUserId(user.getId());
                    return new AdminUserDTO(user, playlistCount);
                })
                .toList();

        int totalAdmins = 0;
        int totalMusicians = 0;
        int totalSubscribers = 0;

        for (AdminUserDTO user : userDTOs) {
            if (user.isAdmin()) totalAdmins++;
            else if (user.isMusician()) totalMusicians++;
            else if (user.isSubscriber()) totalSubscribers++;
        }

        model.addAttribute("users", userDTOs);
        model.addAttribute("search", search);
        model.addAttribute("totalAdmins", totalAdmins);
        model.addAttribute("totalMusicians", totalMusicians);
        model.addAttribute("totalSubscribers", totalSubscribers);

        return "admin/users";
    }

    // ==================== USER ROLE MANAGEMENT ====================

    @PostMapping("/users/{id}/make-musician")
    public String makeMusician(@PathVariable Integer id,
                               HttpServletRequest request,
                               RedirectAttributes redirectAttributes) {

        if (!authService.isAdmin(request)) {
            redirectAttributes.addFlashAttribute("error", "Access denied");
            return "redirect:/";
        }

        try {
            userService.updateRole(id, User.UserRole.Musician);
            redirectAttributes.addFlashAttribute("success", "User role updated to Musician");
            logger.info("User {} role updated to Musician", id);
        } catch (Exception e) {
            logger.error("Failed to update user role", e);
            redirectAttributes.addFlashAttribute("error", "Failed to update user role");
        }

        return "redirect:/admin/users";
    }

    @PostMapping("/users/{id}/make-admin")
    public String makeAdmin(@PathVariable Integer id,
                            HttpServletRequest request,
                            RedirectAttributes redirectAttributes) {

        if (!authService.isAdmin(request)) {
            redirectAttributes.addFlashAttribute("error", "Access denied");
            return "redirect:/";
        }

        try {
            userService.updateRole(id, User.UserRole.Admin);
            redirectAttributes.addFlashAttribute("success", "User role updated to Admin");
            logger.info("User {} role updated to Admin", id);
        } catch (Exception e) {
            logger.error("Failed to update user role", e);
            redirectAttributes.addFlashAttribute("error", "Failed to update user role");
        }

        return "redirect:/admin/users";
    }

    @PostMapping("/users/{id}/make-user")
    public String makeUser(@PathVariable Integer id,
                           HttpServletRequest request,
                           RedirectAttributes redirectAttributes) {

        if (!authService.isAdmin(request)) {
            redirectAttributes.addFlashAttribute("error", "Access denied");
            return "redirect:/";
        }

        try {
            userService.updateRole(id, User.UserRole.User);
            redirectAttributes.addFlashAttribute("success", "User role updated to User");
            logger.info("User {} role updated to User", id);
        } catch (Exception e) {
            logger.error("Failed to update user role", e);
            redirectAttributes.addFlashAttribute("error", "Failed to update user role");
        }

        return "redirect:/admin/users";
    }

    @PostMapping("/users/{id}/make-subscriber")
    public String makeSubscriber(@PathVariable Integer id,
                                 HttpServletRequest request,
                                 RedirectAttributes redirectAttributes) {

        if (!authService.isAdmin(request)) {
            redirectAttributes.addFlashAttribute("error", "Access denied");
            return "redirect:/";
        }

        try {
            userService.updateRole(id, User.UserRole.Subscriber);
            redirectAttributes.addFlashAttribute("success", "User role updated to Subscriber");
            logger.info("User {} role updated to Subscriber", id);
        } catch (Exception e) {
            logger.error("Failed to update user role", e);
            redirectAttributes.addFlashAttribute("error", "Failed to update user role");
        }

        return "redirect:/admin/users";
    }


    // ==================== TRACK MANAGEMENT ====================

    @GetMapping("/tracks")
    public String tracks(@RequestParam(required = false) String search,
                         Model model, HttpServletRequest request,
                         RedirectAttributes redirectAttributes) {

        if (!authService.isAdmin(request)) {
            redirectAttributes.addFlashAttribute("error", "Access denied");
            return "redirect:/";
        }

        User currentUser = authService.getCurrentUser(request);
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("pageTitle", "Управление треками");
        model.addAttribute("activePage", "tracks");

        try {
            List<Track> tracks;
            if (search != null && !search.isEmpty()) {
                tracks = trackService.search(search);
            } else {
                tracks = trackService.findAll();
            }

            // ДОБАВЛЯЕМ: статусы модерации для каждого трека
            Map<Integer, String> trackModerationStatuses = new HashMap<>();
            for (Track track : tracks) {
                trackModerationStatuses.put(track.getId(), getTrackModerationStatus(track));
            }

            model.addAttribute("tracks", tracks);
            model.addAttribute("totalTracks", tracks.size());
            model.addAttribute("pendingCount", trackService.findPendingModeration().size());
            model.addAttribute("search", search);
            model.addAttribute("trackModerationStatuses", trackModerationStatuses); // ДОБАВЛЕНО

            model.addAttribute("artists", artistService.findAll());
            model.addAttribute("albums", albumService.findAll());
            model.addAttribute("genres", genreService.findAll());

        } catch (Exception e) {
            logger.error("Error loading tracks", e);
            model.addAttribute("tracks", List.of());
            model.addAttribute("totalTracks", 0);
            model.addAttribute("pendingCount", 0);
        }

        return "admin/tracks";
    }

    @GetMapping("/tracks/new")
    public String newTrackForm(Model model, HttpServletRequest request,
                               RedirectAttributes redirectAttributes) {

        if (!authService.isAdmin(request)) {
            redirectAttributes.addFlashAttribute("error", "Access denied");
            return "redirect:/";
        }

        User currentUser = authService.getCurrentUser(request);
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("track", new Track());
        model.addAttribute("artists", artistService.findAll());
        model.addAttribute("albums", albumService.findAll());
        model.addAttribute("genres", genreService.findAll());

        return "admin/track-form";
    }

    @GetMapping("/tracks/{id}")
    public String viewTrack(@PathVariable Integer id, Model model,
                            HttpServletRequest request,
                            RedirectAttributes redirectAttributes) {

        if (!authService.isAdmin(request)) {
            redirectAttributes.addFlashAttribute("error", "Access denied");
            return "redirect:/";
        }

        User currentUser = authService.getCurrentUser(request);
        model.addAttribute("currentUser", currentUser);

        Track track = trackService.findByIdWithUser(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid track Id:" + id));

        TrackDTO trackDTO = new TrackDTO(track);
        model.addAttribute("track", trackDTO);

        return "admin/track-view";
    }

    @GetMapping("/tracks/edit/{id}")
    public String editTrackForm(@PathVariable Integer id, Model model,
                                HttpServletRequest request,
                                RedirectAttributes redirectAttributes) {

        if (!authService.isAdmin(request)) {
            redirectAttributes.addFlashAttribute("error", "Access denied");
            return "redirect:/";
        }

        User currentUser = authService.getCurrentUser(request);
        model.addAttribute("currentUser", currentUser);

        Track track = trackService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid track Id:" + id));

        model.addAttribute("track", track);
        model.addAttribute("artists", artistService.findAll());
        model.addAttribute("albums", albumService.findAll());
        model.addAttribute("genres", genreService.findAll());

        return "admin/track-form";
    }

    @PostMapping("/tracks/save")
    public String saveTrack(@ModelAttribute Track track,
                            @RequestParam(required = false) Integer artistId,
                            @RequestParam(required = false) Integer albumId,
                            @RequestParam(required = false) Integer genreId,
                            @RequestParam(value = "audioFile", required = false) MultipartFile audioFile,
                            @RequestParam(required = false) Boolean moderated,
                            HttpServletRequest request,
                            RedirectAttributes redirectAttributes) {

        if (!authService.isAdmin(request)) {
            redirectAttributes.addFlashAttribute("error", "Access denied");
            return "redirect:/";
        }

        try {
            User currentUser = authService.getCurrentUser(request);

            // Если это существующий трек (редактирование), загружаем его из базы
            if (track.getId() != null) {
                Track existingTrack = trackService.findById(track.getId())
                        .orElseThrow(() -> new IllegalArgumentException("Трек не найден: " + track.getId()));

                // Сохраняем filePath из существующего трека, если не загружен новый файл
                if (audioFile == null || audioFile.isEmpty()) {
                    track.setFilePath(existingTrack.getFilePath());
                    track.setDuration(existingTrack.getDuration()); // Сохраняем старую длительность
                }

                // Сохраняем uploadedByUser из существующего трека
                if (track.getUploadedByUser() == null) {
                    track.setUploadedByUser(existingTrack.getUploadedByUser());
                }
            }

            // Установка связей
            if (artistId != null && artistId > 0) {
                artistService.findById(artistId).ifPresent(track::setArtist);
            }

            if (albumId != null && albumId > 0) {
                albumService.findById(albumId).ifPresent(track::setAlbum);
            }

            if (genreId != null && genreId > 0) {
                genreService.findById(genreId).ifPresent(track::setGenre);
            }

            // Обработка загрузки нового файла
            if (audioFile != null && !audioFile.isEmpty()) {
                logger.info("Processing uploaded file: {}, size: {} bytes",
                        audioFile.getOriginalFilename(), audioFile.getSize());

                String userDir = System.getProperty("user.dir");
                Path uploadDir = Paths.get(userDir, "uploads", "music");

                logger.info("Project root (user.dir): {}", userDir);
                logger.info("Upload directory: {}", uploadDir.toAbsolutePath());

                // Создаём папку если её нет
                if (!Files.exists(uploadDir)) {
                    Files.createDirectories(uploadDir);
                    logger.info("Created upload directory: {}", uploadDir.toAbsolutePath());
                }

                // Генерируем уникальное имя файла
                String originalFilename = audioFile.getOriginalFilename();
                String extension = ".mp3";
                if (originalFilename != null && originalFilename.contains(".")) {
                    extension = originalFilename.substring(originalFilename.lastIndexOf("."));
                }
                String filename = UUID.randomUUID().toString() + extension;

                // Сохраняем файл
                Path destFile = uploadDir.resolve(filename);
                audioFile.transferTo(destFile.toFile());

                logger.info("Saved audio file to: {}", destFile.toAbsolutePath());
                logger.info("File exists after save: {}", Files.exists(destFile));
                logger.info("File size after save: {} bytes", Files.size(destFile));

                // Сохраняем имя файла в БД
                track.setFilePath(filename);

                // АВТОМАТИЧЕСКОЕ ОПРЕДЕЛЕНИЕ ДЛИТЕЛЬНОСТИ
                int detectedDuration = audioMetadataService.getDurationInSeconds(destFile.toString());
                if (detectedDuration > 0) {
                    track.setDuration(detectedDuration);
                    logger.info("Auto-detected duration: {} seconds ({}:{})",
                            detectedDuration, detectedDuration / 60, detectedDuration % 60);
                } else if (track.getDuration() == null || track.getDuration() == 0) {
                    // Если не удалось определить и нет явно указанной длительности
                    track.setDuration(180); // 3 минуты по умолчанию
                    logger.warn("Using default duration of 180 seconds");
                }
            } else if (track.getDuration() == null || track.getDuration() == 0) {
                // Если файл не загружался и длительность не указана
                track.setDuration(180);
            }

            // Обработка статуса модерации
            if (moderated != null) {
                track.setModerated(moderated);
            }

            if (track.getUploadedByUser() == null && currentUser != null) {
                track.setUploadedByUser(currentUser);
            }

            // Сохраняем трек
            trackService.save(track);

            String message;
            if (track.getId() != null && audioFile != null && !audioFile.isEmpty()) {
                message = "Трек успешно обновлён. Длительность: " + track.getFormattedDuration();
            } else if (track.getId() != null) {
                message = "Трек успешно обновлён";
            } else {
                message = "Трек успешно создан. Длительность: " + track.getFormattedDuration();
            }

            redirectAttributes.addFlashAttribute("success", message);
            logger.info("Saved track: id={}, duration={}s", track.getId(), track.getDuration());

        } catch (Exception e) {
            logger.error("Error saving track", e);
            redirectAttributes.addFlashAttribute("error", "Ошибка при сохранении: " + e.getMessage());

            if (track.getId() != null) {
                return "redirect:/admin/tracks/edit/" + track.getId();
            }
            return "redirect:/admin/tracks/new";
        }

        return "redirect:/admin/tracks";
    }

    @PostMapping("/tracks/delete/{id}")
    public String deleteTrack(@PathVariable Integer id,
                              HttpServletRequest request,
                              RedirectAttributes redirectAttributes) {

        if (!authService.isAdmin(request)) {
            redirectAttributes.addFlashAttribute("error", "Access denied");
            return "redirect:/";
        }

        try {
            trackService.delete(id);
            redirectAttributes.addFlashAttribute("success", "Track deleted successfully");
        } catch (Exception e) {
            logger.error("Error deleting track", e);
            redirectAttributes.addFlashAttribute("error", "Error deleting track");
        }

        return "redirect:/admin/tracks";
    }

    @PostMapping("/tracks/{id}/approve")
    public String approveTrack(@PathVariable Integer id,
                               @RequestParam(required = false) String comment,
                               HttpServletRequest request,
                               RedirectAttributes redirectAttributes) {

        if (!authService.isAdmin(request)) {
            redirectAttributes.addFlashAttribute("error", "Access denied");
            return "redirect:/";
        }

        try {
            User moderator = authService.getCurrentUser(request);
            trackService.approveTrack(id, moderator.getId(), comment);
            redirectAttributes.addFlashAttribute("success", "Track approved successfully");
        } catch (Exception e) {
            logger.error("Failed to approve track", e);
            redirectAttributes.addFlashAttribute("error", "Failed to approve track");
        }

        return "redirect:/admin/tracks";
    }

    @PostMapping("/tracks/{id}/reject")
    public String rejectTrack(@PathVariable Integer id,
                              @RequestParam String comment,
                              HttpServletRequest request,
                              RedirectAttributes redirectAttributes) {

        if (!authService.isAdmin(request)) {
            redirectAttributes.addFlashAttribute("error", "Access denied");
            return "redirect:/";
        }

        try {
            User moderator = authService.getCurrentUser(request);
            trackService.rejectTrack(id, moderator.getId(), comment);
            redirectAttributes.addFlashAttribute("success", "Track rejected");
        } catch (Exception e) {
            logger.error("Failed to reject track", e);
            redirectAttributes.addFlashAttribute("error", "Failed to reject track");
        }

        return "redirect:/admin/tracks";
    }

    // ==================== ALBUM MANAGEMENT ====================

    @GetMapping("/albums")
    public String albums(Model model, HttpServletRequest request,
                         RedirectAttributes redirectAttributes) {

        if (!authService.isAdmin(request)) {
            redirectAttributes.addFlashAttribute("error", "Access denied");
            return "redirect:/";
        }

        User currentUser = authService.getCurrentUser(request);
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("albums", albumService.findAll());
        model.addAttribute("artists", artistService.findAll());
        model.addAttribute("pageTitle", "Управление альбомами");
        model.addAttribute("activePage", "albums");

        return "admin/albums/list";
    }

    @GetMapping("/albums/new")
    public String newAlbumForm(Model model, HttpServletRequest request,
                               RedirectAttributes redirectAttributes) {

        if (!authService.isAdmin(request)) {
            redirectAttributes.addFlashAttribute("error", "Access denied");
            return "redirect:/";
        }

        User currentUser = authService.getCurrentUser(request);
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("album", new Album());
        model.addAttribute("artists", artistService.findAll());
        model.addAttribute("pageTitle", "Добавление альбома");
        model.addAttribute("activePage", "albums");

        return "admin/albums/form";
    }

    @PostMapping("/albums/save")
    public String saveAlbum(@RequestParam(required = false) Integer id,
                            @RequestParam String title,
                            @RequestParam Integer artistId,
                            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate releaseDate,
                            @RequestParam(value = "coverFile", required = false) MultipartFile coverFile, // Изменено
                            HttpServletRequest request,
                            RedirectAttributes redirectAttributes) {

        if (!authService.isAdmin(request)) {
            redirectAttributes.addFlashAttribute("error", "Access denied");
            return "redirect:/";
        }

        try {
            Album album;
            if (id != null) {
                album = albumService.findById(id)
                        .orElseThrow(() -> new IllegalArgumentException("Album not found: " + id));
            } else {
                album = new Album();
            }

            album.setTitle(title);
            album.setReleaseDate(releaseDate);
            artistService.findById(artistId).ifPresent(album::setArtist);

            // Загрузка обложки
            if (coverFile != null && !coverFile.isEmpty()) {
                String filename = saveImage(coverFile, "albums");
                album.setCoverPath("/images/" + filename);
            }

            albumService.save(album);
            redirectAttributes.addFlashAttribute("success", "Альбом сохранен");
        } catch (Exception e) {
            logger.error("Error saving album", e);
            redirectAttributes.addFlashAttribute("error", "Ошибка: " + e.getMessage());
        }
        return "redirect:/admin/albums";
    }


    @GetMapping("/albums/edit/{id}")
    public String editAlbumForm(@PathVariable Integer id, Model model,
                                HttpServletRequest request,
                                RedirectAttributes redirectAttributes) {

        if (!authService.isAdmin(request)) {
            redirectAttributes.addFlashAttribute("error", "Access denied");
            return "redirect:/";
        }

        User currentUser = authService.getCurrentUser(request);
        Album album = albumService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid album Id: " + id));

        model.addAttribute("currentUser", currentUser);
        model.addAttribute("album", album);
        model.addAttribute("artists", artistService.findAll());
        model.addAttribute("pageTitle", "Редактирование альбома");
        model.addAttribute("activePage", "albums");

        return "admin/albums/form";
    }

    @GetMapping("/albums/{id}")
    public String viewAlbum(@PathVariable Integer id, Model model,
                            HttpServletRequest request,
                            RedirectAttributes redirectAttributes) {

        if (!authService.isAdmin(request)) {
            redirectAttributes.addFlashAttribute("error", "Access denied");
            return "redirect:/";
        }

        User currentUser = authService.getCurrentUser(request);
        Album album = albumService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid album Id: " + id));

        // Записываем прослушивание альбома
        albumService.recordAlbumPlay(id);

        List<Track> tracks = albumService.getAlbumTracks(id);
        Long totalListens = albumService.getTotalListenCount(id);
        int totalDuration = tracks.stream()
                .mapToInt(track -> track.getDuration() != null ? track.getDuration() : 0)
                .sum();

        model.addAttribute("currentUser", currentUser);
        model.addAttribute("album", album);
        model.addAttribute("tracks", tracks);
        model.addAttribute("totalListens", totalListens);
        model.addAttribute("totalDuration", totalDuration);
        model.addAttribute("pageTitle", album.getTitle());
        model.addAttribute("activePage", "albums");

        return "admin/albums/view";
    }

    @PostMapping("/albums/delete/{id}")
    public String deleteAlbum(@PathVariable Integer id,
                              HttpServletRequest request,
                              RedirectAttributes redirectAttributes) {

        if (!authService.isAdmin(request)) {
            redirectAttributes.addFlashAttribute("error", "Access denied");
            return "redirect:/";
        }

        try {
            albumService.deleteAlbum(id);
            redirectAttributes.addFlashAttribute("success", "Альбом удален");
        } catch (Exception e) {
            logger.error("Error deleting album", e);
            redirectAttributes.addFlashAttribute("error", "Ошибка при удалении: " + e.getMessage());
        }

        return "redirect:/admin/albums";
    }

    // ==================== GENRE MANAGEMENT ====================

    @GetMapping("/genres")
    public String genres(Model model, HttpServletRequest request,
                         RedirectAttributes redirectAttributes) {

        if (!authService.isAdmin(request)) {
            redirectAttributes.addFlashAttribute("error", "Access denied");
            return "redirect:/";
        }

        User currentUser = authService.getCurrentUser(request);
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("genres", genreService.findAllDTOs());
        model.addAttribute("pageTitle", "Управление жанрами");
        model.addAttribute("activePage", "genres");

        return "admin/genres/list";
    }

    @GetMapping("/genres/new")
    public String newGenreForm(Model model, HttpServletRequest request,
                               RedirectAttributes redirectAttributes) {

        if (!authService.isAdmin(request)) {
            redirectAttributes.addFlashAttribute("error", "Access denied");
            return "redirect:/";
        }

        User currentUser = authService.getCurrentUser(request);
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("genre", new Genre());
        model.addAttribute("pageTitle", "Добавление жанра");
        model.addAttribute("activePage", "genres");

        return "admin/genres/form";
    }

    @PostMapping("/genres/save")
    public String saveGenre(@ModelAttribute Genre genre,
                            HttpServletRequest request,
                            RedirectAttributes redirectAttributes) {

        if (!authService.isAdmin(request)) {
            redirectAttributes.addFlashAttribute("error", "Access denied");
            return "redirect:/";
        }

        try {
            genreService.save(genre);
            redirectAttributes.addFlashAttribute("success", "Жанр сохранен");
        } catch (Exception e) {
            logger.error("Error saving genre", e);
            redirectAttributes.addFlashAttribute("error", "Ошибка при сохранении: " + e.getMessage());
        }

        return "redirect:/admin/genres";
    }

    @GetMapping("/genres/edit/{id}")
    public String editGenreForm(@PathVariable Integer id, Model model,
                                HttpServletRequest request,
                                RedirectAttributes redirectAttributes) {

        if (!authService.isAdmin(request)) {
            redirectAttributes.addFlashAttribute("error", "Access denied");
            return "redirect:/";
        }

        User currentUser = authService.getCurrentUser(request);
        Genre genre = genreService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid genre Id: " + id));

        model.addAttribute("currentUser", currentUser);
        model.addAttribute("genre", genre);
        model.addAttribute("pageTitle", "Редактирование жанра");
        model.addAttribute("activePage", "genres");

        return "admin/genres/form";
    }

    @PostMapping("/genres/delete/{id}")
    public String deleteGenre(@PathVariable Integer id,
                              HttpServletRequest request,
                              RedirectAttributes redirectAttributes) {

        if (!authService.isAdmin(request)) {
            redirectAttributes.addFlashAttribute("error", "Access denied");
            return "redirect:/";
        }

        try {
            genreService.delete(id);
            redirectAttributes.addFlashAttribute("success", "Жанр удален");
        } catch (Exception e) {
            logger.error("Error deleting genre", e);
            redirectAttributes.addFlashAttribute("error", "Ошибка при удалении: " + e.getMessage());
        }

        return "redirect:/admin/genres";
    }

    // ==================== ARTIST MANAGEMENT ====================

//    @PostMapping("/artists/save")
//    public String saveArtist(@RequestParam(required = false) Integer id,
//                             @RequestParam String name,
//                             @RequestParam(required = false) String description,
//                             @RequestParam(value = "photoFile", required = false) MultipartFile photoFile,
//                             HttpServletRequest request,
//                             RedirectAttributes redirectAttributes) {
//
//        if (!authService.isAdmin(request)) {
//            redirectAttributes.addFlashAttribute("error", "Access denied");
//            return "redirect:/";
//        }
//
//        try {
//            Artist artist;
//            if (id != null) {
//                artist = artistService.findById(id)
//                        .orElseThrow(() -> new IllegalArgumentException("Artist not found: " + id));
//            } else {
//                artist = new Artist();
//            }
//
//            artist.setName(name);
//            artist.setDescription(description);
//
//            // Загрузка фото
//            if (photoFile != null && !photoFile.isEmpty()) {
//                String filename = saveImage(photoFile, "artists");
//                artist.setPhotoPath("/images/" + filename);
//            }
//
//            artistService.save(artist);
//            redirectAttributes.addFlashAttribute("success", "Исполнитель сохранен");
//        } catch (Exception e) {
//            logger.error("Error saving artist", e);
//            redirectAttributes.addFlashAttribute("error", "Ошибка: " + e.getMessage());
//        }
//        return "redirect:/admin/artists";
//    }

    // ==================== MODERATION ====================

    @GetMapping("/moderation")
    public String moderation(@RequestParam(required = false, defaultValue = "0") int page,
                             Model model, HttpServletRequest request,
                             RedirectAttributes redirectAttributes) {

        if (!authService.isAdmin(request)) {
            redirectAttributes.addFlashAttribute("error", "Access denied");
            return "redirect:/";
        }

        User currentUser = authService.getCurrentUser(request);
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("pageTitle", "Модерация контента");
        model.addAttribute("activePage", "moderation");

        // Получаем ВСЕ непроверенные треки
        List<Track> pendingTracks = trackService.findPendingModeration();

        // Фильтруем - исключаем отклоненные треки
        // Отклоненный трек - это трек, у которого последняя запись в Moderations имеет статус Rejected
        List<Track> filteredPendingTracks = new ArrayList<>();
        for (Track track : pendingTracks) {
            Optional<Moderation> latestModeration = moderationRepository.findLatestByTrackId(track.getId());

            // Показываем трек только если:
            // 1. Нет записей в модерации (новый трек)
            // 2. Или последний статус не Rejected
            if (latestModeration.isEmpty() || latestModeration.get().getStatus() != Moderation.ModerationStatus.Rejected) {
                filteredPendingTracks.add(track);
            }
        }

        model.addAttribute("pendingTracks", filteredPendingTracks);
        model.addAttribute("pendingCount", filteredPendingTracks.size());

        // История модерации с пагинацией
        int pageSize = 10;
        Pageable pageable = PageRequest.of(page, pageSize);
        Page<Moderation> moderationPage = moderationRepository.findModerationHistory(pageable);

        model.addAttribute("moderationHistory", moderationPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", moderationPage.getTotalPages());
        model.addAttribute("totalItems", moderationPage.getTotalElements());

        // Жанры для фильтров
        model.addAttribute("genres", genreService.findAll());

        return "admin/moderation";
    }

    private String saveImage(MultipartFile file, String subfolder) {
        try {
            String userDir = System.getProperty("user.dir");
            Path uploadDir = Paths.get(userDir, "uploads", "images", subfolder);

            if (!Files.exists(uploadDir)) {
                Files.createDirectories(uploadDir);
            }

            String originalFilename = file.getOriginalFilename();
            String extension = ".jpg";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String filename = subfolder + "_" + UUID.randomUUID().toString().substring(0, 8) + extension;

            Path destFile = uploadDir.resolve(filename);
            file.transferTo(destFile.toFile());

            logger.info("Saved image: {}", destFile.toAbsolutePath());
            return subfolder + "/" + filename;
        } catch (Exception e) {
            logger.error("Error saving image", e);
            return "default.jpg";
        }
    }
    private String getTrackModerationStatus(Track track) {
        if (track.isModerated()) {
            return "approved";
        }

        // Проверяем последнюю запись в Moderations
        Optional<Moderation> latestModeration = moderationRepository.findLatestByTrackId(track.getId());

        if (latestModeration.isPresent() &&
                latestModeration.get().getStatus() == Moderation.ModerationStatus.Rejected) {
            return "rejected";
        }

        return "pending";
    }
}
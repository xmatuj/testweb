package com.musicstreaming.controller;

import com.musicstreaming.model.*;
import com.musicstreaming.service.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

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

    @Autowired
    public AdminController(UserService userService, TrackService trackService,
                           AdminService adminService, AuthService authService,
                           ArtistService artistService, AlbumService albumService,
                           GenreService genreService) {
        this.userService = userService;
        this.trackService = trackService;
        this.adminService = adminService;
        this.authService = authService;
        this.artistService = artistService;
        this.albumService = albumService;
        this.genreService = genreService;
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

        List<User> users;
        if (search != null && !search.isEmpty()) {
            users = userService.search(search);
        } else {
            users = userService.findAll();
        }

        // Подсчет статистики по ролям
        int totalAdmins = 0;
        int totalMusicians = 0;
        int totalSubscribers = 0;

        for (User user : users) {
            switch (user.getRole()) {
                case Admin:
                    totalAdmins++;
                    break;
                case Musician:
                    totalMusicians++;
                    break;
                case Subscriber:
                    totalSubscribers++;
                    break;
                default:
                    break;
            }
        }

        model.addAttribute("users", users);
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

    // ==================== TRACK MANAGEMENT (FULL CRUD) ====================

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

        try {
            List<Track> tracks;
            if (search != null && !search.isEmpty()) {
                tracks = trackService.search(search);
            } else {
                tracks = trackService.findAll();
            }

            model.addAttribute("tracks", tracks);
            model.addAttribute("totalTracks", tracks.size());
            model.addAttribute("pendingCount", trackService.findPendingModeration().size());
            model.addAttribute("search", search);

            // For create form
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

        Track track = trackService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid track Id:" + id));

        model.addAttribute("track", track);

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
                            HttpServletRequest request,
                            RedirectAttributes redirectAttributes) {

        if (!authService.isAdmin(request)) {
            redirectAttributes.addFlashAttribute("error", "Access denied");
            return "redirect:/";
        }

        try {
            User currentUser = authService.getCurrentUser(request);

            // Set relationships
            track.setArtistId(artistId);
            track.setAlbumId(albumId);
            track.setGenreId(genreId);

            if (track.getId() == null) {
                // Create new track
                track.setUploadedByUserId(currentUser.getId());
                trackService.save(track);
                redirectAttributes.addFlashAttribute("success", "Track created successfully");
            } else {
                // Update existing track
                trackService.save(track);
                redirectAttributes.addFlashAttribute("success", "Track updated successfully");
            }

        } catch (Exception e) {
            logger.error("Error saving track", e);
            redirectAttributes.addFlashAttribute("error", "Error saving track: " + e.getMessage());
            return "redirect:/admin/tracks/edit/" + (track.getId() != null ? track.getId() : "");
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

    // ==================== OTHER MANAGEMENT PAGES ====================

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

        return "admin/albums";
    }

    @GetMapping("/artists")
    public String artists(Model model, HttpServletRequest request,
                          RedirectAttributes redirectAttributes) {

        if (!authService.isAdmin(request)) {
            redirectAttributes.addFlashAttribute("error", "Access denied");
            return "redirect:/";
        }

        User currentUser = authService.getCurrentUser(request);
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("artists", artistService.findAll());

        return "admin/artists";
    }

    @GetMapping("/genres")
    public String genres(Model model, HttpServletRequest request,
                         RedirectAttributes redirectAttributes) {

        if (!authService.isAdmin(request)) {
            redirectAttributes.addFlashAttribute("error", "Access denied");
            return "redirect:/";
        }

        User currentUser = authService.getCurrentUser(request);
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("genres", genreService.findAll());

        return "admin/genres";
    }

    @GetMapping("/moderation")
    public String moderation(Model model, HttpServletRequest request,
                             RedirectAttributes redirectAttributes) {

        if (!authService.isAdmin(request)) {
            redirectAttributes.addFlashAttribute("error", "Access denied");
            return "redirect:/";
        }

        User currentUser = authService.getCurrentUser(request);
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("pendingTracks", trackService.findPendingModeration());
        model.addAttribute("pendingCount", trackService.findPendingModeration().size());

        return "admin/moderation";
    }
}
package com.musicstreaming.controller;

import com.musicstreaming.model.User;
import com.musicstreaming.service.AdminService;
import com.musicstreaming.service.AuthService;
import com.musicstreaming.service.TrackService;
import com.musicstreaming.service.UserService;
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

    @Autowired
    public AdminController(UserService userService, TrackService trackService,
                           AdminService adminService, AuthService authService) {
        this.userService = userService;
        this.trackService = trackService;
        this.adminService = adminService;
        this.authService = authService;
    }

    @GetMapping
    public String index(Model model, HttpServletRequest request, RedirectAttributes redirectAttributes) {
        logger.info("Admin index accessed");

        // Check if user is admin
        if (!authService.isAdmin(request)) {
            logger.warn("Non-admin user attempted to access admin panel");
            redirectAttributes.addFlashAttribute("error", "Access denied");
            return "redirect:/";
        }

        User currentUser = authService.getCurrentUser(request);
        model.addAttribute("currentUser", currentUser);

        try {
            // Get statistics
            List<User> allUsers = userService.findAll();
            int totalUsers = allUsers.size();
            int totalAdmins = (int) allUsers.stream().filter(User::isAdmin).count();
            int totalMusicians = (int) allUsers.stream().filter(User::isMusician).count();
            int totalSubscribers = (int) allUsers.stream().filter(u -> u.getRole() == User.UserRole.Subscriber).count();

            model.addAttribute("totalUsers", totalUsers);
            model.addAttribute("totalAdmins", totalAdmins);
            model.addAttribute("totalMusicians", totalMusicians);
            model.addAttribute("totalSubscribers", totalSubscribers);

            // Track statistics
            int totalTracks = trackService.findAll().size();
            int pendingTracks = trackService.findPendingModeration().size();
            int moderatedTracks = trackService.findModerated().size();

            model.addAttribute("totalTracks", totalTracks);
            model.addAttribute("pendingCount", pendingTracks);
            model.addAttribute("moderatedTracks", moderatedTracks);

            // Artist, album, genre statistics would come from respective services
            model.addAttribute("totalArtists", 45); // Placeholder
            model.addAttribute("totalAlbums", 78); // Placeholder
            model.addAttribute("totalGenres", 23); // Placeholder

        } catch (Exception e) {
            logger.error("Error loading admin statistics", e);
            model.addAttribute("totalUsers", 0);
            model.addAttribute("pendingCount", 0);
        }

        return "admin/index";
    }

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

        model.addAttribute("users", users);
        model.addAttribute("search", search);

        return "admin/users";
    }

    @PostMapping("/users/{id}/make-musician")
    public String makeMusician(@PathVariable Integer id,
                               HttpServletRequest request,
                               RedirectAttributes redirectAttributes) {
        return updateRole(id, User.UserRole.Musician, request, redirectAttributes);
    }

    @PostMapping("/users/{id}/make-admin")
    public String makeAdmin(@PathVariable Integer id,
                            HttpServletRequest request,
                            RedirectAttributes redirectAttributes) {
        return updateRole(id, User.UserRole.Admin, request, redirectAttributes);
    }

    @PostMapping("/users/{id}/make-user")
    public String makeUser(@PathVariable Integer id,
                           HttpServletRequest request,
                           RedirectAttributes redirectAttributes) {
        return updateRole(id, User.UserRole.User, request, redirectAttributes);
    }

    @PostMapping("/users/{id}/make-subscriber")
    public String makeSubscriber(@PathVariable Integer id,
                                 HttpServletRequest request,
                                 RedirectAttributes redirectAttributes) {
        return updateRole(id, User.UserRole.Subscriber, request, redirectAttributes);
    }

    private String updateRole(Integer userId, User.UserRole newRole,
                              HttpServletRequest request, RedirectAttributes redirectAttributes) {
        if (!authService.isAdmin(request)) {
            redirectAttributes.addFlashAttribute("error", "Access denied");
            return "redirect:/";
        }

        try {
            userService.updateRole(userId, newRole);
            redirectAttributes.addFlashAttribute("success", "User role updated successfully");
        } catch (Exception e) {
            logger.error("Failed to update user role", e);
            redirectAttributes.addFlashAttribute("error", "Failed to update user role");
        }

        return "redirect:/admin/users";
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

        // Get pending tracks
        model.addAttribute("pendingTracks", trackService.findPendingModeration());

        return "admin/moderation";
    }

    @PostMapping("/moderation/approve/{trackId}")
    public String approveTrack(@PathVariable Integer trackId,
                               @RequestParam(required = false) String comment,
                               HttpServletRequest request,
                               RedirectAttributes redirectAttributes) {
        if (!authService.isAdmin(request)) {
            redirectAttributes.addFlashAttribute("error", "Access denied");
            return "redirect:/";
        }

        try {
            User moderator = authService.getCurrentUser(request);
            trackService.approveTrack(trackId, moderator.getId(), comment);
            redirectAttributes.addFlashAttribute("success", "Track approved successfully");
        } catch (Exception e) {
            logger.error("Failed to approve track", e);
            redirectAttributes.addFlashAttribute("error", "Failed to approve track");
        }

        return "redirect:/admin/moderation";
    }

    @PostMapping("/moderation/reject/{trackId}")
    public String rejectTrack(@PathVariable Integer trackId,
                              @RequestParam String comment,
                              HttpServletRequest request,
                              RedirectAttributes redirectAttributes) {
        if (!authService.isAdmin(request)) {
            redirectAttributes.addFlashAttribute("error", "Access denied");
            return "redirect:/";
        }

        try {
            User moderator = authService.getCurrentUser(request);
            trackService.rejectTrack(trackId, moderator.getId(), comment);
            redirectAttributes.addFlashAttribute("success", "Track rejected");
        } catch (Exception e) {
            logger.error("Failed to reject track", e);
            redirectAttributes.addFlashAttribute("error", "Failed to reject track");
        }

        return "redirect:/admin/moderation";
    }

    @GetMapping("/tracks")
    public String tracks(Model model, HttpServletRequest request) {
        if (!authService.isAdmin(request)) {
            return "redirect:/";
        }
        model.addAttribute("tracks", trackService.findAll());
        return "admin/tracks";
    }

    @GetMapping("/artists")
    public String artists(Model model, HttpServletRequest request) {
        if (!authService.isAdmin(request)) {
            return "redirect:/";
        }
        // model.addAttribute("artists", artistService.findAll());
        return "admin/artists";
    }

    @GetMapping("/albums")
    public String albums(Model model, HttpServletRequest request) {
        if (!authService.isAdmin(request)) {
            return "redirect:/";
        }
        // model.addAttribute("albums", albumService.findAll());
        return "admin/albums";
    }

    @GetMapping("/genres")
    public String genres(Model model, HttpServletRequest request) {
        if (!authService.isAdmin(request)) {
            return "redirect:/";
        }
        // model.addAttribute("genres", genreService.findAll());
        return "admin/genres";
    }
}
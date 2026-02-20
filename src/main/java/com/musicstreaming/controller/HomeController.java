package com.musicstreaming.controller;

import com.musicstreaming.model.User;
import com.musicstreaming.service.AlbumService;
import com.musicstreaming.service.AuthService;
import com.musicstreaming.service.TrackService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;

@Controller
public class HomeController {

    private static final Logger logger = LoggerFactory.getLogger(HomeController.class);

    private final TrackService trackService;
    private final AlbumService albumService;
    private final AuthService authService;

    @Autowired
    public HomeController(TrackService trackService, AlbumService albumService, AuthService authService) {
        this.trackService = trackService;
        this.albumService = albumService;
        this.authService = authService;
    }

    @GetMapping("/")
    public String index(Model model, HttpServletRequest request) {
        logger.info("Home page accessed");

        User currentUser = authService.getCurrentUser(request);
        logger.info("Current user: {}", currentUser != null ? currentUser.getUsername() : "null");

        model.addAttribute("currentUser", currentUser);
        model.addAttribute("isAuthenticated", currentUser != null);

        if (currentUser != null) {
            model.addAttribute("isAdmin", currentUser.isAdmin());
            model.addAttribute("isMusician", currentUser.isMusician());
            model.addAttribute("isSubscriber", currentUser.isSubscriber() || currentUser.hasActiveSubscription());
            model.addAttribute("hasActiveSubscription", currentUser.hasActiveSubscription());
        }

        try {
            // Get popular tracks
            model.addAttribute("popularTracks", trackService.findPopularTracks(10));
            // Get new releases (albums)
            model.addAttribute("newReleases", albumService.findNewReleases(8));
            logger.info("Loaded {} tracks and {} albums",
                    trackService.findPopularTracks(10).size(),
                    albumService.findNewReleases(8).size());
        } catch (Exception e) {
            logger.error("Error loading data", e);
            model.addAttribute("popularTracks", java.util.Collections.emptyList());
            model.addAttribute("newReleases", java.util.Collections.emptyList());
        }

        return "index";
    }

    @GetMapping("/search")
    public String search(@RequestParam(required = false) String query,
                         Model model,
                         HttpServletRequest request) {
        User currentUser = authService.getCurrentUser(request);
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("isAuthenticated", currentUser != null);
        model.addAttribute("query", query);

        if (query != null && !query.trim().isEmpty()) {
            try {
                model.addAttribute("tracks", trackService.search(query));
                model.addAttribute("albums", albumService.search(query));
                // You would also add artists search here

                int totalResults = trackService.search(query).size() +
                        albumService.search(query).size();
                model.addAttribute("totalResults", totalResults);
            } catch (Exception e) {
                logger.error("Error searching", e);
                model.addAttribute("totalResults", 0);
            }
        }

        try {
            model.addAttribute("popularTracks", trackService.findPopularTracks(5));
        } catch (Exception e) {
            logger.error("Error loading popular tracks", e);
        }

        return "search/index";
    }
}
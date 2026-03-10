package com.musicstreaming.controller;

import com.musicstreaming.dto.HomeUserDTO;
import com.musicstreaming.model.Track;
import com.musicstreaming.model.Album;
import com.musicstreaming.model.User;
import com.musicstreaming.service.AlbumService;
import com.musicstreaming.service.AuthService;
import com.musicstreaming.service.TrackService;
import com.musicstreaming.service.SubscriptionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Optional;

@Controller
public class HomeController {

    private static final Logger logger = LoggerFactory.getLogger(HomeController.class);

    private final TrackService trackService;
    private final AlbumService albumService;
    private final AuthService authService;
    private final SubscriptionService subscriptionService;

    @Autowired
    public HomeController(TrackService trackService, AlbumService albumService,
                          AuthService authService, SubscriptionService subscriptionService) {
        this.trackService = trackService;
        this.albumService = albumService;
        this.authService = authService;
        this.subscriptionService = subscriptionService;
    }

    @GetMapping("/")
    public String index(Model model, HttpServletRequest request) {
        logger.info("Home page accessed");

        User sessionUser = authService.getCurrentUser(request);
        HomeUserDTO currentUser = null;

        if (sessionUser != null) {
            // Проверяем наличие активной подписки через отдельный сервис
            boolean hasActiveSubscription = subscriptionService.findActiveByUserId(sessionUser.getId()).isPresent();
            currentUser = new HomeUserDTO(sessionUser, hasActiveSubscription);
            logger.info("Current user: {}, hasActiveSubscription: {}", sessionUser.getUsername(), hasActiveSubscription);
        } else {
            logger.info("Current user: null");
        }

        model.addAttribute("currentUser", currentUser);
        model.addAttribute("isAuthenticated", currentUser != null);

        if (currentUser != null) {
            model.addAttribute("isAdmin", currentUser.isAdmin());
            model.addAttribute("isMusician", currentUser.isMusician());
            model.addAttribute("isSubscriber", currentUser.isSubscriber());
            model.addAttribute("hasActiveSubscription", currentUser.isHasActiveSubscription());
        }

        try {
            // Популярные треки
            List<Track> popularTracks = trackService.findPopularTracks(10);
            logger.info("Loaded {} popular tracks", popularTracks.size());
            model.addAttribute("popularTracks", popularTracks);

            // Новые релизы
            List<Album> newReleases = albumService.findNewReleases(8);
            logger.info("Loaded {} new releases", newReleases.size());
            model.addAttribute("newReleases", newReleases);

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
        User sessionUser = authService.getCurrentUser(request);
        HomeUserDTO currentUser = null;

        if (sessionUser != null) {
            boolean hasActiveSubscription = subscriptionService.findActiveByUserId(sessionUser.getId()).isPresent();
            currentUser = new HomeUserDTO(sessionUser, hasActiveSubscription);
        }

        model.addAttribute("currentUser", currentUser);
        model.addAttribute("isAuthenticated", currentUser != null);
        model.addAttribute("query", query);

        if (query != null && !query.trim().isEmpty()) {
            try {
                List<Track> tracks = trackService.search(query);
                List<Album> albums = albumService.search(query);

                logger.info("Search '{}' found {} tracks and {} albums", query, tracks.size(), albums.size());

                model.addAttribute("tracks", tracks);
                model.addAttribute("albums", albums);

                int totalResults = tracks.size() + albums.size();
                model.addAttribute("totalResults", totalResults);
            } catch (Exception e) {
                logger.error("Error searching", e);
                model.addAttribute("totalResults", 0);
            }
        }

        try {
            List<Track> popularTracks = trackService.findPopularTracks(5);
            model.addAttribute("popularTracks", popularTracks);
        } catch (Exception e) {
            logger.error("Error loading popular tracks", e);
        }

        return "search/index";
    }
}
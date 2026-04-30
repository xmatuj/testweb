package com.musicstreaming.controller;

import com.musicstreaming.dto.HomeUserDTO;
import com.musicstreaming.model.Track;
import com.musicstreaming.model.Album;
import com.musicstreaming.model.User;
import com.musicstreaming.repository.RecommendationRepository;
import com.musicstreaming.service.AlbumService;
import com.musicstreaming.service.AuthService;
import com.musicstreaming.service.TrackService;
import com.musicstreaming.service.SubscriptionService;
import com.musicstreaming.service.RecommendationService;
import com.musicstreaming.dto.ArtistDTO;
import com.musicstreaming.service.ArtistService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Controller
public class HomeController {

    private static final Logger logger = LoggerFactory.getLogger(HomeController.class);

    private final TrackService trackService;
    private final AlbumService albumService;
    private final AuthService authService;
    private final SubscriptionService subscriptionService;
    private final ArtistService artistService;
    private final RecommendationService recommendationService;

    @Autowired
    private RecommendationRepository recommendationRepository;

    @Autowired
    public HomeController(TrackService trackService, AlbumService albumService,
                          AuthService authService, SubscriptionService subscriptionService,
                          ArtistService artistService, RecommendationService recommendationService) {
        this.trackService = trackService;
        this.albumService = albumService;
        this.authService = authService;
        this.subscriptionService = subscriptionService;
        this.artistService = artistService;
        this.recommendationService = recommendationService;
    }

    @GetMapping("/")
    public String index(Model model, HttpServletRequest request) {
        logger.info("=== HOME PAGE ACCESSED ===");

        User sessionUser = authService.getCurrentUser(request);
        HomeUserDTO currentUser = null;

        if (sessionUser != null) {
            boolean hasActiveSubscription = subscriptionService.findActiveByUserId(sessionUser.getId()).isPresent();
            currentUser = new HomeUserDTO(sessionUser, hasActiveSubscription);
            logger.info("Current user: {}, id={}", sessionUser.getUsername(), sessionUser.getId());
        } else {
            logger.info("No authenticated user");
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
            // Новые релизы
            List<Album> newReleases = albumService.findNewReleases(8);
            model.addAttribute("newReleases", newReleases);

            // Рекомендации
            List<Track> recommendedTracks = new ArrayList<>();
            boolean hasPersonalizedRecommendations = false;

            if (sessionUser != null) {
                logger.info("=== Getting recommendations for user {} (id={}) ===",
                        sessionUser.getUsername(), sessionUser.getId());

                long listenCount = recommendationRepository.countByUserId(sessionUser.getId());
                logger.info("User {} has {} total listenings", sessionUser.getUsername(), listenCount);

                recommendedTracks = recommendationService.getRecommendationsForHome(
                        sessionUser.getId(), 10);
                hasPersonalizedRecommendations = recommendationService.hasEnoughDataForPersonalized(sessionUser.getId());

                logger.info("Got {} recommended tracks, hasPersonalizedRecommendations={}",
                        recommendedTracks.size(), hasPersonalizedRecommendations);
            } else {
                recommendedTracks = recommendationService.getPopularTracksForPeriod(10);
                hasPersonalizedRecommendations = false;
            }

            model.addAttribute("recommendedTracks", recommendedTracks);
            model.addAttribute("hasPersonalizedRecommendations", hasPersonalizedRecommendations);

            // Популярные треки
            List<Track> popularTracks = trackService.findPopularTracks(10);
            model.addAttribute("popularTracks", popularTracks);

        } catch (Exception e) {
            logger.error("Error loading data", e);
            model.addAttribute("popularTracks", Collections.emptyList());
            model.addAttribute("newReleases", Collections.emptyList());
            model.addAttribute("recommendedTracks", Collections.emptyList());
            model.addAttribute("hasPersonalizedRecommendations", false);
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

        if (currentUser != null) {
            model.addAttribute("isAdmin", currentUser.isAdmin());
            model.addAttribute("isMusician", currentUser.isMusician());
        }

        model.addAttribute("query", query);

        if (query != null && !query.trim().isEmpty()) {
            try {
                List<Track> tracks = trackService.search(query);
                List<Album> albums = albumService.search(query);
                List<ArtistDTO> artists = artistService.searchDTOs(query);

                model.addAttribute("tracks", tracks);
                model.addAttribute("albums", albums);
                model.addAttribute("artists", artists);

                int totalResults = tracks.size() + albums.size() + artists.size();
                model.addAttribute("totalResults", totalResults);
            } catch (Exception e) {
                logger.error("Error searching", e);
                model.addAttribute("totalResults", 0);
            }
        } else {
            // Если поисковый запрос пустой, показываем рекомендации
            try {
                List<Track> recommendedTracks;
                boolean hasPersonalizedRecommendations = false;

                if (sessionUser != null) {
                    recommendedTracks = recommendationService.getRecommendationsForSearch(
                            sessionUser.getId(), 10);
                    hasPersonalizedRecommendations = recommendationService.hasEnoughDataForPersonalized(sessionUser.getId());
                } else {
                    recommendedTracks = recommendationService.getPopularTracksForPeriod(10);
                }

                model.addAttribute("recommendedTracks", recommendedTracks);
                model.addAttribute("hasPersonalizedRecommendations", hasPersonalizedRecommendations);
            } catch (Exception e) {
                logger.error("Error loading recommendations for search", e);
                model.addAttribute("recommendedTracks", java.util.Collections.emptyList());
                model.addAttribute("hasPersonalizedRecommendations", false);
            }
        }

        try {
            List<Track> popularTracks = trackService.findPopularTracks(5);
            model.addAttribute("popularTracks", popularTracks);
        } catch (Exception e) {
            logger.error("Error loading popular tracks", e);
            model.addAttribute("popularTracks", java.util.Collections.emptyList());
        }

        return "search/index";
    }
}
package com.musicstreaming.controller;

import com.musicstreaming.dto.ArtistDTO;
import com.musicstreaming.dto.HomeUserDTO;
import com.musicstreaming.model.Album;
import com.musicstreaming.model.Track;
import com.musicstreaming.model.User;
import com.musicstreaming.service.AlbumService;
import com.musicstreaming.service.ArtistService;
import com.musicstreaming.service.AuthService;
import com.musicstreaming.service.SubscriptionService;
import com.musicstreaming.service.TrackService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Controller
@RequestMapping("/artist")
public class ArtistPublicController {

    @Autowired
    private ArtistService artistService;

    @Autowired
    private TrackService trackService;

    @Autowired
    private AlbumService albumService;

    @Autowired
    private AuthService authService;

    @Autowired
    private SubscriptionService subscriptionService;

    @GetMapping("/{id}")
    public String viewArtist(@PathVariable Integer id, Model model, HttpServletRequest request) {
        ArtistDTO artist = artistService.findDTOById(id);
        if (artist == null) {
            return "redirect:/search";
        }

        // Получаем текущего пользователя для авторизации
        User sessionUser = authService.getCurrentUser(request);
        HomeUserDTO currentUser = null;

        if (sessionUser != null) {
            boolean hasActiveSubscription = subscriptionService.findActiveByUserId(sessionUser.getId()).isPresent();
            currentUser = new HomeUserDTO(sessionUser, hasActiveSubscription);
        }

        // Добавляем атрибуты авторизации
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("isAuthenticated", currentUser != null);
        model.addAttribute("isAdmin", currentUser != null && currentUser.isAdmin());

        List<Track> popularTracks = trackService.findByArtistId(id);
        List<Album> albums = albumService.findByArtistId(id);

        model.addAttribute("artist", artist);
        model.addAttribute("popularTracks", popularTracks);
        model.addAttribute("albums", albums);

        // Заглушка для "похожих исполнителей"
        model.addAttribute("relatedArtists", artistService.findAllDTOs().stream().limit(4).toList());

        long totalListens = popularTracks.stream()
                .mapToLong(track -> trackService.getTotalListenCount(track.getId()))
                .sum();
        model.addAttribute("totalListens", totalListens);

        return "artist/view";
    }
}
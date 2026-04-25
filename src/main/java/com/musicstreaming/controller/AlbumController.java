package com.musicstreaming.controller;

import com.musicstreaming.model.Album;
import com.musicstreaming.model.Track;
import com.musicstreaming.service.AlbumService;
import com.musicstreaming.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Controller
@RequestMapping("/album")
public class AlbumController {

    @Autowired
    private AlbumService albumService;

    @Autowired
    private AuthService authService;

    @GetMapping("/{id}")
    public String viewAlbum(@PathVariable Integer id, Model model,
                            HttpServletRequest request) {
        // Записываем прослушивание альбома
        albumService.recordAlbumPlay(id);

        Album album = albumService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid album Id: " + id));

        List<Track> tracks = albumService.getAlbumTracks(id);
        Long totalListens = albumService.getTotalListenCount(id);
        int totalDuration = tracks.stream()
                .mapToInt(track -> track.getDuration() != null ? track.getDuration() : 0)
                .sum();

        model.addAttribute("album", album);
        model.addAttribute("tracks", tracks);
        model.addAttribute("totalListens", totalListens);
        model.addAttribute("totalDuration", totalDuration);
        model.addAttribute("isAuthenticated", authService.isAuthenticated(request));

        return "album/view";
    }
}
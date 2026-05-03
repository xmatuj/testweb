package com.musicstreaming.controller;

import com.musicstreaming.model.Album;
import com.musicstreaming.model.Track;
import com.musicstreaming.service.AlbumService;
import com.musicstreaming.service.AuthService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/album")
public class AlbumController {

    private static final Logger logger = LoggerFactory.getLogger(AlbumController.class);

    @Autowired
    private AlbumService albumService;

    @Autowired
    private AuthService authService;

    @GetMapping("/{id}")
    public String viewAlbum(@PathVariable Integer id, Model model,
                            HttpServletRequest request) {

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

    // Запись прослушивания альбома
    @PostMapping("/{id}/record-play")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> recordPlay(@PathVariable Integer id) {
        albumService.recordAlbumPlay(id);
        Long totalListens = albumService.getTotalListenCount(id);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("albumId", id);
        response.put("totalListens", totalListens);

        return ResponseEntity.ok(response);
    }

   // Запись прослушивания трека из альбома
    @PostMapping("/{albumId}/track/{trackId}/record-play")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> recordTrackPlay(@PathVariable Integer albumId,
                                                               @PathVariable Integer trackId) {
        albumService.recordAlbumPlay(albumId);
        Long totalListens = albumService.getTotalListenCount(albumId);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("albumId", albumId);
        response.put("trackId", trackId);
        response.put("totalListens", totalListens);

        return ResponseEntity.ok(response);
    }
}
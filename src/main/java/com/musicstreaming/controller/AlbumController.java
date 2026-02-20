package com.musicstreaming.controller;

import com.musicstreaming.model.Album;
import com.musicstreaming.model.Track;
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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping("/album")
public class AlbumController {

    private static final Logger logger = LoggerFactory.getLogger(AlbumController.class);

    private final AlbumService albumService;
    private final TrackService trackService;
    private final AuthService authService;

    @Autowired
    public AlbumController(AlbumService albumService, TrackService trackService, AuthService authService) {
        this.albumService = albumService;
        this.trackService = trackService;
        this.authService = authService;
    }

    /**
     * View album details page
     */
    @GetMapping("/{id}")
    public String viewAlbum(@PathVariable Integer id, Model model, HttpServletRequest request) {
        logger.info("Viewing album with ID: {}", id);

        Optional<Album> albumOpt = albumService.findById(id);
        if (albumOpt.isEmpty()) {
            return "redirect:/?error=Album not found";
        }

        Album album = albumOpt.get();
        List<Track> tracks = albumService.getAlbumTracks(id);

        User currentUser = authService.getCurrentUser(request);
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("isAuthenticated", currentUser != null);
        model.addAttribute("album", album);
        model.addAttribute("tracks", tracks);

        // Get similar albums (by same artist or genre)
        List<Album> similarAlbums = albumService.findByArtistId(album.getArtistId()).stream()
                .filter(a -> !a.getId().equals(id))
                .limit(4)
                .toList();
        model.addAttribute("similarAlbums", similarAlbums);

        return "album/view";
    }

    /**
     * API endpoint to get album data (for AJAX)
     */
    @GetMapping("/api/{id}")
    @ResponseBody
    public Map<String, Object> getAlbumApi(@PathVariable Integer id) {
        Map<String, Object> response = new HashMap<>();

        Optional<Album> albumOpt = albumService.findById(id);
        if (albumOpt.isPresent()) {
            Album album = albumOpt.get();
            List<Track> tracks = albumService.getAlbumTracks(id);

            response.put("success", true);
            response.put("album", album);
            response.put("tracks", tracks);
            response.put("trackCount", tracks.size());
        } else {
            response.put("success", false);
            response.put("message", "Album not found");
        }

        return response;
    }

    /**
     * Play first track of album
     */
    @GetMapping("/{id}/play")
    public String playAlbum(@PathVariable Integer id) {
        List<Track> tracks = albumService.getAlbumTracks(id);
        if (!tracks.isEmpty()) {
            // Redirect to first track
            return "redirect:/tracks/" + tracks.get(0).getId();
        }
        return "redirect:/album/" + id + "?error=No tracks available";
    }
}
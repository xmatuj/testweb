package com.musicstreaming.controller;

import com.musicstreaming.dto.PlaylistDTO;
import com.musicstreaming.model.User;
import com.musicstreaming.service.AuthService;
import com.musicstreaming.service.PlaylistService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/playlists")
public class PlaylistApiController {

    @Autowired
    private PlaylistService playlistService;

    @Autowired
    private AuthService authService;

    /**
     * Получить плейлисты текущего пользователя для модального окна
     */
    @GetMapping("/my")
    public ResponseEntity<Map<String, Object>> getMyPlaylists(HttpServletRequest request) {
        Map<String, Object> response = new HashMap<>();

        User currentUser = authService.getCurrentUser(request);
        if (currentUser == null) {
            response.put("success", false);
            response.put("error", "Необходимо войти в систему");
            return ResponseEntity.ok(response);
        }

        List<PlaylistDTO> playlists = playlistService.findByUserId(currentUser.getId());
        List<Map<String, Object>> playlistList = playlists.stream().map(playlist -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", playlist.getId());
            map.put("title", playlist.getTitle());
            map.put("trackCount", playlist.getTrackCount());
            map.put("visibility", playlist.getVisibility().name());
            return map;
        }).collect(Collectors.toList());

        response.put("success", true);
        response.put("playlists", playlistList);

        return ResponseEntity.ok(response);
    }

    /**
     * Добавить трек в плейлист
     */
    @PostMapping("/{playlistId}/add-track")
    public ResponseEntity<Map<String, Object>> addTrackToPlaylist(
            @PathVariable Integer playlistId,
            @RequestParam Integer trackId,
            HttpServletRequest request) {

        Map<String, Object> response = new HashMap<>();

        User currentUser = authService.getCurrentUser(request);
        if (currentUser == null) {
            response.put("success", false);
            response.put("error", "Необходимо войти в систему");
            return ResponseEntity.ok(response);
        }

        try {
            playlistService.addTrack(playlistId, trackId);
            response.put("success", true);
            response.put("message", "Трек добавлен в плейлист");
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
        }

        return ResponseEntity.ok(response);
    }

    /**
     * Проверить, есть ли трек в плейлисте
     */
    @GetMapping("/{playlistId}/has-track/{trackId}")
    public ResponseEntity<Map<String, Object>> hasTrackInPlaylist(
            @PathVariable Integer playlistId,
            @PathVariable Integer trackId) {

        Map<String, Object> response = new HashMap<>();

        try {
            List<com.musicstreaming.model.Track> tracks = playlistService.getTracks(playlistId);
            boolean hasTrack = tracks.stream().anyMatch(t -> t.getId().equals(trackId));

            response.put("success", true);
            response.put("hasTrack", hasTrack);
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
        }

        return ResponseEntity.ok(response);
    }
}
package com.musicstreaming.controller;

import com.musicstreaming.dto.PlaylistDTO;
import com.musicstreaming.model.User;
import com.musicstreaming.service.AuthService;
import com.musicstreaming.service.PlaylistService;
import com.musicstreaming.service.SubscriptionService;
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

    @Autowired
    private SubscriptionService subscriptionService;

    // Проверить, может ли пользователь создавать/редактировать плейлисты
    @GetMapping("/can-create")
    public ResponseEntity<Map<String, Object>> canCreatePlaylist(HttpServletRequest request) {
        Map<String, Object> response = new HashMap<>();

        User currentUser = authService.getCurrentUser(request);
        if (currentUser == null) {
            response.put("success", false);
            response.put("canCreate", false);
            response.put("error", "Необходимо войти в систему");
            return ResponseEntity.ok(response);
        }

        boolean canCreate = canUserManagePlaylists(currentUser);
        response.put("success", true);
        response.put("canCreate", canCreate);

        if (!canCreate) {
            response.put("message", "Для добавления треков в плейлисты необходима активная подписка Premium");
            response.put("subscriptionRequired", true);
        }

        return ResponseEntity.ok(response);
    }

    // Получить плейлисты текущего пользователя для модального окна
    @GetMapping("/my")
    public ResponseEntity<Map<String, Object>> getMyPlaylists(HttpServletRequest request) {
        Map<String, Object> response = new HashMap<>();

        User currentUser = authService.getCurrentUser(request);
        if (currentUser == null) {
            response.put("success", false);
            response.put("error", "Необходимо войти в систему");
            return ResponseEntity.ok(response);
        }

        // Проверяем, может ли пользователь управлять плейлистами
        boolean canManage = canUserManagePlaylists(currentUser);

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
        response.put("canManage", canManage);

        if (!canManage) {
            response.put("message", "Для добавления треков в плейлисты необходима активная подписка Premium");
        }

        return ResponseEntity.ok(response);
    }

    // Добавить трек в плейлист
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
            response.put("needAuth", true);
            return ResponseEntity.ok(response);
        }

        // Проверяем, может ли пользователь добавлять треки в плейлисты
        if (!canUserManagePlaylists(currentUser)) {
            response.put("success", false);
            response.put("error", "Для добавления треков в плейлисты необходима активная подписка Premium");
            response.put("subscriptionRequired", true);
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

    // Проверить, есть ли трек в плейлисте
    @GetMapping("/{playlistId}/has-track/{trackId}")
    public ResponseEntity<Map<String, Object>> hasTrackInPlaylist(
            @PathVariable Integer playlistId,
            @PathVariable Integer trackId,
            HttpServletRequest request) {

        Map<String, Object> response = new HashMap<>();

        User currentUser = authService.getCurrentUser(request);
        if (currentUser == null) {
            response.put("success", false);
            response.put("needAuth", true);
            return ResponseEntity.ok(response);
        }

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

    // Проверяет, может ли пользователь управлять плейлистами
    private boolean canUserManagePlaylists(User user) {
        if (user == null) return false;

        // Администраторы и музыканты могут управлять плейлистами
        if (user.getRole() == com.musicstreaming.model.User.UserRole.Admin ||
                user.getRole() == com.musicstreaming.model.User.UserRole.Musician) {
            return true;
        }

        // Подписчики могут управлять плейлистами
        if (user.getRole() == com.musicstreaming.model.User.UserRole.Subscriber) {
            return true;
        }

        // Проверяем наличие активной подписки
        return subscriptionService.findActiveByUserId(user.getId()).isPresent();
    }
}
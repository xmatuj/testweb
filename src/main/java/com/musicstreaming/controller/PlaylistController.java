package com.musicstreaming.controller;

import com.musicstreaming.dto.PlaylistDTO;
import com.musicstreaming.model.Playlist;
import com.musicstreaming.model.Track;
import com.musicstreaming.model.User;
import com.musicstreaming.service.AuthService;
import com.musicstreaming.service.PlaylistService;
import com.musicstreaming.service.SubscriptionService;
import com.musicstreaming.service.TrackService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Controller
@RequestMapping("/playlists")
public class PlaylistController {

    @Autowired
    private PlaylistService playlistService;

    @Autowired
    private TrackService trackService;

    @Autowired
    private AuthService authService;

    @Autowired
    private SubscriptionService subscriptionService;

    // ============ READ ALL ============
    @GetMapping
    public String listPlaylists(Model model, HttpServletRequest request) {
        User currentUser = authService.getCurrentUser(request);

        if (currentUser == null) {
            return "redirect:/account/login?redirect=/playlists";
        }

        model.addAttribute("playlists", playlistService.findByUserId(currentUser.getId()));
        model.addAttribute("publicPlaylists", playlistService.findPublicPlaylistsDTO());
        model.addAttribute("currentUser", currentUser);

        // Добавляем информацию о возможности создавать плейлисты
        boolean canCreatePlaylist = canCreatePlaylist(currentUser);
        model.addAttribute("canCreatePlaylist", canCreatePlaylist);

        return "playlists/list";
    }

    // ============ CREATE ============
    @GetMapping("/create")
    public String createForm(Model model, HttpServletRequest request,
                             RedirectAttributes redirectAttributes) {
        User currentUser = authService.getCurrentUser(request);
        if (currentUser == null) {
            return "redirect:/account/login?redirect=/playlists/create";
        }

        // Проверяем, может ли пользователь создавать плейлисты
        if (!canCreatePlaylist(currentUser)) {
            redirectAttributes.addFlashAttribute("error",
                    "Для создания плейлистов необходима активная подписка Premium");
            return "redirect:/playlists";
        }

        model.addAttribute("playlist", new Playlist());
        model.addAttribute("currentUser", currentUser);
        return "playlists/form";
    }

    @PostMapping("/save")
    public String savePlaylist(@ModelAttribute Playlist playlist,
                               HttpServletRequest request,
                               RedirectAttributes redirectAttributes) {
        User currentUser = authService.getCurrentUser(request);
        if (currentUser == null) {
            return "redirect:/account/login";
        }

        // Проверяем, может ли пользователь создавать плейлисты
        if (!canCreatePlaylist(currentUser)) {
            redirectAttributes.addFlashAttribute("error",
                    "Для создания плейлистов необходима активная подписка Premium");
            return "redirect:/playlists";
        }

        playlist.setUser(currentUser);
        Playlist saved = playlistService.save(playlist);

        redirectAttributes.addFlashAttribute("success", "Плейлист создан успешно!");
        return "redirect:/playlists/" + saved.getId();
    }

    // ============ READ ONE ============
    @GetMapping("/{id}")
    public String viewPlaylist(@PathVariable Integer id, Model model,
                               HttpServletRequest request,
                               RedirectAttributes redirectAttributes) {
        User currentUser = authService.getCurrentUser(request);

        // Получаем DTO с полностью загруженными данными
        PlaylistDTO playlistDTO = playlistService.findDTOById(id)
                .orElseThrow(() -> new IllegalArgumentException("Неверный ID плейлиста: " + id));

        // Проверка доступа к приватному плейлисту
        if (!playlistDTO.isPublic()) {
            if (currentUser == null || !currentUser.getId().equals(playlistDTO.getUserId())) {
                redirectAttributes.addFlashAttribute("error", "У вас нет доступа к этому плейлисту");
                return "redirect:/playlists";
            }
        }

        // Получаем треки плейлиста
        List<Track> tracks = playlistService.getTracks(id);
        List<Track> availableTracks = trackService.findModerated();

        model.addAttribute("playlist", playlistDTO);
        model.addAttribute("tracks", tracks);
        model.addAttribute("availableTracks", availableTracks);
        model.addAttribute("currentUser", currentUser);

        // Проверяем, может ли пользователь редактировать плейлист
        boolean canEdit = currentUser != null &&
                (currentUser.getId().equals(playlistDTO.getUserId()) || currentUser.isAdmin());
        model.addAttribute("canEdit", canEdit);

        return "playlists/view";
    }

    // ============ UPDATE (Add track to playlist) ============
    @PostMapping("/{playlistId}/add-track")
    public String addTrackToPlaylist(@PathVariable Integer playlistId,
                                     @RequestParam Integer trackId,
                                     HttpServletRequest request,
                                     RedirectAttributes redirectAttributes) {
        User currentUser = authService.getCurrentUser(request);
        if (currentUser == null) {
            return "redirect:/account/login";
        }

        // Проверяем права на редактирование плейлиста
        PlaylistDTO playlistDTO = playlistService.findDTOById(playlistId).orElse(null);
        if (playlistDTO == null || (!currentUser.getId().equals(playlistDTO.getUserId()) && !currentUser.isAdmin())) {
            redirectAttributes.addFlashAttribute("error", "У вас нет прав на редактирование этого плейлиста");
            return "redirect:/playlists/" + playlistId;
        }

        try {
            playlistService.addTrack(playlistId, trackId);
            redirectAttributes.addFlashAttribute("success", "Трек добавлен в плейлист");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Ошибка при добавлении трека: " + e.getMessage());
        }

        return "redirect:/playlists/" + playlistId;
    }

    // ============ DELETE (Remove track from playlist) ============
    @PostMapping("/{playlistId}/remove-track")
    public String removeTrackFromPlaylist(@PathVariable Integer playlistId,
                                          @RequestParam Integer trackId,
                                          HttpServletRequest request,
                                          RedirectAttributes redirectAttributes) {
        User currentUser = authService.getCurrentUser(request);
        if (currentUser == null) {
            return "redirect:/account/login";
        }

        // Проверяем права на редактирование плейлиста
        PlaylistDTO playlistDTO = playlistService.findDTOById(playlistId).orElse(null);
        if (playlistDTO == null || (!currentUser.getId().equals(playlistDTO.getUserId()) && !currentUser.isAdmin())) {
            redirectAttributes.addFlashAttribute("error", "У вас нет прав на редактирование этого плейлиста");
            return "redirect:/playlists/" + playlistId;
        }

        try {
            playlistService.removeTrack(playlistId, trackId);
            redirectAttributes.addFlashAttribute("success", "Трек удален из плейлиста");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Ошибка при удалении трека");
        }

        return "redirect:/playlists/" + playlistId;
    }

    // ============ UPDATE (Edit playlist) ============
    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Integer id, Model model,
                           HttpServletRequest request,
                           RedirectAttributes redirectAttributes) {
        User currentUser = authService.getCurrentUser(request);
        if (currentUser == null) {
            return "redirect:/account/login";
        }

        Playlist playlist = playlistService.findByIdWithUser(id)
                .orElseThrow(() -> new IllegalArgumentException("Неверный ID плейлиста: " + id));

        if (!currentUser.getId().equals(playlist.getUser().getId()) && !currentUser.isAdmin()) {
            redirectAttributes.addFlashAttribute("error", "Вы не можете редактировать этот плейлист");
            return "redirect:/playlists";
        }

        model.addAttribute("playlist", playlist);
        model.addAttribute("currentUser", currentUser);
        return "playlists/form";
    }

    // ============ DELETE (Delete playlist) ============
    @PostMapping("/delete/{id}")
    public String deletePlaylist(@PathVariable Integer id,
                                 HttpServletRequest request,
                                 RedirectAttributes redirectAttributes) {
        User currentUser = authService.getCurrentUser(request);
        if (currentUser == null) {
            return "redirect:/account/login";
        }

        PlaylistDTO playlistDTO = playlistService.findDTOById(id)
                .orElseThrow(() -> new IllegalArgumentException("Неверный ID плейлиста: " + id));

        if (!currentUser.getId().equals(playlistDTO.getUserId()) && !currentUser.isAdmin()) {
            redirectAttributes.addFlashAttribute("error", "Вы не можете удалить этот плейлист");
            return "redirect:/playlists";
        }

        playlistService.delete(id);
        redirectAttributes.addFlashAttribute("success", "Плейлист удален");
        return "redirect:/playlists";
    }

    /**
     * Проверяет, может ли пользователь создавать плейлисты
     */
    private boolean canCreatePlaylist(User user) {
        if (user == null) return false;

        // Администраторы и музыканты могут создавать плейлисты
        if (user.isAdmin() || user.isMusician()) return true;

        // Подписчики могут создавать плейлисты
        if (user.isSubscriber()) return true;

        // Проверяем наличие активной подписки
        return subscriptionService.findActiveByUserId(user.getId()).isPresent();
    }
}
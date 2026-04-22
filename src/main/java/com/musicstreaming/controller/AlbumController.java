package com.musicstreaming.controller;

import com.musicstreaming.model.Album;
import com.musicstreaming.model.Track;
import com.musicstreaming.service.AlbumService;
import com.musicstreaming.service.ArtistService;
import com.musicstreaming.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin/albums")
public class AlbumController {

    @Autowired
    private AlbumService albumService;

    @Autowired
    private ArtistService artistService;

    @Autowired
    private AuthService authService;

    @GetMapping
    public String listAlbums(Model model, HttpServletRequest request) {
        if (!authService.isAdmin(request)) return "redirect:/";

        model.addAttribute("albums", albumService.findAll());
        model.addAttribute("artists", artistService.findAll());
        model.addAttribute("currentUser", authService.getCurrentUser(request));
        return "admin/albums/list";
    }

    @GetMapping("/new")
    public String newAlbumForm(Model model, HttpServletRequest request) {
        if (!authService.isAdmin(request)) return "redirect:/";

        model.addAttribute("album", new Album());
        model.addAttribute("artists", artistService.findAll());
        model.addAttribute("currentUser", authService.getCurrentUser(request));
        return "admin/albums/form";
    }

    @PostMapping("/save")
    public String saveAlbum(@ModelAttribute Album album,
                            @RequestParam Integer artistId,
                            HttpServletRequest request,
                            RedirectAttributes redirectAttributes) {
        if (!authService.isAdmin(request)) return "redirect:/";

        album.setArtist(artistService.findById(artistId).orElse(null));
        albumService.save(album);
        redirectAttributes.addFlashAttribute("success", "Альбом сохранен");
        return "redirect:/admin/albums";
    }

    @GetMapping("/edit/{id}")
    public String editAlbumForm(@PathVariable Integer id, Model model,
                                HttpServletRequest request) {
        if (!authService.isAdmin(request)) return "redirect:/";

        Album album = albumService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid album Id"));

        model.addAttribute("album", album);
        model.addAttribute("artists", artistService.findAll());
        model.addAttribute("currentUser", authService.getCurrentUser(request));
        return "admin/albums/form";
    }

    @GetMapping("/{id}/tracks")
    @ResponseBody
    public List<Map<String, Object>> getAlbumTracks(@PathVariable Integer id) {
        List<Track> tracks = albumService.getAlbumTracks(id);
        return tracks.stream().map(track -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", track.getId());
            map.put("title", track.getTitle());
            map.put("artistName", track.getArtist() != null ? track.getArtist().getName() :
                    (track.getAlbum() != null && track.getAlbum().getArtist() != null ?
                            track.getAlbum().getArtist().getName() : "Неизвестный"));
            map.put("duration", track.getDuration());
            map.put("formattedDuration", track.getFormattedDuration());
            return map;
        }).collect(Collectors.toList());
    }

    @PostMapping("/delete/{id}")
    public String deleteAlbum(@PathVariable Integer id,
                              HttpServletRequest request,
                              RedirectAttributes redirectAttributes) {
        if (!authService.isAdmin(request)) return "redirect:/";

        albumService.deleteAlbum(id);
        redirectAttributes.addFlashAttribute("success", "Альбом удален");
        return "redirect:/admin/albums";
    }
}
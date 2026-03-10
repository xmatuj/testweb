package com.musicstreaming.controller;

import com.musicstreaming.dto.ArtistDTO;
import com.musicstreaming.model.Artist;
import com.musicstreaming.service.ArtistService;
import com.musicstreaming.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin/artists")
public class ArtistController {

    @Autowired
    private ArtistService artistService;

    @Autowired
    private AuthService authService;

    // READ ALL - Список всех исполнителей (ИСПРАВЛЕНО)
    @GetMapping
    public String listArtists(Model model, HttpServletRequest request) {
        if (!authService.isAdmin(request)) {
            return "redirect:/";
        }

        // ВАЖНО: Конвертируем в DTO, чтобы избежать LazyInitializationException
        List<ArtistDTO> artistDTOs = artistService.findAll()
                .stream()
                .map(ArtistDTO::new)
                .collect(Collectors.toList());

        model.addAttribute("artists", artistDTOs);
        model.addAttribute("currentUser", authService.getCurrentUser(request));
        return "admin/artists/list";
    }

    // READ ONE - Просмотр деталей (ИСПРАВЛЕНО)
    @GetMapping("/{id}")
    public String viewArtist(@PathVariable Integer id, Model model,
                             HttpServletRequest request) {
        if (!authService.isAdmin(request)) {
            return "redirect:/";
        }

        Artist artist = artistService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid artist Id"));

        // ВАЖНО: Используем DTO для безопасного отображения
        model.addAttribute("artist", new ArtistDTO(artist));
        model.addAttribute("currentUser", authService.getCurrentUser(request));
        return "admin/artists/view";
    }

    // CREATE - Форма создания
    @GetMapping("/new")
    public String newArtistForm(Model model, HttpServletRequest request) {
        if (!authService.isAdmin(request)) {
            return "redirect:/";
        }

        model.addAttribute("artist", new Artist());
        model.addAttribute("currentUser", authService.getCurrentUser(request));
        return "admin/artists/form";
    }

    // CREATE/UPDATE - Сохранение
    @PostMapping("/save")
    public String saveArtist(@ModelAttribute Artist artist,
                             HttpServletRequest request,
                             RedirectAttributes redirectAttributes) {
        if (!authService.isAdmin(request)) {
            return "redirect:/";
        }

        artistService.save(artist);
        redirectAttributes.addFlashAttribute("success", "Исполнитель сохранен");
        return "redirect:/admin/artists";
    }

    // UPDATE - Форма редактирования
    @GetMapping("/edit/{id}")
    public String editArtistForm(@PathVariable Integer id, Model model,
                                 HttpServletRequest request) {
        if (!authService.isAdmin(request)) {
            return "redirect:/";
        }

        Artist artist = artistService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid artist Id"));

        model.addAttribute("artist", artist);
        model.addAttribute("currentUser", authService.getCurrentUser(request));
        return "admin/artists/form";
    }

    // DELETE - Удаление
    @PostMapping("/delete/{id}")
    public String deleteArtist(@PathVariable Integer id,
                               HttpServletRequest request,
                               RedirectAttributes redirectAttributes) {
        if (!authService.isAdmin(request)) {
            return "redirect:/";
        }

        artistService.delete(id);
        redirectAttributes.addFlashAttribute("success", "Исполнитель удален");
        return "redirect:/admin/artists";
    }
}
package com.musicstreaming.controller;

import com.musicstreaming.dto.ArtistDTO;
import com.musicstreaming.model.Artist;
import com.musicstreaming.service.ArtistService;
import com.musicstreaming.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/admin/artists")
public class ArtistController {

    @Autowired
    private ArtistService artistService;

    @Autowired
    private AuthService authService;

    // Список всех исполнителей
    @GetMapping
    public String listArtists(@RequestParam(required = false) String search,
                              Model model,
                              HttpServletRequest request) {
        if (!authService.isAdmin(request)) {
            return "redirect:/";
        }

        List<ArtistDTO> artistDTOs;
        if (search != null && !search.trim().isEmpty()) {
            artistDTOs = artistService.searchDTOs(search);
            model.addAttribute("search", search);
        } else {
            artistDTOs = artistService.findAllDTOs();
        }

        model.addAttribute("artists", artistDTOs);
        model.addAttribute("currentUser", authService.getCurrentUser(request));
        return "admin/artists/list";
    }

    // Просмотр деталей
    @GetMapping("/{id}")
    public String viewArtist(@PathVariable Integer id,
                             Model model,
                             HttpServletRequest request,
                             RedirectAttributes redirectAttributes) {
        if (!authService.isAdmin(request)) {
            return "redirect:/";
        }

        ArtistDTO artistDTO = artistService.findDTOById(id);
        if (artistDTO == null) {
            redirectAttributes.addFlashAttribute("error", "Исполнитель не найден");
            return "redirect:/admin/artists";
        }

        model.addAttribute("artist", artistDTO);
        model.addAttribute("currentUser", authService.getCurrentUser(request));
        return "admin/artists/view";
    }

    // Форма создания
    @GetMapping("/new")
    public String newArtistForm(Model model, HttpServletRequest request) {
        if (!authService.isAdmin(request)) {
            return "redirect:/";
        }

        model.addAttribute("artist", new Artist());
        model.addAttribute("currentUser", authService.getCurrentUser(request));
        return "admin/artists/form";
    }

    // Сохранение
    @PostMapping("/save")
    public String saveArtist(@RequestParam(required = false) Integer id,
                             @RequestParam String name,
                             @RequestParam(required = false) String description,
                             @RequestParam(value = "photoFile", required = false) MultipartFile photoFile,
                             HttpServletRequest request,
                             RedirectAttributes redirectAttributes) {
        if (!authService.isAdmin(request)) {
            return "redirect:/";
        }

        try {
            Artist artist;
            if (id != null) {
                artist = artistService.findById(id)
                        .orElseThrow(() -> new IllegalArgumentException("Invalid artist Id"));
            } else {
                artist = new Artist();
            }

            artist.setName(name);
            artist.setDescription(description);

            if (photoFile != null && !photoFile.isEmpty()) {
                String userDir = System.getProperty("user.dir");
                Path uploadDir = Paths.get(userDir, "uploads", "images", "artists");
                Files.createDirectories(uploadDir);

                String ext = photoFile.getOriginalFilename();
                ext = ext != null && ext.contains(".") ? ext.substring(ext.lastIndexOf(".")) : ".jpg";
                String filename = "artist_" + UUID.randomUUID().toString().substring(0, 8) + ext;

                photoFile.transferTo(uploadDir.resolve(filename).toFile());
                artist.setPhotoPath("/images/artists/" + filename);
            }

            artistService.save(artist);
            redirectAttributes.addFlashAttribute("success", "Исполнитель сохранен");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Ошибка: " + e.getMessage());
        }
        return "redirect:/admin/artists";
    }

    // Форма редактирования
    @GetMapping("/edit/{id}")
    public String editArtistForm(@PathVariable Integer id,
                                 Model model,
                                 HttpServletRequest request,
                                 RedirectAttributes redirectAttributes) {
        if (!authService.isAdmin(request)) {
            return "redirect:/";
        }

        Artist artist = artistService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid artist Id"));

        model.addAttribute("artist", artist);
        model.addAttribute("currentUser", authService.getCurrentUser(request));
        return "admin/artists/form";
    }

    // Удаление
    @PostMapping("/delete/{id}")
    public String deleteArtist(@PathVariable Integer id,
                               HttpServletRequest request,
                               RedirectAttributes redirectAttributes) {
        if (!authService.isAdmin(request)) {
            return "redirect:/";
        }

        try {
            artistService.delete(id);
            redirectAttributes.addFlashAttribute("success", "Исполнитель удален");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Ошибка при удалении: " + e.getMessage());
        }
        return "redirect:/admin/artists";
    }
}
package com.musicstreaming.controller;

import com.musicstreaming.model.Genre;
import com.musicstreaming.service.GenreService;
import com.musicstreaming.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;

@Controller
@RequestMapping("/admin/genres")
public class GenreController {

    @Autowired
    private GenreService genreService;

    @Autowired
    private AuthService authService;

    @GetMapping
    public String listGenres(Model model, HttpServletRequest request) {
        if (!authService.isAdmin(request)) return "redirect:/";

        model.addAttribute("genres", genreService.findAll());
        model.addAttribute("currentUser", authService.getCurrentUser(request));
        return "admin/genres/list";
    }

    @GetMapping("/new")
    public String newGenreForm(Model model, HttpServletRequest request) {
        if (!authService.isAdmin(request)) return "redirect:/";

        model.addAttribute("genre", new Genre());
        model.addAttribute("currentUser", authService.getCurrentUser(request));
        return "admin/genres/form";
    }

    @PostMapping("/save")
    public String saveGenre(@ModelAttribute Genre genre,
                            HttpServletRequest request,
                            RedirectAttributes redirectAttributes) {
        if (!authService.isAdmin(request)) return "redirect:/";

        genreService.save(genre);
        redirectAttributes.addFlashAttribute("success", "Жанр сохранен");
        return "redirect:/admin/genres";
    }

    @GetMapping("/edit/{id}")
    public String editGenreForm(@PathVariable Integer id, Model model,
                                HttpServletRequest request) {
        if (!authService.isAdmin(request)) return "redirect:/";

        Genre genre = genreService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid genre Id"));

        model.addAttribute("genre", genre);
        model.addAttribute("currentUser", authService.getCurrentUser(request));
        return "admin/genres/form";
    }

    @PostMapping("/delete/{id}")
    public String deleteGenre(@PathVariable Integer id,
                              HttpServletRequest request,
                              RedirectAttributes redirectAttributes) {
        if (!authService.isAdmin(request)) return "redirect:/";

        genreService.delete(id);
        redirectAttributes.addFlashAttribute("success", "Жанр удален");
        return "redirect:/admin/genres";
    }
}
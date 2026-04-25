package com.musicstreaming.controller;

import com.musicstreaming.model.Genre;
import com.musicstreaming.model.Track;
import com.musicstreaming.service.GenreService;
import com.musicstreaming.service.TrackService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/genre")
public class GenreController {

    @Autowired
    private GenreService genreService;

    @Autowired
    private TrackService trackService;

    @GetMapping("/{id}")
    public String viewGenre(@PathVariable Integer id, Model model) {
        Genre genre = genreService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid genre Id: " + id));

        List<Track> tracks = trackService.findByGenreId(id);

        model.addAttribute("genre", genre);
        model.addAttribute("tracks", tracks);

        return "genre/view";
    }
}
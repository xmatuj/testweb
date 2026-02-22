package com.musicstreaming.service;

import com.musicstreaming.dao.GenreDAO;
import com.musicstreaming.model.Genre;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class GenreService {

    private final GenreDAO genreDAO;

    @Autowired
    public GenreService(GenreDAO genreDAO) {
        this.genreDAO = genreDAO;
    }

    public List<Genre> findAll() {
        return genreDAO.findAll();
    }

    public Optional<Genre> findById(Integer id) {
        return genreDAO.findById(id);
    }

    public Genre save(Genre genre) {
        return genreDAO.save(genre);
    }

    public void delete(Integer id) {
        genreDAO.delete(id);
    }
}
package com.musicstreaming.service;

import com.musicstreaming.model.Genre;
import com.musicstreaming.repository.GenreRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class GenreService {

    private final GenreRepository genreRepository;

    @Autowired
    public GenreService(GenreRepository genreRepository) {
        this.genreRepository = genreRepository;
    }

    public List<Genre> findAll() {
        return genreRepository.findAllOrdered();
    }

    public Optional<Genre> findById(Integer id) {
        return genreRepository.findById(id);
    }

    public Optional<Genre> findByName(String name) {
        return genreRepository.findByName(name);
    }

    @Transactional
    public Genre save(Genre genre) {
        return genreRepository.save(genre);
    }

    @Transactional
    public void delete(Integer id) {
        genreRepository.deleteById(id);
    }

    public boolean existsByName(String name) {
        return genreRepository.existsByName(name);
    }
}
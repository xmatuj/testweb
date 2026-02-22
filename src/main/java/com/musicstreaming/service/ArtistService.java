package com.musicstreaming.service;

import com.musicstreaming.dao.ArtistDAO;
import com.musicstreaming.model.Artist;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ArtistService {

    private final ArtistDAO artistDAO;

    @Autowired
    public ArtistService(ArtistDAO artistDAO) {
        this.artistDAO = artistDAO;
    }

    public List<Artist> findAll() {
        return artistDAO.findAll();
    }

    public Optional<Artist> findById(Integer id) {
        return artistDAO.findById(id);
    }

    public Artist save(Artist artist) {
        return artistDAO.save(artist);
    }

    public void delete(Integer id) {
        artistDAO.delete(id);
    }
}
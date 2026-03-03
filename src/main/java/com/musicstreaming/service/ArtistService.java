package com.musicstreaming.service;

import com.musicstreaming.model.Artist;
import com.musicstreaming.repository.ArtistRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ArtistService {

    private final ArtistRepository artistRepository;

    @Autowired
    public ArtistService(ArtistRepository artistRepository) {
        this.artistRepository = artistRepository;
    }

    public List<Artist> findAll() {
        return artistRepository.findAllOrdered();
    }

    public Optional<Artist> findById(Integer id) {
        return artistRepository.findById(id);
    }

    public Optional<Artist> findByName(String name) {
        return artistRepository.findByName(name);
    }

    public List<Artist> search(String query) {
        return artistRepository.search(query);
    }

    @Transactional
    public Artist save(Artist artist) {
        return artistRepository.save(artist);
    }

    @Transactional
    public void delete(Integer id) {
        artistRepository.deleteById(id);
    }
}
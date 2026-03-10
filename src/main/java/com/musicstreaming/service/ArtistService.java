package com.musicstreaming.service;

import com.musicstreaming.dto.ArtistDTO;
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

    // Существующие методы для работы с сущностями
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

    // НОВЫЕ МЕТОДЫ для работы с DTO (без LazyInitializationException)

    public List<ArtistDTO> findAllDTOs() {
        return artistRepository.findAllArtistDTOs();
    }

    public List<ArtistDTO> searchDTOs(String query) {
        if (query == null || query.trim().isEmpty()) {
            return findAllDTOs();
        }
        return artistRepository.searchArtistDTOs(query);
    }

    // Метод для получения одного исполнителя с подсчетами
    public ArtistDTO findDTOById(Integer id) {
        return artistRepository.findById(id)
                .map(artist -> new ArtistDTO(
                        artist.getId(),
                        artist.getName(),
                        artist.getDescription(),
                        artist.getAlbums().size(),
                        artist.getTracks().size()
                ))
                .orElse(null);
    }
}
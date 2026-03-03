package com.musicstreaming.service;

import com.musicstreaming.dto.PlaylistDTO;
import com.musicstreaming.model.Playlist;
import com.musicstreaming.model.PlaylistTrack;
import com.musicstreaming.repository.PlaylistRepository;
import com.musicstreaming.repository.PlaylistTrackRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class PlaylistService {

    private final PlaylistRepository playlistRepository;
    private final PlaylistTrackRepository playlistTrackRepository;

    @Autowired
    public PlaylistService(PlaylistRepository playlistRepository,
                           PlaylistTrackRepository playlistTrackRepository) {
        this.playlistRepository = playlistRepository;
        this.playlistTrackRepository = playlistTrackRepository;
    }

    public Optional<Playlist> findById(Integer id) {
        return playlistRepository.findById(id);
    }

    public List<PlaylistDTO> findDTOByUserId(Integer userId) {
        List<Playlist> playlists = playlistRepository.findByUserIdOrderByCreatedDateDesc(userId);
        return playlists.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<PlaylistDTO> findPublicPlaylistsDTO() {
        List<Playlist> playlists = playlistRepository.findPublicPlaylists();
        return playlists.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<PlaylistDTO> findByUserIdAndVisibilityDTO(Integer userId, Playlist.PlaylistVisibility visibility) {
        List<Playlist> playlists = playlistRepository.findByUserIdAndVisibility(userId, visibility);
        return playlists.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    private PlaylistDTO convertToDTO(Playlist playlist) {
        List<PlaylistTrack> tracks = playlistTrackRepository.findByPlaylistIdOrdered(playlist.getId());
        return new PlaylistDTO(playlist, tracks);
    }

    @Transactional
    public Playlist save(Playlist playlist) {
        return playlistRepository.save(playlist);
    }

    @Transactional
    public void delete(Integer id) {
        playlistRepository.deleteById(id);
    }

    public int countByUserId(Integer userId) {
        return playlistRepository.countByUserId(userId);
    }

    // Оставляем старые методы для обратной совместимости
    public List<Playlist> findByUserId(Integer userId) {
        return playlistRepository.findByUserIdOrderByCreatedDateDesc(userId);
    }
}
package com.musicstreaming.service;

import com.musicstreaming.dto.PlaylistDTO;
import com.musicstreaming.model.Playlist;
import com.musicstreaming.model.PlaylistTrack;
import com.musicstreaming.model.Track;
import com.musicstreaming.repository.PlaylistRepository;
import com.musicstreaming.repository.PlaylistTrackRepository;
import com.musicstreaming.repository.TrackRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class PlaylistService {

    @Autowired
    private PlaylistRepository playlistRepository;

    @Autowired
    private PlaylistTrackRepository playlistTrackRepository;

    @Autowired
    private TrackRepository trackRepository;

    // CREATE
    @Transactional
    public Playlist save(Playlist playlist) {
        return playlistRepository.save(playlist);
    }

    // READ ALL
    public List<Playlist> findAll() {
        return playlistRepository.findAll();
    }

    // READ BY USER
    public List<PlaylistDTO> findByUserId(Integer userId) {
        return playlistRepository.findByUserIdOrderByCreatedDateDesc(userId)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // READ ONE
    public Optional<Playlist> findById(Integer id) {
        return playlistRepository.findById(id);
    }

    // GET TRACKS FOR PLAYLIST (связь Many-to-Many)
    public List<Track> getTracks(Integer playlistId) {
        return playlistTrackRepository.findByPlaylistIdOrdered(playlistId)
                .stream()
                .map(PlaylistTrack::getTrack)
                .collect(Collectors.toList());
    }

    // ADD TRACK TO PLAYLIST (CREATE связь Many-to-Many)
    @Transactional
    public void addTrack(Integer playlistId, Integer trackId) {
        Playlist playlist = playlistRepository.findById(playlistId)
                .orElseThrow(() -> new IllegalArgumentException("Playlist not found"));
        Track track = trackRepository.findById(trackId)
                .orElseThrow(() -> new IllegalArgumentException("Track not found"));

        // Проверяем, нет ли уже такого трека
        boolean exists = playlistTrackRepository.findByPlaylistIdOrdered(playlistId)
                .stream()
                .anyMatch(pt -> pt.getTrack().getId().equals(trackId));

        if (!exists) {
            PlaylistTrack playlistTrack = new PlaylistTrack(playlist, track);
            playlistTrack.setPosition(playlistTrackRepository.countByPlaylistId(playlistId) + 1);
            playlistTrackRepository.save(playlistTrack);

            playlist.setUpdatedDate(java.time.LocalDateTime.now());
            playlistRepository.save(playlist);
        }
    }

    // REMOVE TRACK FROM PLAYLIST (DELETE связь Many-to-Many)
    @Transactional
    public void removeTrack(Integer playlistId, Integer trackId) {
        playlistTrackRepository.deleteByPlaylistIdAndTrackId(playlistId, trackId);

        // Обновляем позиции оставшихся треков
        List<PlaylistTrack> remainingTracks = playlistTrackRepository.findByPlaylistIdOrdered(playlistId);
        int position = 1;
        for (PlaylistTrack pt : remainingTracks) {
            pt.setPosition(position++);
            playlistTrackRepository.save(pt);
        }
    }

    // DELETE PLAYLIST (каскадное удаление связей)
    @Transactional
    public void delete(Integer id) {
        playlistTrackRepository.deleteByPlaylistId(id); // Сначала удаляем связи
        playlistRepository.deleteById(id); // Потом сам плейлист
    }

    // PUBLIC PLAYLISTS
    public List<PlaylistDTO> findPublicPlaylistsDTO() {
        return playlistRepository.findPublicPlaylists()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // CONVERT TO DTO
    private PlaylistDTO convertToDTO(Playlist playlist) {
        List<PlaylistTrack> tracks = playlistTrackRepository.findByPlaylistIdOrdered(playlist.getId());
        return new PlaylistDTO(playlist, tracks);
    }

    // COUNT BY USER
    public int countByUserId(Integer userId) {
        return playlistRepository.countByUserId(userId);
    }
}
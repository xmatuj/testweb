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

    // Создать
    @Transactional
    public Playlist save(Playlist playlist) {
        return playlistRepository.save(playlist);
    }

    // Вывести все
    public List<Playlist> findAll() {
        return playlistRepository.findAll();
    }

    // Вывести по юзеру
    @Transactional(readOnly = true)
    public List<PlaylistDTO> findByUserId(Integer userId) {
        return playlistRepository.findByUserIdOrderByCreatedDateDesc(userId)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // Вывести по айди
    public Optional<Playlist> findById(Integer id) {
        return playlistRepository.findById(id);
    }

    // Найти плейлист с пользователем и треками
    @Transactional(readOnly = true)
    public Optional<Playlist> findByIdWithUser(Integer id) {
        return playlistRepository.findByIdWithUser(id);
    }

    // Получить треки плейлиста
    @Transactional(readOnly = true)
    public List<Track> getTracks(Integer playlistId) {
        List<PlaylistTrack> playlistTracks = playlistTrackRepository.findByPlaylistIdOrdered(playlistId);
        return playlistTracks.stream()
                .map(PlaylistTrack::getTrack)
                .collect(Collectors.toList());
    }

    // Получить DTO плейлиста с полностью загруженными данными
    @Transactional(readOnly = true)
    public Optional<PlaylistDTO> findDTOById(Integer id) {
        return playlistRepository.findByIdWithUser(id)
                .map(playlist -> {
                    List<PlaylistTrack> tracks = playlistTrackRepository.findByPlaylistIdOrdered(id);
                    return new PlaylistDTO(playlist, tracks);
                });
    }

    // Добавить треки в плейлист
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

    // Удалить трек из плейлиста
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

    // Удалить плейлист
    @Transactional
    public void delete(Integer id) {
        playlistTrackRepository.deleteByPlaylistId(id);
        playlistRepository.deleteById(id);
    }

    // Публичные плейлисты
    @Transactional(readOnly = true)
    public List<PlaylistDTO> findPublicPlaylistsDTO() {
        return playlistRepository.findPublicPlaylists()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // Конвертация в DTO
    private PlaylistDTO convertToDTO(Playlist playlist) {
        List<PlaylistTrack> tracks = playlistTrackRepository.findByPlaylistIdOrdered(playlist.getId());
        return new PlaylistDTO(playlist, tracks);
    }

    // Подсчет по юзеру
    public int countByUserId(Integer userId) {
        return playlistRepository.countByUserId(userId);
    }
}
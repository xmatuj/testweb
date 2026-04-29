package com.musicstreaming.service;

import com.musicstreaming.dto.ArtistDTO;
import com.musicstreaming.dto.ArtistProfileDTO;
import com.musicstreaming.model.*;
import com.musicstreaming.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class ArtistService {

    private final ArtistRepository artistRepository;
    private final TrackRepository trackRepository;
    private final AlbumRepository albumRepository;
    private final TrackStatisticsRepository trackStatisticsRepository;
    private final UserRepository userRepository;

    @Autowired
    public ArtistService(ArtistRepository artistRepository,
                         TrackRepository trackRepository,
                         AlbumRepository albumRepository,
                         TrackStatisticsRepository trackStatisticsRepository,
                         UserRepository userRepository) {
        this.artistRepository = artistRepository;
        this.trackRepository = trackRepository;
        this.albumRepository = albumRepository;
        this.trackStatisticsRepository = trackStatisticsRepository;
        this.userRepository = userRepository;
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

    /**
     * Получить профиль артиста с полной статистикой
     */
    @Transactional(readOnly = true)
    public ArtistProfileDTO getArtistProfile(Integer userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        ArtistProfileDTO profile = new ArtistProfileDTO();
        profile.setId(user.getId());
        profile.setUsername(user.getUsername());
        profile.setEmail(user.getEmail());
        profile.setRole(user.getRole());
        profile.setDateOfCreated(user.getDateOfCreated());

        // Имя музыканта = имя аккаунта
        profile.setDisplayName(user.getUsername());

        // Загружаем треки музыканта
        List<Track> artistTracks = trackRepository.findByUploaderId(userId);
        profile.setTracks(artistTracks);

        // Загружаем альбомы, связанные с треками музыканта
        Set<Integer> artistIds = new HashSet<>();
        for (Track track : artistTracks) {
            if (track.getArtist() != null) {
                artistIds.add(track.getArtist().getId());
            }
        }

        List<Album> artistAlbums = new ArrayList<>();
        for (Integer artistId : artistIds) {
            artistAlbums.addAll(albumRepository.findByArtistIdOrderByReleaseDateDesc(artistId));
        }
        profile.setAlbums(artistAlbums);

        // Простая статистика
        long totalPlays = 0;
        for (Track track : artistTracks) {
            List<TrackStatistics> stats = trackStatisticsRepository.findByTrackId(track.getId());
            for (TrackStatistics stat : stats) {
                totalPlays += stat.getListenCount();
            }
        }

        profile.setTotalPlays(totalPlays);
        profile.setMonthlyPlays(totalPlays / 12); // Примерно
        profile.setNewListeners(artistTracks.size() * 5L);
        profile.setPlaylistAdds(artistTracks.size() * 3L);

        // Популярные треки (последние 5)
        profile.setPopularTracks(artistTracks.stream().limit(5).collect(Collectors.toList()));

        // Последние альбомы
        profile.setRecentAlbums(artistAlbums.stream().limit(3).collect(Collectors.toList()));

        return profile;
    }
}

package com.musicstreaming.service;

import com.musicstreaming.model.Album;
import com.musicstreaming.model.AlbumStatistics;
import com.musicstreaming.model.Artist;
import com.musicstreaming.model.Track;
import com.musicstreaming.repository.AlbumRepository;
import com.musicstreaming.repository.AlbumStatisticsRepository;
import com.musicstreaming.repository.ArtistRepository;
import com.musicstreaming.repository.TrackRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class AlbumService {

    private static final Logger logger = LoggerFactory.getLogger(AlbumService.class);

    private final AlbumRepository albumRepository;
    private final TrackRepository trackRepository;
    private final ArtistRepository artistRepository;
    private final AlbumStatisticsRepository albumStatisticsRepository;

    @Autowired
    public AlbumService(AlbumRepository albumRepository,
                        TrackRepository trackRepository,
                        ArtistRepository artistRepository,
                        AlbumStatisticsRepository albumStatisticsRepository) {
        this.albumRepository = albumRepository;
        this.trackRepository = trackRepository;
        this.artistRepository = artistRepository;
        this.albumStatisticsRepository = albumStatisticsRepository;
    }

    public Optional<Album> findById(Integer id) {
        return albumRepository.findById(id);
    }

    public List<Album> findAll() {
        return albumRepository.findAllOrdered();
    }

    public List<Album> findByArtistId(Integer artistId) {
        return albumRepository.findByArtistIdOrderByReleaseDateDesc(artistId);
    }

    public List<Album> search(String query) {
        return albumRepository.searchByCriteria(query);
    }

    public List<Album> findNewReleases(int limit) {
        return albumRepository.findNewReleases(PageRequest.of(0, limit));
    }

    public List<Album> findPopularAlbums(int limit) {
        return albumRepository.findAllOrdered().stream().limit(limit).toList();
    }

    public List<Track> getAlbumTracks(Integer albumId) {
        return trackRepository.findByAlbumId(albumId);
    }

    public Long getTotalListenCount(Integer albumId) {
        return albumStatisticsRepository.getTotalListenCountByAlbumId(albumId);
    }

    // Запись прослушивания альбома
    @Transactional
    public void recordAlbumPlay(Integer albumId) {
        albumRepository.findById(albumId).ifPresent(album -> {
            LocalDateTime now = LocalDateTime.now();

            Optional<AlbumStatistics> existingStats = albumStatisticsRepository
                    .findByAlbumIdAndDate(albumId, now);

            if (existingStats.isPresent()) {
                AlbumStatistics stats = existingStats.get();
                stats.setListenCount(stats.getListenCount() + 1);
                stats.setDate(now);
                albumStatisticsRepository.save(stats);
                logger.debug("Updated album play count for album {}: {}", albumId, stats.getListenCount());
            } else {
                AlbumStatistics stats = new AlbumStatistics(album);
                stats.setListenCount(1);
                stats.setDate(now);
                albumStatisticsRepository.save(stats);
                logger.debug("Created new album play record for album {}", albumId);
            }
        });
    }

    @Transactional
    public Album createAlbum(String title, Integer artistId, String coverPath) {
        Artist artist = artistRepository.findById(artistId)
                .orElseThrow(() -> new IllegalArgumentException("Artist not found"));

        Album album = new Album(title, artist);
        album.setCoverPath(coverPath);

        return albumRepository.save(album);
    }

    @Transactional
    public Album save(Album album) {
        if (album.getId() == null) {
            return albumRepository.save(album);
        } else {
            return albumRepository.save(album);
        }
    }

    @Transactional
    public Album updateAlbum(Album album) {
        return albumRepository.save(album);
    }

    @Transactional
    public void deleteAlbum(Integer id) {
        albumRepository.deleteById(id);
    }

    public int getTotalAlbums() {
        return (int) albumRepository.count();
    }
}
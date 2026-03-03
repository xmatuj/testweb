package com.musicstreaming.service;

import com.musicstreaming.model.Album;
import com.musicstreaming.model.Artist;
import com.musicstreaming.model.Track;
import com.musicstreaming.repository.AlbumRepository;
import com.musicstreaming.repository.ArtistRepository;
import com.musicstreaming.repository.TrackRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class AlbumService {

    private final AlbumRepository albumRepository;
    private final TrackRepository trackRepository;
    private final ArtistRepository artistRepository;

    @Autowired
    public AlbumService(AlbumRepository albumRepository,
                        TrackRepository trackRepository,
                        ArtistRepository artistRepository) {
        this.albumRepository = albumRepository;
        this.trackRepository = trackRepository;
        this.artistRepository = artistRepository;
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
        return albumRepository.search(query);
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

    @Transactional
    public Album createAlbum(String title, Integer artistId, String coverPath) {
        Artist artist = artistRepository.findById(artistId)
                .orElseThrow(() -> new IllegalArgumentException("Artist not found"));

        Album album = new Album(title, artist);
        album.setCoverPath(coverPath);

        return albumRepository.save(album);
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
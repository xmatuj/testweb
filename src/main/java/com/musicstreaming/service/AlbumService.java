package com.musicstreaming.service;

import com.musicstreaming.dao.AlbumDAO;
import com.musicstreaming.dao.TrackDAO;
import com.musicstreaming.model.Album;
import com.musicstreaming.model.Track;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class AlbumService {

    private static final Logger logger = LoggerFactory.getLogger(AlbumService.class);

    private final AlbumDAO albumDAO;
    private final TrackDAO trackDAO;

    @Autowired
    public AlbumService(AlbumDAO albumDAO, TrackDAO trackDAO) {
        this.albumDAO = albumDAO;
        this.trackDAO = trackDAO;
    }

    /**
     * Find album by ID
     */
    public Optional<Album> findById(Integer id) {
        return albumDAO.findById(id);
    }

    /**
     * Get all albums
     */
    public List<Album> findAll() {
        return albumDAO.findAll();
    }

    /**
     * Find albums by artist ID
     */
    public List<Album> findByArtistId(Integer artistId) {
        return albumDAO.findByArtistId(artistId);
    }

    /**
     * Search albums by title or artist name
     */
    public List<Album> search(String query) {
        return albumDAO.search(query);
    }

    /**
     * Get new releases (latest albums)
     */
    public List<Album> findNewReleases(int limit) {
        List<Album> allAlbums = albumDAO.findAll();
        // Sort by release date (newest first) and limit
        return allAlbums.stream()
                .sorted((a1, a2) -> {
                    if (a1.getReleaseDate() == null && a2.getReleaseDate() == null) return 0;
                    if (a1.getReleaseDate() == null) return 1;
                    if (a2.getReleaseDate() == null) return -1;
                    return a2.getReleaseDate().compareTo(a1.getReleaseDate());
                })
                .limit(limit)
                .toList();
    }

    /**
     * Get popular albums (based on total listens)
     */
    public List<Album> findPopularAlbums(int limit) {
        // This would normally use statistics, for now return all albums
        return albumDAO.findAll().stream()
                .limit(limit)
                .toList();
    }

    /**
     * Get tracks for an album
     */
    public List<Track> getAlbumTracks(Integer albumId) {
        return trackDAO.findByAlbumId(albumId);
    }

    /**
     * Create new album
     */
    public Album createAlbum(String title, Integer artistId, String coverPath) {
        Album album = new Album();
        album.setTitle(title);
        album.setArtistId(artistId);
        album.setCoverPath(coverPath);

        Album savedAlbum = albumDAO.save(album);
        logger.info("Created new album: {} (ID: {})", title, savedAlbum.getId());

        return savedAlbum;
    }

    /**
     * Update existing album
     */
    public Album updateAlbum(Album album) {
        Album updatedAlbum = albumDAO.save(album);
        logger.info("Updated album: {} (ID: {})", album.getTitle(), album.getId());

        return updatedAlbum;
    }

    /**
     * Delete album
     */
    public void deleteAlbum(Integer id) {
        albumDAO.delete(id);
        logger.info("Deleted album with ID: {}", id);
    }

    /**
     * Get total number of albums
     */
    public int getTotalAlbums() {
        return albumDAO.findAll().size();
    }
}
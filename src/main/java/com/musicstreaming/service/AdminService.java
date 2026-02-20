package com.musicstreaming.service;

import com.musicstreaming.dao.*;
import com.musicstreaming.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AdminService {

    private static final Logger logger = LoggerFactory.getLogger(AdminService.class);

    private final UserDAO userDAO;
    private final TrackDAO trackDAO;
    private final ArtistDAO artistDAO;
    private final AlbumDAO albumDAO;
    private final GenreDAO genreDAO;
    private final ModerationDAO moderationDAO;

    @Autowired
    public AdminService(UserDAO userDAO, TrackDAO trackDAO, ArtistDAO artistDAO,
                        AlbumDAO albumDAO, GenreDAO genreDAO, ModerationDAO moderationDAO) {
        this.userDAO = userDAO;
        this.trackDAO = trackDAO;
        this.artistDAO = artistDAO;
        this.albumDAO = albumDAO;
        this.genreDAO = genreDAO;
        this.moderationDAO = moderationDAO;
    }

    // Dashboard statistics
    public int getTotalUsers() {
        return userDAO.findAll().size();
    }

    public int getTotalTracks() {
        return trackDAO.findAll().size();
    }

    public int getTotalArtists() {
        return artistDAO.findAll().size();
    }

    public int getTotalAlbums() {
        return albumDAO.findAll().size();
    }

    public int getTotalGenres() {
        return genreDAO.findAll().size();
    }

    public int getPendingModerationCount() {
        return moderationDAO.findPendingCount();
    }

    // User management
    public List<User> getAllUsers() {
        return userDAO.findAll();
    }

    public List<User> searchUsers(String searchTerm) {
        return userDAO.search(searchTerm);
    }

    @Transactional
    public void updateUserRole(Integer userId, User.UserRole newRole) {
        userDAO.updateRole(userId, newRole);
        logger.info("User {} role updated to {}", userId, newRole);
    }

    // Track management
    public List<Track> getAllTracks() {
        return trackDAO.findAll();
    }

    public List<Track> getPendingTracks() {
        return trackDAO.findPendingModeration();
    }

    @Transactional
    public void approveTrack(Integer trackId, Integer moderatorId, String comment) {
        trackDAO.updateModerationStatus(trackId, true);

        Moderation moderation = new Moderation();
        moderation.setTrackId(trackId);
        moderation.setModeratorId(moderatorId);
        moderation.setStatus(Moderation.ModerationStatus.Approved);
        moderation.setComment(comment != null ? comment : "Track approved");
        moderationDAO.save(moderation);

        logger.info("Track {} approved by moderator {}", trackId, moderatorId);
    }

    @Transactional
    public void rejectTrack(Integer trackId, Integer moderatorId, String comment) {
        trackDAO.updateModerationStatus(trackId, false);

        Moderation moderation = new Moderation();
        moderation.setTrackId(trackId);
        moderation.setModeratorId(moderatorId);
        moderation.setStatus(Moderation.ModerationStatus.Rejected);
        moderation.setComment(comment != null ? comment : "Track rejected");
        moderationDAO.save(moderation);

        logger.info("Track {} rejected by moderator {}", trackId, moderatorId);
    }

    // Artist management
    public Artist createArtist(String name, String description, String photoPath) {
        Artist artist = new Artist();
        artist.setName(name);
        artist.setDescription(description);
        artist.setPhotoPath(photoPath);
        return artistDAO.save(artist);
    }

    public void updateArtist(Artist artist) {
        artistDAO.save(artist);
    }

    public void deleteArtist(Integer id) {
        artistDAO.delete(id);
    }

    // Album management
    public Album createAlbum(String title, Integer artistId, String coverPath) {
        Album album = new Album();
        album.setTitle(title);
        album.setArtistId(artistId);
        album.setCoverPath(coverPath);
        return albumDAO.save(album);
    }

    public void updateAlbum(Album album) {
        albumDAO.save(album);
    }

    public void deleteAlbum(Integer id) {
        albumDAO.delete(id);
    }

    // Genre management
    public Genre createGenre(String name) {
        if (genreDAO.existsByName(name)) {
            throw new IllegalArgumentException("Genre with name '" + name + "' already exists");
        }
        Genre genre = new Genre();
        genre.setName(name);
        return genreDAO.save(genre);
    }

    public void updateGenre(Genre genre) {
        genreDAO.save(genre);
    }

    public void deleteGenre(Integer id) {
        genreDAO.delete(id);
    }
}
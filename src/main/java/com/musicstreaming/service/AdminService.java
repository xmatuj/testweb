package com.musicstreaming.service;

import com.musicstreaming.model.*;
import com.musicstreaming.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class AdminService {

    private static final Logger logger = LoggerFactory.getLogger(AdminService.class);

    private final UserRepository userRepository;
    private final TrackRepository trackRepository;
    private final ArtistRepository artistRepository;
    private final AlbumRepository albumRepository;
    private final GenreRepository genreRepository;
    private final ModerationRepository moderationRepository;

    @Autowired
    public AdminService(UserRepository userRepository, TrackRepository trackRepository,
                        ArtistRepository artistRepository, AlbumRepository albumRepository,
                        GenreRepository genreRepository, ModerationRepository moderationRepository) {
        this.userRepository = userRepository;
        this.trackRepository = trackRepository;
        this.artistRepository = artistRepository;
        this.albumRepository = albumRepository;
        this.genreRepository = genreRepository;
        this.moderationRepository = moderationRepository;
    }

    public int getTotalUsers() {
        return (int) userRepository.count();
    }

    public int getTotalTracks() {
        return (int) trackRepository.count();
    }

    public int getTotalArtists() {
        return (int) artistRepository.count();
    }

    public int getTotalAlbums() {
        return (int) albumRepository.count();
    }

    public int getTotalGenres() {
        return (int) genreRepository.count();
    }

    public int getPendingModerationCount() {
        return (int) moderationRepository.countPending();
    }

    public List<User> getAllUsers() {
        return userRepository.findAllOrdered();
    }

    public List<User> searchUsers(String searchTerm) {
        return userRepository.search(searchTerm);
    }

    @Transactional
    public void updateUserRole(Integer userId, User.UserRole newRole) {
        userRepository.findById(userId).ifPresent(user -> {
            user.setRole(newRole);
            userRepository.save(user);
            logger.info("User {} role updated to {}", userId, newRole);
        });
    }

    public List<Track> getAllTracks() {
        return trackRepository.findAll();
    }

    public List<Track> getPendingTracks() {
        return trackRepository.findPendingModeration();
    }

    @Transactional
    public void approveTrack(Integer trackId, Integer moderatorId, String comment) {
        trackRepository.updateModerationStatus(trackId, true);

        trackRepository.findById(trackId).ifPresent(track -> {
            Moderation moderation = new Moderation();
            moderation.setTrack(track);
            User moderator = new User();
            moderator.setId(moderatorId);
            moderation.setModerator(moderator);
            moderation.setStatus(Moderation.ModerationStatus.Approved);
            moderation.setComment(comment != null ? comment : "Track approved");
            moderationRepository.save(moderation);
            logger.info("Track {} approved by moderator {}", trackId, moderatorId);
        });
    }

    @Transactional
    public void rejectTrack(Integer trackId, Integer moderatorId, String comment) {
        trackRepository.findById(trackId).ifPresent(track -> {
            Moderation moderation = new Moderation();
            moderation.setTrack(track);
            User moderator = new User();
            moderator.setId(moderatorId);
            moderation.setModerator(moderator);
            moderation.setStatus(Moderation.ModerationStatus.Rejected);
            moderation.setComment(comment != null ? comment : "Track rejected");
            moderationRepository.save(moderation);
            logger.info("Track {} rejected by moderator {}", trackId, moderatorId);
        });
    }

    @Transactional
    public Artist createArtist(String name, String description, String photoPath) {
        Artist artist = new Artist(name, description);
        artist.setPhotoPath(photoPath);
        return artistRepository.save(artist);
    }

    @Transactional
    public void updateArtist(Artist artist) {
        artistRepository.save(artist);
    }

    @Transactional
    public void deleteArtist(Integer id) {
        artistRepository.deleteById(id);
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
    public void updateAlbum(Album album) {
        albumRepository.save(album);
    }

    @Transactional
    public void deleteAlbum(Integer id) {
        albumRepository.deleteById(id);
    }

    @Transactional
    public Genre createGenre(String name) {
        if (genreRepository.existsByName(name)) {
            throw new IllegalArgumentException("Genre with name '" + name + "' already exists");
        }
        Genre genre = new Genre(name);
        return genreRepository.save(genre);
    }

    @Transactional
    public void updateGenre(Genre genre) {
        genreRepository.save(genre);
    }

    @Transactional
    public void deleteGenre(Integer id) {
        genreRepository.deleteById(id);
    }

    public List<Artist> getAllArtists() {
        return artistRepository.findAllOrdered();
    }

    public List<Album> getAllAlbums() {
        return albumRepository.findAllOrdered();
    }

    public List<Genre> getAllGenres() {
        return genreRepository.findAllOrdered();
    }
}
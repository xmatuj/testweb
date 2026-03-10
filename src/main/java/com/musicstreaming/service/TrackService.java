package com.musicstreaming.service;

import com.musicstreaming.model.*;
import com.musicstreaming.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class TrackService {

    private final TrackRepository trackRepository;
    private final ModerationRepository moderationRepository;
    private final ArtistRepository artistRepository;
    private final AlbumRepository albumRepository;
    private final GenreRepository genreRepository;

    @Autowired
    public TrackService(TrackRepository trackRepository,
                        ModerationRepository moderationRepository,
                        ArtistRepository artistRepository,
                        AlbumRepository albumRepository,
                        GenreRepository genreRepository) {
        this.trackRepository = trackRepository;
        this.moderationRepository = moderationRepository;
        this.artistRepository = artistRepository;
        this.albumRepository = albumRepository;
        this.genreRepository = genreRepository;
    }

    public Optional<Track> findById(Integer id) {
        return trackRepository.findById(id);
    }

    public List<Track> findAll() {
        return trackRepository.findAll();
    }

    public List<Track> findModerated() {
        return trackRepository.findByIsModeratedTrueOrderByIdDesc();
    }

    public List<Track> findPendingModeration() {
        return trackRepository.findPendingModeration();
    }

    public List<Track> findByArtistId(Integer artistId) {
        return trackRepository.findByArtistId(artistId);
    }

    public List<Track> findByAlbumId(Integer albumId) {
        return trackRepository.findByAlbumId(albumId);
    }

    public List<Track> findByGenreId(Integer genreId) {
        return trackRepository.findByGenreId(genreId);
    }

    public List<Track> findByUploaderId(Integer userId) {
        return trackRepository.findByUploaderId(userId);
    }

    public List<Track> search(String query) {
        return trackRepository.search(query);
    }

    public List<Track> findSimilar(Integer genreId, Integer excludeTrackId, int limit) {
        return trackRepository.findSimilar(genreId, excludeTrackId, PageRequest.of(0, limit));
    }

    public List<Track> findPopularTracks(int limit) {
        return trackRepository.findByIsModeratedTrueOrderByIdDesc()
                .stream()
                .limit(limit)
                .toList();
    }

    @Transactional
    public Track save(Track track) {
        // Load relationships if IDs are set via entity objects
        if (track.getArtist() != null && track.getArtist().getId() != null) {
            artistRepository.findById(track.getArtist().getId()).ifPresent(track::setArtist);
        }
        if (track.getAlbum() != null && track.getAlbum().getId() != null) {
            albumRepository.findById(track.getAlbum().getId()).ifPresent(track::setAlbum);
        }
        if (track.getGenre() != null && track.getGenre().getId() != null) {
            genreRepository.findById(track.getGenre().getId()).ifPresent(track::setGenre);
        }
        return trackRepository.save(track);
    }

    @Transactional
    public void approveTrack(Integer trackId, Integer moderatorId, String comment) {
        int updated = trackRepository.updateModerationStatus(trackId, true);

        if (updated > 0) {
            trackRepository.findById(trackId).ifPresent(track -> {
                Moderation moderation = new Moderation();
                moderation.setTrack(track);
                User moderator = new User();
                moderator.setId(moderatorId);
                moderation.setModerator(moderator);
                moderation.setStatus(Moderation.ModerationStatus.Approved);
                moderation.setComment(comment != null ? comment : "Track approved");
                moderationRepository.save(moderation);
            });
        }
    }

    @Transactional
    public void rejectTrack(Integer trackId, Integer moderatorId, String comment) {
        int updated = trackRepository.updateModerationStatus(trackId, false);

        if (updated > 0) {
            trackRepository.findById(trackId).ifPresent(track -> {
                Moderation moderation = new Moderation();
                moderation.setTrack(track);
                User moderator = new User();
                moderator.setId(moderatorId);
                moderation.setModerator(moderator);
                moderation.setStatus(Moderation.ModerationStatus.Rejected);
                moderation.setComment(comment != null ? comment : "Track rejected");
                moderationRepository.save(moderation);
            });
        }
    }

    public Optional<Track> findByIdWithUser(Integer id) {
        return trackRepository.findByIdWithUser(id);
    }

    @Transactional
    public void delete(Integer id) {
        trackRepository.deleteById(id);
    }
}
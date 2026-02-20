package com.musicstreaming.service;

import com.musicstreaming.dao.TrackDAO;
import com.musicstreaming.model.Track;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TrackService {

    private final TrackDAO trackDAO;

    @Autowired
    public TrackService(TrackDAO trackDAO) {
        this.trackDAO = trackDAO;
    }

    public Optional<Track> findById(Integer id) {
        return trackDAO.findById(id);
    }

    public List<Track> findAll() {
        return trackDAO.findAll();
    }

    public List<Track> findModerated() {
        return trackDAO.findModerated();
    }

    public List<Track> findPendingModeration() {
        return trackDAO.findPendingModeration();
    }

    public List<Track> findByArtistId(Integer artistId) {
        return trackDAO.findByArtistId(artistId);
    }

    public List<Track> findByAlbumId(Integer albumId) {
        return trackDAO.findByAlbumId(albumId);
    }

    public List<Track> findByGenreId(Integer genreId) {
        return trackDAO.findByGenreId(genreId);
    }

    public List<Track> findByUploaderId(Integer userId) {
        return trackDAO.findByUploaderId(userId);
    }

    public List<Track> search(String query) {
        return trackDAO.search(query);
    }

    public List<Track> findSimilar(Integer genreId, Integer excludeTrackId, int limit) {
        return trackDAO.findSimilar(genreId, excludeTrackId, limit);
    }

    public List<Track> findPopularTracks(int limit) {
        // This would normally use statistics, for now just return latest moderated tracks
        return trackDAO.findModerated().stream().limit(limit).toList();
    }

    public Track save(Track track) {
        return trackDAO.save(track);
    }

    public void approveTrack(Integer trackId, Integer moderatorId, String comment) {
        trackDAO.updateModerationStatus(trackId, true);
        // Here you would also create a moderation record
    }

    public void rejectTrack(Integer trackId, Integer moderatorId, String comment) {
        trackDAO.updateModerationStatus(trackId, false);
        // Here you would also create a moderation record with rejection comment
    }

    public void delete(Integer id) {
        trackDAO.delete(id);
    }
}
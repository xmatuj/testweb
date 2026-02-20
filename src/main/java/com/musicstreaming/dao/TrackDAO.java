package com.musicstreaming.dao;

import com.musicstreaming.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;

@Repository
public class TrackDAO {

    private final JdbcTemplate jdbcTemplate;
    private final ArtistDAO artistDAO;
    private final AlbumDAO albumDAO;
    private final GenreDAO genreDAO;
    private final UserDAO userDAO;

    @Autowired
    public TrackDAO(JdbcTemplate jdbcTemplate, ArtistDAO artistDAO,
                    AlbumDAO albumDAO, GenreDAO genreDAO, UserDAO userDAO) {
        this.jdbcTemplate = jdbcTemplate;
        this.artistDAO = artistDAO;
        this.albumDAO = albumDAO;
        this.genreDAO = genreDAO;
        this.userDAO = userDAO;
    }

    private final RowMapper<Track> trackRowMapper = (rs, rowNum) -> {
        Track track = new Track();
        track.setId(rs.getInt("Id"));
        track.setTitle(rs.getString("Title"));
        track.setFilePath(rs.getString("FilePath"));
        track.setDuration(rs.getInt("Duration"));
        track.setGenreId(rs.getInt("GenreId"));
        track.setAlbumId(rs.getInt("AlbumId"));
        if (rs.wasNull()) track.setAlbumId(null);
        track.setArtistId(rs.getInt("ArtistId"));
        if (rs.wasNull()) track.setArtistId(null);
        track.setModerated(rs.getBoolean("IsModerated"));
        track.setUploadedByUserId(rs.getInt("UploadedByUserId"));
        if (rs.wasNull()) track.setUploadedByUserId(null);
        return track;
    };

    public Optional<Track> findById(Integer id) {
        String sql = "SELECT * FROM Tracks WHERE Id = ?";
        try {
            Track track = jdbcTemplate.queryForObject(sql, trackRowMapper, id);
            if (track != null) {
                loadRelatedData(track);
            }
            return Optional.ofNullable(track);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    public List<Track> findAll() {
        String sql = "SELECT * FROM Tracks ORDER BY Id DESC";
        List<Track> tracks = jdbcTemplate.query(sql, trackRowMapper);
        tracks.forEach(this::loadRelatedData);
        return tracks;
    }

    public List<Track> findModerated() {
        String sql = "SELECT * FROM Tracks WHERE IsModerated = TRUE ORDER BY Id DESC";
        List<Track> tracks = jdbcTemplate.query(sql, trackRowMapper);
        tracks.forEach(this::loadRelatedData);
        return tracks;
    }

    public List<Track> findPendingModeration() {
        String sql = "SELECT t.* FROM Tracks t LEFT JOIN Moderations m ON t.Id = m.TrackId " +
                "WHERE t.IsModerated = FALSE AND (m.Status != 'Rejected' OR m.Id IS NULL) " +
                "ORDER BY t.Id DESC";
        List<Track> tracks = jdbcTemplate.query(sql, trackRowMapper);
        tracks.forEach(this::loadRelatedData);
        return tracks;
    }

    public List<Track> findByArtistId(Integer artistId) {
        String sql = "SELECT * FROM Tracks WHERE ArtistId = ? AND IsModerated = TRUE ORDER BY Id DESC";
        List<Track> tracks = jdbcTemplate.query(sql, trackRowMapper, artistId);
        tracks.forEach(this::loadRelatedData);
        return tracks;
    }

    public List<Track> findByGenreId(Integer genreId) {
        String sql = "SELECT * FROM Tracks WHERE GenreId = ? AND IsModerated = TRUE ORDER BY Id DESC";
        List<Track> tracks = jdbcTemplate.query(sql, trackRowMapper, genreId);
        tracks.forEach(this::loadRelatedData);
        return tracks;
    }

    public List<Track> findByUploaderId(Integer userId) {
        String sql = "SELECT * FROM Tracks WHERE UploadedByUserId = ? ORDER BY Id DESC";
        List<Track> tracks = jdbcTemplate.query(sql, trackRowMapper, userId);
        tracks.forEach(this::loadRelatedData);
        return tracks;
    }

    public List<Track> search(String query) {
        String sql = "SELECT * FROM Tracks t " +
                "LEFT JOIN Artists a ON t.ArtistId = a.Id " +
                "WHERE t.IsModerated = TRUE AND (t.Title LIKE ? OR a.Name LIKE ?) " +
                "ORDER BY t.Id DESC";
        String pattern = "%" + query + "%";
        List<Track> tracks = jdbcTemplate.query(sql, trackRowMapper, pattern, pattern);
        tracks.forEach(this::loadRelatedData);
        return tracks;
    }

    public List<Track> findSimilar(Integer genreId, Integer excludeTrackId, int limit) {
        String sql = "SELECT * FROM Tracks WHERE GenreId = ? AND Id != ? AND IsModerated = TRUE " +
                "ORDER BY Id DESC LIMIT ?";
        List<Track> tracks = jdbcTemplate.query(sql, trackRowMapper, genreId, excludeTrackId, limit);
        tracks.forEach(this::loadRelatedData);
        return tracks;
    }

    public Track save(Track track) {
        if (track.getId() == null) {
            return insert(track);
        } else {
            return update(track);
        }
    }

    private Track insert(Track track) {
        String sql = "INSERT INTO Tracks (Title, FilePath, Duration, GenreId, AlbumId, ArtistId, IsModerated, UploadedByUserId) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, track.getTitle());
            ps.setString(2, track.getFilePath());
            ps.setInt(3, track.getDuration());
            ps.setInt(4, track.getGenreId());
            if (track.getAlbumId() != null) {
                ps.setInt(5, track.getAlbumId());
            } else {
                ps.setNull(5, java.sql.Types.INTEGER);
            }
            if (track.getArtistId() != null) {
                ps.setInt(6, track.getArtistId());
            } else {
                ps.setNull(6, java.sql.Types.INTEGER);
            }
            ps.setBoolean(7, track.isModerated());
            if (track.getUploadedByUserId() != null) {
                ps.setInt(8, track.getUploadedByUserId());
            } else {
                ps.setNull(8, java.sql.Types.INTEGER);
            }
            return ps;
        }, keyHolder);

        track.setId(keyHolder.getKey().intValue());
        return track;
    }

    private Track update(Track track) {
        String sql = "UPDATE Tracks SET Title = ?, FilePath = ?, Duration = ?, GenreId = ?, " +
                "AlbumId = ?, ArtistId = ?, IsModerated = ?, UploadedByUserId = ? WHERE Id = ?";
        jdbcTemplate.update(sql,
                track.getTitle(),
                track.getFilePath(),
                track.getDuration(),
                track.getGenreId(),
                track.getAlbumId(),
                track.getArtistId(),
                track.isModerated(),
                track.getUploadedByUserId(),
                track.getId()
        );
        return track;
    }

    public void updateModerationStatus(Integer trackId, boolean moderated) {
        String sql = "UPDATE Tracks SET IsModerated = ? WHERE Id = ?";
        jdbcTemplate.update(sql, moderated, trackId);
    }

    public void delete(Integer id) {
        String sql = "DELETE FROM Tracks WHERE Id = ?";
        jdbcTemplate.update(sql, id);
    }

    private void loadRelatedData(Track track) {
        if (track.getGenreId() != null) {
            genreDAO.findById(track.getGenreId()).ifPresent(track::setGenre);
        }
        if (track.getArtistId() != null) {
            artistDAO.findById(track.getArtistId()).ifPresent(track::setArtist);
        }
        if (track.getAlbumId() != null) {
            albumDAO.findById(track.getAlbumId()).ifPresent(track::setAlbum);
        }
        if (track.getUploadedByUserId() != null) {
            userDAO.findById(track.getUploadedByUserId()).ifPresent(track::setUploadedByUser);
        }
    }

    public List<Track> findByAlbumId(Integer albumId) {
        String sql = "SELECT * FROM Tracks WHERE AlbumId = ? AND IsModerated = TRUE ORDER BY Id";
        List<Track> tracks = jdbcTemplate.query(sql, trackRowMapper, albumId);
        tracks.forEach(this::loadRelatedData);
        return tracks;
    }
}
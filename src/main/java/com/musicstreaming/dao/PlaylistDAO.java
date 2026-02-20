package com.musicstreaming.dao;

import com.musicstreaming.model.Playlist;
import com.musicstreaming.model.PlaylistTrack;
import com.musicstreaming.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.List;
import java.util.Optional;

@Repository
public class PlaylistDAO {

    private final JdbcTemplate jdbcTemplate;
    private final UserDAO userDAO;
    private final PlaylistTrackDAO playlistTrackDAO;

    @Autowired
    public PlaylistDAO(JdbcTemplate jdbcTemplate, UserDAO userDAO, PlaylistTrackDAO playlistTrackDAO) {
        this.jdbcTemplate = jdbcTemplate;
        this.userDAO = userDAO;
        this.playlistTrackDAO = playlistTrackDAO;
    }

    private final RowMapper<Playlist> playlistRowMapper = (rs, rowNum) -> {
        Playlist playlist = new Playlist();
        playlist.setId(rs.getInt("Id"));
        playlist.setTitle(rs.getString("Title"));
        playlist.setDescription(rs.getString("Description"));
        playlist.setUserId(rs.getInt("UserId"));
        playlist.setVisibility(Playlist.PlaylistVisibility.valueOf(rs.getString("Visibility")));
        playlist.setCreatedDate(rs.getTimestamp("CreatedDate").toLocalDateTime());
        Timestamp updatedDate = rs.getTimestamp("UpdatedDate");
        if (updatedDate != null) {
            playlist.setUpdatedDate(updatedDate.toLocalDateTime());
        }
        playlist.setCoverImagePath(rs.getString("CoverImagePath"));
        return playlist;
    };

    public Optional<Playlist> findById(Integer id) {
        String sql = "SELECT * FROM Playlists WHERE Id = ?";
        try {
            Playlist playlist = jdbcTemplate.queryForObject(sql, playlistRowMapper, id);
            if (playlist != null) {
                loadRelatedData(playlist);
            }
            return Optional.ofNullable(playlist);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    public List<Playlist> findByUserId(Integer userId) {
        String sql = "SELECT * FROM Playlists WHERE UserId = ? ORDER BY CreatedDate DESC";
        List<Playlist> playlists = jdbcTemplate.query(sql, playlistRowMapper, userId);
        playlists.forEach(this::loadTracks);
        return playlists;
    }

    public List<Playlist> findPublicPlaylists() {
        String sql = "SELECT * FROM Playlists WHERE Visibility = 'Public' ORDER BY CreatedDate DESC";
        List<Playlist> playlists = jdbcTemplate.query(sql, playlistRowMapper);
        playlists.forEach(p -> {
            userDAO.findById(p.getUserId()).ifPresent(p::setUser);
            loadTracks(p);
        });
        return playlists;
    }

    public Playlist save(Playlist playlist) {
        if (playlist.getId() == null) {
            return insert(playlist);
        } else {
            return update(playlist);
        }
    }

    private Playlist insert(Playlist playlist) {
        String sql = "INSERT INTO Playlists (Title, Description, UserId, Visibility, CreatedDate, CoverImagePath) " +
                "VALUES (?, ?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, playlist.getTitle());
            ps.setString(2, playlist.getDescription());
            ps.setInt(3, playlist.getUserId());
            ps.setString(4, playlist.getVisibility().name());
            ps.setTimestamp(5, Timestamp.valueOf(playlist.getCreatedDate()));
            ps.setString(6, playlist.getCoverImagePath());
            return ps;
        }, keyHolder);

        playlist.setId(keyHolder.getKey().intValue());
        return playlist;
    }

    private Playlist update(Playlist playlist) {
        String sql = "UPDATE Playlists SET Title = ?, Description = ?, Visibility = ?, " +
                "UpdatedDate = ?, CoverImagePath = ? WHERE Id = ?";
        playlist.setUpdatedDate(java.time.LocalDateTime.now());

        jdbcTemplate.update(sql,
                playlist.getTitle(),
                playlist.getDescription(),
                playlist.getVisibility().name(),
                Timestamp.valueOf(playlist.getUpdatedDate()),
                playlist.getCoverImagePath(),
                playlist.getId()
        );
        return playlist;
    }

    public void delete(Integer id) {
        String sql = "DELETE FROM Playlists WHERE Id = ?";
        jdbcTemplate.update(sql, id);
    }

    private void loadRelatedData(Playlist playlist) {
        userDAO.findById(playlist.getUserId()).ifPresent(playlist::setUser);
        loadTracks(playlist);
    }

    private void loadTracks(Playlist playlist) {
        List<PlaylistTrack> tracks = playlistTrackDAO.findByPlaylistId(playlist.getId());
        playlist.setPlaylistTracks(tracks);
    }
}
package com.musicstreaming.dao;

import com.musicstreaming.model.PlaylistTrack;
import com.musicstreaming.model.Track;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

@Repository
public class PlaylistTrackDAO {

    private final JdbcTemplate jdbcTemplate;
    private final TrackDAO trackDAO;

    @Autowired
    public PlaylistTrackDAO(JdbcTemplate jdbcTemplate, TrackDAO trackDAO) {
        this.jdbcTemplate = jdbcTemplate;
        this.trackDAO = trackDAO;
    }

    private final RowMapper<PlaylistTrack> playlistTrackRowMapper = (rs, rowNum) -> {
        PlaylistTrack playlistTrack = new PlaylistTrack();
        playlistTrack.setPlaylistId(rs.getInt("PlaylistId"));
        playlistTrack.setTrackId(rs.getInt("TrackId"));
        playlistTrack.setPosition(rs.getInt("Position"));
        playlistTrack.setAddedDate(rs.getTimestamp("AddedDate").toLocalDateTime());
        return playlistTrack;
    };

    public Optional<PlaylistTrack> findById(Integer playlistId, Integer trackId) {
        String sql = "SELECT * FROM PlaylistTracks WHERE PlaylistId = ? AND TrackId = ?";
        try {
            PlaylistTrack playlistTrack = jdbcTemplate.queryForObject(sql, playlistTrackRowMapper, playlistId, trackId);
            if (playlistTrack != null) {
                loadTrack(playlistTrack);
            }
            return Optional.ofNullable(playlistTrack);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    public List<PlaylistTrack> findByPlaylistId(Integer playlistId) {
        String sql = "SELECT * FROM PlaylistTracks WHERE PlaylistId = ? ORDER BY Position ASC, AddedDate ASC";
        List<PlaylistTrack> tracks = jdbcTemplate.query(sql, playlistTrackRowMapper, playlistId);
        tracks.forEach(this::loadTrack);
        return tracks;
    }

    public void save(PlaylistTrack playlistTrack) {
        String sql = "INSERT INTO PlaylistTracks (PlaylistId, TrackId, Position, AddedDate) " +
                "VALUES (?, ?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE Position = ?, AddedDate = ?";

        jdbcTemplate.update(sql,
                playlistTrack.getPlaylistId(),
                playlistTrack.getTrackId(),
                playlistTrack.getPosition(),
                Timestamp.valueOf(playlistTrack.getAddedDate()),
                playlistTrack.getPosition(),
                Timestamp.valueOf(playlistTrack.getAddedDate())
        );
    }

    public void delete(Integer playlistId, Integer trackId) {
        String sql = "DELETE FROM PlaylistTracks WHERE PlaylistId = ? AND TrackId = ?";
        jdbcTemplate.update(sql, playlistId, trackId);
    }

    public void deleteByPlaylistId(Integer playlistId) {
        String sql = "DELETE FROM PlaylistTracks WHERE PlaylistId = ?";
        jdbcTemplate.update(sql, playlistId);
    }

    public void updatePositions(Integer playlistId, List<Integer> trackIds) {
        String deleteSql = "DELETE FROM PlaylistTracks WHERE PlaylistId = ?";
        jdbcTemplate.update(deleteSql, playlistId);

        String insertSql = "INSERT INTO PlaylistTracks (PlaylistId, TrackId, Position, AddedDate) VALUES (?, ?, ?, ?)";

        for (int i = 0; i < trackIds.size(); i++) {
            jdbcTemplate.update(insertSql,
                    playlistId,
                    trackIds.get(i),
                    i,
                    Timestamp.valueOf(java.time.LocalDateTime.now())
            );
        }
    }

    private void loadTrack(PlaylistTrack playlistTrack) {
        trackDAO.findById(playlistTrack.getTrackId()).ifPresent(playlistTrack::setTrack);
    }
}
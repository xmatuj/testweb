package com.musicstreaming.dao;

import com.musicstreaming.model.Moderation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

@Repository
public class ModerationDAO {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public ModerationDAO(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<Moderation> moderationRowMapper = (rs, rowNum) -> {
        Moderation moderation = new Moderation();
        moderation.setId(rs.getInt("Id"));
        moderation.setTrackId(rs.getInt("TrackId"));
        moderation.setModeratorId(rs.getInt("ModeratorId"));
        moderation.setStatus(Moderation.ModerationStatus.valueOf(rs.getString("Status")));
        moderation.setComment(rs.getString("Comment"));
        moderation.setModerationDate(rs.getTimestamp("ModerationDate").toLocalDateTime());
        return moderation;
    };

    public Optional<Moderation> findById(Integer id) {
        String sql = "SELECT * FROM Moderations WHERE Id = ?";
        try {
            Moderation moderation = jdbcTemplate.queryForObject(sql, moderationRowMapper, id);
            return Optional.ofNullable(moderation);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    public Optional<Moderation> findByTrackId(Integer trackId) {
        String sql = "SELECT * FROM Moderations WHERE TrackId = ? ORDER BY ModerationDate DESC LIMIT 1";
        try {
            Moderation moderation = jdbcTemplate.queryForObject(sql, moderationRowMapper, trackId);
            return Optional.ofNullable(moderation);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    public List<Moderation> findAll() {
        String sql = "SELECT * FROM Moderations ORDER BY ModerationDate DESC";
        return jdbcTemplate.query(sql, moderationRowMapper);
    }

    public List<Moderation> findByModeratorId(Integer moderatorId) {
        String sql = "SELECT * FROM Moderations WHERE ModeratorId = ? ORDER BY ModerationDate DESC";
        return jdbcTemplate.query(sql, moderationRowMapper, moderatorId);
    }

    public List<Moderation> findByStatus(Moderation.ModerationStatus status) {
        String sql = "SELECT * FROM Moderations WHERE Status = ? ORDER BY ModerationDate DESC";
        return jdbcTemplate.query(sql, moderationRowMapper, status.name());
    }

    public int findPendingCount() {
        String sql = "SELECT COUNT(*) FROM Moderations WHERE Status = 'Pending'";
        return jdbcTemplate.queryForObject(sql, Integer.class);
    }

    public Moderation save(Moderation moderation) {
        if (moderation.getId() == null) {
            return insert(moderation);
        } else {
            return update(moderation);
        }
    }

    private Moderation insert(Moderation moderation) {
        String sql = "INSERT INTO Moderations (TrackId, ModeratorId, Status, Comment, ModerationDate) " +
                "VALUES (?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, moderation.getTrackId());
            ps.setInt(2, moderation.getModeratorId());
            ps.setString(3, moderation.getStatus().name());
            ps.setString(4, moderation.getComment());
            ps.setTimestamp(5, Timestamp.valueOf(moderation.getModerationDate()));
            return ps;
        }, keyHolder);

        moderation.setId(keyHolder.getKey().intValue());
        return moderation;
    }

    private Moderation update(Moderation moderation) {
        String sql = "UPDATE Moderations SET Status = ?, Comment = ?, ModerationDate = ? " +
                "WHERE TrackId = ? AND Id = ?";
        jdbcTemplate.update(sql,
                moderation.getStatus().name(),
                moderation.getComment(),
                Timestamp.valueOf(moderation.getModerationDate()),
                moderation.getTrackId(),
                moderation.getId()
        );
        return moderation;
    }

    public void delete(Integer id) {
        String sql = "DELETE FROM Moderations WHERE Id = ?";
        jdbcTemplate.update(sql, id);
    }
}
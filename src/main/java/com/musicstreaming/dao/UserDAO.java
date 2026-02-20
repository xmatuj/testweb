package com.musicstreaming.dao;

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
import java.util.List;
import java.util.Optional;

@Repository
public class UserDAO {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public UserDAO(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<User> userRowMapper = (rs, rowNum) -> {
        User user = new User();
        user.setId(rs.getInt("Id"));
        user.setUsername(rs.getString("Username"));
        user.setEmail(rs.getString("Email"));
        user.setPasswordHash(rs.getString("PasswordHash"));
        user.setRole(User.UserRole.valueOf(rs.getString("Role")));
        user.setDateOfCreated(rs.getTimestamp("DateOfCreated").toLocalDateTime());
        return user;
    };

    public Optional<User> findById(Integer id) {
        String sql = "SELECT * FROM Users WHERE Id = ?";
        try {
            User user = jdbcTemplate.queryForObject(sql, userRowMapper, id);
            return Optional.ofNullable(user);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    public Optional<User> findByUsername(String username) {
        String sql = "SELECT * FROM Users WHERE Username = ?";
        try {
            User user = jdbcTemplate.queryForObject(sql, userRowMapper, username);
            return Optional.ofNullable(user);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    public Optional<User> findByEmail(String email) {
        String sql = "SELECT * FROM Users WHERE Email = ?";
        try {
            User user = jdbcTemplate.queryForObject(sql, userRowMapper, email);
            return Optional.ofNullable(user);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    public Optional<User> findByUsernameOrEmail(String usernameOrEmail) {
        String sql = "SELECT * FROM Users WHERE Username = ? OR Email = ?";
        try {
            User user = jdbcTemplate.queryForObject(sql, userRowMapper, usernameOrEmail, usernameOrEmail);
            return Optional.ofNullable(user);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    public List<User> findAll() {
        String sql = "SELECT * FROM Users ORDER BY Role, Username";
        return jdbcTemplate.query(sql, userRowMapper);
    }

    public List<User> search(String searchTerm) {
        String sql = "SELECT * FROM Users WHERE Username LIKE ? OR Email LIKE ? OR Role LIKE ?";
        String pattern = "%" + searchTerm + "%";
        return jdbcTemplate.query(sql, userRowMapper, pattern, pattern, pattern);
    }

    public User save(User user) {
        if (user.getId() == null) {
            return insert(user);
        } else {
            return update(user);
        }
    }

    private User insert(User user) {
        String sql = "INSERT INTO Users (Username, Email, PasswordHash, Role, DateOfCreated) VALUES (?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, user.getUsername());
            ps.setString(2, user.getEmail());
            ps.setString(3, user.getPasswordHash());
            ps.setString(4, user.getRole().name());
            ps.setTimestamp(5, Timestamp.valueOf(user.getDateOfCreated()));
            return ps;
        }, keyHolder);

        user.setId(keyHolder.getKey().intValue());
        return user;
    }

    private User update(User user) {
        String sql = "UPDATE Users SET Username = ?, Email = ?, PasswordHash = ?, Role = ? WHERE Id = ?";
        jdbcTemplate.update(sql,
                user.getUsername(),
                user.getEmail(),
                user.getPasswordHash(),
                user.getRole().name(),
                user.getId()
        );
        return user;
    }

    public void updateRole(Integer userId, User.UserRole newRole) {
        String sql = "UPDATE Users SET Role = ? WHERE Id = ?";
        jdbcTemplate.update(sql, newRole.name(), userId);
    }

    public boolean existsByUsername(String username) {
        String sql = "SELECT COUNT(*) FROM Users WHERE Username = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, username);
        return count != null && count > 0;
    }

    public boolean existsByEmail(String email) {
        String sql = "SELECT COUNT(*) FROM Users WHERE Email = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, email);
        return count != null && count > 0;
    }
}
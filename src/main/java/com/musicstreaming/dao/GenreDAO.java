package com.musicstreaming.dao;

import com.musicstreaming.model.Genre;
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
public class GenreDAO {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public GenreDAO(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<Genre> genreRowMapper = (rs, rowNum) -> {
        Genre genre = new Genre();
        genre.setId(rs.getInt("Id"));
        genre.setName(rs.getString("Name"));
        return genre;
    };

    public Optional<Genre> findById(Integer id) {
        String sql = "SELECT * FROM Genres WHERE Id = ?";
        try {
            Genre genre = jdbcTemplate.queryForObject(sql, genreRowMapper, id);
            return Optional.ofNullable(genre);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    public Optional<Genre> findByName(String name) {
        String sql = "SELECT * FROM Genres WHERE Name = ?";
        try {
            Genre genre = jdbcTemplate.queryForObject(sql, genreRowMapper, name);
            return Optional.ofNullable(genre);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    public List<Genre> findAll() {
        String sql = "SELECT * FROM Genres ORDER BY Name";
        return jdbcTemplate.query(sql, genreRowMapper);
    }

    public Genre save(Genre genre) {
        if (genre.getId() == null) {
            return insert(genre);
        } else {
            return update(genre);
        }
    }

    private Genre insert(Genre genre) {
        String sql = "INSERT INTO Genres (Name) VALUES (?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, genre.getName());
            return ps;
        }, keyHolder);

        genre.setId(keyHolder.getKey().intValue());
        return genre;
    }

    private Genre update(Genre genre) {
        String sql = "UPDATE Genres SET Name = ? WHERE Id = ?";
        jdbcTemplate.update(sql, genre.getName(), genre.getId());
        return genre;
    }

    public void delete(Integer id) {
        String sql = "DELETE FROM Genres WHERE Id = ?";
        jdbcTemplate.update(sql, id);
    }

    public boolean existsByName(String name) {
        String sql = "SELECT COUNT(*) FROM Genres WHERE Name = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, name);
        return count != null && count > 0;
    }
}
package com.musicstreaming.dao;

import com.musicstreaming.model.Artist;
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
public class ArtistDAO {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public ArtistDAO(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<Artist> artistRowMapper = (rs, rowNum) -> {
        Artist artist = new Artist();
        artist.setId(rs.getInt("Id"));
        artist.setName(rs.getString("Name"));
        artist.setDescription(rs.getString("Description"));
        artist.setPhotoPath(rs.getString("PhotoPath"));
        return artist;
    };

    public Optional<Artist> findById(Integer id) {
        String sql = "SELECT * FROM Artists WHERE Id = ?";
        try {
            Artist artist = jdbcTemplate.queryForObject(sql, artistRowMapper, id);
            return Optional.ofNullable(artist);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    public Optional<Artist> findByName(String name) {
        String sql = "SELECT * FROM Artists WHERE Name = ?";
        try {
            Artist artist = jdbcTemplate.queryForObject(sql, artistRowMapper, name);
            return Optional.ofNullable(artist);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    public List<Artist> findAll() {
        String sql = "SELECT * FROM Artists ORDER BY Name";
        return jdbcTemplate.query(sql, artistRowMapper);
    }

    public List<Artist> search(String query) {
        String sql = "SELECT * FROM Artists WHERE Name LIKE ? OR Description LIKE ? ORDER BY Name";
        String pattern = "%" + query + "%";
        return jdbcTemplate.query(sql, artistRowMapper, pattern, pattern);
    }

    public Artist save(Artist artist) {
        if (artist.getId() == null) {
            return insert(artist);
        } else {
            return update(artist);
        }
    }

    private Artist insert(Artist artist) {
        String sql = "INSERT INTO Artists (Name, Description, PhotoPath) VALUES (?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, artist.getName());
            ps.setString(2, artist.getDescription());
            ps.setString(3, artist.getPhotoPath());
            return ps;
        }, keyHolder);

        artist.setId(keyHolder.getKey().intValue());
        return artist;
    }

    private Artist update(Artist artist) {
        String sql = "UPDATE Artists SET Name = ?, Description = ?, PhotoPath = ? WHERE Id = ?";
        jdbcTemplate.update(sql,
                artist.getName(),
                artist.getDescription(),
                artist.getPhotoPath(),
                artist.getId()
        );
        return artist;
    }

    public void delete(Integer id) {
        String sql = "DELETE FROM Artists WHERE Id = ?";
        jdbcTemplate.update(sql, id);
    }
}
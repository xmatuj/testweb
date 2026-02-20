package com.musicstreaming.dao;

import com.musicstreaming.model.Album;
import com.musicstreaming.model.Artist;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Types;
import java.util.List;
import java.util.Optional;

@Repository
public class AlbumDAO {

    private final JdbcTemplate jdbcTemplate;
    private final ArtistDAO artistDAO;

    @Autowired
    public AlbumDAO(JdbcTemplate jdbcTemplate, ArtistDAO artistDAO) {
        this.jdbcTemplate = jdbcTemplate;
        this.artistDAO = artistDAO;
    }

    private final RowMapper<Album> albumRowMapper = (rs, rowNum) -> {
        Album album = new Album();
        album.setId(rs.getInt("Id"));
        album.setTitle(rs.getString("Title"));
        album.setArtistId(rs.getInt("ArtistId"));
        if (rs.getDate("ReleaseDate") != null) {
            album.setReleaseDate(rs.getDate("ReleaseDate").toLocalDate());
        }
        album.setCoverPath(rs.getString("CoverPath"));
        return album;
    };

    public Optional<Album> findById(Integer id) {
        String sql = "SELECT * FROM Albums WHERE Id = ?";
        try {
            Album album = jdbcTemplate.queryForObject(sql, albumRowMapper, id);
            if (album != null) {
                loadArtist(album);
            }
            return Optional.ofNullable(album);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    public List<Album> findAll() {
        String sql = "SELECT * FROM Albums ORDER BY ReleaseDate DESC, Title";
        List<Album> albums = jdbcTemplate.query(sql, albumRowMapper);
        albums.forEach(this::loadArtist);
        return albums;
    }

    public List<Album> findByArtistId(Integer artistId) {
        String sql = "SELECT * FROM Albums WHERE ArtistId = ? ORDER BY ReleaseDate DESC";
        List<Album> albums = jdbcTemplate.query(sql, albumRowMapper, artistId);
        albums.forEach(this::loadArtist);
        return albums;
    }

    public Album save(Album album) {
        if (album.getId() == null) {
            return insert(album);
        } else {
            return update(album);
        }
    }

    private Album insert(Album album) {
        String sql = "INSERT INTO Albums (Title, ArtistId, ReleaseDate, CoverPath) VALUES (?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, album.getTitle());
            ps.setInt(2, album.getArtistId());
            if (album.getReleaseDate() != null) {
                ps.setDate(3, java.sql.Date.valueOf(album.getReleaseDate()));
            } else {
                ps.setNull(3, Types.DATE);
            }
            ps.setString(4, album.getCoverPath());
            return ps;
        }, keyHolder);

        album.setId(keyHolder.getKey().intValue());
        return album;
    }

    private Album update(Album album) {
        String sql = "UPDATE Albums SET Title = ?, ArtistId = ?, ReleaseDate = ?, CoverPath = ? WHERE Id = ?";
        jdbcTemplate.update(sql,
                album.getTitle(),
                album.getArtistId(),
                album.getReleaseDate() != null ? java.sql.Date.valueOf(album.getReleaseDate()) : null,
                album.getCoverPath(),
                album.getId()
        );
        return album;
    }

    public void delete(Integer id) {
        String sql = "DELETE FROM Albums WHERE Id = ?";
        jdbcTemplate.update(sql, id);
    }

    private void loadArtist(Album album) {
        if (album.getArtistId() != null) {
            artistDAO.findById(album.getArtistId()).ifPresent(album::setArtist);
        }
    }

    public List<Album> search(String query) {
        String sql = "SELECT a.* FROM Albums a " +
                "LEFT JOIN Artists ar ON a.ArtistId = ar.Id " +
                "WHERE a.Title LIKE ? OR ar.Name LIKE ? " +
                "ORDER BY a.ReleaseDate DESC";
        String pattern = "%" + query + "%";
        List<Album> albums = jdbcTemplate.query(sql, albumRowMapper, pattern, pattern);
        albums.forEach(this::loadArtist);
        return albums;
    }
}
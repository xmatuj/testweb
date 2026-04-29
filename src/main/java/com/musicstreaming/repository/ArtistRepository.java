package com.musicstreaming.repository;

import com.musicstreaming.dto.ArtistDTO;
import com.musicstreaming.model.Artist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ArtistRepository extends JpaRepository<Artist, Integer> {

    Optional<Artist> findByName(String name);

    @Query("SELECT a FROM Artist a WHERE LOWER(a.name) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(a.description) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<Artist> search(@Param("query") String query);

    @Query("SELECT a FROM Artist a ORDER BY a.name")
    List<Artist> findAllOrdered();

    // Метод для получения DTO с подсчетом альбомов и треков
    @Query("SELECT new com.musicstreaming.dto.ArtistDTO(" +
            "a.id, a.name, a.description, " +
            "SIZE(a.albums), SIZE(a.tracks), a.photoPath) " +
            "FROM Artist a " +
            "ORDER BY a.name")
    List<ArtistDTO> findAllArtistDTOs();

    // Метод для поиска с DTO
    @Query("SELECT new com.musicstreaming.dto.ArtistDTO(" +
            "a.id, a.name, a.description, " +
            "SIZE(a.albums), SIZE(a.tracks), a.photoPath) " +
            "FROM Artist a " +
            "WHERE LOWER(a.name) LIKE LOWER(CONCAT('%', :query, '%')) " +
            "OR LOWER(a.description) LIKE LOWER(CONCAT('%', :query, '%')) " +
            "ORDER BY a.name")
    List<ArtistDTO> searchArtistDTOs(@Param("query") String query);
}
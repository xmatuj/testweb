package com.musicstreaming.repository;

import com.musicstreaming.model.Album;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AlbumRepository extends JpaRepository<Album, Integer> {

    @EntityGraph(attributePaths = {"artist"})
    List<Album> findByArtistIdOrderByReleaseDateDesc(Integer artistId);

    @EntityGraph(attributePaths = {"artist"})
    @Query("SELECT a FROM Album a ORDER BY a.releaseDate DESC NULLS LAST, a.title")
    List<Album> findAllOrdered();

    @EntityGraph(attributePaths = {"artist"})
    @Query("SELECT a FROM Album a WHERE LOWER(a.title) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(a.artist.name) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<Album> search(@Param("query") String query);

    @EntityGraph(attributePaths = {"artist"})
    @Query("SELECT a FROM Album a ORDER BY a.releaseDate DESC NULLS LAST")
    List<Album> findNewReleases(Pageable pageable);

    @EntityGraph(attributePaths = {"artist"})
    @Override
    Optional<Album> findById(Integer id);
}
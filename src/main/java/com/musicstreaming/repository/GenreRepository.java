package com.musicstreaming.repository;

import com.musicstreaming.model.Genre;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GenreRepository extends JpaRepository<Genre, Integer> {

    Optional<Genre> findByName(String name);

    boolean existsByName(String name);

    @Query("SELECT g FROM Genre g ORDER BY g.name")
    List<Genre> findAllOrdered();

    // Метод для загрузки жанров с количеством треков без инициализации всей коллекции
    @Query("SELECT g.id as id, g.name as name, COUNT(t.id) as trackCount " +
            "FROM Genre g LEFT JOIN g.tracks t " +
            "GROUP BY g.id, g.name " +
            "ORDER BY g.name")
    List<Object[]> findAllWithTrackCount();
}
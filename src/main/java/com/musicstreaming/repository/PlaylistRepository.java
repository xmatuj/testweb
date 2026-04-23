package com.musicstreaming.repository;

import com.musicstreaming.model.Playlist;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PlaylistRepository extends JpaRepository<Playlist, Integer> {

    List<Playlist> findByUserIdOrderByCreatedDateDesc(Integer userId);

    @Query("SELECT p FROM Playlist p WHERE p.visibility = 'Public' ORDER BY p.createdDate DESC")
    List<Playlist> findPublicPlaylists();

    @Query("SELECT p FROM Playlist p WHERE p.user.id = :userId AND p.visibility = :visibility")
    List<Playlist> findByUserIdAndVisibility(@Param("userId") Integer userId, @Param("visibility") Playlist.PlaylistVisibility visibility);

    @Query("SELECT COUNT(p) FROM Playlist p WHERE p.user.id = :userId")
    int countByUserId(@Param("userId") Integer userId);

    // Загружаем плейлист с пользователем и треками
    @EntityGraph(attributePaths = {"user", "playlistTracks", "playlistTracks.track", "playlistTracks.track.artist"})
    @Query("SELECT p FROM Playlist p WHERE p.id = :id")
    Optional<Playlist> findByIdWithUser(@Param("id") Integer id);
}
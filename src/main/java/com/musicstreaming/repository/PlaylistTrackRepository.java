package com.musicstreaming.repository;

import com.musicstreaming.model.PlaylistTrack;
import com.musicstreaming.model.PlaylistTrack.PlaylistTrackId;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PlaylistTrackRepository extends JpaRepository<PlaylistTrack, PlaylistTrackId> {

    @EntityGraph(attributePaths = {"track", "track.artist", "track.album", "track.genre"})
    @Query("SELECT pt FROM PlaylistTrack pt WHERE pt.playlist.id = :playlistId ORDER BY pt.position ASC, pt.addedDate ASC")
    List<PlaylistTrack> findByPlaylistIdOrdered(@Param("playlistId") Integer playlistId);

    @Modifying
    @Query("DELETE FROM PlaylistTrack pt WHERE pt.playlist.id = :playlistId")
    void deleteByPlaylistId(@Param("playlistId") Integer playlistId);

    @Modifying
    @Query("DELETE FROM PlaylistTrack pt WHERE pt.playlist.id = :playlistId AND pt.track.id = :trackId")
    void deleteByPlaylistIdAndTrackId(@Param("playlistId") Integer playlistId, @Param("trackId") Integer trackId);

    @Query("SELECT COUNT(pt) FROM PlaylistTrack pt WHERE pt.playlist.id = :playlistId")
    int countByPlaylistId(@Param("playlistId") Integer playlistId);
}
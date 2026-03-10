package com.musicstreaming.repository;

import com.musicstreaming.model.Track;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TrackRepository extends JpaRepository<Track, Integer> {

    @EntityGraph(attributePaths = {"artist", "album", "genre"})
    List<Track> findByIsModeratedTrueOrderByIdDesc();

    @EntityGraph(attributePaths = {"artist", "album", "genre", "uploadedByUser"})
    @Query("SELECT t FROM Track t WHERE t.isModerated = false")
    List<Track> findPendingModeration();

    @EntityGraph(attributePaths = {"artist", "album", "genre"})
    @Query("SELECT t FROM Track t WHERE t.artist.id = :artistId AND t.isModerated = true ORDER BY t.id DESC")
    List<Track> findByArtistId(@Param("artistId") Integer artistId);

    @EntityGraph(attributePaths = {"artist", "album", "genre"})
    @Query("SELECT t FROM Track t WHERE t.album.id = :albumId AND t.isModerated = true ORDER BY t.id")
    List<Track> findByAlbumId(@Param("albumId") Integer albumId);

    @EntityGraph(attributePaths = {"artist", "album", "genre"})
    @Query("SELECT t FROM Track t WHERE t.genre.id = :genreId AND t.isModerated = true ORDER BY t.id DESC")
    List<Track> findByGenreId(@Param("genreId") Integer genreId);

    // Добавленный метод для поиска по загрузившему пользователю
    @EntityGraph(attributePaths = {"artist", "album", "genre"})
    @Query("SELECT t FROM Track t WHERE t.uploadedByUser.id = :userId ORDER BY t.id DESC")
    List<Track> findByUploaderId(@Param("userId") Integer userId);

    @EntityGraph(attributePaths = {"artist", "album", "genre"})
    @Query("SELECT t FROM Track t WHERE t.isModerated = true AND (LOWER(t.title) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(t.artist.name) LIKE LOWER(CONCAT('%', :query, '%')))")
    List<Track> search(@Param("query") String query);

    // Добавленный метод для поиска похожих треков
    @EntityGraph(attributePaths = {"artist", "album", "genre"})
    @Query("SELECT t FROM Track t WHERE t.genre.id = :genreId AND t.id != :excludeTrackId AND t.isModerated = true ORDER BY t.id DESC")
    List<Track> findSimilar(@Param("genreId") Integer genreId, @Param("excludeTrackId") Integer excludeTrackId, Pageable pageable);

    // Добавленный метод для обновления статуса модерации
    @Modifying
    @Query("UPDATE Track t SET t.isModerated = :moderated WHERE t.id = :trackId")
    int updateModerationStatus(@Param("trackId") Integer trackId, @Param("moderated") boolean moderated);

    @EntityGraph(attributePaths = {"artist", "album", "genre", "uploadedByUser"})
    @Override
    Optional<Track> findById(Integer id);

    @EntityGraph(attributePaths = {"artist", "album", "genre", "uploadedByUser"})
    @Query("SELECT t FROM Track t WHERE t.id = :id")
    Optional<Track> findByIdWithUser(@Param("id") Integer id);

    // Для поиска всех треков с пользователем
    @EntityGraph(attributePaths = {"artist", "album", "genre", "uploadedByUser"})
    @Override
    List<Track> findAll();
}
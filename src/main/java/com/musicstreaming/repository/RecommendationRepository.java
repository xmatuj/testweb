package com.musicstreaming.repository;

import com.musicstreaming.model.Recommendation;
import com.musicstreaming.model.Track;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface RecommendationRepository extends JpaRepository<Recommendation, Integer> {

    @Query("SELECT COUNT(r) FROM Recommendation r WHERE r.user.id = :userId")
    long countByUserId(@Param("userId") Integer userId);

    @Query("SELECT r FROM Recommendation r WHERE r.user.id = :userId ORDER BY r.date DESC")
    List<Recommendation> findByUserId(@Param("userId") Integer userId);

    @Query("SELECT r FROM Recommendation r WHERE r.user.id = :userId AND r.date > :after ORDER BY r.date DESC")
    List<Recommendation> findByUserIdAndDateAfter(@Param("userId") Integer userId, @Param("after") LocalDateTime after);

    @Query("SELECT DISTINCT r.track FROM Recommendation r WHERE r.user.id = :userId")
    List<Track> findDistinctTracksByUserId(@Param("userId") Integer userId);

    @Query("SELECT r.track.genre, COUNT(r) FROM Recommendation r " +
            "WHERE r.user.id = :userId AND r.track.genre IS NOT NULL " +
            "GROUP BY r.track.genre ORDER BY COUNT(r) DESC")
    List<Object[]> findGenrePreferencesByUserId(@Param("userId") Integer userId);

    @Query("SELECT r.track.artist, COUNT(r) FROM Recommendation r " +
            "WHERE r.user.id = :userId AND r.track.artist IS NOT NULL " +
            "GROUP BY r.track.artist ORDER BY COUNT(r) DESC")
    List<Object[]> findArtistPreferencesByUserId(@Param("userId") Integer userId);

    @Query("SELECT t FROM Track t WHERE t.genre.id IN :genreIds AND t.isModerated = true " +
            "AND t.id NOT IN (SELECT r.track.id FROM Recommendation r WHERE r.user.id = :userId) " +
            "ORDER BY t.id DESC")
    List<Track> findTracksByGenres(@Param("genreIds") List<Integer> genreIds,
                                   @Param("userId") Integer userId,
                                   Pageable pageable);

    @Query("SELECT t FROM Track t WHERE t.artist.id IN :artistIds AND t.isModerated = true " +
            "AND t.id NOT IN (SELECT r.track.id FROM Recommendation r WHERE r.user.id = :userId) " +
            "ORDER BY t.id DESC")
    List<Track> findTracksByArtists(@Param("artistIds") List<Integer> artistIds,
                                    @Param("userId") Integer userId,
                                    Pageable pageable);

    @Query("SELECT t FROM Track t WHERE t.album.id IN " +
            "(SELECT DISTINCT r.track.album.id FROM Recommendation r WHERE r.user.id = :userId AND r.track.album IS NOT NULL) " +
            "AND t.isModerated = true " +
            "AND t.id NOT IN (SELECT r.track.id FROM Recommendation r WHERE r.user.id = :userId)")
    List<Track> findTracksFromSameAlbums(@Param("userId") Integer userId);

    @Query("SELECT t, COUNT(r) FROM Recommendation r " +
            "JOIN r.track t " +
            "WHERE r.date > :after AND t.isModerated = true " +
            "GROUP BY t ORDER BY COUNT(r) DESC")
    List<Object[]> findPopularTracksSince(@Param("after") LocalDateTime after, Pageable pageable);

    @Query(value = "SELECT t.* FROM tracks t " +
            "INNER JOIN recommendations r2 ON t.Id = r2.TrackId " +
            "WHERE r2.UserId IN (" +
            "   SELECT r.UserId FROM recommendations r " +
            "   WHERE r.TrackId IN (SELECT TrackId FROM recommendations WHERE UserId = :userId) " +
            "   AND r.UserId != :userId " +
            "   GROUP BY r.UserId ORDER BY COUNT(r.TrackId) DESC LIMIT 10" +
            ") " +
            "AND t.Id NOT IN (SELECT TrackId FROM recommendations WHERE UserId = :userId) " +
            "AND t.IsModerated = 1 " +
            "GROUP BY t.Id ORDER BY COUNT(r2.TrackId) DESC LIMIT :limit", nativeQuery = true)
    List<Track> findCollaborativeRecommendations(@Param("userId") Integer userId, @Param("limit") int limit);
}
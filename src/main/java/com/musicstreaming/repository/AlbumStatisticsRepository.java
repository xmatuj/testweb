package com.musicstreaming.repository;

import com.musicstreaming.model.AlbumStatistics;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface AlbumStatisticsRepository extends JpaRepository<AlbumStatistics, Integer> {

    @Query("SELECT COALESCE(SUM(ats.listenCount), 0) FROM AlbumStatistics ats WHERE ats.album.id = :albumId")
    Long getTotalListenCountByAlbumId(@Param("albumId") Integer albumId);

    @Query("SELECT ats FROM AlbumStatistics ats WHERE ats.album.id = :albumId ORDER BY ats.date DESC")
    List<AlbumStatistics> findByAlbumId(@Param("albumId") Integer albumId);

    @Query("SELECT ats FROM AlbumStatistics ats WHERE ats.album.id = :albumId AND FUNCTION('DATE', ats.date) = FUNCTION('DATE', :date)")
    Optional<AlbumStatistics> findByAlbumIdAndDate(@Param("albumId") Integer albumId, @Param("date") LocalDateTime date);

    @Query("SELECT ats.album.id as albumId, SUM(ats.listenCount) as total FROM AlbumStatistics ats GROUP BY ats.album.id ORDER BY total DESC")
    List<Object[]> findPopularAlbums(org.springframework.data.domain.Pageable pageable);
}
package com.musicstreaming.repository;

import com.musicstreaming.model.TrackStatistics;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TrackStatisticsRepository extends JpaRepository<TrackStatistics, Integer> {

    @Query("SELECT ts FROM TrackStatistics ts WHERE ts.track.id = :trackId AND DATE(ts.date) = DATE(:date)")
    List<TrackStatistics> findByTrackIdAndDate(@Param("trackId") Integer trackId, @Param("date") LocalDateTime date);

    @Query("SELECT ts.track.id, SUM(ts.listenCount) as total FROM TrackStatistics ts GROUP BY ts.track.id ORDER BY total DESC")
    List<Object[]> findPopularTracks(org.springframework.data.domain.Pageable pageable);
}
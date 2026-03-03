package com.musicstreaming.repository;

import com.musicstreaming.model.Moderation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ModerationRepository extends JpaRepository<Moderation, Integer> {

    @Query("SELECT m FROM Moderation m WHERE m.track.id = :trackId ORDER BY m.moderationDate DESC")
    Optional<Moderation> findByTrackId(@Param("trackId") Integer trackId);

    List<Moderation> findByModeratorIdOrderByModerationDateDesc(Integer moderatorId);

    @Query("SELECT m FROM Moderation m WHERE m.status = 'Pending' ORDER BY m.moderationDate DESC")
    List<Moderation> findPendingModerations();

    @Query("SELECT COUNT(m) FROM Moderation m WHERE m.status = 'Pending'")
    long countPending();
}
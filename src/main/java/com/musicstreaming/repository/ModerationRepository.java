package com.musicstreaming.repository;

import com.musicstreaming.model.Moderation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ModerationRepository extends JpaRepository<Moderation, Integer> {

    @Query("SELECT m FROM Moderation m WHERE m.track.id = :trackId ORDER BY m.moderationDate DESC")
    List<Moderation> findByTrackId(@Param("trackId") Integer trackId);

    // Добавляем метод для получения последней модерации по треку
    default Optional<Moderation> findLatestByTrackId(Integer trackId) {
        List<Moderation> moderations = findByTrackId(trackId);
        return moderations.isEmpty() ? Optional.empty() : Optional.of(moderations.get(0));
    }

    List<Moderation> findByModeratorIdOrderByModerationDateDesc(Integer moderatorId);

    @Query("SELECT m FROM Moderation m WHERE m.status = 'Pending' ORDER BY m.moderationDate DESC")
    List<Moderation> findPendingModerations();

    @Query("SELECT COUNT(m) FROM Moderation m WHERE m.status = 'Pending'")
    long countPending();

    // получить историю модерации с пагинацией
    @EntityGraph(attributePaths = {"track", "track.artist", "moderator"})
    @Query("SELECT m FROM Moderation m ORDER BY m.moderationDate DESC")
    Page<Moderation> findModerationHistory(Pageable pageable);

    // получить всю историю модерации
    @EntityGraph(attributePaths = {"track", "track.artist", "moderator"})
    @Query("SELECT m FROM Moderation m ORDER BY m.moderationDate DESC")
    List<Moderation> findAllModerationHistory();
}
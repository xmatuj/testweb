package com.musicstreaming.repository;

import com.musicstreaming.model.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, Integer> {

    List<Subscription> findByUserIdOrderByStartDateDesc(Integer userId);

    @Query("SELECT s FROM Subscription s WHERE s.user.id = :userId AND s.isActivated = true AND s.endDate > :now")
    Optional<Subscription> findActiveByUserId(@Param("userId") Integer userId, @Param("now") LocalDateTime now);

    @Query("SELECT s FROM Subscription s WHERE s.isActivated = true AND s.endDate > :now")
    List<Subscription> findActiveSubscriptions(@Param("now") LocalDateTime now);
}
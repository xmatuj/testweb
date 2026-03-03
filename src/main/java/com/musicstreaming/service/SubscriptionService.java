package com.musicstreaming.service;

import com.musicstreaming.model.Subscription;
import com.musicstreaming.model.User;
import com.musicstreaming.repository.SubscriptionRepository;
import com.musicstreaming.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class SubscriptionService {

    private static final Logger logger = LoggerFactory.getLogger(SubscriptionService.class);

    private final SubscriptionRepository subscriptionRepository;
    private final UserRepository userRepository;

    @Autowired
    public SubscriptionService(SubscriptionRepository subscriptionRepository,
                               UserRepository userRepository) {
        this.subscriptionRepository = subscriptionRepository;
        this.userRepository = userRepository;
    }

    public Optional<Subscription> findById(Integer id) {
        return subscriptionRepository.findById(id);
    }

    public List<Subscription> findByUserId(Integer userId) {
        return subscriptionRepository.findByUserIdOrderByStartDateDesc(userId);
    }

    public Optional<Subscription> findActiveByUserId(Integer userId) {
        return subscriptionRepository.findActiveByUserId(userId, LocalDateTime.now());
    }

    @Transactional
    public Subscription createSubscription(Integer userId, String plan, BigDecimal amount) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Subscription subscription = new Subscription(user);

        if ("yearly".equals(plan)) {
            subscription.setEndDate(LocalDateTime.now().plusYears(1));
        } else {
            subscription.setEndDate(LocalDateTime.now().plusMonths(1));
        }

        subscription.setAmount(amount);
        subscription.setStatus("pending");
        subscription.setActivated(false);

        Subscription saved = subscriptionRepository.save(subscription);
        logger.info("Created subscription for user {}: {}", userId, saved.getId());

        return saved;
    }

    @Transactional
    public void activateSubscription(Integer subscriptionId, String transactionId) {
        subscriptionRepository.findById(subscriptionId).ifPresent(subscription -> {
            subscription.setActivated(true);
            subscription.setStatus("active");
            subscription.setTransactionId(transactionId);
            subscriptionRepository.save(subscription);
            logger.info("Activated subscription: {}", subscriptionId);
        });
    }

    @Transactional
    public void cancelSubscription(Integer subscriptionId) {
        subscriptionRepository.findById(subscriptionId).ifPresent(subscription -> {
            subscription.setStatus("cancelled");
            subscriptionRepository.save(subscription);
            logger.info("Cancelled subscription: {}", subscriptionId);
        });
    }

    @Transactional
    public void checkExpiredSubscriptions() {
        List<Subscription> activeSubscriptions = subscriptionRepository.findActiveSubscriptions(LocalDateTime.now());
        LocalDateTime now = LocalDateTime.now();

        for (Subscription sub : activeSubscriptions) {
            if (sub.getEndDate().isBefore(now)) {
                sub.setStatus("expired");
                sub.setActivated(false);
                subscriptionRepository.save(sub);
                logger.info("Subscription {} expired", sub.getId());
            }
        }
    }
}
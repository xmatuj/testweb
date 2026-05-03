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

        // Устанавливаем длительность в зависимости от плана
        LocalDateTime now = LocalDateTime.now();

        switch (plan != null ? plan.toLowerCase() : "monthly") {
            case "yearly":
                subscription.setEndDate(now.plusYears(1));
                subscription.setAmount(amount != null ? amount : new BigDecimal("2990.00"));
                break;
            case "family":
                subscription.setEndDate(now.plusMonths(1));
                subscription.setAmount(amount != null ? amount : new BigDecimal("449.00"));
                break;
            case "premium":
            case "monthly":
            default:
                subscription.setEndDate(now.plusMonths(1));
                subscription.setAmount(amount != null ? amount : new BigDecimal("299.00"));
                break;
        }

        subscription.setStatus("active");
        subscription.setActivated(true);
        subscription.setTransactionId("TXN-" + System.currentTimeMillis());

        // Обновляем роль пользователя
        if (user.getRole() == User.UserRole.User) {
            user.setRole(User.UserRole.Subscriber);
            userRepository.save(user);
            logger.info("User {} role updated to Subscriber", user.getUsername());
        }

        Subscription saved = subscriptionRepository.save(subscription);
        logger.info("Created and activated subscription for user {}: plan={}, endDate={}, id={}",
                userId, plan, subscription.getEndDate(), saved.getId());

        return saved;
    }

    @Transactional
    public void activateSubscription(Integer subscriptionId, String transactionId) {
        subscriptionRepository.findById(subscriptionId).ifPresent(subscription -> {
            subscription.setActivated(true);
            subscription.setStatus("active");
            subscription.setTransactionId(transactionId);
            subscriptionRepository.save(subscription);

            // Обновляем роль пользователя при активации
            User user = subscription.getUser();
            if (user != null && user.getRole() == User.UserRole.User) {
                user.setRole(User.UserRole.Subscriber);
                userRepository.save(user);
                logger.info("User {} role updated to Subscriber during activation", user.getUsername());
            }

            logger.info("Activated subscription: {}", subscriptionId);
        });
    }

    @Transactional
    public void cancelSubscription(Integer subscriptionId) {
        subscriptionRepository.findById(subscriptionId).ifPresent(subscription -> {
            subscription.setStatus("cancelled");
            subscriptionRepository.save(subscription);

            // Проверяем, есть ли другие активные подписки
            User user = subscription.getUser();
            if (user != null) {
                checkAndUpdateUserRole(user.getId());
            }

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

                // Обновляем роль пользователя
                User user = sub.getUser();
                if (user != null) {
                    checkAndUpdateUserRole(user.getId());
                }
            }
        }
    }

    @Transactional
    public void checkAndUpdateUserRole(Integer userId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return;
        }

        if (user.getRole() == User.UserRole.Admin || user.getRole() == User.UserRole.Musician) {
            return;
        }

        boolean hasActiveSubscription = findActiveByUserId(userId).isPresent();

        if (hasActiveSubscription) {
            if (user.getRole() != User.UserRole.Subscriber) {
                user.setRole(User.UserRole.Subscriber);
                userRepository.save(user);
                logger.info("User {} role restored to Subscriber (has active subscription)", user.getUsername());
            }
        } else {
            if (user.getRole() == User.UserRole.Subscriber) {
                user.setRole(User.UserRole.User);
                userRepository.save(user);
                logger.info("User {} role downgraded to User (no active subscription)", user.getUsername());
            }
        }
    }

    @Transactional
    public void checkSubscriptionOnLogin(User user) {
        if (user == null) return;

        if (user.getRole() == User.UserRole.Admin || user.getRole() == User.UserRole.Musician) {
            return;
        }

        checkAndUpdateUserRole(user.getId());
    }
}
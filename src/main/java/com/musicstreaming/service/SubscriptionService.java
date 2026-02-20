package com.musicstreaming.service;

import com.musicstreaming.dao.SubscriptionDAO;
import com.musicstreaming.model.Subscription;
import com.musicstreaming.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class SubscriptionService {

    private static final Logger logger = LoggerFactory.getLogger(SubscriptionService.class);

    private final SubscriptionDAO subscriptionDAO;

    @Autowired
    public SubscriptionService(SubscriptionDAO subscriptionDAO) {
        this.subscriptionDAO = subscriptionDAO;
    }

    public Optional<Subscription> findById(Integer id) {
        return subscriptionDAO.findById(id);
    }

    public List<Subscription> findByUserId(Integer userId) {
        return subscriptionDAO.findByUserId(userId);
    }

    public Optional<Subscription> findActiveByUserId(Integer userId) {
        return subscriptionDAO.findActiveByUserId(userId);
    }

    public Subscription createSubscription(Integer userId, String plan, BigDecimal amount) {
        Subscription subscription = new Subscription();
        subscription.setUserId(userId);
        subscription.setStartDate(LocalDateTime.now());

        // Set end date based on plan (1 month for monthly, 1 year for yearly)
        if ("yearly".equals(plan)) {
            subscription.setEndDate(LocalDateTime.now().plusYears(1));
        } else {
            subscription.setEndDate(LocalDateTime.now().plusMonths(1));
        }

        subscription.setAmount(amount);
        subscription.setStatus("pending");
        subscription.setActivated(false);

        Subscription saved = subscriptionDAO.save(subscription);
        logger.info("Created subscription for user {}: {}", userId, saved.getId());

        return saved;
    }

    public void activateSubscription(Integer subscriptionId, String transactionId) {
        Optional<Subscription> opt = subscriptionDAO.findById(subscriptionId);
        if (opt.isPresent()) {
            Subscription subscription = opt.get();
            subscription.setActivated(true);
            subscription.setStatus("active");
            subscription.setTransactionId(transactionId);
            subscriptionDAO.save(subscription);
            logger.info("Activated subscription: {}", subscriptionId);
        }
    }

    public void cancelSubscription(Integer subscriptionId) {
        Optional<Subscription> opt = subscriptionDAO.findById(subscriptionId);
        if (opt.isPresent()) {
            Subscription subscription = opt.get();
            subscription.setStatus("cancelled");
            subscriptionDAO.save(subscription);
            logger.info("Cancelled subscription: {}", subscriptionId);
        }
    }

    public void checkExpiredSubscriptions() {
        List<Subscription> activeSubscriptions = subscriptionDAO.findActiveSubscriptions();
        LocalDateTime now = LocalDateTime.now();

        for (Subscription sub : activeSubscriptions) {
            if (sub.getEndDate().isBefore(now)) {
                sub.setStatus("expired");
                sub.setActivated(false);
                subscriptionDAO.save(sub);
                logger.info("Subscription {} expired", sub.getId());
            }
        }
    }
}
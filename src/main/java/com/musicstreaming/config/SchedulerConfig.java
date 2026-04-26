package com.musicstreaming.config;

import com.musicstreaming.service.SubscriptionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

@Configuration
@EnableScheduling
public class SchedulerConfig {

    private static final Logger logger = LoggerFactory.getLogger(SchedulerConfig.class);

    @Autowired
    private SubscriptionService subscriptionService;

    /**
     * Проверка истекших подписок каждый час
     */
    @Scheduled(fixedRate = 3600000) // Каждый час (3600000 мс = 1 час)
    public void checkExpiredSubscriptions() {
        logger.info("Running scheduled check for expired subscriptions...");
        try {
            subscriptionService.checkExpiredSubscriptions();
            logger.info("Expired subscription check completed successfully");
        } catch (Exception e) {
            logger.error("Error during expired subscription check", e);
        }
    }
}
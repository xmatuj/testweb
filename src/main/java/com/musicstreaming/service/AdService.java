package com.musicstreaming.service;

import com.musicstreaming.dto.AdDTO;
import com.musicstreaming.model.User;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

@Service
public class AdService {

    private final AuthService authService;
    private final SubscriptionService subscriptionService;
    private final Random random = new Random();

    // Примеры рекламных объявлений
    private final List<AdDTO> ads = Arrays.asList(
            new AdDTO("ad1", "Премиум подписка", "Оформите подписку всего за 299₽/мес и слушайте без рекламы!", "/images/ads/premium1.jpg", 5),
            new AdDTO("ad2", "Новый альбом", "Слушайте новый альбом вашего любимого исполнителя прямо сейчас!", "/images/ads/new-album.jpg", 5),
            new AdDTO("ad3", "Концерты", "Билеты на концерты уже в продаже. Успейте купить!", "/images/ads/concerts.jpg", 5),
            new AdDTO("ad4", "Семейный тариф", "Подключите до 5 аккаунтов всего за 449₽/мес", "/images/ads/family.jpg", 5),
            new AdDTO("ad5", "Спецпредложение", "Первый месяц подписки всего за 1₽!", "/images/ads/special.jpg", 5)
    );

    public AdService(AuthService authService, SubscriptionService subscriptionService) {
        this.authService = authService;
        this.subscriptionService = subscriptionService;
    }

    /**
     * Проверяет, нужно ли показывать рекламу пользователю
     */
    public boolean shouldShowAd(HttpServletRequest request) {
        User user = authService.getCurrentUser(request);

        if (user == null) {
            // Неавторизованные пользователи видят рекламу
            return true;
        }

        // Администраторы и музыканты не видят рекламу
        if (user.isAdmin() || user.isMusician() || user.isSubscriber()) {
            return false;
        }

        // Проверяем наличие активной подписки
        boolean hasActiveSubscription = subscriptionService.findActiveByUserId(user.getId()).isPresent();

        // Пользователи с активной подпиской не видят рекламу
        return !hasActiveSubscription;
    }

    /**
     * Получает случайное рекламное объявление
     */
    public AdDTO getRandomAd() {
        return ads.get(random.nextInt(ads.size()));
    }
}
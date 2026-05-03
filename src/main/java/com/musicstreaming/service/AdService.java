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
            new AdDTO("ad1", "Премиум подписка", "Оформите подписку всего за 299₽/мес и слушайте без рекламы!", "", 5),
            new AdDTO("ad2", "Новый альбом", "Слушайте новый альбом вашего любимого исполнителя прямо сейчас!", "", 5),
            new AdDTO("ad3", "Концерты", "Билеты на концерты уже в продаже. Успейте купить где-нибудь в интеренете, мы не продаем билеты!", "", 5),
            new AdDTO("ad4", "Семейный тариф", "Подключите до 5 аккаунтов всего за 449₽/мес, но только не в этом сервисе, у нас нет семейной подписки :)", "", 5),
            new AdDTO("ad5", "Спецпредложение", "Первый месяц подписки всего за 299₽! как и все остальные, халявы не будет", "", 5)
    );

    public AdService(AuthService authService, SubscriptionService subscriptionService) {
        this.authService = authService;
        this.subscriptionService = subscriptionService;
    }

    // Проверяет, нужно ли показывать рекламу пользователю
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

    // Получает случайное рекламное объявление
    public AdDTO getRandomAd() {
        return ads.get(random.nextInt(ads.size()));
    }
}
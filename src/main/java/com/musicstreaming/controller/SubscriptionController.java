package com.musicstreaming.controller;

import com.musicstreaming.dto.SubscriptionUserDTO;
import com.musicstreaming.model.User;
import com.musicstreaming.service.AuthService;
import com.musicstreaming.service.SubscriptionService;
import com.musicstreaming.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;

@Controller
@RequestMapping("/subscription")
public class SubscriptionController {

    private static final Logger logger = LoggerFactory.getLogger(SubscriptionController.class);

    private final SubscriptionService subscriptionService;
    private final AuthService authService;
    private final UserService userService;

    @Autowired
    public SubscriptionController(SubscriptionService subscriptionService,
                                  AuthService authService,
                                  UserService userService) {
        this.subscriptionService = subscriptionService;
        this.authService = authService;
        this.userService = userService;
    }

    @GetMapping("/plans")
    public String plans(Model model, HttpServletRequest request) {
        User sessionUser = authService.getCurrentUser(request);
        SubscriptionUserDTO currentUser = null;

        if (sessionUser != null) {
            boolean hasActiveSubscription = subscriptionService.findActiveByUserId(sessionUser.getId()).isPresent();
            currentUser = new SubscriptionUserDTO(sessionUser, hasActiveSubscription);
        }

        model.addAttribute("currentUser", currentUser);
        model.addAttribute("isAuthenticated", currentUser != null);

        if (currentUser != null) {
            model.addAttribute("hasActiveSubscription", currentUser.hasActiveSubscription());
        }

        return "subscription/plans";
    }

    @GetMapping("/create")
    public String createForm(Model model, HttpServletRequest request,
                             @RequestParam(required = false) String plan,
                             RedirectAttributes redirectAttributes) {

        if (!authService.isAuthenticated(request)) {
            redirectAttributes.addAttribute("redirect", "/subscription/create");
            return "redirect:/account/login";
        }

        User sessionUser = authService.getCurrentUser(request);
        boolean hasActiveSubscription = subscriptionService.findActiveByUserId(sessionUser.getId()).isPresent();
        SubscriptionUserDTO currentUser = new SubscriptionUserDTO(sessionUser, hasActiveSubscription);

        model.addAttribute("currentUser", currentUser);

        String selectedPlan = plan != null ? plan : "premium";
        String planName;
        int planPrice;
        String planPeriod;

        switch (selectedPlan.toLowerCase()) {
            case "yearly":
                planName = "Годовой";
                planPrice = 2990;
                planPeriod = "год";
                break;
            case "monthly":
            default:
                planName = "Премиум";
                planPrice = 299;
                planPeriod = "Ежемесячно";
                selectedPlan = "monthly";
                break;
        }

        model.addAttribute("selectedPlan", selectedPlan);
        model.addAttribute("planName", planName);
        model.addAttribute("planPrice", planPrice);
        model.addAttribute("planPeriod", planPeriod);

        return "subscription/create";
    }

    @PostMapping("/create")
    public String createSubscription(@RequestParam String plan,
                                     @RequestParam String cardNumber,
                                     @RequestParam String cardExpiry,
                                     @RequestParam String cardCvc,
                                     @RequestParam String cardName,
                                     @RequestParam(required = false) String promoCode,
                                     HttpServletRequest request,
                                     RedirectAttributes redirectAttributes) {

        if (!authService.isAuthenticated(request)) {
            return "redirect:/account/login";
        }

        User currentUser = authService.getCurrentUser(request);

        try {
            // Определяем сумму в зависимости от плана
            BigDecimal amount;
            switch (plan.toLowerCase()) {
                case "yearly":
                    amount = new BigDecimal("2990.00");
                    break;
                case "monthly":
                default:
                    amount = new BigDecimal("299.00");
                    break;
            }

            // Применяем промокод (простая логика для примера)
            if ("FIRSTMONTH".equalsIgnoreCase(promoCode)) {
                amount = new BigDecimal("1.00");
                logger.info("Promo code applied: {}", promoCode);
            }

            // Создаем и активируем подписку
            subscriptionService.createSubscription(currentUser.getId(), plan, amount);

            // Обновляем пользователя в сессии после изменения роли
            User updatedUser = userService.findById(currentUser.getId())
                    .orElseThrow(() -> new IllegalArgumentException("User not found"));

            authService.login(request, updatedUser);

            logger.info("Subscription activated for user: {}, plan: {}, role: {}",
                    updatedUser.getUsername(), plan, updatedUser.getRole());

            redirectAttributes.addFlashAttribute("success", "Подписка успешно оформлена! Добро пожаловать в MusicStream Premium!");
            return "redirect:/subscription/success";
        } catch (Exception e) {
            logger.error("Error creating subscription", e);
            redirectAttributes.addFlashAttribute("error", "Ошибка при оформлении подписки: " + e.getMessage());
            return "redirect:/subscription/plans";
        }
    }

    @GetMapping("/success")
    public String success(Model model, HttpServletRequest request) {
        User sessionUser = authService.getCurrentUser(request);
        SubscriptionUserDTO currentUser = null;

        if (sessionUser != null) {
            boolean hasActiveSubscription = subscriptionService.findActiveByUserId(sessionUser.getId()).isPresent();
            currentUser = new SubscriptionUserDTO(sessionUser, hasActiveSubscription);
        }

        model.addAttribute("currentUser", currentUser);
        model.addAttribute("isAuthenticated", currentUser != null);

        return "subscription/success";
    }

    @GetMapping("/my")
    public String mySubscriptions(Model model, HttpServletRequest request,
                                  RedirectAttributes redirectAttributes) {

        if (!authService.isAuthenticated(request)) {
            return "redirect:/account/login";
        }

        User sessionUser = authService.getCurrentUser(request);
        boolean hasActiveSubscription = subscriptionService.findActiveByUserId(sessionUser.getId()).isPresent();
        SubscriptionUserDTO currentUser = new SubscriptionUserDTO(sessionUser, hasActiveSubscription);

        model.addAttribute("currentUser", currentUser);

        model.addAttribute("subscriptions", subscriptionService.findByUserId(currentUser.getId()));

        model.addAttribute("activeSubscription",
                subscriptionService.findActiveByUserId(currentUser.getId()).orElse(null));

        return "subscription/my";
    }
}
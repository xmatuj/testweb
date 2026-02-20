package com.musicstreaming.controller;

import com.musicstreaming.model.User;
import com.musicstreaming.service.AuthService;
import com.musicstreaming.service.SubscriptionService;
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

@Controller
@RequestMapping("/subscription")
public class SubscriptionController {

    private static final Logger logger = LoggerFactory.getLogger(SubscriptionController.class);

    private final SubscriptionService subscriptionService;
    private final AuthService authService;

    @Autowired
    public SubscriptionController(SubscriptionService subscriptionService, AuthService authService) {
        this.subscriptionService = subscriptionService;
        this.authService = authService;
    }

    @GetMapping("/plans")
    public String plans(Model model, HttpServletRequest request) {
        User currentUser = authService.getCurrentUser(request);

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

        User currentUser = authService.getCurrentUser(request);
        model.addAttribute("currentUser", currentUser);

        // Default to premium plan
        String selectedPlan = plan != null ? plan : "premium";
        model.addAttribute("selectedPlan", selectedPlan);
        model.addAttribute("planName", selectedPlan.equals("premium") ? "Премиум" : "Семейный");
        model.addAttribute("planPrice", selectedPlan.equals("premium") ? 299 : 449);
        model.addAttribute("planPeriod", "Ежемесячно");

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
            // Here you would process the payment and create subscription
            // For now, we'll just simulate success

            redirectAttributes.addFlashAttribute("success", "Подписка успешно оформлена!");
            return "redirect:/subscription/success";
        } catch (Exception e) {
            logger.error("Error creating subscription", e);
            redirectAttributes.addFlashAttribute("error", "Ошибка при оформлении подписки");
            return "redirect:/subscription/plans";
        }
    }

    @GetMapping("/success")
    public String success(Model model, HttpServletRequest request) {
        User currentUser = authService.getCurrentUser(request);
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

        User currentUser = authService.getCurrentUser(request);
        model.addAttribute("currentUser", currentUser);

        // Get user subscriptions
        model.addAttribute("subscriptions", subscriptionService.findByUserId(currentUser.getId()));

        // Find active subscription
        model.addAttribute("activeSubscription",
                currentUser.getSubscriptions() != null ?
                        currentUser.getSubscriptions().stream()
                                .filter(s -> s.isActivated() && s.getEndDate().isAfter(java.time.LocalDateTime.now()))
                                .findFirst().orElse(null) : null);

        return "subscription/my";
    }

    @PostMapping("/cancel")
    public String cancelSubscription(@RequestParam Integer subscriptionId,
                                     HttpServletRequest request,
                                     RedirectAttributes redirectAttributes) {

        if (!authService.isAuthenticated(request)) {
            return "redirect:/account/login";
        }

        try {
            subscriptionService.cancelSubscription(subscriptionId);
            redirectAttributes.addFlashAttribute("success", "Подписка отменена");
        } catch (Exception e) {
            logger.error("Error canceling subscription", e);
            redirectAttributes.addFlashAttribute("error", "Ошибка при отмене подписки");
        }

        return "redirect:/subscription/my";
    }
}
package com.musicstreaming.controller;

import com.musicstreaming.dto.UserProfileDTO;
import com.musicstreaming.model.User;
import com.musicstreaming.service.AuthService;
import com.musicstreaming.service.UserService;
import com.musicstreaming.service.PlaylistService;
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
import java.util.Optional;

@Controller
@RequestMapping("/account")
public class AccountController {

    private static final Logger logger = LoggerFactory.getLogger(AccountController.class);

    private final UserService userService;
    private final AuthService authService;
    private final PlaylistService playlistService;
    private final SubscriptionService subscriptionService;

    @Autowired
    public AccountController(UserService userService, AuthService authService,
                             PlaylistService playlistService, SubscriptionService subscriptionService) {
        this.userService = userService;
        this.authService = authService;
        this.playlistService = playlistService;
        this.subscriptionService = subscriptionService;
    }

    @GetMapping("/login")
    public String loginForm(@RequestParam(required = false) String error,
                            Model model, HttpServletRequest request) {
        if (authService.isAuthenticated(request)) {
            return "redirect:/";
        }

        if (error != null) {
            model.addAttribute("error", "Invalid username or password");
        }

        return "account/login";
    }

    @PostMapping("/login")
    public String login(@RequestParam String usernameOrEmail,
                        @RequestParam String password,
                        @RequestParam(required = false) boolean remember,
                        HttpServletRequest request,
                        RedirectAttributes redirectAttributes) {

        Optional<User> userOpt = userService.authenticate(usernameOrEmail, password);

        if (userOpt.isPresent()) {
            User user = userOpt.get();

            // Проверяем статус подписки при входе
            subscriptionService.checkSubscriptionOnLogin(user);

            // Загружаем обновленного пользователя
            User refreshedUser = userService.findById(user.getId())
                    .orElse(user);

            authService.login(request, refreshedUser);
            logger.info("User {} logged in successfully, role: {}", refreshedUser.getUsername(), refreshedUser.getRole());
            return "redirect:/";
        } else {
            redirectAttributes.addAttribute("error", true);
            return "redirect:/account/login";
        }
    }

    @GetMapping("/register")
    public String registerForm(HttpServletRequest request) {
        if (authService.isAuthenticated(request)) {
            return "redirect:/";
        }
        return "account/register";
    }

    @PostMapping("/register")
    public String register(@RequestParam String username,
                           @RequestParam String email,
                           @RequestParam String password,
                           @RequestParam String confirmPassword,
                           HttpServletRequest request,
                           Model model) {

        if (!password.equals(confirmPassword)) {
            model.addAttribute("error", "Passwords do not match");
            return "account/register";
        }

        try {
            User user = userService.registerUser(username, email, password);
            authService.login(request, user);
            logger.info("User {} registered successfully", username);
            return "redirect:/";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            return "account/register";
        }
    }

    @GetMapping("/profile")
    public String profile(Model model, HttpServletRequest request) {
        User currentUser = authService.getCurrentUser(request);

        if (currentUser == null) {
            return "redirect:/account/login";
        }

        // Проверяем статус подписки
        subscriptionService.checkSubscriptionOnLogin(currentUser);

        // Загружаем обновленного пользователя
        User refreshedUser = userService.findById(currentUser.getId())
                .orElse(currentUser);

        // Обновляем пользователя в сессии, если роль изменилась
        if (refreshedUser.getRole() != currentUser.getRole()) {
            authService.login(request, refreshedUser);
        }

        // Если пользователь музыкант
        if (refreshedUser.isMusician()) {
            return "redirect:/artist/cabinet";
        }

        // Для обычных пользователей и админов без статуса музыканта
        UserProfileDTO userProfile = new UserProfileDTO(
                refreshedUser,
                playlistService.findByUserId(refreshedUser.getId()),
                subscriptionService.findByUserId(refreshedUser.getId())
        );

        model.addAttribute("user", userProfile);
        model.addAttribute("isAdmin", userProfile.isAdmin());
        model.addAttribute("isMusician", userProfile.isMusician());
        model.addAttribute("canUploadTracks", userProfile.canUploadTracks());

        return "account/profile";
    }

    @PostMapping("/logout")
    public String logout(HttpServletRequest request) {
        authService.logout(request);
        logger.info("User logged out");
        return "redirect:/";
    }
}
package com.musicstreaming.service;

import com.musicstreaming.dao.UserDAO;
import com.musicstreaming.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    private final UserDAO userDAO;
    private final BCryptPasswordEncoder passwordEncoder;

    @Autowired
    public UserService(UserDAO userDAO) {
        this.userDAO = userDAO;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    public Optional<User> findById(Integer id) {
        return userDAO.findById(id);
    }

    public Optional<User> findByUsername(String username) {
        return userDAO.findByUsername(username);
    }

    public Optional<User> findByEmail(String email) {
        return userDAO.findByEmail(email);
    }

    public Optional<User> findByUsernameOrEmail(String usernameOrEmail) {
        return userDAO.findByUsernameOrEmail(usernameOrEmail);
    }

    public List<User> findAll() {
        return userDAO.findAll();
    }

    public List<User> search(String searchTerm) {
        return userDAO.search(searchTerm);
    }

    public User registerUser(String username, String email, String password) {
        // Check if user already exists
        if (userDAO.existsByUsername(username)) {
            throw new IllegalArgumentException("Username already exists");
        }
        if (userDAO.existsByEmail(email)) {
            throw new IllegalArgumentException("Email already exists");
        }

        // Hash password using Spring Security Crypto
        String hashedPassword = passwordEncoder.encode(password);

        // Create user (always as regular User)
        User user = new User(username, email, hashedPassword);
        user.setRole(User.UserRole.User);

        return userDAO.save(user);
    }

    public Optional<User> authenticate(String usernameOrEmail, String password) {
        Optional<User> userOpt = userDAO.findByUsernameOrEmail(usernameOrEmail);

        if (userOpt.isPresent()) {
            User user = userOpt.get();
            if (passwordEncoder.matches(password, user.getPasswordHash())) {
                return userOpt;
            }
        }

        return Optional.empty();
    }

    public User updateUser(User user) {
        return userDAO.save(user);
    }

    public void updateRole(Integer userId, User.UserRole newRole) {
        userDAO.updateRole(userId, newRole);
    }

    public boolean isUsernameTaken(String username) {
        return userDAO.existsByUsername(username);
    }

    public boolean isEmailTaken(String email) {
        return userDAO.existsByEmail(email);
    }

    public void changePassword(Integer userId, String oldPassword, String newPassword) {
        Optional<User> userOpt = userDAO.findById(userId);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            if (passwordEncoder.matches(oldPassword, user.getPasswordHash())) {
                user.setPasswordHash(passwordEncoder.encode(newPassword));
                userDAO.save(user);
            } else {
                throw new IllegalArgumentException("Old password is incorrect");
            }
        } else {
            throw new IllegalArgumentException("User not found");
        }
    }
}
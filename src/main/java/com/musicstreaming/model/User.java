package com.musicstreaming.model;

import java.time.LocalDateTime;
import java.util.List;

public class User {
    private Integer id;
    private String username;
    private String email;
    private String passwordHash;
    private UserRole role;
    private LocalDateTime dateOfCreated;

    // Transient fields for related data
    private List<Subscription> subscriptions;
    private List<Playlist> playlists;

    public enum UserRole {
        User, Subscriber, Musician, Admin
    }

    // Constructors
    public User() {
        this.role = UserRole.User;
        this.dateOfCreated = LocalDateTime.now();
    }

    public User(String username, String email, String passwordHash) {
        this();
        this.username = username;
        this.email = email;
        this.passwordHash = passwordHash;
    }

    // Getters and Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public UserRole getRole() { return role; }
    public void setRole(UserRole role) { this.role = role; }

    public LocalDateTime getDateOfCreated() { return dateOfCreated; }
    public void setDateOfCreated(LocalDateTime dateOfCreated) { this.dateOfCreated = dateOfCreated; }

    public List<Subscription> getSubscriptions() { return subscriptions; }
    public void setSubscriptions(List<Subscription> subscriptions) { this.subscriptions = subscriptions; }

    public List<Playlist> getPlaylists() { return playlists; }
    public void setPlaylists(List<Playlist> playlists) { this.playlists = playlists; }

    // Helper methods
    public boolean canUploadTracks() {
        return role == UserRole.Admin || role == UserRole.Musician;
    }

    public boolean hasActiveSubscription() {
        if (subscriptions == null || subscriptions.isEmpty()) return false;
        LocalDateTime now = LocalDateTime.now();
        return subscriptions.stream()
                .anyMatch(s -> s.isActivated() && s.getEndDate().isAfter(now));
    }

    public boolean isAdmin() {
        return role == UserRole.Admin;
    }

    public boolean isMusician() {
        return role == UserRole.Musician || role == UserRole.Admin;
    }

    public boolean isSubscriber() {
        return role == UserRole.Subscriber || role == UserRole.Admin || hasActiveSubscription();
    }
}
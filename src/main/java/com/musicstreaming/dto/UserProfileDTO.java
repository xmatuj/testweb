package com.musicstreaming.dto;

import com.musicstreaming.model.User;
import com.musicstreaming.model.Subscription;

import java.time.LocalDateTime;
import java.util.List;

public class UserProfileDTO {
    private Integer id;
    private String username;
    private String email;
    private User.UserRole role;
    private LocalDateTime dateOfCreated;
    private List<Subscription> subscriptions;
    private List<PlaylistDTO> playlists;
    private boolean hasActiveSubscription;

    public UserProfileDTO(User user, List<PlaylistDTO> playlists, List<Subscription> subscriptions) {
        this.id = user.getId();
        this.username = user.getUsername();
        this.email = user.getEmail();
        this.role = user.getRole();
        this.dateOfCreated = user.getDateOfCreated();
        this.playlists = playlists;
        this.subscriptions = subscriptions;
        this.hasActiveSubscription = subscriptions.stream()
                .anyMatch(s -> s.isActivated() && s.getEndDate().isAfter(LocalDateTime.now()));
    }

    // Getters
    public Integer getId() { return id; }
    public String getUsername() { return username; }
    public String getEmail() { return email; }
    public User.UserRole getRole() { return role; }
    public LocalDateTime getDateOfCreated() { return dateOfCreated; }
    public List<Subscription> getSubscriptions() { return subscriptions; }
    public List<PlaylistDTO> getPlaylists() { return playlists; }

    public boolean hasActiveSubscription() {
        return hasActiveSubscription;
    }

    public boolean isHasActiveSubscription() {
        return hasActiveSubscription;
    }

    public boolean isAdmin() { return role == User.UserRole.Admin; }
    public boolean isMusician() { return role == User.UserRole.Musician || role == User.UserRole.Admin; }
    public boolean isSubscriber() { return role == User.UserRole.Subscriber || role == User.UserRole.Admin || hasActiveSubscription; }
    public boolean canUploadTracks() { return role == User.UserRole.Admin || role == User.UserRole.Musician; }
}
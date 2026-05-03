package com.musicstreaming.dto;

import com.musicstreaming.model.User;
import java.time.LocalDateTime;

public class HomeUserDTO {
    private Integer id;
    private String username;
    private String email;
    private User.UserRole role;
    private LocalDateTime dateOfCreated;
    private boolean hasActiveSubscription;

    public HomeUserDTO(User user, boolean hasActiveSubscription) {
        this.id = user.getId();
        this.username = user.getUsername();
        this.email = user.getEmail();
        this.role = user.getRole();
        this.dateOfCreated = user.getDateOfCreated();
        this.hasActiveSubscription = hasActiveSubscription;
    }

    // Getters
    public Integer getId() { return id; }
    public String getUsername() { return username; }
    public String getEmail() { return email; }
    public User.UserRole getRole() { return role; }
    public LocalDateTime getDateOfCreated() { return dateOfCreated; }
    public boolean isHasActiveSubscription() { return hasActiveSubscription; }

    public boolean isAdmin() { return role == User.UserRole.Admin; }
    public boolean isMusician() { return role == User.UserRole.Musician || role == User.UserRole.Admin; }
    public boolean isSubscriber() { return role == User.UserRole.Subscriber || role == User.UserRole.Admin || hasActiveSubscription; }
    public boolean canUploadTracks() { return role == User.UserRole.Admin || role == User.UserRole.Musician; }
}
package com.musicstreaming.dto;

import com.musicstreaming.model.User;
import java.time.LocalDateTime;

public class AdminUserDTO {
    private Integer id;
    private String username;
    private String email;
    private User.UserRole role;
    private LocalDateTime dateOfCreated;
    private int playlistCount;

    public AdminUserDTO(User user, int playlistCount) {
        this.id = user.getId();
        this.username = user.getUsername();
        this.email = user.getEmail();
        this.role = user.getRole();
        this.dateOfCreated = user.getDateOfCreated();
        this.playlistCount = playlistCount;
    }

    // Getters
    public Integer getId() { return id; }
    public String getUsername() { return username; }
    public String getEmail() { return email; }
    public User.UserRole getRole() { return role; }
    public LocalDateTime getDateOfCreated() { return dateOfCreated; }
    public int getPlaylistCount() { return playlistCount; }

    public boolean isAdmin() { return role == User.UserRole.Admin; }
    public boolean isMusician() { return role == User.UserRole.Musician; }
    public boolean isSubscriber() { return role == User.UserRole.Subscriber; }
    public boolean isUser() { return role == User.UserRole.User; }
}
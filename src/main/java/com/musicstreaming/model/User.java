package com.musicstreaming.model;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "Users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id")
    private Integer id;

    @Column(name = "Username", nullable = false, unique = true, length = 50)
    private String username;

    @Column(name = "Email", nullable = false, unique = true, length = 100)
    private String email;

    @Column(name = "PasswordHash", nullable = false)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(name = "Role", nullable = false, length = 20)
    private UserRole role;

    @Column(name = "DateOfCreated", nullable = false)
    private LocalDateTime dateOfCreated;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Subscription> subscriptions = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Playlist> playlists = new ArrayList<>();

    public enum UserRole {
        User, Subscriber, Musician, Admin
    }

    public User() {
        this.dateOfCreated = LocalDateTime.now();
        this.role = UserRole.User;
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
        return role == UserRole.Musician;
    }

    public boolean isSubscriber() {
        return role == UserRole.Subscriber || role == UserRole.Admin || hasActiveSubscription();
    }
}
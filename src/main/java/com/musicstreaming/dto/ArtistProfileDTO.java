package com.musicstreaming.dto;

import com.musicstreaming.model.Track;
import com.musicstreaming.model.Album;
import com.musicstreaming.model.User;

import java.time.LocalDateTime;
import java.util.List;

public class ArtistProfileDTO {
    private Integer id;
    private String username;
    private String email;
    private User.UserRole role;
    private LocalDateTime dateOfCreated;
    private List<Track> tracks;
    private List<Album> albums;
    private long totalPlays;
    private long monthlyPlays;
    private long newListeners;
    private long playlistAdds;
    private List<Track> popularTracks;
    private List<Album> recentAlbums;
    private boolean hasActiveSubscription;
    private String displayName; // Имя аккаунта (username)

    public ArtistProfileDTO() {}

    // Getters and Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public User.UserRole getRole() { return role; }
    public void setRole(User.UserRole role) { this.role = role; }

    public LocalDateTime getDateOfCreated() { return dateOfCreated; }
    public void setDateOfCreated(LocalDateTime dateOfCreated) { this.dateOfCreated = dateOfCreated; }

    public List<Track> getTracks() { return tracks; }
    public void setTracks(List<Track> tracks) { this.tracks = tracks; }

    public List<Album> getAlbums() { return albums; }
    public void setAlbums(List<Album> albums) { this.albums = albums; }

    public long getTotalPlays() { return totalPlays; }
    public void setTotalPlays(long totalPlays) { this.totalPlays = totalPlays; }

    public long getMonthlyPlays() { return monthlyPlays; }
    public void setMonthlyPlays(long monthlyPlays) { this.monthlyPlays = monthlyPlays; }

    public long getNewListeners() { return newListeners; }
    public void setNewListeners(long newListeners) { this.newListeners = newListeners; }

    public long getPlaylistAdds() { return playlistAdds; }
    public void setPlaylistAdds(long playlistAdds) { this.playlistAdds = playlistAdds; }

    public List<Track> getPopularTracks() { return popularTracks; }
    public void setPopularTracks(List<Track> popularTracks) { this.popularTracks = popularTracks; }

    public List<Album> getRecentAlbums() { return recentAlbums; }
    public void setRecentAlbums(List<Album> recentAlbums) { this.recentAlbums = recentAlbums; }

    public boolean isHasActiveSubscription() { return hasActiveSubscription; }
    public void setHasActiveSubscription(boolean hasActiveSubscription) { this.hasActiveSubscription = hasActiveSubscription; }

    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }

    public boolean isAdmin() { return role == User.UserRole.Admin; }
    public boolean isMusician() { return role == User.UserRole.Musician || role == User.UserRole.Admin; }
    public boolean canUploadTracks() { return role == User.UserRole.Admin || role == User.UserRole.Musician; }
}
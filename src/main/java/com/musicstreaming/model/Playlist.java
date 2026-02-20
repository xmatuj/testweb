package com.musicstreaming.model;

import java.time.LocalDateTime;
import java.util.List;

public class Playlist {
    private Integer id;
    private String title;
    private String description;
    private Integer userId;
    private PlaylistVisibility visibility;
    private LocalDateTime createdDate;
    private LocalDateTime updatedDate;
    private String coverImagePath;

    // Related objects
    private User user;
    private List<PlaylistTrack> playlistTracks;

    public enum PlaylistVisibility {
        Private, Public, Unlisted
    }

    // Constructors
    public Playlist() {
        this.createdDate = LocalDateTime.now();
        this.visibility = PlaylistVisibility.Private;
    }

    public Playlist(String title, Integer userId) {
        this();
        this.title = title;
        this.userId = userId;
    }

    // Getters and Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }

    public PlaylistVisibility getVisibility() { return visibility; }
    public void setVisibility(PlaylistVisibility visibility) { this.visibility = visibility; }

    public LocalDateTime getCreatedDate() { return createdDate; }
    public void setCreatedDate(LocalDateTime createdDate) { this.createdDate = createdDate; }

    public LocalDateTime getUpdatedDate() { return updatedDate; }
    public void setUpdatedDate(LocalDateTime updatedDate) { this.updatedDate = updatedDate; }

    public String getCoverImagePath() { return coverImagePath; }
    public void setCoverImagePath(String coverImagePath) { this.coverImagePath = coverImagePath; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public List<PlaylistTrack> getPlaylistTracks() { return playlistTracks; }
    public void setPlaylistTracks(List<PlaylistTrack> playlistTracks) { this.playlistTracks = playlistTracks; }

    // Helper methods
    public int getTrackCount() {
        return playlistTracks != null ? playlistTracks.size() : 0;
    }

    public int getTotalDuration() {
        if (playlistTracks == null) return 0;
        return playlistTracks.stream()
                .mapToInt(pt -> pt.getTrack() != null ? pt.getTrack().getDuration() : 0)
                .sum();
    }

    public boolean isPublic() {
        return visibility == PlaylistVisibility.Public;
    }
}
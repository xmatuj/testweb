package com.musicstreaming.dto;

import com.musicstreaming.model.Playlist;
import com.musicstreaming.model.PlaylistTrack;

import java.time.LocalDateTime;
import java.util.List;

public class PlaylistDTO {
    private Integer id;
    private String title;
    private String description;
    private String username;
    private Playlist.PlaylistVisibility visibility;
    private LocalDateTime createdDate;
    private LocalDateTime updatedDate;
    private String coverImagePath;
    private int trackCount;
    private int totalDuration;

    public PlaylistDTO(Playlist playlist, List<PlaylistTrack> tracks) {
        this.id = playlist.getId();
        this.title = playlist.getTitle();
        this.description = playlist.getDescription();
        this.username = playlist.getUser() != null ? playlist.getUser().getUsername() : null;
        this.visibility = playlist.getVisibility();
        this.createdDate = playlist.getCreatedDate();
        this.updatedDate = playlist.getUpdatedDate();
        this.coverImagePath = playlist.getCoverImagePath();
        this.trackCount = tracks.size();
        this.totalDuration = tracks.stream()
                .mapToInt(pt -> pt.getTrack() != null ? pt.getTrack().getDuration() : 0)
                .sum();
    }

    // Getters
    public Integer getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getUsername() { return username; }
    public Playlist.PlaylistVisibility getVisibility() { return visibility; }
    public LocalDateTime getCreatedDate() { return createdDate; }
    public LocalDateTime getUpdatedDate() { return updatedDate; }
    public String getCoverImagePath() { return coverImagePath; }
    public int getTrackCount() { return trackCount; }
    public int getTotalDuration() { return totalDuration; }

    public boolean isPublic() {
        return visibility == Playlist.PlaylistVisibility.Public;
    }
}
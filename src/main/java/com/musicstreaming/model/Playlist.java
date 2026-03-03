package com.musicstreaming.model;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "Playlists")
public class Playlist {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id")
    private Integer id;

    @Column(name = "Title", nullable = false, length = 100)
    private String title;

    @Column(name = "Description", length = 500)
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "UserId", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "Visibility", nullable = false, length = 20)
    private PlaylistVisibility visibility;

    @Column(name = "CreatedDate", nullable = false)
    private LocalDateTime createdDate;

    @Column(name = "UpdatedDate")
    private LocalDateTime updatedDate;

    @Column(name = "CoverImagePath", length = 255)
    private String coverImagePath;

    @OneToMany(mappedBy = "playlist", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<PlaylistTrack> playlistTracks = new ArrayList<>();

    public enum PlaylistVisibility {
        Private, Public, Unlisted
    }

    public Playlist() {
        this.createdDate = LocalDateTime.now();
        this.visibility = PlaylistVisibility.Private;
    }

    public Playlist(String title, User user) {
        this();
        this.title = title;
        this.user = user;
    }

    // Getters and Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public PlaylistVisibility getVisibility() { return visibility; }
    public void setVisibility(PlaylistVisibility visibility) { this.visibility = visibility; }

    public LocalDateTime getCreatedDate() { return createdDate; }
    public void setCreatedDate(LocalDateTime createdDate) { this.createdDate = createdDate; }

    public LocalDateTime getUpdatedDate() { return updatedDate; }
    public void setUpdatedDate(LocalDateTime updatedDate) { this.updatedDate = updatedDate; }

    public String getCoverImagePath() { return coverImagePath; }
    public void setCoverImagePath(String coverImagePath) { this.coverImagePath = coverImagePath; }

    public List<PlaylistTrack> getPlaylistTracks() { return playlistTracks; }
    public void setPlaylistTracks(List<PlaylistTrack> playlistTracks) { this.playlistTracks = playlistTracks; }

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
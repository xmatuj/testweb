package com.musicstreaming.model;

import java.time.LocalDateTime;

public class PlaylistTrack {
    private Integer playlistId;
    private Integer trackId;
    private Integer position;
    private LocalDateTime addedDate;

    // Related objects
    private Playlist playlist;
    private Track track;

    // Constructors
    public PlaylistTrack() {
        this.addedDate = LocalDateTime.now();
        this.position = 0;
    }

    public PlaylistTrack(Integer playlistId, Integer trackId) {
        this();
        this.playlistId = playlistId;
        this.trackId = trackId;
    }

    // Getters and Setters
    public Integer getPlaylistId() { return playlistId; }
    public void setPlaylistId(Integer playlistId) { this.playlistId = playlistId; }

    public Integer getTrackId() { return trackId; }
    public void setTrackId(Integer trackId) { this.trackId = trackId; }

    public Integer getPosition() { return position; }
    public void setPosition(Integer position) { this.position = position; }

    public LocalDateTime getAddedDate() { return addedDate; }
    public void setAddedDate(LocalDateTime addedDate) { this.addedDate = addedDate; }

    public Playlist getPlaylist() { return playlist; }
    public void setPlaylist(Playlist playlist) { this.playlist = playlist; }

    public Track getTrack() { return track; }
    public void setTrack(Track track) { this.track = track; }
}
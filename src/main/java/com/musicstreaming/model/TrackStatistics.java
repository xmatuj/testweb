package com.musicstreaming.model;

import java.time.LocalDateTime;

public class TrackStatistics {
    private Integer id;
    private Integer trackId;
    private LocalDateTime date;
    private Integer listenCount;

    // Related objects
    private Track track;

    // Constructors
    public TrackStatistics() {
        this.date = LocalDateTime.now();
        this.listenCount = 1;
    }

    public TrackStatistics(Integer trackId) {
        this();
        this.trackId = trackId;
    }

    // Getters and Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Integer getTrackId() { return trackId; }
    public void setTrackId(Integer trackId) { this.trackId = trackId; }

    public LocalDateTime getDate() { return date; }
    public void setDate(LocalDateTime date) { this.date = date; }

    public Integer getListenCount() { return listenCount; }
    public void setListenCount(Integer listenCount) { this.listenCount = listenCount; }

    public Track getTrack() { return track; }
    public void setTrack(Track track) { this.track = track; }
}
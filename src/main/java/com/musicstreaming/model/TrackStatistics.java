package com.musicstreaming.model;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "TrackStatistics")
public class TrackStatistics {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "TrackId", nullable = false)
    private Track track;

    @Column(name = "Date", nullable = false)
    private LocalDateTime date;

    @Column(name = "ListenCount", nullable = false)
    private Integer listenCount;

    public TrackStatistics() {
        this.date = LocalDateTime.now();
        this.listenCount = 1;
    }

    public TrackStatistics(Track track) {
        this();
        this.track = track;
    }

    // Getters and Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Track getTrack() { return track; }
    public void setTrack(Track track) { this.track = track; }

    public LocalDateTime getDate() { return date; }
    public void setDate(LocalDateTime date) { this.date = date; }

    public Integer getListenCount() { return listenCount; }
    public void setListenCount(Integer listenCount) { this.listenCount = listenCount; }
}
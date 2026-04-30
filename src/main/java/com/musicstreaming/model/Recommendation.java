package com.musicstreaming.model;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "recommendations")
public class Recommendation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "UserId", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "TrackId", nullable = false)
    private Track track;

    @Column(name = "Date", nullable = false)
    private LocalDateTime date;

    public Recommendation() {
        this.date = LocalDateTime.now();
    }

    public Recommendation(User user, Track track) {
        this();
        this.user = user;
        this.track = track;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public Track getTrack() { return track; }
    public void setTrack(Track track) { this.track = track; }

    public LocalDateTime getDate() { return date; }
    public void setDate(LocalDateTime date) { this.date = date; }
}
package com.musicstreaming.model;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "Moderations")
public class Moderation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "TrackId", nullable = false)
    private Track track;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ModeratorId", nullable = false)
    private User moderator;

    @Enumerated(EnumType.STRING)
    @Column(name = "Status", nullable = false, length = 20)
    private ModerationStatus status;

    @Column(name = "Comment", length = 500)
    private String comment;

    @Column(name = "ModerationDate", nullable = false)
    private LocalDateTime moderationDate;

    public enum ModerationStatus {
        Approved, Rejected, Pending
    }

    public Moderation() {
        this.status = ModerationStatus.Pending;
        this.moderationDate = LocalDateTime.now();
    }

    public Moderation(Track track, User moderator) {
        this();
        this.track = track;
        this.moderator = moderator;
    }

    // Getters and Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Track getTrack() { return track; }
    public void setTrack(Track track) { this.track = track; }

    public User getModerator() { return moderator; }
    public void setModerator(User moderator) { this.moderator = moderator; }

    public ModerationStatus getStatus() { return status; }
    public void setStatus(ModerationStatus status) { this.status = status; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }

    public LocalDateTime getModerationDate() { return moderationDate; }
    public void setModerationDate(LocalDateTime moderationDate) { this.moderationDate = moderationDate; }
}
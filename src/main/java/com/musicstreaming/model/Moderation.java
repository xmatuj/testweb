package com.musicstreaming.model;

import java.time.LocalDateTime;

public class Moderation {
    private Integer id;
    private Integer trackId;
    private Integer moderatorId;
    private ModerationStatus status;
    private String comment;
    private LocalDateTime moderationDate;

    // Related objects
    private Track track;
    private User moderator;

    public enum ModerationStatus {
        Approved, Rejected, Pending
    }

    // Constructors
    public Moderation() {
        this.status = ModerationStatus.Pending;
        this.moderationDate = LocalDateTime.now();
    }

    public Moderation(Integer trackId, Integer moderatorId) {
        this();
        this.trackId = trackId;
        this.moderatorId = moderatorId;
    }

    // Getters and Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Integer getTrackId() { return trackId; }
    public void setTrackId(Integer trackId) { this.trackId = trackId; }

    public Integer getModeratorId() { return moderatorId; }
    public void setModeratorId(Integer moderatorId) { this.moderatorId = moderatorId; }

    public ModerationStatus getStatus() { return status; }
    public void setStatus(ModerationStatus status) { this.status = status; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }

    public LocalDateTime getModerationDate() { return moderationDate; }
    public void setModerationDate(LocalDateTime moderationDate) { this.moderationDate = moderationDate; }

    public Track getTrack() { return track; }
    public void setTrack(Track track) { this.track = track; }

    public User getModerator() { return moderator; }
    public void setModerator(User moderator) { this.moderator = moderator; }
}
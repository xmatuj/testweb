package com.musicstreaming.model;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "PlaylistTracks")
@IdClass(PlaylistTrack.PlaylistTrackId.class)
public class PlaylistTrack {

    @Id
    @Column(name = "PlaylistId")
    private Integer playlistId;

    @Id
    @Column(name = "TrackId")
    private Integer trackId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PlaylistId", insertable = false, updatable = false)
    private Playlist playlist;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "TrackId", insertable = false, updatable = false)
    private Track track;

    @Column(name = "Position")
    private Integer position;

    @Column(name = "AddedDate", nullable = false)
    private LocalDateTime addedDate;

    public static class PlaylistTrackId implements Serializable {
        private Integer playlistId;
        private Integer trackId;

        public PlaylistTrackId() {}

        public PlaylistTrackId(Integer playlistId, Integer trackId) {
            this.playlistId = playlistId;
            this.trackId = trackId;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            PlaylistTrackId that = (PlaylistTrackId) o;
            return Objects.equals(playlistId, that.playlistId) &&
                    Objects.equals(trackId, that.trackId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(playlistId, trackId);
        }
    }

    public PlaylistTrack() {
        this.addedDate = LocalDateTime.now();
        this.position = 0;
    }

    public PlaylistTrack(Playlist playlist, Track track) {
        this();
        this.playlist = playlist;
        this.track = track;
        this.playlistId = playlist.getId();
        this.trackId = track.getId();
    }

    // Getters and Setters
    public Integer getPlaylistId() { return playlistId; }
    public void setPlaylistId(Integer playlistId) { this.playlistId = playlistId; }

    public Integer getTrackId() { return trackId; }
    public void setTrackId(Integer trackId) { this.trackId = trackId; }

    public Playlist getPlaylist() { return playlist; }
    public void setPlaylist(Playlist playlist) {
        this.playlist = playlist;
        if (playlist != null) {
            this.playlistId = playlist.getId();
        }
    }

    public Track getTrack() { return track; }
    public void setTrack(Track track) {
        this.track = track;
        if (track != null) {
            this.trackId = track.getId();
        }
    }

    public Integer getPosition() { return position; }
    public void setPosition(Integer position) { this.position = position; }

    public LocalDateTime getAddedDate() { return addedDate; }
    public void setAddedDate(LocalDateTime addedDate) { this.addedDate = addedDate; }
}
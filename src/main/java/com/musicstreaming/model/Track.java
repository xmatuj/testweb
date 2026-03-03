package com.musicstreaming.model;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "Tracks")
public class Track {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id")
    private Integer id;

    @Column(name = "Title", nullable = false, length = 200)
    private String title;

    @Column(name = "FilePath", nullable = false, length = 255)
    private String filePath;

    @Column(name = "Duration")
    private Integer duration;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "GenreId")
    private Genre genre;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "AlbumId")
    private Album album;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ArtistId")
    private Artist artist;

    @Column(name = "IsModerated", nullable = false)
    private boolean isModerated;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "UploadedByUserId")
    private User uploadedByUser;

    @OneToMany(mappedBy = "track", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<PlaylistTrack> playlistTracks = new ArrayList<>();

    public Track() {}

    public Track(String title, String filePath, Integer duration, Genre genre, Artist artist) {
        this.title = title;
        this.filePath = filePath;
        this.duration = duration;
        this.genre = genre;
        this.artist = artist;
        this.isModerated = false;
    }

    // Getters and Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) { this.filePath = filePath; }

    public Integer getDuration() { return duration; }
    public void setDuration(Integer duration) { this.duration = duration; }

    public Genre getGenre() { return genre; }
    public void setGenre(Genre genre) { this.genre = genre; }

    public Album getAlbum() { return album; }
    public void setAlbum(Album album) { this.album = album; }

    public Artist getArtist() { return artist; }
    public void setArtist(Artist artist) { this.artist = artist; }

    public boolean isModerated() { return isModerated; }
    public void setModerated(boolean moderated) { isModerated = moderated; }

    public User getUploadedByUser() { return uploadedByUser; }
    public void setUploadedByUser(User uploadedByUser) { this.uploadedByUser = uploadedByUser; }

    public List<PlaylistTrack> getPlaylistTracks() { return playlistTracks; }
    public void setPlaylistTracks(List<PlaylistTrack> playlistTracks) { this.playlistTracks = playlistTracks; }

    public String getCoverImage() {
        if (album != null && album.getCoverPath() != null && !album.getCoverPath().isEmpty()) {
            return album.getCoverPath();
        }
        if (artist != null && artist.getPhotoPath() != null && !artist.getPhotoPath().isEmpty()) {
            return artist.getPhotoPath();
        }
        return "/images/default-track-cover.jpg";
    }

    public String getFormattedDuration() {
        if (duration == null || duration <= 0) {
            return "3:45";
        }
        int minutes = duration / 60;
        int seconds = duration % 60;
        return String.format("%d:%02d", minutes, seconds);
    }
}
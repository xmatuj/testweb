package com.musicstreaming.model;

import java.time.LocalDate;
import java.util.List;

public class Album {
    private Integer id;
    private String title;
    private Integer artistId;
    private LocalDate releaseDate;
    private String coverPath;

    // Related objects
    private Artist artist;
    private List<Track> tracks;

    // Constructors
    public Album() {}

    public Album(String title, Integer artistId) {
        this.title = title;
        this.artistId = artistId;
    }

    // Getters and Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public Integer getArtistId() { return artistId; }
    public void setArtistId(Integer artistId) { this.artistId = artistId; }

    public LocalDate getReleaseDate() { return releaseDate; }
    public void setReleaseDate(LocalDate releaseDate) { this.releaseDate = releaseDate; }

    public String getCoverPath() { return coverPath; }
    public void setCoverPath(String coverPath) { this.coverPath = coverPath; }

    public Artist getArtist() { return artist; }
    public void setArtist(Artist artist) { this.artist = artist; }

    public List<Track> getTracks() { return tracks; }
    public void setTracks(List<Track> tracks) { this.tracks = tracks; }
}
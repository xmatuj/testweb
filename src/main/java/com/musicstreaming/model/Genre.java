package com.musicstreaming.model;

import java.util.List;

public class Genre {
    private Integer id;
    private String name;

    // Related objects
    private List<Track> tracks;

    // Constructors
    public Genre() {}

    public Genre(String name) {
        this.name = name;
    }

    // Getters and Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public List<Track> getTracks() { return tracks; }
    public void setTracks(List<Track> tracks) { this.tracks = tracks; }
}
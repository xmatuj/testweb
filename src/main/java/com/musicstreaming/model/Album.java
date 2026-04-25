package com.musicstreaming.model;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;

@Entity
@Table(name = "Albums")
public class Album {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id")
    private Integer id;

    @Column(name = "Title", nullable = false, length = 100)
    private String title;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ArtistId", nullable = false)
    private Artist artist;

    @Column(name = "ReleaseDate")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate releaseDate;

    @Column(name = "CoverPath", length = 255)
    private String coverPath;

    @OneToMany(mappedBy = "album", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Track> tracks = new ArrayList<>();

    public Album() {}

    public Album(String title, Artist artist) {
        this.title = title;
        this.artist = artist;
    }

    // Getters and Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public Artist getArtist() { return artist; }
    public void setArtist(Artist artist) { this.artist = artist; }

    public LocalDate getReleaseDate() { return releaseDate; }
    public void setReleaseDate(LocalDate releaseDate) { this.releaseDate = releaseDate; }

    public String getCoverPath() { return coverPath; }
    public void setCoverPath(String coverPath) { this.coverPath = coverPath; }

    public List<Track> getTracks() { return tracks; }
    public void setTracks(List<Track> tracks) { this.tracks = tracks; }
}
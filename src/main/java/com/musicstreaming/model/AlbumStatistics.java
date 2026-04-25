package com.musicstreaming.model;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "AlbumStatistics")
public class AlbumStatistics {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "AlbumId", nullable = false)
    private Album album;

    @Column(name = "Date")
    private LocalDateTime date;

    @Column(name = "ListenCount")
    private Integer listenCount;

    public AlbumStatistics() {
        this.date = LocalDateTime.now();
        this.listenCount = 1;
    }

    public AlbumStatistics(Album album) {
        this();
        this.album = album;
    }

    // Getters and Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Album getAlbum() { return album; }
    public void setAlbum(Album album) { this.album = album; }

    public LocalDateTime getDate() { return date; }
    public void setDate(LocalDateTime date) { this.date = date; }

    public Integer getListenCount() { return listenCount; }
    public void setListenCount(Integer listenCount) { this.listenCount = listenCount; }
}
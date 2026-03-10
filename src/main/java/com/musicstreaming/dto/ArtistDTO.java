package com.musicstreaming.dto;

import com.musicstreaming.model.Artist;

public class ArtistDTO {
    private Integer id;
    private String name;
    private String description;
    private int albumCount;
    private int trackCount;

    public ArtistDTO(Artist artist) {
        this.id = artist.getId();
        this.name = artist.getName();
        this.description = artist.getDescription();
        // Безопасно получаем размеры коллекций
        this.albumCount = artist.getAlbums() != null ? artist.getAlbums().size() : 0;
        this.trackCount = artist.getTracks() != null ? artist.getTracks().size() : 0;
    }

    // Геттеры
    public Integer getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public int getAlbumCount() { return albumCount; }
    public int getTrackCount() { return trackCount; }
}
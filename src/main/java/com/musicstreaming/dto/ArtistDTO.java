package com.musicstreaming.dto;

public class ArtistDTO {
    private Integer id;
    private String name;
    private String description;
    private int albumCount;
    private int trackCount;

    // Конструктор для JPQL проекции
    public ArtistDTO(Integer id, String name, String description,
                     int albumCount, int trackCount) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.albumCount = albumCount;
        this.trackCount = trackCount;
    }

    // Геттеры
    public Integer getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public int getAlbumCount() {
        return albumCount;
    }

    public int getTrackCount() {
        return trackCount;
    }
}
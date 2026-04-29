package com.musicstreaming.dto;

public class ArtistDTO {
    private Integer id;
    private String name;
    private String description;
    private int albumCount;
    private int trackCount;
    private String photoPath;

    public ArtistDTO(Integer id, String name, String description,
                     int albumCount, int trackCount, String photoPath) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.albumCount = albumCount;
        this.trackCount = trackCount;
        this.photoPath = photoPath;
    }

    public String getPhotoPath() {
        return photoPath;
    }

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
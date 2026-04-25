package com.musicstreaming.dto;

public class GenreDTO {
    private Integer id;
    private String name;
    private Long trackCount;

    public GenreDTO(Integer id, String name, Long trackCount) {
        this.id = id;
        this.name = name;
        this.trackCount = trackCount;
    }

    // Getters
    public Integer getId() { return id; }
    public String getName() { return name; }
    public Long getTrackCount() { return trackCount; }
}
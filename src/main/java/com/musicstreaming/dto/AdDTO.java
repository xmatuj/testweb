package com.musicstreaming.dto;

public class AdDTO {
    private String id;
    private String title;
    private String message;
    private String imageUrl;
    private int durationSeconds;

    public AdDTO(String id, String title, String message, String imageUrl, int durationSeconds) {
        this.id = id;
        this.title = title;
        this.message = message;
        this.imageUrl = imageUrl;
        this.durationSeconds = durationSeconds;
    }

    // Getters
    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getMessage() { return message; }
    public String getImageUrl() { return imageUrl; }
    public int getDurationSeconds() { return durationSeconds; }
}
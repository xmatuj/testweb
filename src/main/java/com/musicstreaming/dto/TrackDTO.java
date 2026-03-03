package com.musicstreaming.dto;

import com.musicstreaming.model.Track;
import com.musicstreaming.model.Artist;
import com.musicstreaming.model.Album;
import com.musicstreaming.model.Genre;
import com.musicstreaming.model.User;

public class TrackDTO {
    private Integer id;
    private String title;
    private String filePath;
    private Integer duration;
    private String formattedDuration;
    private Genre genre;
    private Album album;
    private Artist artist;
    private boolean isModerated;
    private String uploadedByUsername;
    private String coverImage;

    public TrackDTO(Track track) {
        this.id = track.getId();
        this.title = track.getTitle();
        this.filePath = track.getFilePath();
        this.duration = track.getDuration();
        this.formattedDuration = track.getFormattedDuration();
        this.genre = track.getGenre();
        this.album = track.getAlbum();
        this.artist = track.getArtist();
        this.isModerated = track.isModerated();
        this.uploadedByUsername = track.getUploadedByUser() != null ? track.getUploadedByUser().getUsername() : null;
        this.coverImage = track.getCoverImage();
    }

    // Getters
    public Integer getId() { return id; }
    public String getTitle() { return title; }
    public String getFilePath() { return filePath; }
    public Integer getDuration() { return duration; }
    public String getFormattedDuration() { return formattedDuration; }
    public Genre getGenre() { return genre; }
    public Album getAlbum() { return album; }
    public Artist getArtist() { return artist; }
    public boolean isModerated() { return isModerated; }
    public String getUploadedByUsername() { return uploadedByUsername; }
    public String getCoverImage() { return coverImage; }
}
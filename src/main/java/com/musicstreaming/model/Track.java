package com.musicstreaming.model;

public class Track {
    private Integer id;
    private String title;
    private String filePath;
    private Integer duration;
    private Integer genreId;
    private Integer albumId;
    private Integer artistId;
    private boolean isModerated;
    private Integer uploadedByUserId;

    // Related objects
    private Genre genre;
    private Album album;
    private Artist artist;
    private User uploadedByUser;

    // Constructors
    public Track() {}

    public Track(String title, String filePath, Integer duration, Integer genreId, Integer artistId) {
        this.title = title;
        this.filePath = filePath;
        this.duration = duration;
        this.genreId = genreId;
        this.artistId = artistId;
        this.isModerated = false;
    }

    // Getters and Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) { this.filePath = filePath; }

    public Integer getDuration() { return duration; }
    public void setDuration(Integer duration) { this.duration = duration; }

    public Integer getGenreId() { return genreId; }
    public void setGenreId(Integer genreId) { this.genreId = genreId; }

    public Integer getAlbumId() { return albumId; }
    public void setAlbumId(Integer albumId) { this.albumId = albumId; }

    public Integer getArtistId() { return artistId; }
    public void setArtistId(Integer artistId) { this.artistId = artistId; }

    public boolean isModerated() { return isModerated; }
    public void setModerated(boolean moderated) { isModerated = moderated; }

    public Integer getUploadedByUserId() { return uploadedByUserId; }
    public void setUploadedByUserId(Integer uploadedByUserId) { this.uploadedByUserId = uploadedByUserId; }

    public Genre getGenre() { return genre; }
    public void setGenre(Genre genre) { this.genre = genre; }

    public Album getAlbum() { return album; }
    public void setAlbum(Album album) { this.album = album; }

    public Artist getArtist() { return artist; }
    public void setArtist(Artist artist) { this.artist = artist; }

    public User getUploadedByUser() { return uploadedByUser; }
    public void setUploadedByUser(User uploadedByUser) { this.uploadedByUser = uploadedByUser; }

    // Helper method for cover image
    public String getCoverImage() {
        if (album != null && album.getCoverPath() != null && !album.getCoverPath().isEmpty()) {
            return album.getCoverPath();
        }
        if (artist != null && artist.getPhotoPath() != null && !artist.getPhotoPath().isEmpty()) {
            return artist.getPhotoPath();
        }
        return "/images/default-track-cover.jpg";
    }

    public String getFormattedDuration() {
        if (duration == null || duration <= 0) {
            return "3:45";
        }
        int minutes = duration / 60;
        int seconds = duration % 60;
        return String.format("%d:%02d", minutes, seconds);
    }
}
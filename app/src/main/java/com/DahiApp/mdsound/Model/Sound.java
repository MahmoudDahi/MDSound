package com.DahiApp.mdsound.Model;

public class Sound {
    private String title ;
    private String artistName ;
    private Long id ;
    private Long albumId ;
    private Long duration;

    public Sound(String title, String artistName, Long id, Long albumId,Long duration) {
        this.title = title;
        this.artistName = artistName;
        this.id = id;
        this.albumId = albumId;
        this.duration = duration;
    }

    public Long getDuration() {
        return duration;
    }

    public String getTitle() {
        return title;
    }

    public String getArtistName() {
        return artistName;
    }

    public Long getId() {
        return id;
    }

    public Long getAlbumId() {
        return albumId;
    }
}

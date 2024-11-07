package com.example.ElectronicDanceMusicDiscovery.model;

public class Discogs {
    private String title;
    private String artist;
    private String label;
    private String releaseDate;

    // Constructor, getters, and setters
    public Discogs(String title, String artist, String label, String releaseDate) {
        this.title = title;
        this.artist = artist;
        this.label = label;
        this.releaseDate = releaseDate;
    }

    public String getTitle() {
        return title;
    }

    public String getArtist() {
        return artist;
    }

    public String getLabel() {
        return label;
    }

    public String getReleaseDate() {
        return releaseDate;
    }
}

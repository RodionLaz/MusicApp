package com.example.musicapp.data.modle;

import java.util.List;

public class Album {
    private String albumName;
    private Artist artist;
    private List<Song> songs;

    public Album(String albumName, Artist artist, List<Song> songs) {
        this.albumName = albumName;
        this.artist = artist;
        this.songs = songs;
    }

    public List<Song> getSongs() {
        return songs;
    }

    public void setSongs(List<Song> songs) {
        this.songs = songs;
    }

    public String getAlbumName() {
        return albumName;
    }

    public void setAlbumName(String albumName) {
        this.albumName = albumName;
    }

    public Artist getArtist() {
        return artist;
    }

    public void setArtist(Artist artist) {
        this.artist = artist;
    }
}

package com.example.loginpage;

public class Song {
    final String songUrl;
    final String name;
    final String artist;
    final String poster;

    public Song(String songUrl, String name, String artist, String poster) {
        this.songUrl = songUrl;
        this.name = name;
        this.artist = artist;
        this.poster = poster;
    }
}

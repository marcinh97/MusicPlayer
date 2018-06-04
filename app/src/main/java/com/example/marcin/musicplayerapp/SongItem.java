package com.example.marcin.musicplayerapp;

class SongItem {
    private String songTitle;
    private String songAuthor;
    private int songId;

    SongItem(String songTitle, String songAuthor, int songId) {
        this.songTitle = songTitle;
        this.songAuthor = songAuthor;
        this.songId = songId;
    }

    String getSongTitle() {
        return songTitle;
    }

    String getSongAuthor() {
        return songAuthor;
    }

    int getSongId() {
        return songId;
    }

}

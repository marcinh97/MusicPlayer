package com.example.marcin.musicplayerapp;

/**
 * Created by marcin on 17.05.2018.
 */

public class SongItem {
    private String songTitle;
    private String songAuthor;
    private int songId;
    private boolean isPlaying = false;

    public SongItem(String songTitle, String songAuthor, int songId) {
        this.songTitle = songTitle;
        this.songAuthor = songAuthor;
        this.songId = songId;
    }

    public String getSongTitle() {
        return songTitle;
    }

    public String getSongAuthor() {
        return songAuthor;
    }

    public int getSongId() {
        return songId;
    }

    public boolean isPlaying() {
        return isPlaying;
    }

    public void setPlaying(boolean playing) {
        isPlaying = playing;
    }
}

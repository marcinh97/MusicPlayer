package com.example.marcin.musicplayerapp;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.media.MediaPlayer;

/**
 * Created by marcin on 18.05.2018.
 */

public class MyViewModel extends ViewModel {
    private MutableLiveData<MediaPlayer> mediaPlayer;
    public LiveData<MediaPlayer> getMediaPlayer(){
        if (mediaPlayer == null){
            mediaPlayer = new MutableLiveData<>();
        }
        return mediaPlayer;
    }
    public void saveMediaPlayer(MediaPlayer myMediaPlayer){
        mediaPlayer.setValue(myMediaPlayer);
    }
}

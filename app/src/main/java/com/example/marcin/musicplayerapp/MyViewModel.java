package com.example.marcin.musicplayerapp;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.media.MediaPlayer;

/**
 * Created by marcin on 18.05.2018.
 */

public class MyViewModel extends ViewModel {
    public MediaPlayer mediaPlayer;
    public int currentPos;

}

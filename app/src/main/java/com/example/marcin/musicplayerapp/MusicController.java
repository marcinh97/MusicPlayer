package com.example.marcin.musicplayerapp;

import android.content.Context;
import android.widget.MediaController;

/**
 * Created by marcin on 23.05.2018.
 */

public class MusicController extends MediaController {

    Context c;
    public MusicController(Context context) {
        super(context);
        c=context;
    }

    @Override
    public void hide(){}
}

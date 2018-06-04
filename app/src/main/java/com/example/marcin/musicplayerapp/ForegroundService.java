package com.example.marcin.musicplayerapp;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class ForegroundService extends Service {
    public static String MAIN_ACTION = "com.example.marcin.musicplayerapp.foregroundservice.action.main";
    public static String INIT_ACTION = "com.example.marcin.musicplayerapp.foregroundservice.action.init";
    public static String PREV_ACTION = "com.example.marcin.musicplayerapp.foregroundservice.action.prev";
    public static String PLAY_ACTION = "com.example.marcin.musicplayerapp.foregroundservice.action.play";
    public static String NEXT_ACTION = "com.example.marcin.musicplayerapp.foregroundservice.action.next";
    public static String STARTFOREGROUND_ACTION = "com.example.marcin.musicplayerapp.foregroundservice.action.startforeground";
    public static String STOPFOREGROUND_ACTION = "com.example.marcin.musicplayerapp.foregroundservice.action.stopforeground";
    public static int FOREGROUND_SERVICE = 101;
    private final IBinder mBinder = new LocalBinder();
    private static final List<SongItem> songs = new ArrayList<>(Arrays.asList(new SongItem[]{
            new SongItem("Counting stars", "One republic", R.raw.counting_stars),
            new SongItem("Get free", "Major lazer", R.raw.get_free),
            new SongItem("High", "Sir Sly", R.raw.high),
            new SongItem("Tremor", "Dmitri Vegas, Martin Garrix, Like Mike", R.raw.tremor),
            new SongItem("Sun Goes Down", "David Guetta & Showtek", R.raw.sun_goes_down),
            new SongItem("Turn up the speakers", "Dmitri Vegas, Martin Garrix, Like Mike", R.raw.turn_up)
    }));

    private int currentPosition = 0;
    private MediaPlayer mediaPlayer;


    @Override
    public void onCreate() {
        super.onCreate();
        currentPosition = 0;
        mediaPlayer = new MediaPlayer();

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String action = intent.getAction();
        if (action.equals(STARTFOREGROUND_ACTION)){
            int pos = intent.getIntExtra("SONG_NUMBER", 0);
            int toSeek = intent.getIntExtra("MEDIA_PLAYER_POSITION", 0);
            currentPosition = pos;
            mediaPlayer = MediaPlayer.create(getApplicationContext(), songs.get(pos).getSongId());
            mediaPlayer.seekTo(toSeek);
            mediaPlayer.start();
            showNotification();
        }
        else if (action.equals(PREV_ACTION)){
            if (mediaPlayer != null){
                mediaPlayer.stop();
            }
            if (currentPosition == 0){
                currentPosition = songs.size()-1;
            }
            else {
                currentPosition--;
            }
            mediaPlayer = MediaPlayer.create(getApplicationContext(), songs.get(currentPosition).getSongId());
            mediaPlayer.start();
            showNotification();

        }
        else if(action.equals(PLAY_ACTION)){
            if (mediaPlayer != null){
                mediaPlayer.stop();
            }
            mediaPlayer = MediaPlayer.create(getApplicationContext(), songs.get(currentPosition%songs.size()).getSongId());
            mediaPlayer.start();
        }
        else if(action.equals(NEXT_ACTION)){
            if (mediaPlayer != null) {
                mediaPlayer.stop();

            }
            currentPosition++;
            mediaPlayer = MediaPlayer.create(getApplicationContext(), songs.get(currentPosition%songs.size()).getSongId());
            mediaPlayer.start();
            showNotification();
        }
        else if(action.equals(STOPFOREGROUND_ACTION)){
            stopForeground(true);
        }

        return START_STICKY;
    }

    private void showNotification() {
        Intent notificationIntent = new Intent(this, MainActivity.class);
        notificationIntent.setAction(MAIN_ACTION);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                notificationIntent, 0);

        Intent previousIntent = new Intent(this, ForegroundService.class);
        previousIntent.setAction(PREV_ACTION);
        PendingIntent ppreviousIntent = PendingIntent.getService(this, 0,
                previousIntent, 0);

        Intent playIntent = new Intent(this, ForegroundService.class);
        playIntent.setAction(PLAY_ACTION);
        PendingIntent pplayIntent = PendingIntent.getService(this, 0,
                playIntent, 0);

        Intent nextIntent = new Intent(this, ForegroundService.class);
        nextIntent.setAction(NEXT_ACTION);
        PendingIntent pnextIntent = PendingIntent.getService(this, 0,
                nextIntent, 0);

        SongItem song = songs.get(currentPosition%songs.size());

        Notification notification = new NotificationCompat.Builder(this)
                .setContentTitle(song.getSongTitle())
                .setTicker(song.getSongAuthor())
                .setContentText(song.getSongAuthor())
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentIntent(pendingIntent)
                .setOngoing(true)
                .addAction(android.R.drawable.ic_media_previous, "Previous",
                        ppreviousIntent)
                .addAction(android.R.drawable.ic_media_play, "Play",
                        pplayIntent)
                .addAction(android.R.drawable.ic_media_next, "Next",
                        pnextIntent).build();
        startForeground(FOREGROUND_SERVICE,
                notification);

    }
    public void onDestroy(){
        super.onDestroy();
        mediaPlayer.stop();
        mediaPlayer.release();
    }

    class LocalBinder extends Binder {
        ForegroundService getService() {
            return ForegroundService.this;
        }
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {

        return super.onUnbind(intent);
    }
}

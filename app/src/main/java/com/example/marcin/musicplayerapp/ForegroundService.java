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

import java.util.List;


public class ForegroundService extends Service {
    public static String MAIN_ACTION = "com.example.marcin.musicplayerapp.foregroundservice.action.main";
    public static String PREV_ACTION = "com.example.marcin.musicplayerapp.foregroundservice.action.prev";
    public static String PLAY_ACTION = "com.example.marcin.musicplayerapp.foregroundservice.action.play";
    public static String NEXT_ACTION = "com.example.marcin.musicplayerapp.foregroundservice.action.next";
    public static String STARTFOREGROUND_ACTION = "com.example.marcin.musicplayerapp.foregroundservice.action.startforeground";
    public static String STOPFOREGROUND_ACTION = "com.example.marcin.musicplayerapp.foregroundservice.action.stopforeground";
    public static int FOREGROUND_SERVICE = 101;
    private final IBinder mBinder = new LocalBinder();
    private static final List<SongItem> songs = MainActivity.songs;

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
        assert action != null;
        if (action.equals(STARTFOREGROUND_ACTION)){
            startMediaPlayer(intent);
            showNotification();
        }
        else if (action.equals(PREV_ACTION)){
            playPreviousSong();
            showNotification();

        }
        else if(action.equals(PLAY_ACTION)){
            restartSong();
        }
        else if(action.equals(NEXT_ACTION)){
            playNextSong();
            showNotification();
        }
        else if(action.equals(STOPFOREGROUND_ACTION)){
            stopForeground(true);
        }
        return START_STICKY;
    }

    private void playNextSong() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();

        }
        currentPosition++;
        mediaPlayer = MediaPlayer.create(getApplicationContext(), songs.get(currentPosition%songs.size()).getSongId());
        mediaPlayer.start();
    }

    private void restartSong() {
        if (mediaPlayer != null){
            mediaPlayer.stop();
        }
        mediaPlayer = MediaPlayer.create(getApplicationContext(), songs.get(currentPosition%songs.size()).getSongId());
        mediaPlayer.start();
    }

    private void playPreviousSong() {
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
    }

    private void startMediaPlayer(Intent intent) {
        int pos = intent.getIntExtra(MainActivity.SONG_NUMBER, 0);
        int toSeek = intent.getIntExtra(MainActivity.MEDIA_PLAYER_POSITION, 0);
        currentPosition = pos;
        mediaPlayer = MediaPlayer.create(getApplicationContext(), songs.get(pos).getSongId());
        mediaPlayer.seekTo(toSeek);
        mediaPlayer.start();
    }

    private void showNotification() {
        Intent notificationIntent = new Intent(this, MainActivity.class);
        Intent previousIntent = new Intent(this, ForegroundService.class);
        Intent playIntent = new Intent(this, ForegroundService.class);
        Intent nextIntent = new Intent(this, ForegroundService.class);

        notificationIntent.setAction(MAIN_ACTION);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        previousIntent.setAction(PREV_ACTION);
        playIntent.setAction(PLAY_ACTION);
        nextIntent.setAction(NEXT_ACTION);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                notificationIntent, 0);
        PendingIntent ppreviousIntent = PendingIntent.getService(this, 0,
                previousIntent, 0);
        PendingIntent pplayIntent = PendingIntent.getService(this, 0,
                playIntent, 0);
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
                .addAction(android.R.drawable.ic_media_previous, getString(R.string.previous),
                        ppreviousIntent)
                .addAction(android.R.drawable.ic_media_play, getString(R.string.replay),
                        pplayIntent)
                .addAction(android.R.drawable.ic_media_next, getString(R.string.next),
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

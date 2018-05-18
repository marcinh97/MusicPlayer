package com.example.marcin.musicplayerapp;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;

/**
 * Created by marcin on 17.05.2018.
 */

public class ForegroundService extends Service {
    private static final String LOG_TAG = "ForegroundService";
    public static boolean IS_SERVICE_RUNNING = false;
    public static String MAIN_ACTION = "com.example.marcin.musicplayerapp.foregroundservice.action.main";
    public static String INIT_ACTION = "com.example.marcin.musicplayerapp.foregroundservice.action.init";
    public static String PREV_ACTION = "com.example.marcin.musicplayerapp.foregroundservice.action.prev";
    public static String PLAY_ACTION = "com.example.marcin.musicplayerapp.foregroundservice.action.play";
    public static String NEXT_ACTION = "com.example.marcin.musicplayerapp.foregroundservice.action.next";
    public static String STARTFOREGROUND_ACTION = "com.example.marcin.musicplayerapp.foregroundservice.action.startforeground";
    public static String STOPFOREGROUND_ACTION = "com.example.marcin.musicplayerapp.foregroundservice.action.stopforeground";
    public static int FOREGROUND_SERVICE = 101;
//    private final IBinder mBinder = new LocalBinder();

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String action = intent.getAction();
        if (action.equals(STARTFOREGROUND_ACTION)){
            Toast.makeText(this, "Service Started!", Toast.LENGTH_SHORT).show();
            showNotification();
        }
        else if (action.equals(PREV_ACTION)){
            Toast.makeText(this, "Clicked Previous!", Toast.LENGTH_SHORT)
                    .show();
        }
        else if(action.equals(PLAY_ACTION)){
            Toast.makeText(this, "Clicked Play!", Toast.LENGTH_SHORT).show();
        }
        else if(action.equals(NEXT_ACTION)){
            Toast.makeText(this, "Clicked Next!", Toast.LENGTH_SHORT).show();
        }
        else if(action.equals(STOPFOREGROUND_ACTION)){
            stopForeground(true);
            //stopSelf();
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


        Notification notification = new NotificationCompat.Builder(this)
                .setContentTitle("TutorialsFace Music Player")
                .setTicker("TutorialsFace Music Player")
                .setContentText("My song")
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
        Toast.makeText(this, "Service Detroyed!", Toast.LENGTH_SHORT).show();
    }

//    public class LocalBinder extends Binder {
//        ForegroundService getService() {
//            // Return this instance of LocalService so clients can call public methods
//            return ForegroundService.this;
//        }
//    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


}

package com.example.marcin.musicplayerapp;

import android.annotation.SuppressLint;
import android.arch.lifecycle.ViewModelProviders;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Configuration;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.PersistableBundle;
import android.support.annotation.RequiresApi;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private ImageButton playSongButton;
    private SeekBar songPointBar;
    private TextView timeFromStart;
    private TextView timeToEnd;
    private MediaPlayer mediaPlayer;
    private SongAdapter adapter;
    private RecyclerView recyclerView;
    private boolean isPlaying;

    private ImageView fastforwardButton;
    private ImageView rewindButton;

    private PopupWindow authorImagePopup;

    private ForegroundService myService = null;

    private static final String IS_PLAYING = "IS_PLAYING";

    private MyViewModel myViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initViews();

        if (savedInstanceState != null && savedInstanceState.containsKey(IS_PLAYING)){
            isPlaying = savedInstanceState.getBoolean(IS_PLAYING);
            Toast.makeText(this, Boolean.toString(isPlaying), Toast.LENGTH_SHORT).show();

        }
        else{
            isPlaying = false;
        }

//        Toast.makeText(this, "Is playing? "+Boolean.toString(isPlaying), Toast.LENGTH_SHORT).show();

        android.support.v7.widget.Toolbar toolbar = findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar);


        final List<SongItem> songs = new ArrayList<>();
        songs.add(new SongItem("Tremor", "Dmitri Vegas, Martin Garrix, Like Mike", R.raw.tremor));
        songs.add(new SongItem("Sun Goes Down", "David Guetta & Showtek", R.raw.sun_goes_down));
        songs.add(new SongItem("Turn up the speakers", "Dmitri Vegas, Martin Garrix, Like Mike", R.raw.turn_up));

        adapter = new SongAdapter(this, songs);
        setLayoutForRecyclerView();
        recyclerView.setAdapter(adapter);


        adapter.setSongOnItemClickListener(new SongAdapter.SongOnItemClickListener() {
            @Override
            public void onItemClick(ImageButton playButton, View view, final SongItem songItem, int position) {
                if (songItem.isPlaying()){
                    songItem.setPlaying(false);
                    mediaPlayer.stop();
                    mediaPlayer.reset();
                    mediaPlayer.release();
                    mediaPlayer = null;
                    isPlaying = false;
//                    Toast.makeText(MainActivity.this, "Stop "+songItem.getSongTitle(), Toast.LENGTH_SHORT).show();
                }
                else{
                    if (isPlaying) {
                        for (SongItem song : songs)
                            song.setPlaying(false);
                        mediaPlayer.stop();
                        mediaPlayer.reset();
                        mediaPlayer.release();
                    }
                    mediaPlayer = MediaPlayer.create(getApplicationContext(), songItem.getSongId());
                    songItem.setPlaying(true);
                    mediaPlayer.start();

//                    Toast.makeText(MainActivity.this, songItem.getSongTitle(), Toast.LENGTH_SHORT).show();

                    isPlaying = true;
                    mediaPlayer.setLooping(true);
                    mediaPlayer.seekTo(0);
                    mediaPlayer.setVolume(0.5f, 0.5f);
                    int totalTime = mediaPlayer.getDuration();
                    songPointBar.setProgress(0);
                    songPointBar.setMax(totalTime);

                }
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        while(mediaPlayer != null){
                            try{
                                Message message = new Message();
                                message.what = mediaPlayer.getCurrentPosition();
                                handler.sendMessage(message);
                                Thread.sleep(1000);
                            }
                            catch (InterruptedException e){
                                e.printStackTrace();
                            }
                        }
                    }
                }).start();
//                Toast.makeText(MainActivity.this, "Is playin??? "+Boolean.toString(isPlaying), Toast.LENGTH_SHORT).show();
            }
        });

//        playSongButton = findViewById(R.id.play_button);
//        playSongButton.setOnClickListener();




        songPointBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    mediaPlayer.seekTo(progress);
                    songPointBar.setProgress(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        final int MILISECONDS_CHANGE = 10000;

        fastforwardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mediaPlayer != null) {
                    int position = mediaPlayer.getCurrentPosition();
                    mediaPlayer.seekTo(position + MILISECONDS_CHANGE);
                    songPointBar.setProgress(position + MILISECONDS_CHANGE);
                }
            }
        });

        rewindButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mediaPlayer != null){
                    int position = mediaPlayer.getCurrentPosition();
                    mediaPlayer.seekTo(position - MILISECONDS_CHANGE);
                    songPointBar.setProgress(position - MILISECONDS_CHANGE);
                }
            }
        });

        final Button button = findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent service = new Intent(MainActivity.this, ForegroundService.class);
                if (!ForegroundService.IS_SERVICE_RUNNING) {
                    service.setAction(ForegroundService.STARTFOREGROUND_ACTION);
                    ForegroundService.IS_SERVICE_RUNNING = true;
                    button.setText("Stop Service");
                } else {
                    service.setAction(ForegroundService.STOPFOREGROUND_ACTION);
                    ForegroundService.IS_SERVICE_RUNNING = false;
                    button.setText("Start Service");

                }
                startService(service);
            }
        });

//        startService(new Intent(this, ForegroundService.class));
//        bindService(new Intent(this,
//                ForegroundService.class), mConnection, Context.BIND_AUTO_CREATE);
//


        myViewModel = ViewModelProviders.of(this).get(MyViewModel.class);
        mediaPlayer = myViewModel.getMediaPlayer().getValue();

        Toast.makeText(this, "Is mediaPlayer null? "+Boolean.toString(mediaPlayer==null), Toast.LENGTH_SHORT).show();
    }

//    private ServiceConnection mConnection = new ServiceConnection() {
//        @Override
//        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
//            myService = ((ForegroundService.LocalBinder) iBinder).getService();
//        }
//
//        @Override
//        public void onServiceDisconnected(ComponentName componentName) {
//            myService = null;
//        }
//    };

    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            int currentPosition = msg.what;
            songPointBar.setProgress(currentPosition);
        }
    };

    private void setLayoutForRecyclerView() {
        RecyclerView.LayoutManager recyclerLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(recyclerLayoutManager);
        recyclerView.setHasFixedSize(true);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
    }

    private void initViews(){
        playSongButton = findViewById(R.id.play_button);
        songPointBar = findViewById(R.id.song_point_sBar);
        timeFromStart = findViewById(R.id.time_from_start_tV);
        timeToEnd = findViewById(R.id.time_to_end_tV);
        recyclerView = findViewById(R.id.main_recyclerView);
        fastforwardButton = findViewById(R.id.fastforward_imView);
        rewindButton = findViewById(R.id.rewind_imView);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.about_author:
                showAuthorImagePopup();
                break;
            case R.id.settings:
                Toast.makeText(this, "Tu beda ustawienia", Toast.LENGTH_SHORT).show();
                break;
            default:
                Toast.makeText(getApplicationContext(), "Cos zle", Toast.LENGTH_LONG).show();
        }
        return true;
    }

    private void showAuthorImagePopup(){
        final float SHADOW_BEHIND_POPUP_LEVEL = 10;
        final ConstraintLayout constraintLayout = findViewById(R.id.constraintLayout);
        int orientation = getResources().getConfiguration().orientation;
        final double POPUP_WINDOW_RATIO = 0.7;
        final int bottomPositionOfImageCenter = orientation == Configuration.ORIENTATION_LANDSCAPE ?
                (int)(getResources().getDimension(R.dimen.bottom_position_of_image_center)) : 0;

        LayoutInflater layoutInflater;
        layoutInflater = (LayoutInflater) getApplicationContext().getSystemService(LAYOUT_INFLATER_SERVICE);
        @SuppressLint("InflateParams") ViewGroup container = (ViewGroup) (layoutInflater != null ?
                layoutInflater.inflate(R.layout.author_info, null) : null);

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

        authorImagePopup = new PopupWindow(container, (int)(POPUP_WINDOW_RATIO *displayMetrics.widthPixels),
                (int)(POPUP_WINDOW_RATIO *displayMetrics.heightPixels), true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            authorImagePopup.setElevation(SHADOW_BEHIND_POPUP_LEVEL);
        }

        showAuthorImagePopupOnScreen(constraintLayout, bottomPositionOfImageCenter);

        allowUserToDismissPopupByClicking(container);
    }

    private void showAuthorImagePopupOnScreen(ConstraintLayout layout, int positionOfImage){
        authorImagePopup.showAtLocation(layout, Gravity.CENTER, 0, positionOfImage);
    }

    private void allowUserToDismissPopupByClicking(ViewGroup container){
        assert container != null;
        container.setOnTouchListener(new View.OnTouchListener() {
            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                authorImagePopup.dismiss();
                return true;
            }
        });
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(IS_PLAYING, isPlaying);
        myViewModel.saveMediaPlayer(mediaPlayer);

    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    //    @Override
//    protected void onDestroy() {
//        super.onDestroy();
//        if (myService != null)
//            unbindService(mConnection);
//    }
}

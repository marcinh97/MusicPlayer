package com.example.marcin.musicplayerapp;

import android.annotation.SuppressLint;
import android.arch.lifecycle.ViewModelProviders;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Configuration;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
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
import android.support.v7.widget.helper.ItemTouchHelper;
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
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class MainActivity extends AppCompatActivity implements SensorEventListener{

    private ImageView playSongButton;
    private SeekBar songPointBar;
    private TextView timeFromStart;
    private TextView timeToEnd;
    private MediaPlayer mediaPlayer;
    private MyAdapter adapter;
    private RecyclerView recyclerView;
    private boolean isPlaying;

    private ImageView fastforwardButton;
    private ImageView rewindButton;

    private PopupWindow authorImagePopup;

    private ForegroundService myService = null;

    private static final String IS_PLAYING = "IS_PLAYING";
    private static final String IS_SENSOR_ALLOWED = "IS_SENSOR_ALLOWED";
    private MyViewModel myViewModel;

    private int pauseCurrentPos;

    private static final int MILISECONDS_CHANGE = 10000;
    private SensorManager manager;
    private Sensor sensor;

    private boolean isStoppedBySensor;
    private boolean isSensorAllowed;


    private Intent AAAplayIntent;

    private MusicService musicSrv;
    private Intent playIntent;
    private boolean musicBound = false;

    private int currentlyPlayedPosition;
    private int serviceMediaPlayerPosition;

    private static final List<SongItem> songs = new ArrayList<>(Arrays.asList(new SongItem[]{
            new SongItem("Counting stars", "One republic", R.raw.counting_stars),
            new SongItem("Get free", "Major lazer", R.raw.get_free),
            new SongItem("High", "Sir Sly", R.raw.high),
            new SongItem("Tremor", "Dmitri Vegas, Martin Garrix, Like Mike", R.raw.tremor),
            new SongItem("Sun Goes Down", "David Guetta & Showtek", R.raw.sun_goes_down),
            new SongItem("Turn up the speakers", "Dmitri Vegas, Martin Garrix, Like Mike", R.raw.turn_up)
    }));

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        initMediaPlayer(savedInstanceState);
        if (savedInstanceState!=null && savedInstanceState.containsKey(IS_SENSOR_ALLOWED))
            isSensorAllowed = savedInstanceState.getBoolean(IS_SENSOR_ALLOWED);
        else
            isSensorAllowed = false;
        initAdapter();
        initBasicPlayButton();
        initListeners();

        setToolbarMenu();
        getSeekBarStatus();



        manager = (SensorManager)getSystemService(SENSOR_SERVICE);
        assert manager != null;
        sensor = manager.getDefaultSensor(Sensor.TYPE_PROXIMITY);

        findViewById(R.id.button3).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int ab = 2;
            }
        });

        Button myButton = findViewById(R.id.button);
        myButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                playIntent = new Intent(MainActivity.this, ForegroundService.class);
                playIntent.setAction(ForegroundService.STARTFOREGROUND_ACTION);
                startService(playIntent);
                mediaPlayer.stop();
            }
        });

//
//        startService(new Intent(this, ForegroundService.class));
//        bindService(new Intent(this,
//                ForegroundService.class), mConnection, Context.BIND_AUTO_CREATE);

    }
//
//
//    @Override
//    protected void onStart() {
//        super.onStart();
//        if(playIntent==null){
//            playIntent = new Intent(this, MySongForegroundService.class);
//            bindService(playIntent, musicConnection, Context.BIND_AUTO_CREATE);
//            startService(playIntent);
//        }
//    }

    public void songPicked(View view){
        musicSrv.setSong(Integer.parseInt(view.getTag().toString()));
        musicSrv.playSong();
    }

    private ServiceConnection musicConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MusicService.MusicBinder binder = (MusicService.MusicBinder)service;
            musicSrv = binder.getService();
            musicSrv.setList(songs);
            musicBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            musicBound = false;
        }
    };

    @Override
    protected void onStart() {
        super.onStart();
//        if (playIntent == null){
//            playIntent = new Intent(this, MusicService.class);
//            bindService(playIntent, musicConnection, Context.BIND_AUTO_CREATE);
//            startService(playIntent);
//        }
    }



    private void initListeners() {
        playSongButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startMediaPlayer();
            }
        });

        fastforwardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                arrowsOnClick(true);
            }
        });

        rewindButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                arrowsOnClick(false);
            }
        });
    }

    private void arrowsOnClick(boolean isFastForward){
        if (mediaPlayer != null){
            int position = mediaPlayer.getCurrentPosition();
            int afterClick = isFastForward ? position + MILISECONDS_CHANGE : position - MILISECONDS_CHANGE;
            mediaPlayer.seekTo(afterClick);
            songPointBar.setProgress(afterClick);
        }
    }

    private void startMediaPlayer() {
        if (!mediaPlayer.isPlaying()) {
            mediaPlayer.seekTo(pauseCurrentPos);
            mediaPlayer.start();
            playSongButton.setImageResource(android.R.drawable.ic_media_pause);
        } else {
            mediaPlayer.pause();
            playSongButton.setImageResource(android.R.drawable.ic_media_play);
        }
        pauseCurrentPos = mediaPlayer.getCurrentPosition();
    }

    private void initBasicPlayButton() {
        if (mediaPlayer.isPlaying()) {
            playSongButton.setImageResource(android.R.drawable.ic_media_pause);
        } else {
            playSongButton.setImageResource(android.R.drawable.ic_media_play);
        }
    }

    private void initAdapter() {
        adapter = new MyAdapter(songs, getApplicationContext());
        setLayoutForRecyclerView();
        recyclerView.setAdapter(adapter);
    }

    private void initMediaPlayer(Bundle savedInstanceState) {
        myViewModel = ViewModelProviders.of(this).get(MyViewModel.class);
        if (savedInstanceState == null) {
            mediaPlayer = MediaPlayer.create(getApplicationContext(), songs.get(0).getSongId());
        } else {
            mediaPlayer = myViewModel.mediaPlayer;
            pauseCurrentPos = myViewModel.currentPos;
        }
    }

    private void setToolbarMenu() {
        android.support.v7.widget.Toolbar toolbar = findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar);
    }

        private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            myService = ((ForegroundService.LocalBinder) iBinder).getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            myService = null;
        }
    };


    public void getSeekBarStatus() {
        new Thread(new Runnable() {

            @Override
            public void run() {
                int currentPosition = 0;
                int total = mediaPlayer.getDuration();
                songPointBar.setMax(total);
                while (mediaPlayer != null && currentPosition < total) {
                    try {
                        Thread.sleep(1000);
                        currentPosition = mediaPlayer.getCurrentPosition();
                    } catch (InterruptedException e) {
                        return;
                    }
                    pauseCurrentPos = currentPosition;
                    songPointBar.setProgress(currentPosition);

                }
            }
        }).start();
        songPointBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(final SeekBar seekBar, int ProgressValue, boolean fromUser) {
                if (fromUser) {
                    mediaPlayer.seekTo(ProgressValue);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
    }

    private void setLayoutForRecyclerView() {
        RecyclerView.LayoutManager recyclerLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(recyclerLayoutManager);
        recyclerView.setHasFixedSize(true);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
    }

    private void initViews() {
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

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.about_author:
                showAuthorImagePopup();
                break;
            case R.id.by_title:
                Collections.sort(songs, new Comparator<SongItem>() {
                    @Override
                    public int compare(SongItem m1, SongItem m2) {
                        return m1.getSongTitle().compareToIgnoreCase(m2.getSongTitle());
                    }
                });
                adapter.notifyDataSetChanged();
                break;
            case R.id.by_author:
                Collections.sort(songs, new Comparator<SongItem>() {
                    @Override
                    public int compare(SongItem m1, SongItem m2) {
                        return m1.getSongAuthor().compareToIgnoreCase(m2.getSongAuthor());
                    }
                });
                adapter.notifyDataSetChanged();
                break;
            case R.id.prox_sensor:
                if (isSensorAllowed)
                    isSensorAllowed = false;
                else
                    isSensorAllowed = true;
                break;
            case R.id.background:
                playIntent = new Intent(MainActivity.this, ForegroundService.class);
                playIntent.setAction(ForegroundService.STARTFOREGROUND_ACTION);
                playIntent.putExtra("SONG_NUMBER", currentlyPlayedPosition);
//                Toast.makeText(MainActivity.this, "Pos"+Integer.toString(currentlyPlayedPosition), Toast.LENGTH_SHORT).show();
                int currPos = mediaPlayer.getCurrentPosition();
                Toast.makeText(this, Integer.toString(currPos), Toast.LENGTH_SHORT).show();
                playIntent.putExtra("MEDIA_PLAYER_POSITION", mediaPlayer.getCurrentPosition());
                startService(playIntent);
//                Toast.makeText(this, "Player"+Integer.toString(mediaPlayer.getCurrentPosition()), Toast.LENGTH_SHORT).show();
                mediaPlayer.stop();
                break;
            default:
                Toast.makeText(getApplicationContext(), "Cos zle", Toast.LENGTH_LONG).show();
        }
        return true;
    }

    private void showAuthorImagePopup() {
        final float SHADOW_BEHIND_POPUP_LEVEL = 10;
        final ConstraintLayout constraintLayout = findViewById(R.id.constraintLayout);
        int orientation = getResources().getConfiguration().orientation;
        final double POPUP_WINDOW_RATIO = 0.7;
        final int bottomPositionOfImageCenter = orientation == Configuration.ORIENTATION_LANDSCAPE ?
                (int) (getResources().getDimension(R.dimen.bottom_position_of_image_center)) : 0;

        LayoutInflater layoutInflater;
        layoutInflater = (LayoutInflater) getApplicationContext().getSystemService(LAYOUT_INFLATER_SERVICE);
        @SuppressLint("InflateParams") ViewGroup container = (ViewGroup) (layoutInflater != null ?
                layoutInflater.inflate(R.layout.author_info, null) : null);

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

        authorImagePopup = new PopupWindow(container, (int) (POPUP_WINDOW_RATIO * displayMetrics.widthPixels),
                (int) (POPUP_WINDOW_RATIO * displayMetrics.heightPixels), true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            authorImagePopup.setElevation(SHADOW_BEHIND_POPUP_LEVEL);
        }

        showAuthorImagePopupOnScreen(constraintLayout, bottomPositionOfImageCenter);

        allowUserToDismissPopupByClicking(container);
    }

    private void showAuthorImagePopupOnScreen(ConstraintLayout layout, int positionOfImage) {
        authorImagePopup.showAtLocation(layout, Gravity.CENTER, 0, positionOfImage);
    }

    private void allowUserToDismissPopupByClicking(ViewGroup container) {
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
        outState.putBoolean(IS_SENSOR_ALLOWED, isSensorAllowed);
        myViewModel.mediaPlayer = mediaPlayer;
        myViewModel.currentPos = pauseCurrentPos;
//        if((Integer)musicService.getPosition()!=null)
//            outState.putInt("position",musicService.getPosition());
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
//        savedInstanceState.getInt("position");
    }

    @Override
    protected void onResume() {
        super.onResume();
        manager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL);

    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder> {
        private List<SongItem> songs;
        private Context context;

        public MyAdapter(List<SongItem> songs, Context context) {
            this.songs = songs;
            this.context = context;
        }

        class MyViewHolder extends RecyclerView.ViewHolder {
            TextView songTitle;
            TextView songAuthor;
            ImageView playSongButton;

            public MyViewHolder(View itemView) {
                super(itemView);
                songTitle = itemView.findViewById(R.id.song_title_tV);
                songAuthor = itemView.findViewById(R.id.song_author_tV);
                playSongButton = itemView.findViewById(R.id.play_song_btn);
            }
        }

        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = getView(parent);
            return new MyViewHolder(view);
        }

        @Override
        public void onBindViewHolder(MyViewHolder holder, final int position) {
            final int pos = holder.getAdapterPosition();
            final SongItem song = songs.get(pos);
            holder.songTitle.setText(song.getSongTitle());
            holder.songAuthor.setText(song.getSongAuthor());
            holder.playSongButton.setImageResource(android.R.drawable.ic_media_play);
            holder.playSongButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mediaPlayer != null) {
                        mediaPlayer.stop();
                        isPlaying = false;
                    }
                    mediaPlayer = MediaPlayer.create(getApplicationContext(), song.getSongId());
                    songPointBar.setMax(mediaPlayer.getDuration());
                    getSeekBarStatus();
                    mediaPlayer.start();
                    isPlaying = true;
                    playSongButton.setImageResource(android.R.drawable.ic_media_pause);
                    currentlyPlayedPosition = position;
                }

            });
        }

        private View getView(ViewGroup parent) {
            LayoutInflater inflater = LayoutInflater.from(context);
            return inflater.inflate(R.layout.song_row, parent, false);
        }

        @Override
        public int getItemCount() {
            return songs.size();
        }
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
//        Toast.makeText(this, "Is allowed on changed"+Boolean.toString(isSensorAllowed), Toast.LENGTH_SHORT).show();
        if (isSensorAllowed && mediaPlayer!=null) {
            Sensor currentSensor = sensorEvent.sensor;
            if (currentSensor.getType() == Sensor.TYPE_PROXIMITY) {
                float val = sensorEvent.values[0];
                if (val == 0) {
                    mediaPlayer.pause();
                    isStoppedBySensor = true;
                    playSongButton.setImageResource(android.R.drawable.ic_media_play);
                } else if (isStoppedBySensor && val > 0) {
                    mediaPlayer.start();
                    isPlaying = true;
                    isStoppedBySensor = false;
                    playSongButton.setImageResource(android.R.drawable.ic_media_pause);
                }
            }
        }
    }


    @Override
    protected void onDestroy() {
        if (playIntent != null)
            stopService(playIntent);
        musicSrv = null;
        super.onDestroy();
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
    }


}
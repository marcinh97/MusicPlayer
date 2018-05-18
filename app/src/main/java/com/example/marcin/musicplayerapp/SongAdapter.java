package com.example.marcin.musicplayerapp;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

/**
 * Created by marcin on 17.05.2018.
 */

public class SongAdapter extends RecyclerView.Adapter<SongAdapter.SongHolder> {
    private TextView songTitle;
    private TextView songAuthor;
    private ImageButton playSongButton;
    private List<SongItem> songs;
    private SongItem song;
    private Context context;

    SongOnItemClickListener songOnItemClickListener;

    public SongAdapter(Context context, List<SongItem> songs) {
        this.songs = songs;
        this.context = context;
    }

    public interface SongOnItemClickListener{
        void onItemClick(ImageButton playButton, View view, SongItem songItem, int position);
    }

    public void setSongOnItemClickListener(SongOnItemClickListener songOnItemClickListener){
        this.songOnItemClickListener = songOnItemClickListener;
    }

    @Override
    public void onBindViewHolder(SongHolder songHolder, int position) {
        final int pos = songHolder.getAdapterPosition();
        song = songs.get(pos);
//        Toast.makeText(context, song.getSongTitle()+Integer.toString(position), Toast.LENGTH_SHORT).show();
        initViewsFromSongHolder(songHolder);
        setViewsValues();
        playSongButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SongItem currentSong = songs.get(pos);
                if (songOnItemClickListener != null) {
                    songOnItemClickListener.onItemClick(playSongButton, view, currentSong, pos);
                }
//                Toast.makeText(context, songs.get(pos).getSongTitle()+Integer.toString(pos), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void initViewsFromSongHolder(SongHolder songHolder){
        songTitle = songHolder.songTitle;
        songAuthor = songHolder.songAuthor;
        playSongButton = songHolder.playSongButton;
    }

    private void setViewsValues(){
        songTitle.setText(song.getSongTitle());
        songAuthor.setText(song.getSongAuthor());
    }
    @Override
    public SongHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = getView(parent);
        return new SongHolder(view);
    }

    private View getView(ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(context);
        return inflater.inflate(R.layout.song_row, parent, false);
    }

    @Override
    public int getItemCount() {
        return songs.size();
    }

    class SongHolder extends RecyclerView.ViewHolder{
        TextView songTitle;
        TextView songAuthor;
        ImageButton playSongButton;

        public SongHolder(View itemView) {
            super(itemView);
            initViews(itemView);
        }

        private void initViews(View itemView){
            songTitle = itemView.findViewById(R.id.song_title_tV);
            songAuthor = itemView.findViewById(R.id.song_author_tV);
            playSongButton = itemView.findViewById(R.id.play_song_btn);
        }
    }
}

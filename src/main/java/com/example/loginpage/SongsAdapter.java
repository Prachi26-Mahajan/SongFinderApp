package com.example.loginpage;

import android.app.ProgressDialog;
import android.content.Context;
import android.media.AudioManager;
import android.media.Image;
import android.media.MediaPlayer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.google.android.material.imageview.ShapeableImageView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.IOException;
import java.util.ArrayList;

public class SongsAdapter extends RecyclerView.Adapter<SongsAdapter.SongView> {

    private final ArrayList<Song> songs = new ArrayList<Song>();
    private final Context context;
    final String URL_ALL_SONGS = "https://mock-songs-api.herokuapp.com/get_songs";
    final String URL_SONG_SEARCH = "https://mock-songs-api.herokuapp.com/filter_songs?song_name=";
    MediaPlayer player  = new MediaPlayer();
    int currentSongPosition;

    public SongsAdapter(Context context) {
        this.context = context;

        player.setOnBufferingUpdateListener(new MediaPlayer.OnBufferingUpdateListener() {
            @Override
            public void onBufferingUpdate(MediaPlayer mediaPlayer, int i) {
                Toast.makeText(context, "loading song...", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void filterSongs(String keyword) {

        // clear songs before search
        songs.clear();

        final ProgressDialog dialog = new ProgressDialog(context);
        final RequestQueue queue = Volley.newRequestQueue(context);
        dialog.setTitle("Searching for '" + keyword + "'");
        dialog.show();

        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, URL_SONG_SEARCH + keyword, null, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                for (int i = 0; i < response.length(); i++) {
                    try {
                        JSONObject song = response.getJSONObject(i);
                        String name = song.getString("name");
                        String artist = song.getString("artist");
                        String songUrl = song.getString("song_url");
                        String poster = song.getString("poster");
                        songs.add(new Song(songUrl, name, artist, poster));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }

                // update data
                notifyDataSetChanged();
                dialog.dismiss();

            }
        },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(context, "Error Occurred..🤔", Toast.LENGTH_SHORT).show();
                    }
                });

        // add request to queue
        queue.add(request);
    }

    // Calls [Mock Song Api] get all songs and convert into in [Song]
    public void getAllSongs() {
        final ProgressDialog dialog = new ProgressDialog(context);
        final RequestQueue queue = Volley.newRequestQueue(context);
        dialog.setMessage("Loading Songs 🎶");
        dialog.show();

        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, URL_ALL_SONGS, null, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                for (int i = 0; i < response.length(); i++) {
                    try {
                        JSONObject song = response.getJSONObject(i);
                        String name = song.getString("name");
                        String artist = song.getString("artist");
                        String songUrl = song.getString("song_url");
                        String poster = song.getString("poster");
                        songs.add(new Song(songUrl, name, artist, poster));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }

                // update data
                notifyDataSetChanged();
                dialog.dismiss();

            }
        },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(context, "Error Occurred..🤔", Toast.LENGTH_SHORT).show();
                    }
                });

        // add request to queue
        queue.add(request);
    }


    @NonNull
    @Override
    public SongView onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View songTile = inflater.inflate(R.layout.song_tile, parent, false);
        SongView songView = new SongView(songTile);

        return songView;
    }

    private void playSong(String songUrl,ImageButton button){
        player.setAudioStreamType(AudioManager.STREAM_MUSIC);
        try {
            player.setDataSource(songUrl);
            ProgressDialog dialog = new ProgressDialog(context);
            dialog.setMessage("loading song...");
            dialog.show();

            player.prepareAsync();

            player.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mediaPlayer) {
                    player.start();
                    dialog.dismiss();
                }
            });
        } catch (IOException e) {
            Toast.makeText(context, e.toString(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onBindViewHolder(@NonNull SongView holder, int position) {
        final Song song = songs.get(position);

        holder.artist.setText(song.artist);
        holder.name.setText(song.name);
        Glide.with(context).load(song.poster).into(holder.poster);

        holder.button.setOnClickListener((v) -> {

            if(player.isPlaying() && currentSongPosition == holder.getAdapterPosition()){
                holder.button.setImageResource(R.drawable.ic_baseline_play_circle_outline_24);
                player.stop();
                player.reset();
            }
            else if(player.isPlaying() && currentSongPosition != holder.getAdapterPosition()){
                Toast.makeText(context, "Stop current song...", Toast.LENGTH_SHORT).show();
            }
            else {
                holder.button.setImageResource(R.drawable.ic_pause_foreground);
                player = new MediaPlayer();
                currentSongPosition = holder.getAdapterPosition();
                playSong(song.songUrl,holder.button);

            }
        });
    }

    @Override
    public int getItemCount() {
        return songs.size();
    }

    public static class SongView extends RecyclerView.ViewHolder {
        final TextView name;
        final TextView artist;
        final ShapeableImageView poster;
        final ImageButton button;

        public SongView(@NonNull View itemView) {
            super(itemView);

            poster = itemView.findViewById(R.id.poster);
            name = itemView.findViewById(R.id.name);
            artist = itemView.findViewById(R.id.artist);
            button = itemView.findViewById(R.id.playPause);

        }
    }
}

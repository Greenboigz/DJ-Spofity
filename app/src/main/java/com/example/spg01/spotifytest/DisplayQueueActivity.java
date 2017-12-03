package com.example.spg01.spotifytest;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.spotify.sdk.android.authentication.AuthenticationClient;
import com.spotify.sdk.android.authentication.AuthenticationRequest;
import com.spotify.sdk.android.authentication.AuthenticationResponse;
import com.spotify.sdk.android.player.Config;
import com.spotify.sdk.android.player.ConnectionStateCallback;
import com.spotify.sdk.android.player.Error;
import com.spotify.sdk.android.player.Player;
import com.spotify.sdk.android.player.PlayerEvent;
import com.spotify.sdk.android.player.Spotify;
import com.spotify.sdk.android.player.SpotifyPlayer;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Track;
import retrofit.Callback;
import retrofit.RetrofitError;
import spg01.SpotifyTest.OnSongListListener;
import spg01.SpotifyTest.QueueAdapter;


public class DisplayQueueActivity extends AppCompatActivity implements ConnectionStateCallback, Player.NotificationCallback {

    private ImageButton mPlayPauseButton;
    private RecyclerView mSongRecyclerView;
    private QueueAdapter mSpotifyAdapter;
    private Fragment mTrackFragment;

    private Track curTrack;

    public ImageView mAlbumImageView;
    public TextView mSongTextView;
    public TextView mAlbumTextView;
    public TextView mArtistTextView;

    private boolean playing = false;
    private ArrayList<Track> mTracks;

    public static DisplayQueueActivity instance;

    public SpotifyApi spotifyApi;
    public SpotifyPlayer mPlayer;

    private final int REQUEST_CODE = 1337;
    private final String CLIENT_ID = "53398bdf6a4c4f77ab76021fd093347d";
    private final String REDIRECT_URI = "http://facebook.com";

    public String data = "default";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        instance = this;

        setContentView(R.layout.activity_display_queue);
//        this.loadData();

        AuthenticationRequest.Builder builder = new AuthenticationRequest.Builder(CLIENT_ID,
                AuthenticationResponse.Type.TOKEN, REDIRECT_URI);
        System.out.println("1");
        builder.setScopes(new String[]{"user-read-private", "streaming"});
        System.out.println("2");
        AuthenticationRequest request = builder.build();
        System.out.println("after request builder");
        AuthenticationClient.openLoginActivity(this, REQUEST_CODE, request);

        mAlbumImageView = (ImageView) findViewById(R.id.CurAlbumImageView);
        mSongTextView = (TextView) findViewById(R.id.CurSongTextView);
        mAlbumTextView = (TextView) findViewById(R.id.CurAlbumTextView);
        mArtistTextView = (TextView) findViewById(R.id.CurArtistTextView);

        mPlayPauseButton = (ImageButton) findViewById(R.id.playPauseButton);

        mSongRecyclerView = (RecyclerView) findViewById(R.id.SongRecyclerView);
        mSongRecyclerView.setLayoutManager(new LinearLayoutManager(this.getBaseContext()));

        mTracks = new ArrayList<>();
        mSpotifyAdapter = new QueueAdapter(mTracks, new OnSongListListener(), this);
        mSongRecyclerView.setAdapter(mSpotifyAdapter);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        connectToSpotify(requestCode, resultCode, intent);
        loadData();
    }


    protected void connectToSpotify(int requestCode, int resultCode, Intent intent) {
        spotifyApi = new SpotifyApi();

        // Check if result comes from the correct activity
        if (requestCode == REQUEST_CODE) {

            AuthenticationResponse response = AuthenticationClient.getResponse(resultCode, intent);
            if (response.getType() == AuthenticationResponse.Type.TOKEN) {
                Config playerConfig = new Config(this, response.getAccessToken(), CLIENT_ID);
                spotifyApi.setAccessToken(response.getAccessToken());
                Spotify.getPlayer(playerConfig, this, new SpotifyPlayer.InitializationObserver() {
                    @Override
                    public void onInitialized(SpotifyPlayer spotifyPlayer) {
                        System.out.println("initialized");
                        final Handler handler = new Handler();
                        final int delay = 5000;
                        handler.postDelayed(new Runnable(){
                            public void run(){
                                DisplayQueueActivity.this.loadData();

                                handler.postDelayed(this, delay);
                            }
                        }, delay);

                        mPlayer = spotifyPlayer;
                        mPlayer.addConnectionStateCallback(DisplayQueueActivity.this);
                        mPlayer.addNotificationCallback(DisplayQueueActivity.this);

                        if (playing && curTrack != null) {
                            DisplayQueueActivity.this.updateCurSong();
                        }
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        Log.e("InitializationObserver", "Get Player Error");
                    }
                });
            }
        }
    }

    public void goToSearch(View v){
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    private void displayData(String s) {
        SpotifyService spotify = spotifyApi.getService();

        JSONArray jsonArray;
        try {
            jsonArray = new JSONArray(s);

            mTracks.clear();

            final ArrayList<String> uriList = new ArrayList<>();
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject json_data;
                String uri = "default";
                try {
                    json_data = jsonArray.getJSONObject(i);
                    uri = json_data.getString("uri");
                    spotify.getTrack(uri, new Callback<Track>() {
                        @Override
                        public void success(Track track, retrofit.client.Response response) {
                            boolean contains = false;
                            for (Track t : mTracks) {
                                if (t.id.equals(track.id)){
                                    contains = true;
                                }
                            }
                            if (!contains) {
                                mTracks.add(track);
                                mSongRecyclerView.setLayoutManager(new LinearLayoutManager(DisplayQueueActivity.this));
                                mSongRecyclerView.setAdapter(mSpotifyAdapter);
                                Log.d("GetTrack", "It worked");
                            }
                        }

                        @Override
                        public void failure(RetrofitError error) {
                            Log.e("GetTrack", "It didn't work");
                        }
                    });
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        } catch (Exception e) {
            Log.e("displayData", "Something Bad just happened", e);
        }

    }

    public void displayDialog(String URI){
        final String uri = URI;
        View V = findViewById(android.R.id.content);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.voteDialog)
                .setTitle(R.string.dialogTitle)
                .setPositiveButton(R.string.upvote, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) { // upvote
                        sendUpvote(uri);
                    }
                })
                .setNegativeButton(R.string.downvote, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) { //downvote
                        sendDownvote(uri);
                    }
                })
                .setNeutralButton(R.string.removeItem, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) { //downvote
                        updateBeforeDelete(uri);
                    }
                });
        AlertDialog dialog = builder.create();
        dialog.show();
    }



    protected void loadData() {
        System.out.println("IN LOAD DATA");
        String URL = "https://mobilefinalproject-184515.appspot.com/ ";
        RequestQueue q = Volley.newRequestQueue(this);
        StringRequest getReq = new StringRequest(Request.Method.GET, URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        System.out.println("GET response received.");
                        System.out.println("response: " + response);

                        if (data.equals(response)){
//                            displayData(response);
                            System.out.println("Nothing has changed");
                            // do nothing because nothing has changed
                        }else{
                            System.out.println("FOUND DIFFERENCE IN DATA UPDATING");
                            data = response;
                            displayData(response);
                        }
//                        displayData(response);



                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("volley:", "error! " + error.toString());
            }
        });
        q.add(getReq);

    }



    public void sendUpvote (String URI) {

        String URL = "https://mobilefinalproject-184515.appspot.com/ ";
        final String uri= URI;
        RequestQueue q = Volley.newRequestQueue(this);
        StringRequest postReq = new StringRequest(Request.Method.PUT, URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                //This code is executed if the server responds, whether or not the response contains data.
                //The String 'response' contains the server's response.
            }
        }, new Response.ErrorListener() { //Create an error listener to handle errors appropriately.
            @Override
            public void onErrorResponse(VolleyError error) {
                //This code is executed if there is an error.
            }
        }) {
            protected Map<String, String> getParams() {
                Map<String, String> MyData = new HashMap<>();

                MyData.put("uri", uri); //Add the data you'd like to send to the server.
                MyData.put("extraInfo", "nothing");
                MyData.put("ranking", String.valueOf(1));


                return MyData;
            }
        };


        q.add(postReq);
        System.out.println("about to refresh");
        this.loadData();
    }

    public void sendDownvote (String URI) {
        String URL = "https://mobilefinalproject-184515.appspot.com/ ";
        final String uri= URI;
        RequestQueue q = Volley.newRequestQueue(this);
        StringRequest postReq = new StringRequest(Request.Method.PUT, URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                //This code is executed if the server responds, whether or not the response contains data.
                //The String 'response' contains the server's response.
            }
        }, new Response.ErrorListener() { //Create an error listener to handle errors appropriately.
            @Override
            public void onErrorResponse(VolleyError error) {
                //This code is executed if there is an error.
            }
        }) {
            protected Map<String, String> getParams() {
                Map<String, String> MyData = new HashMap<>();
                MyData.put("uri",uri ); //Add the data you'd like to send to the server.
                MyData.put("extraInfo", "nothing");
                MyData.put("ranking", String.valueOf(-1));


                return MyData;
            }
        };


        q.add(postReq);

    }

    public void updateBeforeDelete (String URI) {
//        JSONObject json = new JSONObject("{\"type\" : \"example\"}");
        Log.v("UpdateBeforeDelete", URI);
        String URL = "https://mobilefinalproject-184515.appspot.com/ ";
        final String uri= URI;
        RequestQueue q = Volley.newRequestQueue(this);
        StringRequest postReq = new StringRequest(Request.Method.PUT, URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                //This code is executed if the server responds, whether or not the response contains data.
                //The String 'response' contains the server's response.
            }
        }, new Response.ErrorListener() { //Create an error listener to handle errors appropriately.
            @Override
            public void onErrorResponse(VolleyError error) {
                //This code is executed if there is an error.
            }
        }) {
            protected Map<String, String> getParams() {
                Map<String, String> MyData = new HashMap<>();
                MyData.put("uri", uri); //Add the data you'd like to send to the server.
                MyData.put("extraInfo", "delete");
                MyData.put("ranking", String.valueOf(0));


                return MyData;
            }
        };


        q.add(postReq);
        deleteEntry();

    }

    public void deleteEntry () {
//        JSONObject json = new JSONObject("{\"type\" : \"example\"}");
        String URL = "https://mobilefinalproject-184515.appspot.com/ ";
        RequestQueue q = Volley.newRequestQueue(this);
        StringRequest postReq = new StringRequest(Request.Method.DELETE, URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                //This code is executed if the server responds, whether or not the response contains data.
                //The String 'response' contains the server's response.
                Log.v("UpdateBeforeDelete", "2");
            }
        }, new Response.ErrorListener() { //Create an error listener to handle errors appropriately.
            @Override
            public void onErrorResponse(VolleyError error) {
                //This code is executed if there is an error.
            }
        }) {
            protected Map<String, String> getParams() {
                Map<String, String> MyData = new HashMap<>();

                return MyData;
            }
        };


        q.add(postReq);
    }


    public void onPlayPause(View v) {
        if (playing) {
            mPlayer.pause(new Player.OperationCallback() {
                @Override
                public void onSuccess() {
                    DisplayQueueActivity.this.playing = false;
                    int res = getResources().getIdentifier("ic_media_play", "drawable", "android");
                    DisplayQueueActivity.this.mPlayPauseButton.setImageResource(res);
                    Log.d("onPause", "Pause Success!");
                }

                @Override
                public void onError(Error error) {
                    Log.e("onPause", "Pause Failed! " + error.toString());
                }
            });
        } else {
            if (curTrack != null) {
                mPlayer.resume(new Player.OperationCallback() {
                    @Override
                    public void onSuccess() {
                        DisplayQueueActivity.this.playing = true;
                        int res = getResources().getIdentifier("ic_media_pause", "drawable", "android");
                        DisplayQueueActivity.this.mPlayPauseButton.setImageResource(res);

                        Log.d("onResume", "Resume Success!");
                    }

                    @Override
                    public void onError(Error error) {
                        Log.e("onResume", "Resume Failed! " + error.toString());
                    }
                });
            } else {
                this.playTopSong();
            }
        }
    }

    private void playTopSong() {
        curTrack = DisplayQueueActivity.this.mTracks.get(0);
        DisplayQueueActivity.this.mPlayer.playUri(new Player.OperationCallback() {
            @Override
            public void onSuccess() {
                updateCurSong();

                updateBeforeDelete(mTracks.get(0).uri.substring(14));
                Log.v("Update before delete", mTracks.get(0).toString());

                DisplayQueueActivity.this.playing = true;
                int res = getResources().getIdentifier("ic_media_pause", "drawable", "android");
                DisplayQueueActivity.this.mPlayPauseButton.setImageResource(res);
            }

            @Override
            public void onError(Error error) {

            }
        }, curTrack.uri, 0, 0);
    }

    private void updateCurSong() {
        new Thread(new Runnable() {
            public void run() {
                boolean loaded = false;
                while (!loaded) {
                    try {
                        final Bitmap bitmap = BitmapFactory.decodeStream((InputStream) new URL(curTrack.album.images.get(0).url).getContent());
                        mAlbumImageView.post(new Runnable() {
                            public void run() {
                                if (bitmap != null) {
                                    mAlbumImageView.setImageBitmap(bitmap);
                                }
                            }
                        });
                        loaded = true;
                    } catch (Exception e) {

                    }
                }
            }
        }).start();
        mSongTextView.setText(curTrack.name);
        mAlbumTextView.setText(curTrack.album.name);
        mArtistTextView.setText(curTrack.artists.get(0).name);
    }

    public void onNext(View v) {
        mPlayer.skipToNext(new Player.OperationCallback() {
            @Override
            public void onSuccess() {
                Log.d("onSkipToNext","Skip Success!");
            }

            @Override
            public void onError(Error error) {
                Log.e("onSkipToNext","Skip Failed! " + error.toString());
            }
        });
    }


    @Override
    public void onLoggedIn() {
        Log.d("onLoggedIn", "It worked!");
    }

    @Override
    public void onLoggedOut() {
        Log.d("onLoggedOut", "It worked!");
    }

    @Override
    public void onLoginFailed(Error error) {
        Log.d("onLoggedFailed", "It worked!");
    }

    @Override
    public void onTemporaryError() {
        Log.d("onTemporaryError", "It worked!");
    }

    @Override
    public void onConnectionMessage(String s) {
        Log.d("onConnectionMessage", s);
    }

    @Override
    public void onPlaybackEvent(PlayerEvent playerEvent) {
        Log.d("onPlaybackEvent", playerEvent.toString());
        if (playerEvent == PlayerEvent.kSpPlaybackNotifyAudioDeliveryDone && this.playing) {
            this.playTopSong();
        }
    }

    @Override
    public void onPlaybackError(Error error) {

    }
}

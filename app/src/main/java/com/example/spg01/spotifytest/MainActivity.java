package com.example.spg01.spotifytest;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

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

import java.util.ArrayList;
import java.util.jar.Manifest;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.TracksPager;
import kaaes.spotify.webapi.android.models.Track;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import spg01.SpotifyTest.Controller;
import spg01.SpotifyTest.SongActivity;
import spg01.SpotifyTest.SongAdapter;
import spg01.SpotifyTest.OnSongListListener;

public class MainActivity extends Activity implements
        SpotifyPlayer.NotificationCallback, ConnectionStateCallback
{

    private RecyclerView mSongRecyclerView;
    private SongAdapter mSpotifyAdapter;

    private ArrayList<Track> mTracks;
    private EditText searchEditText;

    public static Player mPlayer;
    public static SpotifyApi spotifyApi;

    public static final int REQUEST_CODE = 1337;
    public static final String CLIENT_ID = "53398bdf6a4c4f77ab76021fd093347d";
    public static final String REDIRECT_URI = "http://facebook.com";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        spotifyApi = Controller.getSpotifyApi();
//        AuthenticationRequest.Builder builder = new AuthenticationRequest.Builder(CLIENT_ID,
//                AuthenticationResponse.Type.TOKEN, REDIRECT_URI);
//        builder.setScopes(new String[]{"user-read-private", "streaming"});
//        AuthenticationRequest request = builder.build();
//
//        AuthenticationClient.openLoginActivity(this, REQUEST_CODE, request);

        searchEditText = (EditText) findViewById(R.id.SearchEditText);

        mSongRecyclerView = (RecyclerView) findViewById(R.id.SongRecyclerView);
        mSongRecyclerView.setLayoutManager(new LinearLayoutManager(this.getBaseContext()));

        mTracks = new ArrayList<>();
        mSpotifyAdapter = new SongAdapter(mTracks, new OnSongListListener());
        mSongRecyclerView.setAdapter(mSpotifyAdapter);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        //connectToSpotify(requestCode, resultCode, intent, this, this, this);
    }

    public static void connectToSpotify(int requestCode, int resultCode, Intent intent,
                                        Activity activity,
                                        final ConnectionStateCallback connectionCallback,
                                        final Player.NotificationCallback notificationCallback) {
        spotifyApi = new SpotifyApi();

        // Check if result comes from the correct activity
        if (requestCode == REQUEST_CODE) {
            AuthenticationResponse response = AuthenticationClient.getResponse(resultCode, intent);
            if (response.getType() == AuthenticationResponse.Type.TOKEN) {
                Config playerConfig = new Config(activity, response.getAccessToken(), CLIENT_ID);
                spotifyApi.setAccessToken(response.getAccessToken());
                Spotify.getPlayer(playerConfig, activity, new SpotifyPlayer.InitializationObserver() {
                    @Override
                    public void onInitialized(SpotifyPlayer spotifyPlayer) {
                        mPlayer = spotifyPlayer;
                        mPlayer.addConnectionStateCallback(connectionCallback);
                        // mPlayer.addNotificationCallback(MainActivity.this);
                        mPlayer.addNotificationCallback(notificationCallback);

                    }

                    @Override
                    public void onError(Throwable throwable) {
                        Log.e("InitializationObserver", "Get Player Error");
                    }
                });
            }
        }
    }

    @Override
    protected void onDestroy() {
        Spotify.destroyPlayer(this);
        super.onDestroy();
    }

    @Override
    public void onPlaybackEvent(PlayerEvent playerEvent) {
        Log.d("MainActivity", "Playback event received: " + playerEvent.name());
        switch (playerEvent) {
            // Handle event type as necessary
            default:
                break;
        }
    }

    @Override
    public void onPlaybackError(Error error) {
        Log.d("MainActivity", "Playback error received: " + error.name());
        switch (error) {
            // Handle error type as necessary
            default:
                break;
        }
    }

    @Override
    public void onLoggedIn() {
        Log.d("MainActivity", "User logged in");

        mPlayer.playUri(null, "spotify:track:3CRDbSIZ4r5MsZ0YwxuEkn", 0, 0);
    }

    @Override
    public void onLoggedOut() {
        Log.d("MainActivity", "User logged out");
    }

    @Override
    public void onLoginFailed(Error error) {
        Log.d("MainActivity", "Login failed");
    }

    @Override
    public void onTemporaryError() {
        Log.d("MainActivity", "Temporary error occurred");
    }

    @Override
    public void onConnectionMessage(String message) {
        Log.d("MainActivity", "Received connection message: " + message);
    }

    public void onPlay(View v) {
        mPlayer.resume(new Player.OperationCallback() {
            @Override
            public void onSuccess() {
                Log.d("onResume","Resume Success!");
            }

            @Override
            public void onError(Error error) {
                Log.e("onPause","Pause Failed! " + error.toString());
            }
        });
    }

    public void onPause(View v) {
        mPlayer.pause(new Player.OperationCallback() {
            @Override
            public void onSuccess() {
                Log.d("onPause","Pause Success!");
            }

            @Override
            public void onError(Error error) {
                Log.e("onPause","Pause Failed! " + error.toString());
            }
        });
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

//    public void onSong(View v) {
//        Log.d("onSong", "Something's happening");
//        setContentView(R.layout.activity_playback);
//    }

    public void onSearch(View v) {

        SpotifyService spotify = spotifyApi.getService();

        spotify.searchTracks(searchEditText.getText().toString(), new Callback<TracksPager>() {
            @Override
            public void success(TracksPager tracksPager, Response response) {
                mTracks.clear();
                mTracks.addAll(tracksPager.tracks.items);
                mSongRecyclerView.setLayoutManager(new LinearLayoutManager(MainActivity.this));
                mSongRecyclerView.setAdapter(mSpotifyAdapter);
                Log.d("onSearch","Updating...");
            }

            @Override
            public void failure(RetrofitError error) {
                Log.e("onSearchFailure", "Failed to find song by " + searchEditText.getText().toString());
                Log.e("onSearchFailure", error.getMessage());
                Log.e("onSearchFailure", error.toString());
            }

        });
    }

}
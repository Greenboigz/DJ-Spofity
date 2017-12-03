package spg01.SpotifyTest;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.example.spg01.spotifytest.R;
import com.spotify.sdk.android.authentication.AuthenticationClient;
import com.spotify.sdk.android.authentication.AuthenticationRequest;
import com.spotify.sdk.android.authentication.AuthenticationResponse;
import com.spotify.sdk.android.player.Config;
import com.spotify.sdk.android.player.ConnectionStateCallback;
import com.spotify.sdk.android.player.Error;
import com.spotify.sdk.android.player.PlaybackState;
import com.spotify.sdk.android.player.Player;
import com.spotify.sdk.android.player.PlayerEvent;
import com.spotify.sdk.android.player.Spotify;
import com.spotify.sdk.android.player.SpotifyPlayer;

import java.util.PriorityQueue;
import java.util.Queue;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.TracksPager;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class SongActivity extends Activity implements
        SpotifyPlayer.NotificationCallback, ConnectionStateCallback
{

    // TODO: Replace with your client ID
    private static final String CLIENT_ID = "53398bdf6a4c4f77ab76021fd093347d";
    // TODO: Replace with your redirect URI
    private static final String REDIRECT_URI = "http://facebook.com";

    public static Player mPlayer;
    private SpotifyApi spotifyApi;

    private EditText searchEditText;

    // Request code that will be used to verify if the result comes from correct activity
    // Can be any integer
    private static final int REQUEST_CODE = 1337;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_song);

//        AuthenticationRequest.Builder builder = new AuthenticationRequest.Builder(CLIENT_ID,
//                AuthenticationResponse.Type.TOKEN,
//                REDIRECT_URI);
//        builder.setScopes(new String[]{"user-read-private", "streaming"});
//        AuthenticationRequest request = builder.build();
//
//        AuthenticationClient.openLoginActivity(this, REQUEST_CODE, request);

        searchEditText = (EditText) findViewById(R.id.SearchEditText);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

//        spotifyApi = new SpotifyApi();
//
//        // Check if result comes from the correct activity
//        if (requestCode == REQUEST_CODE) {
//            AuthenticationResponse response = AuthenticationClient.getResponse(resultCode, intent);
//            if (response.getType() == AuthenticationResponse.Type.TOKEN) {
//                Config playerConfig = new Config(this, response.getAccessToken(), CLIENT_ID);
//                spotifyApi.setAccessToken(response.getAccessToken());
//                Spotify.getPlayer(playerConfig, this, new SpotifyPlayer.InitializationObserver() {
//                    @Override
//                    public void onInitialized(SpotifyPlayer spotifyPlayer) {
//                        mPlayer = spotifyPlayer;
//                        mPlayer.addConnectionStateCallback(SongActivity.this);
//                        // mPlayer.addNotificationCallback(SongActivity.this);
//                        mPlayer.addNotificationCallback(new Player.NotificationCallback() {
//
//                            @Override
//                            public void onPlaybackEvent(PlayerEvent playerEvent) {
//
//                                if (playerEvent == PlayerEvent.kSpPlaybackNotifyTrackDelivered) {
//
//                                    Toast.makeText(SongActivity.this, "Delivered", Toast.LENGTH_LONG).show();
//                                    System.out.println("Delivered");
//
//                                }
//                            }
//
//                            @Override
//                            public void onPlaybackError(Error error) {
//
//                            }
//                        });
//                    }
//
//                    @Override
//                    public void onError(Throwable throwable) {
//                        Log.e("SongActivity", "Could not authenticate player: " + throwable.getMessage());
//                    }
//                });
//            }
//        }
    }

    @Override
    protected void onDestroy() {
        //Spotify.destroyPlayer(this);
        super.onDestroy();
    }

    @Override
    public void onPlaybackEvent(PlayerEvent playerEvent) {
        Log.d("SongActivity", "Playback event received: " + playerEvent.name());
        switch (playerEvent) {
            // Handle event type as necessary
            default:
                break;
        }
    }

    @Override
    public void onPlaybackError(Error error) {
        Log.d("SongActivity", "Playback error received: " + error.name());
        switch (error) {
            // Handle error type as necessary
            default:
                break;
        }
    }

    @Override
    public void onLoggedIn() {
        Log.d("SongActivity", "User logged in");

        mPlayer.playUri(null, "spotify:track:6MFoqu0qP9MNxj7UgYaxpD", 0, 0);
    }

    @Override
    public void onLoggedOut() {
        Log.d("SongActivity", "User logged out");
    }

    @Override
    public void onLoginFailed(Error error) {
        Log.d("SongActivity", "Login failed");
    }

    @Override
    public void onTemporaryError() {
        Log.d("SongActivity", "Temporary error occurred");
    }

    @Override
    public void onConnectionMessage(String message) {
        Log.d("SongActivity", "Received connection message: " + message);
    }

    public void onSearch(View v) {
        Intent myIntent = new Intent(SongActivity.this, PlaybackActivity.class);
        SongActivity.this.startActivity(myIntent);

        SpotifyService spotify = spotifyApi.getService();

        spotify.searchTracks(searchEditText.getText().toString(), new Callback<TracksPager>() {
            @Override
            public void success(TracksPager tracksPager, Response response) {
                mPlayer.queue(new Player.OperationCallback() {
                    @Override
                    public void onSuccess() {
                        mPlayer.skipToNext(null);
                    }

                    @Override
                    public void onError(Error error) {

                    }
                }, tracksPager.tracks.items.get(0).uri);
                Log.d("onSearchSuccess", String.format("Playing song: %s", tracksPager.tracks.items.get(1).name));
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
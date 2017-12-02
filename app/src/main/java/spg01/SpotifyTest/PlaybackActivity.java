package spg01.SpotifyTest;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.example.spg01.spotifytest.MainActivity;
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

import java.io.InputStream;
import java.net.URL;
import java.util.Random;

import kaaes.spotify.webapi.android.SpotifyApi;

import static com.spotify.sdk.android.authentication.LoginActivity.REQUEST_CODE;

public class PlaybackActivity extends AppCompatActivity implements
        SpotifyPlayer.NotificationCallback, ConnectionStateCallback {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playback);

        AuthenticationRequest.Builder builder = new AuthenticationRequest.Builder(MainActivity.CLIENT_ID,
                AuthenticationResponse.Type.TOKEN, MainActivity.REDIRECT_URI);
        builder.setScopes(new String[]{"user-read-private", "streaming"});
        AuthenticationRequest request = builder.build();

        AuthenticationClient.openLoginActivity(this, REQUEST_CODE, request);

        Log.d("onActivityResult", "It done worked!");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        MainActivity.connectToSpotify(requestCode, resultCode, intent, this, this, this);
    }

    @Override
    public void onLoggedIn() {
        Controller.getmPlayer().resume(null);
        final PlaybackState[] playbackState = {Controller.getmPlayer().getPlaybackState()};

        final ImageView albumArt = (ImageView) findViewById(R.id.albumArt);
        final String artURL = Controller.getmPlayer().getMetadata().currentTrack.albumCoverWebUrl;
        new Thread(new Runnable()
        {
            public void run()
            {
                boolean loaded = false;
                while (!loaded) {
                    try {
                        final Bitmap bitmap = BitmapFactory.decodeStream((InputStream) new URL(artURL).getContent());
                        albumArt.post(new Runnable() {
                            public void run() {
                                if (bitmap != null) {
                                    albumArt.setImageBitmap(bitmap);
                                    Log.d("setImageBitmap", "It done worked!");
                                }
                            }
                        });
                        loaded = true;
                    } catch (Exception e) {
                        Log.e("ImageLoad", "It ain't working");
                    }
                }
            }                }).start();

        final Button playPause = (Button) findViewById(R.id.play_pause);
        if (playbackState[0].isPlaying){
            playPause.setText("Pause");
        } else {
            playPause.setText("Play");
        }
        playPause.setOnClickListener( new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                playbackState[0] = Controller.getmPlayer().getPlaybackState();
                if (playbackState[0].isPlaying){
                    Controller.getmPlayer().pause(null);
                    playPause.setText("Play");
                } else {
                    Controller.getmPlayer().resume(null);
                    playPause.setText("Pause");
                }
            }
        });
        final Button skip = (Button) findViewById(R.id.skip);
        skip.setOnClickListener( new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Controller.getmPlayer().skipToNext(new Player.OperationCallback() {
                    @Override
                    public void onSuccess() {
                        final String artURL = Controller.getmPlayer().getMetadata().currentTrack.albumCoverWebUrl;
                        new Thread(new Runnable()
                        {
                            public void run()
                            {
                                boolean loaded = false;
                                while (!loaded) {
                                    try {
                                        final Bitmap bitmap = BitmapFactory.decodeStream((InputStream) new URL(artURL).getContent());
                                        albumArt.post(new Runnable() {
                                            public void run() {
                                                if (bitmap != null) {
                                                    albumArt.setImageBitmap(bitmap);
                                                }
                                            }
                                        });
                                        loaded = true;
                                    } catch (Exception e) {

                                    }
                                }
                            }                }).start();
                    }

                    @Override
                    public void onError(Error error) {
                        final String artURL = Controller.getmPlayer().getMetadata().currentTrack.albumCoverWebUrl;
                        new Thread(new Runnable()
                        {
                            public void run()
                            {
                                boolean loaded = false;
                                while (!loaded) {
                                    try {
                                        final Bitmap bitmap = BitmapFactory.decodeStream((InputStream) new URL(artURL).getContent());
                                        albumArt.post(new Runnable() {
                                            public void run() {
                                                if (bitmap != null) {
                                                    albumArt.setImageBitmap(bitmap);
                                                }
                                            }
                                        });
                                        loaded = true;
                                    } catch (Exception e) {

                                    }
                                }
                            }                }).start();
                    }
                });
            }
        });
        final Button insert = (Button) findViewById(R.id.insert);
        insert.setOnClickListener( new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                String[] tracks = {"spotify:track:0BIRqnH1V9FIFs6zTaXsIY","spotify:track:6DkweOE7miAbtP6DwjWreE",
                        "spotify:track:6gAX1KFBsimJOvb3fOikyU", "spotify:track:4FfBIKMCVGzENuwgQUEd3H",
                        "spotify:track:1RSy7B2vfPi84N80QJ6frX", "spotify:track:0a9N94pHEOmE2xLBsygyy7",
                        "spotify:track:6HbTF52swZiGSJ2cvAJ7PU", "spotify:track:76GlO5H5RT6g7y0gev86Nk",
                        "spotify:track:32eLNDWRd4vdORyUS2uGHD"};
                int rnd = new Random().nextInt(tracks.length);
                Controller.getmPlayer().queue(null, tracks[rnd]);
            }
        });
    }

    @Override
    public void onLoggedOut() {

    }

    @Override
    public void onLoginFailed(Error error) {

    }

    @Override
    public void onTemporaryError() {

    }

    @Override
    public void onConnectionMessage(String s) {

    }

    @Override
    public void onPlaybackEvent(PlayerEvent playerEvent) {

    }

    @Override
    public void onPlaybackError(Error error) {

    }

}

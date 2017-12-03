package spg01.SpotifyTest;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.example.spg01.spotifytest.MainActivity;
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

import kaaes.spotify.webapi.android.SpotifyApi;

import static android.app.Activity.RESULT_OK;

/**
 * Singleton used for accessing mPlayer and SpotifyApi
 */

public class Controller implements ConnectionStateCallback {

    private static Controller instance;

    private static Player mPlayer;
    private static SpotifyApi spotifyApi;

    private static Context context;

    private static final int REQUEST_CODE = 1337;
    private static final String CLIENT_ID = "53398bdf6a4c4f77ab76021fd093347d";
    private static final String REDIRECT_URI = "http://facebook.com";

    public Controller(Activity activity) {
        this.context = activity.getBaseContext();

        AuthenticationRequest.Builder builder = new AuthenticationRequest.Builder(CLIENT_ID,
                AuthenticationResponse.Type.TOKEN, REDIRECT_URI);
        builder.setScopes(new String[]{"user-read-private", "streaming"});
        AuthenticationRequest request = builder.build();

        AuthenticationClient.openLoginActivity(activity, REQUEST_CODE, request);
        this.authenticate(activity.getIntent(), REQUEST_CODE, RESULT_OK, activity);
        Log.i("Controller", "Authenticated");

    }

    public static Controller getInstance(Activity activity){
        if (null==Controller.instance){
            Controller.instance = new Controller(activity);
        }
        return Controller.instance;
    }

    public static Player getmPlayer(){
        if (mPlayer == null){
            Log.e("getmPlayer","Must authenticate first");
        }
        return mPlayer;
    }
    public static SpotifyApi getSpotifyApi(){
        if (spotifyApi == null){
            Log.e("getSpotifyApi","Must authenticate first");
        }
        return spotifyApi;
    }

    private void authenticate(Intent intent, int requestCode, int resultCode, final Activity activity/*,
                             final Player.NotificationCallback notificationCallback*/) {
        spotifyApi = new SpotifyApi();

        // Check if result comes from the correct activity
        if (requestCode == REQUEST_CODE) {
            AuthenticationResponse response = AuthenticationClient.getResponse(resultCode, intent);
            if (response.getType() == AuthenticationResponse.Type.TOKEN) {
                Config playerConfig = new Config(context, response.getAccessToken(), CLIENT_ID);
                spotifyApi.setAccessToken(response.getAccessToken());
                Spotify.getPlayer(playerConfig, context, new SpotifyPlayer.InitializationObserver() {
                    @Override
                    public void onInitialized(SpotifyPlayer spotifyPlayer) {
                        mPlayer = spotifyPlayer;
                        mPlayer.addConnectionStateCallback(Controller.this);
                        //mPlayer.addNotificationCallback(null);

                    }

                    @Override
                    public void onError(Throwable throwable) {
                        Log.e("InitializationObserver", "Get Player Error");
                    }
                });
            } else {
                Log.e("authenticate", "Response is not a token");
            }
        } else {
            Log.e("authenticate", "Request code incorrect");
        }
    }


    @Override
    public void onLoggedIn() {
        Log.d("Controller", "User logged in");

        mPlayer.playUri(null, "spotify:track:3CRDbSIZ4r5MsZ0YwxuEkn", 0, 0);
    }

    @Override
    public void onLoggedOut() {
        Log.e("Controller", "User logged out");
    }

    @Override
    public void onLoginFailed(Error error) {
        Log.e("Controller", "Login failed");
    }

    @Override
    public void onTemporaryError() {
        Log.e("Controller", "Temporary error occurred");
    }

    @Override
    public void onConnectionMessage(String message) {
        Log.d("Controller", "Received connection message: " + message);
    }

}

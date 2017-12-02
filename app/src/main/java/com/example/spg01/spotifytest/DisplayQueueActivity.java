package com.example.spg01.spotifytest;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;

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
import com.spotify.sdk.android.player.Player;
import com.spotify.sdk.android.player.Spotify;
import com.spotify.sdk.android.player.SpotifyPlayer;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Track;
import kaaes.spotify.webapi.android.models.TracksPager;
import retrofit.Callback;
import retrofit.RetrofitError;
import spg01.SpotifyTest.OnSongListListener;
import spg01.SpotifyTest.QueueAdapter;
import spg01.SpotifyTest.SongAdapter;


public class DisplayQueueActivity extends AppCompatActivity {


    private ListView listView;
    private RecyclerView mSongRecyclerView;
    private QueueAdapter mSpotifyAdapter;

    private ArrayList<Track> mTracks;

    public static SpotifyApi spotifyApi;
    public static final int REQUEST_CODE = 1337;
    public static final String CLIENT_ID = "53398bdf6a4c4f77ab76021fd093347d";
    public static final String REDIRECT_URI = "http://facebook.com";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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

//        searchEditText = (EditText) findViewById(R.id.SearchEditText);

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


    public void connectToSpotify(int requestCode, int resultCode, Intent intent) {
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
                        DisplayQueueActivity.this.loadData();
//                        mPlayer = spotifyPlayer;
//                        mPlayer.addConnectionStateCallback(connectionCallback);
//                        // mPlayer.addNotificationCallback(MainActivity.this);
//                        mPlayer.addNotificationCallback(notificationCallback);

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
//        intent.putExtra("spotifyApi", spotifyApi);
        startActivity(intent);
    }

    public void displayData(String s) {
        SpotifyService spotify = spotifyApi.getService();

        spotify.searchTracks("Free Bird", new Callback<TracksPager>() {
            @Override
            public void success(TracksPager tracksPager, retrofit.client.Response response) {
                Log.d("SearchSuccess", "Track Name: " + tracksPager.tracks.items.get(0).name);
            }

            @Override
            public void failure(RetrofitError error) {

            }
        });


        JSONArray jsonArray;
        try {
            jsonArray = new JSONArray(s);
        } catch (Exception e) {
            throw new Error();
        }

//        String newStr = s.replace("}", ")");
//        System.out.println("new string is" + newStr);

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

    }

    public void displayDialog(String URI){
        final String uri = URI;
        View V = findViewById(android.R.id.content);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.voteDialog)
                .setTitle(R.string.dialogTitle).setPositiveButton(R.string.upvote, new DialogInterface.OnClickListener() {
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



    public void loadData() {
        String URL = "https://mobilefinalproject-184515.appspot.com/ ";
        RequestQueue q = Volley.newRequestQueue(this);
        StringRequest getReq = new StringRequest(Request.Method.GET, URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        System.out.println("GET response received.");
                        System.out.println("response: " + response);
                        displayData(response);



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

                MyData.put("uri",uri ); //Add the data you'd like to send to the server.
                MyData.put("extraInfo", "nothing");
                MyData.put("ranking", String.valueOf(1));


                return MyData;
            }
        };


        q.add(postReq);
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


}

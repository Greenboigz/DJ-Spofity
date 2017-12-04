package spg01.SpotifyTest;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.spg01.spotifytest.MainActivity;
import com.example.spg01.spotifytest.R;
import com.spotify.sdk.android.player.Error;
import com.spotify.sdk.android.player.Player;

import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kaaes.spotify.webapi.android.models.Track;

/**
 * Created by Levi on 12/1/2017.
 */

public class QueueAdapter extends RecyclerView.Adapter<QueueAdapter.ViewHolder>  {

    private List<Track> mValues;
    private final OnSongListListener mListener;
    private final Context mContext;

    public QueueAdapter(List<Track> items, OnSongListListener listener, Context context) {
        mValues = items;
        mListener = listener;
        mContext = context;
    }

    @Override
    public QueueAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_spotifysong, parent, false);
        return new QueueAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final QueueAdapter.ViewHolder holder, int position) {
        holder.mTrack = mValues.get(position);
        holder.bind();

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                displayDialog(holder.mTrack.id, mContext);
//                if (null != mListener) {
//                    MainActivity.mPlayer.queue(new Player.OperationCallback() {
//                        @Override
//                        public void onSuccess() {
//                            Log.d("onClick", "Add Song to Queue in queue adapter");
//                            displayDialog(holder.mTrack.id, mContext);
//                        }
//                        // add the popup
//                        @Override
//                        public void onError(Error error) {
//                            Log.e("onClick", "Add Song to Queue");
//                        }
//                    }, holder.mTrack.uri);
////                    MainActivity.activity.setContentView(R.layout.activity_song);
//                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final ImageView mAlbumImageView;
        public final TextView mSongTextView;
        public final TextView mAlbumTextView;
        public final TextView mArtistTextView;

        public Track mTrack;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mAlbumImageView = (ImageView) view.findViewById(R.id.AlbumImageView);
            mSongTextView = (TextView) view.findViewById(R.id.SongTextView);
            mAlbumTextView = (TextView) view.findViewById(R.id.AlbumTextView);
            mArtistTextView = (TextView) view.findViewById(R.id.ArtistTextView);

            Typeface typeface = Typeface.createFromAsset(view.getContext().getAssets(), "fonts/ProximaNova_Regular.otf");
            mSongTextView.setTypeface(typeface);
            mAlbumTextView.setTypeface(typeface);
            mArtistTextView.setTypeface(typeface);

            this.bind();
        }

        public void bind() {

            if (mTrack != null) {
                new Thread(new Runnable()
                {
                    public void run()
                    {
                        boolean loaded = false;
                        while (!loaded) {
                            try {
                                final Bitmap bitmap = BitmapFactory.decodeStream((InputStream) new URL(mTrack.album.images.get(0).url).getContent());
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
                    }                }).start();

                mSongTextView.setText(mTrack.name.toString());
                mAlbumTextView.setText(mTrack.album.name);
                mArtistTextView.setText(mTrack.artists.get(0).name);
            }
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mTrack.name + "'";
        }
    }

    public void displayDialog(String URI, Context context){
        final String uri = URI;
        final Context c = context;
//        View V = findViewById(android.R.id.content);
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(R.string.voteDialog)
                .setTitle(R.string.dialogTitle).setPositiveButton(R.string.upvote, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) { // upvote
                sendUpvote(uri,c);
            }
        })
                .setNegativeButton(R.string.downvote, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) { //downvote
                        sendDownvote(uri,c);
                    }
                })
                .setNeutralButton(R.string.removeItem, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) { //downvote
                        updateBeforeDelete(uri,c);
                    }
                });
        AlertDialog dialog = builder.create();
        dialog.show();
    }
    public void sendUpvote (String URI, Context c) {

        String URL = "https://mobilefinalproject-184515.appspot.com/ ";
        final String uri= URI;
        RequestQueue q = Volley.newRequestQueue(c);
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

    public void sendDownvote (String URI, Context c) {
        String URL = "https://mobilefinalproject-184515.appspot.com/ ";
        final String uri= URI;
        RequestQueue q = Volley.newRequestQueue(c);
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

    public void updateBeforeDelete (String URI, Context c) {
//        JSONObject json = new JSONObject("{\"type\" : \"example\"}");
        String URL = "https://mobilefinalproject-184515.appspot.com/ ";
        final String uri= URI;
        RequestQueue q = Volley.newRequestQueue(c);
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
        deleteEntry(c);
    }
    public void deleteEntry (Context c) {
//        JSONObject json = new JSONObject("{\"type\" : \"example\"}");
        String URL = "https://mobilefinalproject-184515.appspot.com/ ";
        RequestQueue q = Volley.newRequestQueue(c);
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

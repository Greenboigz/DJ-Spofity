package spg01.SpotifyTest;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
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

import kaaes.spotify.webapi.android.models.Track;
import spg01.SpotifyTest.SongFragment.OnListFragmentInteractionListener;

import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * {@link RecyclerView.Adapter} that can display a {@link Track} and makes a call to the
 * specified {@link OnListFragmentInteractionListener}.
 * TODO: Replace the implementation with code for your data type.
 */
public class SongAdapter extends RecyclerView.Adapter<SongAdapter.ViewHolder> {

    private List<Track> mValues;
    private final OnSongListListener mListener;
    private Context context;

    public SongAdapter(List<Track> items, OnSongListListener listener, Context context) {
        mValues = items;
        mListener = listener;
        this.context = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_spotifysong, parent, false);
        return new SongAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final SongAdapter.ViewHolder holder, int position) {
        holder.mTrack = mValues.get(position);
        holder.bind();

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mListener) {
                    DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            switch (which){
                                case DialogInterface.BUTTON_POSITIVE:
                                    addSong(holder.mTrack.uri, holder.mTrack.name);
                                    Log.v("OnClick", String.format("Adding song %s to Queue", holder.mTrack.name));
                                    break;

                                case DialogInterface.BUTTON_NEGATIVE:
                                    //No button clicked
                                    break;
                            }
                        }
                    };

                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setMessage("Add song \"" + holder.mTrack.name + "\" to the queue?").setPositiveButton("Yes", dialogClickListener)
                            .setNegativeButton("Cancel", dialogClickListener).show();
                }
            }
        });
    }

    public void addSong (String URI, final String name) {

        String URL = "https://mobilefinalproject-184515.appspot.com/ ";
        final String uri= URI.substring(14);
        RequestQueue q = Volley.newRequestQueue(MainActivity.mActivity);
        StringRequest postReq = new StringRequest(Request.Method.POST, URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                //This code is executed if the server responds, whether or not the response contains data.
                //The String 'response' contains the server's response.
                //Toast.makeText(context, "Added song " + name + " to the Queue!", Toast.LENGTH_LONG);
                Log.v("OnClick", String.format("Added song %s to Queue", name));
            }
        }, new Response.ErrorListener() { //Create an error listener to handle errors appropriately.
            @Override
            public void onErrorResponse(VolleyError error) {
                //This code is executed if there is an error.
            }
        }) {
            protected Map<String, String> getParams() {
                Map<String, String> MyData = new HashMap<>();

                MyData.put("uri", uri ); //Add the data you'd like to send to the server.
                MyData.put("extraInfo", "nothing");
                MyData.put("ranking", String.valueOf(1));


                return MyData;
            }
        };


        q.add(postReq);
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

                Typeface typeface = Typeface.createFromAsset(context.getAssets(), "fonts/ProximaNova_Regular.otf");
                mSongTextView.setTypeface(typeface);
                mAlbumTextView.setTypeface(typeface);
                mArtistTextView.setTypeface(typeface);

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
}

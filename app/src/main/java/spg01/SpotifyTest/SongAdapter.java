package spg01.SpotifyTest;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.spg01.spotifytest.MainActivity;
import com.example.spg01.spotifytest.R;
import com.spotify.sdk.android.player.Error;
import com.spotify.sdk.android.player.Player;
import com.spotify.sdk.android.player.Player.OperationCallback;

import kaaes.spotify.webapi.android.models.Track;
import spg01.SpotifyTest.SongFragment.OnListFragmentInteractionListener;

import java.io.InputStream;
import java.net.URL;
import java.util.List;

/**
 * {@link RecyclerView.Adapter} that can display a {@link Track} and makes a call to the
 * specified {@link OnListFragmentInteractionListener}.
 * TODO: Replace the implementation with code for your data type.
 */
public class SongAdapter extends RecyclerView.Adapter<SongAdapter.ViewHolder> {

    private List<Track> mValues;
    private final OnSongListListener mListener;

    public SongAdapter(List<Track> items, OnSongListListener listener) {
        mValues = items;
        mListener = listener;
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
                    MainActivity.mPlayer.queue(new OperationCallback() {
                        @Override
                        public void onSuccess() {
                            Log.d("onClick", "Add Song to Queue");
                        }

                        @Override
                        public void onError(Error error) {
                            Log.e("onClick", "Add Song to Queue");
                        }
                    }, holder.mTrack.uri);
//                    MainActivity.activity.setContentView(R.layout.activity_song);
                }
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
}

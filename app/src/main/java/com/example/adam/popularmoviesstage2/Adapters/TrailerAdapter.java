package com.example.adam.popularmoviesstage2.Adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.adam.popularmoviesstage2.R;
import com.example.adam.popularmoviesstage2.model.Video;
import com.example.adam.popularmoviesstage2.model.VideoResults;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

/**
 * Created by Adam on 2/23/2018.
 */

public class TrailerAdapter extends RecyclerView.Adapter<TrailerAdapter.TrailerViewHolder> {
    private final Context context;
    private final   VideoResults  tTrailers;

    public class TrailerViewHolder extends RecyclerView.ViewHolder {
         private ImageButton play_igbtn;
        private TextView trailer_title_tv;
        public TrailerViewHolder(View view) {
            super(view);
            play_igbtn = (ImageButton)view.findViewById(R.id.play_igbtn);
            trailer_title_tv = (TextView)view.findViewById(R.id.trailer_title_tv);
        }
    }
    /**
     * Constructor
     *
     * @param context Application context
     * @param trailers  Movie array
     */
    public TrailerAdapter(Context context, VideoResults trailers) {
        this.context = context;
        tTrailers = trailers;
    }


    @Override
    public TrailerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.movie_trailer_item, parent, false);

        return new TrailerViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final TrailerViewHolder holder, int position) {
        final Video trailer = tTrailers.getResults().get(position);
         holder.trailer_title_tv.setText(trailer.getName());
        final Target mTarget = new Target() {
            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom loadedFrom) {
                Log.d("DEBUG", "onBitmapLoaded");
                BitmapDrawable mBitmapDrawable = new BitmapDrawable(context.getResources(), bitmap);
                holder.play_igbtn.setBackground(mBitmapDrawable);
            }

            @Override
            public void onBitmapFailed(Drawable drawable) {
                Log.d("DEBUG", "onBitmapFailed");
            }

            @Override
            public void onPrepareLoad(Drawable drawable) {
                Log.d("DEBUG", "onPrepareLoad");
            }
        };

       // https://stackoverflow.com/questions/24180805/onbitmaploaded-of-target-object-not-called-on-first-load#answers
holder.play_igbtn.setTag(mTarget);
        Picasso.with(context)
                .load(buildThumbnailUrl(trailer.getKey()))
                .into(mTarget);
        holder.play_igbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
              /*  Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/watch?v=".concat(trailer.getKey())));
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
               context.startActivity(intent);
*/
                Intent appIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube:" + trailer.getKey()));
                appIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                Intent webIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/watch?v=".concat(trailer.getKey())));
                webIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
// Verify that the intent will resolve to an activity
                if (appIntent.resolveActivity(context.getPackageManager()) != null) {
                    // Open Youtube client
                    context.startActivity(appIntent);
                }
                else {
                    // Default to Web browser
                    context. startActivity(webIntent);
                }
            }
        } );
    }


    @Override
    public int getItemCount() {
        return tTrailers.getResults().size();
    }

    private String buildThumbnailUrl(String videoId) {
        return "https://img.youtube.com/vi/" + videoId + "/mqdefault.jpg";
    }
}
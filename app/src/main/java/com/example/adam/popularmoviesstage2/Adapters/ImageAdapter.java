package com.example.adam.popularmoviesstage2.Adapters;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.example.adam.popularmoviesstage2.MovieDetailsActivity;
import com.example.adam.popularmoviesstage2.R;
import com.example.adam.popularmoviesstage2.model.MovieResult;
import com.example.adam.popularmoviesstage2.model.Movies;
import com.orhanobut.logger.Logger;
import com.squareup.picasso.Picasso;



/**
 * Created by Adam on 2/23/2018.
 */

public class ImageAdapter  extends RecyclerView.Adapter<ImageAdapter.ImageViewHolder> {
    private final Context context;
    private final Movies mMovies;

    public class ImageViewHolder extends RecyclerView.ViewHolder {
        private ImageView imageView;

        public ImageViewHolder(View view) {
            super(view);
            imageView = (ImageView) view.findViewById(R.id.poster_imgv);
            ;
        }
    }

    /**
     * Constructor
     *
     * @param context Application context
     * @param movies  Movie array
     */
    public ImageAdapter(Context context, Movies movies) {
        this.context = context;
        mMovies = movies;
    }


    @Override
    public ImageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.movie_list_item, parent, false);

        return new ImageViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(ImageViewHolder holder, int position) {
        try {
            MovieResult movie = mMovies.getResults().get(position);
            Picasso.with(context)
                    .load(movie.getPosterPath())
                    .resize(context.getResources().getInteger(R.integer.the_movie_db_poster_w185_width),
                            context.getResources().getInteger(R.integer.the_movie_db_poster_w185_height))
                    .error(R.mipmap.ic_launcher_round)//will enhance it later with good photo
                    .placeholder(R.mipmap.ic_launcher)
                    .into(holder.imageView);
            holder.imageView.setTag(movie.getId());
            holder.imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Integer movieID = (Integer) v.getTag();
                    Intent intent = new Intent(context, MovieDetailsActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.putExtra("movieID", movieID);
                    context.startActivity(intent);
                }
            });
        } catch (Exception e) {
            Logger.e("Failed to bind  data.");
            e.printStackTrace();

        }
    }


    @Override
    public int getItemCount() {
        return mMovies.getResults().size();
    }

    public void updateMovies(Movies movies) {
        try {
            int position = this.mMovies.getResults().size() + 1;
            this.mMovies.appendMovies(movies);
            notifyItemRangeInserted(position, movies.getResults().size());
        } catch (Exception e) {
            Logger.e("Failed to update  data.");
            e.printStackTrace();

        }
    }

}
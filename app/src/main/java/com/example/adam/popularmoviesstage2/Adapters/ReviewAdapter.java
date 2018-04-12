package com.example.adam.popularmoviesstage2.Adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.adam.popularmoviesstage2.R;
import com.example.adam.popularmoviesstage2.model.Review;
import com.example.adam.popularmoviesstage2.model.Reviews;

/**
 * Created by Adam on 2/23/2018.
 */

public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder> {
    private final Context context;
    private final Reviews rReviews;

    public class ReviewViewHolder extends RecyclerView.ViewHolder {
        private TextView review_tv;
        public ReviewViewHolder(View view) {
            super(view);
            review_tv = (TextView)view.findViewById(R.id.review_tv);
        }
    }
    /**
     * Constructor
     *
     * @param context Application context
     * @param reviews  Movie array
     */
    public ReviewAdapter(Context context, Reviews reviews) {
        this.context = context;
        rReviews = reviews;
    }


    @Override
    public ReviewViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.movie_review_item, parent, false);

        return new ReviewViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(ReviewViewHolder holder, int position) {
        Review review = rReviews.getResults().get(position);
        holder.review_tv.setText(review.getContent());

    }


    @Override
    public int getItemCount() {
        return rReviews.getResults().size();
    }


}
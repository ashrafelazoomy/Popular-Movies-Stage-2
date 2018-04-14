package com.example.adam.popularmoviesstage2;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.example.adam.popularmoviesstage2.Adapters.ReviewAdapter;
import com.example.adam.popularmoviesstage2.Adapters.TrailerAdapter;
import com.example.adam.popularmoviesstage2.api.MoviesApiCallback;
import com.example.adam.popularmoviesstage2.api.MoviesApiManager;
import com.example.adam.popularmoviesstage2.db.MoviesContract;
import com.example.adam.popularmoviesstage2.model.Genre;
import com.example.adam.popularmoviesstage2.model.Movie;
import com.example.adam.popularmoviesstage2.model.Movies;
import com.example.adam.popularmoviesstage2.model.Reviews;
import com.example.adam.popularmoviesstage2.model.Video;
import com.example.adam.popularmoviesstage2.model.VideoResults;
import com.example.adam.popularmoviesstage2.utils.Globals;
import com.orhanobut.logger.Logger;
import com.squareup.picasso.Picasso;

import java.text.ParseException;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MovieDetailsActivity extends AppCompatActivity {
    private Context context;
    private Movie mMovie;
    private Reviews mReviews;
    private VideoResults mVideos;

    @BindView(R.id.poster_imgv)
    ImageView poster_imgv;
    @BindView(R.id.original_title_tv)
    TextView original_title_tv;
    @BindView(R.id.overview_tv)
    TextView overview_tv;
    @BindView(R.id.vote_average_tv)
    TextView vote_average_tv;
    @BindView(R.id.release_date_tv)
    TextView release_date_tv;
    @BindView(R.id.length_tv)
    TextView length_tv;
    @BindView(R.id.favouriteMove_tgbt)
    ToggleButton favouriteMove_tgbt;
    @BindView(R.id.trailers_rv)
    RecyclerView trailers_rv;
    @BindView(R.id.reviews_rv)
    RecyclerView reviews_rv;
    @BindView(R.id.movie_details_scv)
    NestedScrollView movie_details_scv;
    public static final String BUNDLE_MOVIE_KEY = "movie";
    public static final String BUNDLE_REVIEWS_KEY = "reviews";
    public static final String BUNDLE_VIDEOS_KEY = "videos";
    public static final String BUNDLE_IS_FAVOURITE_KEY = "IsFavourite";
    public  static  int NoOfColumnsInRecyclerView=0;
    private boolean mIsFavourite=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_details);
        ButterKnife.bind(this);
        context = getApplicationContext();
        if (savedInstanceState == null) {
            Intent intent = getIntent();
            Integer movieID = intent.getIntExtra("movieID", 0);

            mIsFavourite = isFavouriteMovie(movieID);
            if (mIsFavourite) {
                setMovieInfo();
            } else {
                getMovies(movieID);
            }
            favouriteMove_tgbt.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isFavouriteChecked) {
                    switchFavouriteStatus(isFavouriteChecked);
                }
            });

            getTrailers(movieID);
            LinearLayoutManager reviewsLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
            reviews_rv.setLayoutManager(reviewsLayoutManager);
            getReviews(movieID);
        }

    }

    private void getMovies(final int movieID) {

        MoviesApiManager.getInstance().getMovie(movieID, new MoviesApiCallback<Movie>() {
            @Override
            public void onResponse(Movie reslut) {
                mMovie = reslut;
                setMovieInfo();
            }

            @Override
            public void onCancel() {
            }
        });

    }

    private void getTrailers(final int movieID) {

        MoviesApiManager.getInstance().getMovieTrailers(movieID, new MoviesApiCallback<VideoResults>() {
            @Override
            public void onResponse(VideoResults result) {
                mVideos=result;
                if (result != null) {
                    mVideos=result;
                    setupTrailersRecyclerView();
                }
            }

            @Override
            public void onCancel() {
            }
        });

    }

    private void getReviews(final int movieID) {

        MoviesApiManager.getInstance().getMovieReviews(movieID, new MoviesApiCallback<Reviews>() {
            @Override
            public void onResponse(Reviews result) {
                mReviews=result;
                if (result != null) {
                    reviews_rv.setAdapter(new ReviewAdapter(getApplicationContext(), result));
                }
            }

            @Override
            public void onCancel() {
            }
        });

    }

    void setMovieInfo() {
        if (mMovie != null) {
            original_title_tv.setText(mMovie.getTitle());
            length_tv.setText(mMovie.getDuration());
            Picasso.with(context)
                    .load(mMovie.getPosterPath())
                    .resize(getResources().getInteger(R.integer.the_movie_db_poster_w185_width),
                            getResources().getInteger(R.integer.the_movie_db_poster_w185_height))
                    .error(R.mipmap.ic_launcher_round)
                    .placeholder(R.mipmap.ic_launcher)
                    .into(poster_imgv);

            overview_tv.setText(mMovie.getOverview());
            vote_average_tv.setText(String.valueOf(mMovie.getVoteAverage()));

            // Get Release Date and show it with the user format.
            String releaseDate = mMovie.getReleaseDate();
            if (releaseDate != null) {
                try {

                    release_date_tv.setText(mMovie.getReleaseDateLocalized(context));
                } catch (Exception e) {

                }
            }
            favouriteMove_tgbt.setChecked(mIsFavourite);
            if (favouriteMove_tgbt.isChecked()) {
                favouriteMove_tgbt.setBackground(getResources().getDrawable(R.drawable.ic_favorite_24dp));
            } else {
                favouriteMove_tgbt.setBackground(getResources().getDrawable(R.drawable.ic_favorite_border_24dp));
            }
        }
    }

    private boolean isFavouriteMovie(int mMovieID) {

        final Cursor cursor;
        cursor = context.getContentResolver().query(MoviesContract.MoviesEntry.CONTENT_URI, null, "movie_id=?", new String[]{String.valueOf(mMovieID)}, null);
        if (cursor.getCount() > 0) {

            int movieIdIndex = cursor.getColumnIndex(MoviesContract.MoviesEntry.COLUMN_ID);
            int titleIndex = cursor.getColumnIndex(MoviesContract.MoviesEntry.COLUMN_TITLE);
            int overviewIndex = cursor.getColumnIndex(MoviesContract.MoviesEntry.COLUMN_OVERVIEW);
            int posterPathIndex = cursor.getColumnIndex(MoviesContract.MoviesEntry.COLUMN_POSTER_PATH);
            int backdropPathIndex = cursor.getColumnIndex(MoviesContract.MoviesEntry.COLUMN_BACKDROP_PATH);
            int releaseDateIndex = cursor.getColumnIndex(MoviesContract.MoviesEntry.COLUMN_RELEASE_DATE);
            int runtimeIndex = cursor.getColumnIndex(MoviesContract.MoviesEntry.COLUMN_RUNTIME);
            int voteAverageIndex = cursor.getColumnIndex(MoviesContract.MoviesEntry.COLUMN_VOTE_AVERAGE);
            cursor.moveToFirst();
            mMovie = new Movie(
                    cursor.getInt(movieIdIndex),
                    cursor.getString(titleIndex),
                    cursor.getString(overviewIndex),
                    cursor.getString(posterPathIndex),
                    cursor.getString(backdropPathIndex),
                    cursor.getString(releaseDateIndex),
                    cursor.getInt(runtimeIndex),
                    cursor.getDouble(voteAverageIndex),
                    new Reviews(),
                    new ArrayList<Genre>()
            );
            cursor.close();
            return true;
        } else {
            return false;
        }
    }

    private void switchFavouriteStatus(boolean isFavouriteChecked) {

        if (mMovie != null) {
            if (isFavouriteChecked == false) {
                Uri uri = MoviesContract.MoviesEntry.CONTENT_URI;
                uri = uri.buildUpon().appendPath(String.valueOf(mMovie.getId())).build();
                int returnUri = context.getContentResolver().delete(uri, null, null);
                Logger.d("ReturnUri: " + returnUri);
                context.getContentResolver().notifyChange(uri, null);
                favouriteMove_tgbt.setBackground(getResources().getDrawable(R.drawable.ic_favorite_border_24dp));
                mIsFavourite = isFavouriteChecked;

                Toast.makeText(context, mMovie.getTitle() + " removed", Toast.LENGTH_SHORT).show();
            } else {
                if (isFavouriteMovie(mMovie.getId())) {
                    return;
                }
                ContentValues contentValues = new ContentValues();
                contentValues.put(MoviesContract.MoviesEntry.COLUMN_ID, mMovie.getId());
                contentValues.put(MoviesContract.MoviesEntry.COLUMN_TITLE, mMovie.getTitle());
                contentValues.put(MoviesContract.MoviesEntry.COLUMN_OVERVIEW, mMovie.getOverview());
                contentValues.put(MoviesContract.MoviesEntry.COLUMN_POSTER_PATH, mMovie.getPosterPath());
                contentValues.put(MoviesContract.MoviesEntry.COLUMN_BACKDROP_PATH, mMovie.getBackdropPath());
                contentValues.put(MoviesContract.MoviesEntry.COLUMN_RELEASE_DATE, mMovie.getReleaseDate());
                contentValues.put(MoviesContract.MoviesEntry.COLUMN_RUNTIME, mMovie.getRuntime());
                contentValues.put(MoviesContract.MoviesEntry.COLUMN_VOTE_AVERAGE, mMovie.getVoteAverage());

                Uri uri = context.getContentResolver().insert(MoviesContract.MoviesEntry.CONTENT_URI, contentValues);
                if (uri != null) {
                    mIsFavourite = isFavouriteChecked;
                    favouriteMove_tgbt.setBackground(getResources().getDrawable(R.drawable.ic_favorite_24dp));

                    Toast.makeText(context, mMovie.getTitle() + " Added", Toast.LENGTH_SHORT).show();
                } else {
                    Logger.d("Uri null");
                }
            }
        }
    }
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putBoolean(BUNDLE_IS_FAVOURITE_KEY, favouriteMove_tgbt.isChecked());
        if ( mMovie !=null) {
            outState.putParcelable(BUNDLE_MOVIE_KEY, mMovie);
        }
        if ( mReviews !=null) {
            outState.putParcelable(BUNDLE_REVIEWS_KEY, mReviews);
        }
        if ( mVideos !=null) {
            outState.putParcelable(BUNDLE_VIDEOS_KEY,mVideos );
        } 
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mIsFavourite= savedInstanceState.getBoolean(BUNDLE_IS_FAVOURITE_KEY);
        Movie tempMovie = savedInstanceState.getParcelable(BUNDLE_MOVIE_KEY);
        if (tempMovie != null) {
            mMovie= tempMovie;
           setMovieInfo();
        }

        Reviews tempReviews = savedInstanceState.getParcelable(BUNDLE_REVIEWS_KEY);
        if (tempReviews != null) {
            mReviews= tempReviews;
            LinearLayoutManager reviewsLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
            reviews_rv.setLayoutManager(reviewsLayoutManager);
            reviews_rv.setAdapter(new ReviewAdapter(this, mReviews));
        }
        VideoResults tempVideos = savedInstanceState.getParcelable(BUNDLE_VIDEOS_KEY);
        if (tempVideos != null) {
            mVideos= tempVideos;
            setupTrailersRecyclerView();
        }


    }
void  setupTrailersRecyclerView(){
    NoOfColumnsInRecyclerView=Globals.calculateNoOfColumnsInRecyclerView(this, getResources().getInteger(R.integer.the_movie_db_poster_w185_width));
    GridLayoutManager trailersLayoutManager = new GridLayoutManager(this,NoOfColumnsInRecyclerView);
    trailers_rv.setLayoutManager(trailersLayoutManager);
    trailers_rv.setAdapter(new TrailerAdapter(this, mVideos));
}
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.movie_details_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(mVideos==null|| mVideos.getResults().isEmpty()){
            return  false;
        }
        if (item.getItemId() == R.id.share_action) {
            shareTrailer(mVideos.getResults().get(0));
        }
        return super.onOptionsItemSelected(item);
    }

    private void shareTrailer(Video video) {
        Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
        sharingIntent.setType("text/plain");
        sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, mMovie.getTitle() + " - " + video.getName());
        sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, "http://www.youtube.com/watch?v=" + video.getKey());
        startActivity(Intent.createChooser(sharingIntent, getString(R.string.share_trailer)));
    }
}

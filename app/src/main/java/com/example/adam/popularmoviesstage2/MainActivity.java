package com.example.adam.popularmoviesstage2;


import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.AsyncTaskLoader;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.example.adam.popularmoviesstage2.Adapters.ImageAdapter;
import com.example.adam.popularmoviesstage2.api.MoviesApiCallback;
import com.example.adam.popularmoviesstage2.api.MoviesApiManager;
import com.example.adam.popularmoviesstage2.db.MoviesContract;
import com.example.adam.popularmoviesstage2.model.MovieResult;
import com.example.adam.popularmoviesstage2.model.Movies;
import com.example.adam.popularmoviesstage2.utils.EndlessRecyclerViewScrollListener;
import com.example.adam.popularmoviesstage2.utils.Globals;
import com.orhanobut.logger.Logger;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    @BindView(R.id.movies_rv)
    RecyclerView movies_rv;

    Menu menuMain;
    private static final String theMovieDBAPIKey = BuildConfig.key_movie_db;
    private EndlessRecyclerViewScrollListener rvScrollListener;
    private Movies mMovies = new Movies();
    public static final String BUNDLE_MOVIES_KEY = "movies";
    public static final String BUNDLE_RECYCLER_POSITION_KEY = "recycler_position";
    public static final int FAVOURITES_MOVIE_LOADER_ID = 100;

    private String sortMethod = "";


    // Receivers
    private final BroadcastReceiver networkChangeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            sortMethod = getSortMethodFromSharedPreferences();
            // Movies never loaded. Get them! (Entry Point)
            if (movies_rv.getAdapter() == null) {
                if (isNetworkAvailable()) {
                    getMovies(1, sortMethod);
                } else {
                    if (sortMethod.equals(getString(R.string.pref_sort_favourite_desc_key))  ) {
                        // We can load favourite movies even no connection
                        getMovies(1, sortMethod);
                    }
                }

            }

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        movies_rv.setHasFixedSize(true);
        GridLayoutManager layoutManager = new GridLayoutManager(this, Globals.calculateNoOfColumnsInRecyclerView(this, getResources().getInteger(R.integer.the_movie_db_poster_w185_width)));

        movies_rv.setLayoutManager(layoutManager);
        rvScrollListener = new EndlessRecyclerViewScrollListener(layoutManager) {
            @Override
            public void onLoadMore(int totalItemsCount, RecyclerView view) {


                loadMoreMovies();

            }
        };
        movies_rv.addOnScrollListener(rvScrollListener);


    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if (rvScrollListener != null && !mMovies.getResults().isEmpty()) {
            outState.putInt(BUNDLE_RECYCLER_POSITION_KEY, rvScrollListener.getFirstCompletelyVisibleItemPosition());
            outState.putParcelable(BUNDLE_MOVIES_KEY, mMovies);
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        Movies tempMovie = savedInstanceState.getParcelable(BUNDLE_MOVIES_KEY);
        int position = savedInstanceState.getInt(BUNDLE_RECYCLER_POSITION_KEY);
        if (tempMovie != null) {
            mMovies = tempMovie;
            movies_rv.setAdapter(new ImageAdapter(this, mMovies));
            movies_rv.getLayoutManager().scrollToPosition(position);
        }
    }

    private void loadMoreMovies() {
        getMovies(mMovies.getPage() + 1, sortMethod);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        menuMain = menu;
        updateMenu();
        return true;
    }

    // Update menu_main to show relevant items
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        mMovies = new Movies();
        switch (item.getItemId()) {
            case R.string.pref_sort_popular_desc_key:
                if (isNetworkAvailable() == false) {

                    Toast.makeText(this, getString(R.string.error_no_internet_connection), Toast.LENGTH_LONG).show();

                    return false;
                }
                updateSharedPrefs(Globals.TMDB_SORT_POP_DESC);
                updateMenu();
                getMoviesFromTheMovieDB(getSortMethodFromSharedPreferences());
                return true;
            case R.string.pref_sort_vote_avg_desc_key:
                if (isNetworkAvailable() == false) {
                    Toast.makeText(this, getString(R.string.error_no_internet_connection), Toast.LENGTH_LONG).show();
                    return false;
                }
                updateSharedPrefs(Globals.TMDB_SORT_VOTE_AVG_DESC);
                updateMenu();
                getMoviesFromTheMovieDB(getSortMethodFromSharedPreferences());
                return true;
            case R.string.pref_sort_favourite_desc_key:
                updateSharedPrefs(getString(R.string.pref_sort_favourite_desc_key));
                updateMenu();


                movies_rv.setAdapter(null);
                getSupportLoaderManager().initLoader(FAVOURITES_MOVIE_LOADER_ID, null, this);
                return true;

            default:
        }

        return super.onOptionsItemSelected(item);
    }


    //Get the movies from the API
    private void getMoviesFromTheMovieDB(String sortMethod) {
        sortMethod = sortMethod;
        getMovies(1, sortMethod);

    }

    private void getMovies(final int page, String sortMethod) {

        if (sortMethod.equals(getString(R.string.pref_sort_favourite_desc_key))) {
            movies_rv.clearOnScrollListeners();
            mMovies = new Movies();
            movies_rv.setAdapter(null);
            getSupportLoaderManager().initLoader(FAVOURITES_MOVIE_LOADER_ID, null, this);
            rvScrollListener.resetState();
        } else {
            if (isNetworkAvailable()) {
                getSupportLoaderManager().destroyLoader(FAVOURITES_MOVIE_LOADER_ID);
                movies_rv.addOnScrollListener(rvScrollListener);
                MoviesApiManager.getInstance().getMovies(sortMethod, page, new MoviesApiCallback<Movies>() {
                    @Override
                    public void onResponse(Movies result) {
                        if (result != null) {

                            mMovies.appendMovies(result);
                            if (page == 1) { // Refreshing movies
                                movies_rv.setAdapter(new ImageAdapter(getApplicationContext(), result));
                                rvScrollListener.resetState();
                            } else {
                                if (movies_rv.getAdapter() instanceof ImageAdapter) {
                                    ((ImageAdapter) movies_rv.getAdapter()).updateMovies(result);
                                }
                            }
                        }
                    }

                    @Override
                    public void onCancel() {
                    }
                });
            } else {
                Toast.makeText(this, getString(R.string.error_no_internet_connection), Toast.LENGTH_LONG).show();
            }
        }

    }

    /**
     * Checks if the Internet is connected.
     */
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();

        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    /**
     * Update menu_main based on method found set in SharedPreferences
     */
    private void updateMenu() {
        sortMethod = getSortMethodFromSharedPreferences();
        setTitleAccordingSort();
        if (sortMethod.equals(Globals.TMDB_SORT_POP_DESC)) {
            menuMain.findItem(R.string.pref_sort_popular_desc_key).setChecked(true);
            menuMain.findItem(R.string.pref_sort_favourite_desc_key).setChecked(false);
            menuMain.findItem(R.string.pref_sort_vote_avg_desc_key).setChecked(false);
        } else if (sortMethod.equals(Globals.TMDB_SORT_VOTE_AVG_DESC)) {
            menuMain.findItem(R.string.pref_sort_popular_desc_key).setChecked(false);
            menuMain.findItem(R.string.pref_sort_favourite_desc_key).setChecked(false);
            menuMain.findItem(R.string.pref_sort_vote_avg_desc_key).setChecked(true);
        } else {
            menuMain.findItem(R.string.pref_sort_vote_avg_desc_key).setChecked(false);
            menuMain.findItem(R.string.pref_sort_favourite_desc_key).setChecked(true);
            menuMain.findItem(R.string.pref_sort_popular_desc_key).setChecked(false);
        }
    }

    /**
     * Gets the sort method SharedPreferences.
     */
    private String getSortMethodFromSharedPreferences() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

        return prefs.getString(getString(R.string.pref_sort_method_key),
                Globals.TMDB_SORT_POP_DESC);
    }

    /**
     * Saves the selected sort method in SharedPreferences
     */
    private void updateSharedPrefs(String sortMethod) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(getString(R.string.pref_sort_method_key), sortMethod);
        editor.apply();
    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new AsyncTaskLoader<Cursor>(this) {

            // Initialize a Cursor, this will hold all the task data
            Cursor mTaskData = null;

            // onStartLoading() is called when a loader first starts loading data
            @Override
            protected void onStartLoading() {
                if (mTaskData != null) {
                    // Delivers any previously loaded data immediately
                    deliverResult(mTaskData);
                } else {
                    // Force a new load
                    forceLoad();
                }
            }

            // loadInBackground() performs asynchronous loading of data
            @Override
            public Cursor loadInBackground() {
                // Will implement to load data

                // Query and load all task data in the background; sort by priority
                // [Hint] use a try/catch block to catch any errors in loading data

                try {
                    return getContentResolver().query(MoviesContract.MoviesEntry.CONTENT_URI,
                            null,
                            null,
                            null,
                            MoviesContract.MoviesEntry._ID);

                } catch (Exception e) {
                    Logger.e("Failed to asynchronously load data.");
                    e.printStackTrace();
                    return null;
                }
            }

            // deliverResult sends the result of the load, a Cursor, to the registered listener
            public void deliverResult(Cursor data) {
                mTaskData = data;
                super.deliverResult(data);
            }
        };

    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data != null && data.getCount() > 0) {
            loadMoviesFromCursor(data);

        }

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    private void loadMoviesFromCursor(Cursor cursor) {
        try {
            mMovies = new Movies();
            List<MovieResult> movies = new ArrayList<>();
            for (int i = 0; i < cursor.getCount(); i++) {
                int movieIdIndex = cursor.getColumnIndex(MoviesContract.MoviesEntry.COLUMN_ID);
                int posterPathIndex = cursor.getColumnIndex(MoviesContract.MoviesEntry.COLUMN_POSTER_PATH);

                cursor.moveToPosition(i);

                movies.add(new MovieResult(
                        cursor.getInt(movieIdIndex),
                        cursor.getString(posterPathIndex)
                ));
            }
            mMovies.setResults(movies);
            movies_rv.setAdapter(new ImageAdapter(getApplicationContext(), mMovies));
        } catch (Exception e) {
                Logger.e("Failed to parse  data.");
                e.printStackTrace();

            }
    }

    private void setTitleAccordingSort() {

        switch (sortMethod) {
            case Globals.TMDB_SORT_POP_DESC:
                setTitle(getString(R.string.most_popular));
                break;
            case Globals.TMDB_SORT_VOTE_AVG_DESC:
                setTitle(getString(R.string.top_rated));
                break;
            default:
                setTitle(getString(R.string.favourite));
                break;
        }
    }
    @Override
    protected void onResume() {
        Logger.i("onResume()");

        super.onResume();
        try {
            registerReceiver(networkChangeReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
        } catch (Exception ex) {
            Logger.e(ex.getMessage());
        }
    }

    @Override
    protected void onPause() {
        Logger.i("onPause()");
        try {
            unregisterReceiver(networkChangeReceiver);
        } catch (Exception ex) {
            Logger.e(ex.getMessage());
        }

        super.onPause();
    }
}

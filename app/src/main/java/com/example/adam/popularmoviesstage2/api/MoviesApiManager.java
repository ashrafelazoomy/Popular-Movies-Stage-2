

package com.example.adam.popularmoviesstage2.api;

import android.support.annotation.NonNull;

import com.example.adam.popularmoviesstage2.model.Movie;
import com.example.adam.popularmoviesstage2.model.Movies;
import com.example.adam.popularmoviesstage2.model.Reviews;
import com.example.adam.popularmoviesstage2.model.VideoResults;
import com.example.adam.popularmoviesstage2.utils.Globals;
import com.orhanobut.logger.Logger;

import java.io.Serializable;


import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

public final class MoviesApiManager implements Serializable {
    private static volatile MoviesApiManager sharedInstance = new MoviesApiManager();

    private MovieApiService movieApiService;

    private MoviesApiManager() {
        if (sharedInstance != null) {
            throw new RuntimeException("Use getInstance() method to get the single instance of this class.");
        }

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Globals.TMDB_API_URL)
                .addConverterFactory(JacksonConverterFactory.create())
                .build();

        movieApiService = retrofit.create(MovieApiService.class);
    }

    public static MoviesApiManager getInstance() {
        if (sharedInstance == null) {
            synchronized (MoviesApiManager.class) {
                if (sharedInstance == null) sharedInstance = new MoviesApiManager();
            }
        }

        return sharedInstance;
    }



    public Call<Movie> getMovie(int movieId, final MoviesApiCallback<Movie> moviesApiCallback) {
        Call<Movie> call = movieApiService.getMovie(movieId, Globals.TMDB_API_KEY);

        call.enqueue(new Callback<Movie>() {
            @Override
            public void onResponse(@NonNull Call<Movie> call, @NonNull Response<Movie> response) {
                moviesApiCallback.onResponse(response.body());
            }

            @Override
            public void onFailure(@NonNull Call<Movie> call, @NonNull Throwable t) {
                if (call.isCanceled()) {
                    Logger.e("Request was cancelled");
                    moviesApiCallback.onCancel();
                } else {
                    Logger.e(t.getMessage());
                    moviesApiCallback.onResponse(null);
                }
            }
        });

        return call;
    }

    public void getMovies(String sortMethod, int page, MoviesApiCallback<Movies> moviesApiCallback) {

        switch (sortMethod) {
            case Globals.TMDB_SORT_POP_DESC :
                getPopularMovies(page, moviesApiCallback);
                break;
            case Globals.TMDB_SORT_VOTE_AVG_DESC :
                getTopRatedMovies(page, moviesApiCallback);
                break;
        }

    }

    private void getPopularMovies(int page, final MoviesApiCallback<Movies> moviesApiCallback) {
        movieApiService.getPopularMovies(Globals.TMDB_API_KEY, page).enqueue(new Callback<Movies>() {

            @Override
            public void onResponse(@NonNull Call<Movies> call, @NonNull Response<Movies> response) {
                moviesApiCallback.onResponse(response.body());
            }

            @Override
            public void onFailure(@NonNull Call<Movies> call, @NonNull Throwable t) {
                if (call.isCanceled()) {
                    Logger.e("Request was cancelled");
                    moviesApiCallback.onCancel();
                } else {
                    Logger.e(t.getMessage());
                    moviesApiCallback.onResponse(null);
                }
            }

        });
    }

    private void getTopRatedMovies(int page, final MoviesApiCallback<Movies> moviesApiCallback) {
        movieApiService.getTopRatedMovies(Globals.TMDB_API_KEY, page).enqueue(new Callback<Movies>() {

            @Override
            public void onResponse(@NonNull Call<Movies> call, @NonNull Response<Movies> response) {
                moviesApiCallback.onResponse(response.body());
            }

            @Override
            public void onFailure(@NonNull Call<Movies> call, @NonNull Throwable t) {
                if (call.isCanceled()) {
                    Logger.e("Request was cancelled");
                    moviesApiCallback.onCancel();
                } else {
                    Logger.e(t.getMessage());
                    moviesApiCallback.onResponse(null);
                }
            }

        });
    }

    public void getMovieTrailers(int movieID, final MoviesApiCallback<VideoResults> moviesApiCallback) {
        movieApiService.getMovieTrailers(movieID,Globals.TMDB_API_KEY).enqueue(new Callback<VideoResults>() {

            @Override
            public void onResponse(@NonNull Call<VideoResults> call, @NonNull Response<VideoResults> response) {
                moviesApiCallback.onResponse(response.body());
            }

            @Override
            public void onFailure(@NonNull Call<VideoResults> call, @NonNull Throwable t) {
                if (call.isCanceled()) {
                    Logger.e("Request was cancelled");
                    moviesApiCallback.onCancel();
                } else {
                    Logger.e(t.getMessage());
                    moviesApiCallback.onResponse(null);
                }
            }

        });
    }

    public void getMovieReviews(int movieID, final MoviesApiCallback<Reviews> moviesApiCallback) {
        movieApiService.getMovieReviews(movieID,Globals.TMDB_API_KEY).enqueue(new Callback<Reviews>() {

            @Override
            public void onResponse(@NonNull Call<Reviews> call, @NonNull Response<Reviews> response) {
                moviesApiCallback.onResponse(response.body());
            }

            @Override
            public void onFailure(@NonNull Call<Reviews> call, @NonNull Throwable t) {
                if (call.isCanceled()) {
                    Logger.e("Request was cancelled");
                    moviesApiCallback.onCancel();
                } else {
                    Logger.e(t.getMessage());
                    moviesApiCallback.onResponse(null);
                }
            }

        });
    }
}


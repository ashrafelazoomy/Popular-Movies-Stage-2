

package com.example.adam.popularmoviesstage2.api;

import com.example.adam.popularmoviesstage2.model.Movie;
import com.example.adam.popularmoviesstage2.model.Movies;
import com.example.adam.popularmoviesstage2.model.Reviews;
import com.example.adam.popularmoviesstage2.model.VideoResults;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

interface MovieApiService {

    @GET("movie/top_rated")
    Call<Movies> getTopRatedMovies(@Query("api_key") String apiKey, @Query("page") int page);

    @GET("movie/popular")
    Call<Movies> getPopularMovies(@Query("api_key") String apiKey, @Query("page") int page);

    @GET("movie/{movieId}")
    Call<Movie> getMovie(@Path("movieId") int movieId, @Query("api_key") String apiKey);
    @GET("movie/{movieId}/reviews")
    Call<Reviews> getMovieReviews(@Path("movieId") int movieId, @Query("api_key") String apiKey);
    @GET("movie/{movieId}/videos")
    Call<VideoResults> getMovieTrailers(@Path("movieId") int movieId, @Query("api_key") String apiKey);
}

package com.example.adam.popularmoviesstage2.utils;

import android.content.Context;
import android.util.DisplayMetrics;

import com.example.adam.popularmoviesstage2.BuildConfig;

public class Globals {
    public static final String TMDB_API_KEY = BuildConfig.key_movie_db;
    public static final String TMDB_API_URL = "http://api.themoviedb.org/3/";
    public static final String TMDB_IMAGE_URL = "http://image.tmdb.org/t/p/w185";

    public static final String TMDB_SORT_POP_DESC="popularity.desc";
    public static final String TMDB_SORT_VOTE_AVG_DESC= "vote_average.desc";

    public static int calculateNoOfColumnsInRecyclerView(Context context,int itemWidth) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        float dpWidth = displayMetrics.widthPixels / displayMetrics.density;
        int noOfColumns = (int) (dpWidth / itemWidth);
        return noOfColumns;
    }

    public static float getScreenWidth(Context context) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        float dpWidth = displayMetrics.widthPixels / displayMetrics.density;

        return dpWidth;
    }
}

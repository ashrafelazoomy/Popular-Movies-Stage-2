package com.example.adam.popularmoviesstage2.model;

/**
 * Created by Adam on 3/29/2018.
 */


import android.os.Parcel;
import android.os.Parcelable;

import com.example.adam.popularmoviesstage2.utils.Globals;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class MovieResult implements Parcelable {

    @JsonProperty("id")
    private Integer id;
    @JsonProperty("poster_path")
    private String posterPath;

    public MovieResult() {
        this.id = 0;
        this.posterPath = "";
    }

    public MovieResult(Integer id, String posterPath) {
        this.id = id;
        this.posterPath = posterPath;
    }

    // Parcelable
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeValue(this.id);
        dest.writeString(this.posterPath);
    }

    protected MovieResult(Parcel in) {
        this.id = (Integer) in.readValue(Integer.class.getClassLoader());
        this.posterPath = in.readString();
    }

    public static final Creator<MovieResult> CREATOR = new Creator<MovieResult>() {
        @Override
        public MovieResult createFromParcel(Parcel source) {
            return new MovieResult(source);
        }

        @Override
        public MovieResult[] newArray(int size) {
            return new MovieResult[size];
        }
    };

    // Getter
    public Integer getId() {
        return id;
    }

    public String getPosterPath() {
        if (posterPath.startsWith( "http://image.tmdb.org" )){
            return posterPath;
        }
        return Globals.TMDB_IMAGE_URL+ posterPath;
    }
}

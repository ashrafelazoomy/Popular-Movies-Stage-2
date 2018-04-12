
package com.example.adam.popularmoviesstage2.api;

public interface MoviesApiCallback<T> {
        void onResponse(T result);

        void onCancel();
        }
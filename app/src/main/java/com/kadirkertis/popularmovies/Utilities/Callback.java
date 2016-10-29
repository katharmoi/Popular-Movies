package com.kadirkertis.popularmovies.Utilities;

import com.kadirkertis.popularmovies.MovieInfo;

/**
 * Created by uyan on 01/10/16.
 */
public interface Callback {

    void onMovieSelected(long itemId);
    void onMovieLongPressed(long id,int caller);
}

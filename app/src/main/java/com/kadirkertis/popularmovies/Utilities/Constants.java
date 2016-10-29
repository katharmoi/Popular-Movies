package com.kadirkertis.popularmovies.Utilities;

import com.kadirkertis.popularmovies.data.PopMoviesContract;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class Constants {
    public static final String POPULAR_END_POINT = "popular";
    public static final String RATING_END_POINT = "top_rated";
    public static final String RATING_CATEGORY = "rate_cat";
    public static final String POPULAR_CATEGORY = "pop_cat";
    public static final String YOUTUBE_BASE_URL = "http://www.youtube.com/watch?v=";
    public static final String POSTER_SMALL_BASE_URL = "http://image.tmdb.org/t/p/w185/";
    public static final String POSTER_BASE_URL = "http://image.tmdb.org/t/p/w780/";
    public static final String VIDEOS_END_POINT = "videos";
    public static final String IMAGES_END_POINT = "images";
    public static final String REVIEWS_END_POINT = "reviews";
    public static final Map<Integer, String> GENRES ;
    public static final int CALLER_POPULAR = 0;
    public static final int CALLER_TOP = 1;
    public static final int CALLER_FAV=2;
    public static final int COL_ID = 0;
    public static final int COL_TITLE = 1;
    public static final int COL_RATING = 2;
    public static final int COL_GENRE = 3;
    public static final int COL_DATE = 4;
    public static final int COL_OVERVIEW = 5;
    public static final int COL_POPULARITY = 6;
    public static final int COL_LANGUAGE = 7;
    public static final int COL_POSTER = 8;
    public static final int COL_CATEGORY = 9;

    public static final String[] MOVIES_COLUMNS = {

            PopMoviesContract.FavMoviesTable._ID,
            PopMoviesContract.FavMoviesTable.COLUMN_TITLE,
            PopMoviesContract.FavMoviesTable.COLUMN_RATING,
            PopMoviesContract.FavMoviesTable.COLUMN_GENRE,
            PopMoviesContract.FavMoviesTable.COLUMN_DATE,
            PopMoviesContract.FavMoviesTable.COLUMN_OVERVIEW,
            PopMoviesContract.FavMoviesTable.COLUMN_POPULARITY,
            PopMoviesContract.FavMoviesTable.COLUMN_LANGUAGE,
            PopMoviesContract.FavMoviesTable.COLUMN_POSTER,
    };
    public static final long EMPTY_FAV = -1 ;

    static  {
        Map<Integer, String> tmp = new HashMap<>();
        tmp.put(28, "Action");
        tmp.put(12, "Adventure");
        tmp.put(16, "Animation");
        tmp.put(35, "Comedy");
        tmp.put(80, "Crime");
        tmp.put(99, "Documentary");
        tmp.put(18, "Drama");
        tmp.put(10751, "Family");
        tmp.put(14, "Fantasy");
        tmp.put(36, "History");
        tmp.put(27, "Horror");
        tmp.put(10402, "Music");
        tmp.put(10749, "Romance");
        tmp.put(878, "Science Fiction");
        tmp.put(10770, "TV Movie");
        tmp.put(53, "Thriller");
        tmp.put(10752, "War");
        tmp.put(37, "Western");
        GENRES = Collections.unmodifiableMap(tmp);
    }

}

package com.kadirkertis.popularmovies.data;

import android.content.ContentUris;
import android.content.CursorLoader;
import android.net.Uri;
import android.provider.BaseColumns;
import android.content.ContentResolver;

/**
 * Created by uyan on 31/08/16.
 */
public class PopMoviesContract {

    public static final String CONTENT_AUTHORITY ="com.kadirkertis.popularmovies.app";

    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    public static final String PATH_FAV_MOVIES = "fav_movies";

    public static final String PATH_SAVED_MOVIES="saved_movies";

    public static final class FavMoviesTable implements BaseColumns{

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_FAV_MOVIES).build();


        //Cursor for directory
        public static final String CONTENT_DIR_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/"
                        + PATH_FAV_MOVIES;

        //Cursor for single entry
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/"
                        + PATH_FAV_MOVIES;


        public static final String TABLE_NAME = "fav_movies";

        public static final String COLUMN_TITLE = "title";
        public static final String COLUMN_RATING= "rating";
        public static final String COLUMN_GENRE = "genre";
        public static final String COLUMN_DATE = "date";
        public static final String COLUMN_OVERVIEW= "overview";
        public static final String COLUMN_POPULARITY = "popularity";
        public static final String COLUMN_LANGUAGE = "language";
        public static final String COLUMN_POSTER = "poster";

        public static Uri buildFavMovieUri(long id){
            return ContentUris.withAppendedId(CONTENT_URI,id);
        }


    }

    public static final class SavedMoviesTable implements BaseColumns{

        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon()
                .appendPath(PATH_SAVED_MOVIES)
                .build();

        public static final String CONTENT_DIR_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE +  "/" + CONTENT_AUTHORITY + "/"
                + PATH_SAVED_MOVIES;

        //Single Entry cursor
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE
                +"/" + CONTENT_AUTHORITY + "/" +PATH_SAVED_MOVIES;

        public static final String TABLE_NAME = "saved_movies";

        public static final String COLUMN_TITLE = "title";
        public static final String COLUMN_RATING= "rating";
        public static final String COLUMN_GENRE = "genre";
        public static final String COLUMN_DATE = "date";
        public static final String COLUMN_OVERVIEW= "overview";
        public static final String COLUMN_POPULARITY = "popularity";
        public static final String COLUMN_LANGUAGE = "language";
        public static final String COLUMN_POSTER = "poster";
        public static final String COLUMN_CATEGORY= "category";

        public static Uri buildSavedMovieUri(long id){
            return ContentUris.withAppendedId(CONTENT_URI,id);
        }


    }
}

package com.kadirkertis.popularmovies.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by uyan on 31/08/16.
 */
public class PopMoviesDbHelper extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 1;

    static final String DATABASE_NAME = "movies.db";

    public PopMoviesDbHelper(Context context){
        super(context,DATABASE_NAME,null,DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        final String SQL_CREATE_FAV_MOVIES_TABLE = "CREATE TABLE "
                + PopMoviesContract.FavMoviesTable.TABLE_NAME
                + " ("
                + PopMoviesContract.FavMoviesTable._ID + " INTEGER PRIMARY KEY,"
                + PopMoviesContract.FavMoviesTable.COLUMN_TITLE + " TEXT, "
                + PopMoviesContract.FavMoviesTable.COLUMN_RATING + " REAL, "
                + PopMoviesContract.FavMoviesTable.COLUMN_GENRE + " INTEGER, "
                + PopMoviesContract.FavMoviesTable.COLUMN_DATE + " TEXT, "
                + PopMoviesContract.FavMoviesTable.COLUMN_OVERVIEW + " TEXT, "
                + PopMoviesContract.FavMoviesTable.COLUMN_POPULARITY + " INTEGER, "
                + PopMoviesContract.FavMoviesTable.COLUMN_LANGUAGE + " TEXT, "
                + PopMoviesContract.FavMoviesTable.COLUMN_POSTER + " TEXT "
                +" );";

        final String SQL_CREATE_SAVED_MOVIES_TABLE = "CREATE TABLE "
                + PopMoviesContract.SavedMoviesTable.TABLE_NAME
                + " ("
                + PopMoviesContract.SavedMoviesTable._ID + " INTEGER PRIMARY KEY, "
                + PopMoviesContract.SavedMoviesTable.COLUMN_TITLE + " TEXT, "
                + PopMoviesContract.SavedMoviesTable.COLUMN_RATING + " REAL, "
                + PopMoviesContract.SavedMoviesTable.COLUMN_GENRE + " INTEGER, "
                + PopMoviesContract.SavedMoviesTable.COLUMN_DATE + " TEXT, "
                + PopMoviesContract.SavedMoviesTable.COLUMN_OVERVIEW + " TEXT, "
                + PopMoviesContract.SavedMoviesTable.COLUMN_POPULARITY + " INTEGER, "
                + PopMoviesContract.SavedMoviesTable.COLUMN_LANGUAGE + " TEXT, "
                + PopMoviesContract.SavedMoviesTable.COLUMN_POSTER + " TEXT, "
                +PopMoviesContract.SavedMoviesTable.COLUMN_CATEGORY + " TEXT "
                +" );";

        db.execSQL(SQL_CREATE_FAV_MOVIES_TABLE);
        db.execSQL(SQL_CREATE_SAVED_MOVIES_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + PopMoviesContract.FavMoviesTable.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + PopMoviesContract.SavedMoviesTable.TABLE_NAME);
        onCreate(db);
    }
}

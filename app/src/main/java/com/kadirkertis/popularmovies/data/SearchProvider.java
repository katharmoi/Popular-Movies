package com.kadirkertis.popularmovies.data;

import android.app.SearchManager;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.SearchRecentSuggestionsProvider;
import android.database.Cursor;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by uyan on 08/10/16.
 */
public class SearchProvider extends ContentProvider {

    public static final String AUTHORITY = "com.kadirkertis.popularmovies.SearchProvider";
    private static final String LOG_TAG = SearchProvider.class.getSimpleName();
    private  PopMoviesDbHelper mDbHelper;
    private  SQLiteQueryBuilder builder ;

    @Override
    public boolean onCreate() {
        mDbHelper = new PopMoviesDbHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        if(selectionArgs != null && selectionArgs.length > 0 && selectionArgs[0].length() > 0){
            String formattedSelection = "%" +selectionArgs[0] +"%";
            builder = new SQLiteQueryBuilder();
            Map<String,String> projectionMap= new HashMap<>();
            projectionMap.put(PopMoviesContract.SavedMoviesTable._ID, PopMoviesContract.SavedMoviesTable._ID);
            projectionMap.put(PopMoviesContract.SavedMoviesTable.COLUMN_TITLE,
                    PopMoviesContract.SavedMoviesTable.COLUMN_TITLE + " AS " + SearchManager.SUGGEST_COLUMN_TEXT_1);
            projectionMap.put("MOVIE_ID",PopMoviesContract.SavedMoviesTable._ID
            +" AS " + SearchManager.SUGGEST_COLUMN_INTENT_DATA_ID);
            builder.setProjectionMap(projectionMap);
            builder.setTables(PopMoviesContract.SavedMoviesTable.TABLE_NAME);
            Cursor cursor = builder.query(
                    mDbHelper.getReadableDatabase(),
                    projection,
                    selection,
                    new String[]{formattedSelection},
                    null,
                    null,
                    null);

            cursor.setNotificationUri(getContext().getContentResolver(), uri);
            return cursor;

        }
        else{
            return null;
        }
    }

    @Nullable
    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        return null;
    }

    @Override
    public int delete(Uri uri, String s, String[] strings) {
        return 0;
    }

    @Override
    public int update(Uri uri, ContentValues contentValues, String s, String[] strings) {
        return 0;
    }
}

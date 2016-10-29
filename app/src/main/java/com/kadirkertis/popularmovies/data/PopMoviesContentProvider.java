package com.kadirkertis.popularmovies.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.util.Log;

public class PopMoviesContentProvider extends ContentProvider {

    private final String LOG_TAG = PopMoviesContentProvider.class.getSimpleName();
    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private PopMoviesDbHelper mOpenHelper;

    private static final int FAV_MOVIES = 1;
    private static final int FAV_MOVIES_WITH_ID = 2;
    private static final int SAVED_MOVIES=3;
    private static final int SAVED_MOVIES_WITH_ID = 4;

    static UriMatcher buildUriMatcher(){

        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = PopMoviesContract.CONTENT_AUTHORITY;

        matcher.addURI(authority,PopMoviesContract.PATH_FAV_MOVIES, FAV_MOVIES);
        matcher.addURI(authority,PopMoviesContract.PATH_FAV_MOVIES + "/#" , FAV_MOVIES_WITH_ID);
        matcher.addURI(authority,PopMoviesContract.PATH_SAVED_MOVIES,SAVED_MOVIES);
        matcher.addURI(authority,PopMoviesContract.PATH_SAVED_MOVIES + "/#", SAVED_MOVIES_WITH_ID);

        return matcher;
    }

    @Override
    public boolean onCreate() {
        mOpenHelper = new PopMoviesDbHelper(getContext());
        Log.d(LOG_TAG,"Created Content Provider");
        return true;
    }

    @Nullable
    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);

        switch (match){
            case FAV_MOVIES:
                return PopMoviesContract.FavMoviesTable.CONTENT_DIR_TYPE;
            case FAV_MOVIES_WITH_ID:
                return PopMoviesContract.FavMoviesTable.CONTENT_ITEM_TYPE;
            case SAVED_MOVIES:
                return PopMoviesContract.SavedMoviesTable.CONTENT_DIR_TYPE;
            case SAVED_MOVIES_WITH_ID:
                return PopMoviesContract.SavedMoviesTable.CONTENT_ITEM_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Cursor cursor;
        switch (sUriMatcher.match(uri)){
            case FAV_MOVIES:{
                cursor = mOpenHelper.getReadableDatabase().query(
                        PopMoviesContract.FavMoviesTable.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                cursor.setNotificationUri(getContext().getContentResolver(), uri);
                return cursor;
            }
            case FAV_MOVIES_WITH_ID:{
                cursor = mOpenHelper.getReadableDatabase().query(
                        PopMoviesContract.FavMoviesTable.TABLE_NAME,
                        projection,
                        PopMoviesContract.FavMoviesTable._ID + " =?",
                        new String[]{String.valueOf(ContentUris.parseId(uri))},
                        null,
                        null,
                        sortOrder
                );
                cursor.setNotificationUri(getContext().getContentResolver(), uri);
                return cursor;

            }
            case SAVED_MOVIES: {
                cursor = mOpenHelper.getReadableDatabase().query(
                        PopMoviesContract.SavedMoviesTable.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                cursor.setNotificationUri(getContext().getContentResolver(), uri);
                return cursor;
            }
            case SAVED_MOVIES_WITH_ID : {
                cursor = mOpenHelper.getReadableDatabase().query(
                        PopMoviesContract.SavedMoviesTable.TABLE_NAME,
                        projection,
                        PopMoviesContract.SavedMoviesTable._ID + " =?",
                        new String[]{String.valueOf(ContentUris.parseId(uri))},
                        null,
                        null,
                        sortOrder
                );
                cursor.setNotificationUri(getContext().getContentResolver(), uri);
                return cursor;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        Uri returnUri;

        switch (sUriMatcher.match(uri)){
            case FAV_MOVIES:{
                long _id = db.insert(PopMoviesContract.FavMoviesTable.TABLE_NAME,null,values);

                if(_id > 0)
                    returnUri = PopMoviesContract.FavMoviesTable.buildFavMovieUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into: " + uri);
                break;

            }
            case SAVED_MOVIES:{
                long _id = db.insert(PopMoviesContract.SavedMoviesTable.TABLE_NAME,null,values);
                if(_id > 0)
                    returnUri = PopMoviesContract.SavedMoviesTable.buildSavedMovieUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into: " +uri);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " +uri);
        }

        getContext().getContentResolver().notifyChange(uri,null);
        return returnUri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int numDeleted;

        switch (sUriMatcher.match(uri)){
            case FAV_MOVIES:
                numDeleted = db.delete(PopMoviesContract.FavMoviesTable.TABLE_NAME,
                        selection,
                        selectionArgs);
                break;
            case FAV_MOVIES_WITH_ID:
                numDeleted = db.delete(PopMoviesContract.FavMoviesTable.TABLE_NAME,
                        PopMoviesContract.FavMoviesTable._ID + " =?",
                        new String[]{String.valueOf(ContentUris.parseId(uri))}
                        );
                break;
            case SAVED_MOVIES:
                numDeleted = db.delete(PopMoviesContract.SavedMoviesTable.TABLE_NAME,
                        selection,
                        selectionArgs);
                break;
            case SAVED_MOVIES_WITH_ID:
                numDeleted = db.delete(PopMoviesContract.SavedMoviesTable.TABLE_NAME,
                        PopMoviesContract.SavedMoviesTable._ID + " =?",
                        new String[]{String.valueOf(ContentUris.parseId(uri))}
                );
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " +uri);


        }
        //Notify Cahnges yol mu?
        getContext().getContentResolver().notifyChange(uri,null);
        return numDeleted;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int numUpdated=0;

        if (values == null){
            throw new IllegalArgumentException("Cannot have null content values");
        }

        switch (sUriMatcher.match(uri)){
            case FAV_MOVIES:
                numUpdated = db.update(PopMoviesContract.FavMoviesTable.TABLE_NAME,
                        values,
                        selection,
                        selectionArgs);
                break;
            case FAV_MOVIES_WITH_ID:
                numUpdated = db.update(PopMoviesContract.FavMoviesTable.TABLE_NAME,
                        values,
                        PopMoviesContract.FavMoviesTable._ID + " =?",
                        new String[]{String.valueOf(ContentUris.parseId(uri))});
                break;
            case SAVED_MOVIES:
                numUpdated = db.update(PopMoviesContract.SavedMoviesTable.TABLE_NAME,
                        values,
                        selection,
                        selectionArgs);
                break;
            case SAVED_MOVIES_WITH_ID:
                numUpdated = db.update(PopMoviesContract.SavedMoviesTable.TABLE_NAME,
                        values,
                        PopMoviesContract.SavedMoviesTable._ID + " =?",
                        new String[]{String.valueOf(ContentUris.parseId(uri))});
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " +uri);
        }
        if(numUpdated > 0){
            getContext().getContentResolver().notifyChange(uri,null);
        }

        return numUpdated;
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();

        int numInsert = 0;
        switch (sUriMatcher.match(uri)){
            case FAV_MOVIES:
                db.beginTransaction();
                try{
                    for(ContentValues value : values){
                        if(value == null){
                            throw new IllegalArgumentException("Cannot have null content values");
                        }
                        long _id = -1;

                        try {
                            _id = db.insertOrThrow(PopMoviesContract.FavMoviesTable.TABLE_NAME,
                                    null,
                                    value);

                        }catch (SQLiteConstraintException e){

                        }
                        if(_id != -1){
                            numInsert++;
                        }

                    }
                    if(numInsert > 0)
                        db.setTransactionSuccessful();
                }finally {
                    db.endTransaction();
                }
                if(numInsert > 0)
                    getContext().getContentResolver().notifyChange(uri,null);

                return numInsert;
            case SAVED_MOVIES:
                db.beginTransaction();
                try{
                    for(ContentValues value : values){
                        if(value == null){
                            throw new IllegalArgumentException("Cannot have null content values");
                        }
                        long _id = -1;

                        try {
                            _id = db.insertOrThrow(PopMoviesContract.SavedMoviesTable.TABLE_NAME,
                                    null,
                                    value);

                        }catch (SQLiteConstraintException e){

                        }
                        if(_id != -1){
                            numInsert++;
                        }

                    }
                    if(numInsert > 0)
                        db.setTransactionSuccessful();
                }finally {
                    db.endTransaction();
                }

                if(numInsert > 0)
                    getContext().getContentResolver().notifyChange(uri,null);

                return numInsert;
            default:
                return super.bulkInsert(uri,values);
        }
    }
}

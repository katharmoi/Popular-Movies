package com.kadirkertis.popularmovies.Utilities;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;

import com.kadirkertis.popularmovies.BuildConfig;
import com.kadirkertis.popularmovies.MovieInfo;
import com.kadirkertis.popularmovies.data.PopMoviesContract;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by uyan on 04/10/16.
 */
public class DbHelpers {

    private static final String LOG_TAG =DbHelpers.class.getSimpleName() ;
    private ContentResolver mContentResolver ;
    private Random mRandom;
    private Context mContext;
    private List<DbHelperListener> mListeners;
    private volatile static DbHelpers mInstance;


    private DbHelpers(Context context){
        mContentResolver= context.getContentResolver();
        mRandom = new Random();
        mContext = context;
        mListeners = new ArrayList<>();
    }

    public static DbHelpers getInstance(Context context){
        if(mInstance == null){
            synchronized (DbHelpers.class){
                if(mInstance == null){
                    mInstance = new DbHelpers(context);
                }
            }
        }
        return mInstance;
    }

    public  boolean isInFavDb(int id){
        Cursor cursor = mContentResolver.query(PopMoviesContract.FavMoviesTable.buildFavMovieUri(id),
                null,
                null,
                null,
                null);
        if((cursor!=null) && (cursor.getCount()>0)){
            cursor.close();
            return true;
        }
        cursor.close();
        return false;
    }

    public  void addToFavDb(MovieInfo details,View view){
        int id = details.getId();
        if(!isInFavDb(id)){
            ContentValues cV = new ContentValues();
            cV.put(PopMoviesContract.FavMoviesTable._ID,id);
            cV.put(PopMoviesContract.FavMoviesTable.COLUMN_TITLE, details.getName());
            cV.put(PopMoviesContract.FavMoviesTable.COLUMN_DATE,details.getDate());
            cV.put(PopMoviesContract.FavMoviesTable.COLUMN_OVERVIEW,details.getSynopsis());
            cV.put(PopMoviesContract.FavMoviesTable.COLUMN_POSTER,details.getImageResource());
            cV.put(PopMoviesContract.FavMoviesTable.COLUMN_RATING, details.getRating());

            mContentResolver.insert(PopMoviesContract.FavMoviesTable.CONTENT_URI,
                    cV);
            notifyAllDbHelperListeners(true,id);
            Snackbar.make(view,details.getName() + " added to Favorites",Snackbar.LENGTH_SHORT).show();
        }
        else{
            mContentResolver.delete(PopMoviesContract.FavMoviesTable
                            .buildFavMovieUri(details.getId()),
                    null,
                    null);
            notifyAllDbHelperListeners(false,id);
            Snackbar.make(view,details.getName() + " removed from Favorites",
                    Snackbar.LENGTH_SHORT).show();

        }
        
    }

    public void removeFromFavDb(int id,View view){
        if(isInFavDb(id)){
            mContentResolver.delete(PopMoviesContract.FavMoviesTable
                            .buildFavMovieUri(id),
                    null,
                    null);
            notifyAllDbHelperListeners(false,id);
            Snackbar.make( view,"Movie removed from Db",
                    Snackbar.LENGTH_SHORT).show();
        }
    }

    public String createURL(String endPoint, MovieInfo details) {
        String BASE_URL = "https://api.themoviedb.org/3/movie";
        String APPID_PARAM = "api_key";
        Uri uri = Uri.parse(BASE_URL).buildUpon()
                .appendPath(Integer.toString(details.getId()))
                .appendPath(endPoint)
                .appendQueryParameter(APPID_PARAM, BuildConfig.MOVIE_DB_API_KEY)
                .build();
        return uri.toString();
    }

    public String createURL(String endPoint, long id) {
        String BASE_URL = "https://api.themoviedb.org/3/movie";
        String APPID_PARAM = "api_key";
        Uri uri = Uri.parse(BASE_URL).buildUpon()
                .appendPath(Long.toString(id))
                .appendPath(endPoint)
                .appendQueryParameter(APPID_PARAM, BuildConfig.MOVIE_DB_API_KEY)
                .build();
        return uri.toString();
    }

    public  String getTrailerDataFromJson(JSONObject response) {
        if (response == null || response.length() == 0) {
            return null;
        }
        try {
            if (response.has("results") && !response.isNull("results")) {
                JSONArray results = response.getJSONArray("results");
                JSONObject result = results.getJSONObject(0);
                return result.getString("key");

            }

        } catch (JSONException e) {
            Log.e(LOG_TAG,e.getStackTrace().toString());
        }

        return null;
    }

    public  String getImagesDataFromJson(JSONObject response) {
        if (response == null || response.length() == 0) {
            return null;
        }
        try {
            if (response.has("backdrops") && !response.isNull("backdrops")) {
                JSONArray bd = response.getJSONArray("backdrops");
                JSONObject sP =bd.getJSONObject(mRandom.nextInt(bd.length()));
                return sP.getString("file_path");
            }

        } catch (JSONException e) {
            Log.e(LOG_TAG,e.getStackTrace().toString());
        }

        return null;
    }

    public static interface DbHelperListener{
        void onDataChanged(boolean isAdded, int id);
    }

    public void addDbHelperListener(DbHelperListener listener){
        mListeners.add(listener);
    }

    public void removeDbHelperListener(DbHelperListener listener){
        int i = mListeners.indexOf(listener);
        if(i >= 0){
            mListeners.remove(listener);
        }
    }

    public void notifyAllDbHelperListeners(boolean isAdded,int id){
        for(int i=0; i< mListeners.size(); i++){
            DbHelperListener listener = mListeners.get(i);
            listener.onDataChanged(isAdded,id);
        }
    }

}

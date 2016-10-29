package com.kadirkertis.popularmovies.sync;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SyncRequest;
import android.content.SyncResult;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

import com.kadirkertis.popularmovies.BuildConfig;
import com.kadirkertis.popularmovies.MainActivity;
import com.kadirkertis.popularmovies.R;
import com.kadirkertis.popularmovies.Utilities.Constants;
import com.kadirkertis.popularmovies.data.PopMoviesContract;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by uyan on 19/09/16.
 */
public class MovieSyncAdapter extends AbstractThreadedSyncAdapter {

    private static final String LOG_TAG =MovieSyncAdapter.class.getSimpleName() ;

    public static final int SYNC_INTERVAL = 60 * 180;
    public static final int SYNC_FLEXTIME = SYNC_INTERVAL/3;
    private static final int NOTIFICATION_ID =1983 ;

    ContentResolver mContentResolver;

    public MovieSyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
        mContentResolver = context.getContentResolver();
    }

    @Override
    public void onPerformSync(Account account, Bundle bundle, String s,
                              ContentProviderClient contentProviderClient, SyncResult syncResult) {

        Log.v(LOG_TAG,"Performing Syncing");
        syncMovies(Constants.POPULAR_END_POINT,Constants.POPULAR_CATEGORY);
        syncMovies(Constants.RATING_END_POINT,Constants.RATING_CATEGORY);

    }

    private void syncMovies(String endPoint,String category){
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;


        String moviesJsonStr = null;

        try {
            final String BASE_URL = "https://api.themoviedb.org/3/movie";
            final String APPID_PARAM = "api_key";

            Uri builtUri = Uri.parse(BASE_URL).buildUpon()
                    .appendPath(endPoint)
                    .appendQueryParameter(APPID_PARAM, BuildConfig.MOVIE_DB_API_KEY)
                    .build();

            URL url = new URL(builtUri.toString());

            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            InputStream inputStream = urlConnection.getInputStream();
            StringBuffer buffer = new StringBuffer();
            if (inputStream == null) {
                return;
            }

            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;

            while ((line = reader.readLine()) != null) {
                buffer.append(line + "\n");
            }

            if (buffer.length() == 0) {
                //Nothing to parse
                return;
            }

            moviesJsonStr = buffer.toString();
        } catch (IOException e) {
            Log.e(LOG_TAG, "Connection Error", e);
            return;
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }

            if (reader != null) {
                try {
                    reader.close();
                } catch (final IOException e) {
                    Log.e(LOG_TAG, "Error Closing Stream", e);
                }
            }
        }

        try {
            saveMoviesDataFromJson(moviesJsonStr,category);
        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        }



    }
    private void saveMoviesDataFromJson(String moviesJsonStr,String category)
            throws JSONException {
        try {
            final String TMDB_RESULTS = "results";
            final String TMDB_ID = "id";
            final String TMDB_TITLE = "original_title";
            final String TMDB_VOTE = "vote_average";
            final String TMDB_GENRE = "genre_ids";
            final String TMDB_RELEASE = "release_date";
            final String TMDB_OVERVIEW = "overview";
            final String TMDB_POPULARITY ="popularity";
            final String TMDB_LANGUAGE ="original_language";
            final String TMDB_POSTER_PATH = "poster_path";

            JSONObject moviesJson = new JSONObject(moviesJsonStr);
            JSONArray resultsArray = moviesJson.getJSONArray(TMDB_RESULTS);

            ArrayList<ContentValues> movies = new ArrayList<ContentValues>();
            for (int i = 0; i < resultsArray.length(); i++) {

                JSONObject movie = resultsArray.getJSONObject(i);
                ContentValues movieCV = new ContentValues();
                movieCV.put(PopMoviesContract.SavedMoviesTable._ID, movie.getInt(TMDB_ID));
                movieCV.put(PopMoviesContract.SavedMoviesTable.COLUMN_TITLE, movie.getString(TMDB_TITLE));
                movieCV.put(PopMoviesContract.SavedMoviesTable.COLUMN_RATING, movie.getDouble(TMDB_VOTE));
                movieCV.put(PopMoviesContract.SavedMoviesTable.COLUMN_GENRE,movie.getJSONArray(TMDB_GENRE).getInt(0));
                movieCV.put(PopMoviesContract.SavedMoviesTable.COLUMN_DATE,movie.getString(TMDB_RELEASE));
                movieCV.put(PopMoviesContract.SavedMoviesTable.COLUMN_OVERVIEW, movie.getString(TMDB_OVERVIEW));
                movieCV.put(PopMoviesContract.SavedMoviesTable.COLUMN_POPULARITY,movie.getInt(TMDB_POPULARITY));
                movieCV.put(PopMoviesContract.SavedMoviesTable.COLUMN_LANGUAGE,movie.getString(TMDB_LANGUAGE));
                movieCV.put(PopMoviesContract.SavedMoviesTable.COLUMN_POSTER,Constants.POSTER_SMALL_BASE_URL + movie.getString(TMDB_POSTER_PATH));
                movieCV.put(PopMoviesContract.SavedMoviesTable.COLUMN_CATEGORY,category);
                movies.add(movieCV);

            }
            int inserted = 0;

            if (movies.size() > 0) {
                ContentValues[] conVals = new ContentValues[movies.size()];
                movies.toArray(conVals);

                //Get the Ids of fetced movies
                Set<Integer> newIds = new HashSet<>();
                for(ContentValues cV : conVals){
                    newIds.add(cV.getAsInteger(PopMoviesContract.SavedMoviesTable._ID));
//                    Log.d(LOG_TAG, "New ids : " +cV.getAsInteger(PopMoviesContract.SavedMoviesTable._ID));
                }



                //Get the ids of movies in db
                Cursor c = mContentResolver.query(PopMoviesContract.SavedMoviesTable.CONTENT_URI,
                        new String[]{PopMoviesContract.SavedMoviesTable._ID},
                        null,
                        null,
                        null
                        );
                Set<Integer> savedIds = new HashSet<>();
                while (c.moveToNext()){
                    savedIds.add(c.getInt(0));
//                    Log.d(LOG_TAG, "New ids : " +c.getInt(0));
                }
                c.close();

                mContentResolver.delete(PopMoviesContract.SavedMoviesTable.CONTENT_URI,
                        PopMoviesContract.SavedMoviesTable.COLUMN_CATEGORY +" = ?",
                        new String[]{category}
                        );

                inserted = mContentResolver
                        .bulkInsert(PopMoviesContract.SavedMoviesTable.CONTENT_URI, conVals);
                //if there are new movies notify user
                savedIds.removeAll(newIds);
                if(savedIds.size() > 0){
                    showNotification();
                }

            }
//            Log.d(LOG_TAG,inserted + " rows inserted to the database succesfully");

        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        }


    }

    private void showNotification(){
        Context context = getContext();
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        Boolean isNotificationsEnabled = preferences
                .getBoolean(context.getString(R.string.pref_notifications_enabled_key),
                        Boolean.parseBoolean(context.getString(R.string.pref_notifications_enabled_default)));

        if(isNotificationsEnabled) {

            NotificationCompat.Builder notBuilder =
                    new NotificationCompat.Builder(context)
                            .setSmallIcon(R.drawable.ic_action_add)
                            .setContentTitle(context.getString(R.string.app_name))
                            .setContentText(context.getString(R.string.notification_new_movies_added))
                            .setAutoCancel(true)
                            .setPriority(NotificationCompat.PRIORITY_MAX)
                            .setDefaults(NotificationCompat.DEFAULT_ALL);


            Intent intent = new Intent(context, MainActivity.class);
            TaskStackBuilder tsb = TaskStackBuilder.create(context);
            tsb.addNextIntent(intent);
            PendingIntent pendingIntent = tsb.getPendingIntent(0,
                    PendingIntent.FLAG_UPDATE_CURRENT);

            notBuilder.setContentIntent(pendingIntent);

            NotificationManager notificationManager =
                    (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.notify(NOTIFICATION_ID, notBuilder.build());


        }

    }

    public static Account getSyncAccount(Context context) {
        Account newAccount = new Account(
                context.getString(R.string.app_name), context.getString(R.string.sync_account_type));

        AccountManager accountManager =
                (AccountManager) context.getSystemService(Context.ACCOUNT_SERVICE);

        if(accountManager.getPassword(newAccount) == null){

            if (!accountManager.addAccountExplicitly(newAccount,"",null)) {

                return null;
            }

            //Dummy Account created succesfully
            //Make  initialization

            onAccountCreated(newAccount,context);
        }
        return newAccount;

    }

    public static  void syncImmediately(Context context){
        Bundle settingsBundle = new Bundle();
        settingsBundle.putBoolean(
                ContentResolver.SYNC_EXTRAS_MANUAL, true);
        settingsBundle.putBoolean(
                ContentResolver.SYNC_EXTRAS_EXPEDITED, true);

        ContentResolver.requestSync(getSyncAccount(context), context.getString(R.string.content_authority), settingsBundle);


    }

    public static void configurePeriodicSync(Context context, int syncInterval, int flexTime) {
        Account account = getSyncAccount(context);
        String authority = context.getString(R.string.content_authority);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            // Stagger initialization
            //So that every app does not start
            //sync at exactly the same time
            SyncRequest request = new SyncRequest.Builder().
                    syncPeriodic(syncInterval, flexTime).
                    setSyncAdapter(account, authority).
                    setExtras(new Bundle()).build();
            ContentResolver.requestSync(request);
        } else {
            ContentResolver.addPeriodicSync(account,
                    authority, new Bundle(), syncInterval);
        }
    }

    private static void onAccountCreated(Account newAccount, Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        int syncInterval = preferences.getInt(context.getString(R.string.pref_sync_freq_key),
                SYNC_INTERVAL);
        MovieSyncAdapter.configurePeriodicSync(context, syncInterval, SYNC_FLEXTIME);
        ContentResolver.setSyncAutomatically(newAccount, context.getString(R.string.content_authority), true);
        syncImmediately(context);
    }

    public static void initializeSyncAdapter(Context context) {
        getSyncAccount(context);
    }

}

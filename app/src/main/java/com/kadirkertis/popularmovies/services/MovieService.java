package com.kadirkertis.popularmovies.services;

import android.app.IntentService;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import com.kadirkertis.popularmovies.BuildConfig;
import com.kadirkertis.popularmovies.MovieInfo;
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

public class MovieService extends IntentService {
    private static final String LOG_TAG = MovieService.class.getSimpleName();
    public static final String REQ_TYPE = "rtype";

    public MovieService() {
        super("PopularMovies");
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        String reqType = intent.getStringExtra(REQ_TYPE);
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        //raw JSON response string
        String moviesJsonStr = null;

        try {
            final String BASE_URL = "https://api.themoviedb.org/3/movie";
            final String APPID_PARAM = "api_key";

            Uri builtUri = Uri.parse(BASE_URL).buildUpon()
                    .appendPath(reqType)
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
            getMoviesDataFromJson(moviesJsonStr);
        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        }
        return;

    }

    private void getMoviesDataFromJson(String moviesJsonStr)
            throws JSONException {
        try {
            final String TMDB_RESULTS = "results";
            final String TMDB_ID = "id";
            final String TMDB_TITLE = "original_title";
            final String TMDB_RELEASE = "release_date";
            final String TMDB_VOTE = "vote_average";
            final String TMDB_OVERVIEW = "overview";
            final String TMDB_POSTER_PATH = "poster_path";

            JSONObject moviesJson = new JSONObject(moviesJsonStr);
            JSONArray resultsArray = moviesJson.getJSONArray(TMDB_RESULTS);

            ArrayList<ContentValues> movies = new ArrayList<ContentValues>();
            for (int i = 0; i < resultsArray.length(); i++) {

                JSONObject movie = resultsArray.getJSONObject(i);
                ContentValues movieCV = new ContentValues();
                movieCV.put(PopMoviesContract.SavedMoviesTable._ID, movie.getInt(TMDB_ID));
                movieCV.put(PopMoviesContract.SavedMoviesTable.COLUMN_TITLE, movie.getString(TMDB_TITLE));
                movieCV.put(PopMoviesContract.SavedMoviesTable.COLUMN_RATING, movie.getString(TMDB_VOTE));
                movieCV.put(PopMoviesContract.SavedMoviesTable.COLUMN_POSTER, movie.getString(TMDB_POSTER_PATH));
                movieCV.put(PopMoviesContract.SavedMoviesTable.COLUMN_DATE, movie.getString(TMDB_RELEASE));
                movieCV.put(PopMoviesContract.SavedMoviesTable.COLUMN_OVERVIEW, movie.getString(TMDB_OVERVIEW));


                movies.add(movieCV);

            }
            int inserted = 0;

            if (movies.size() > 0) {
                inserted = this.getContentResolver()
                        .bulkInsert(PopMoviesContract.SavedMoviesTable.CONTENT_URI, (ContentValues[]) movies.toArray());
            }
            Log.d(LOG_TAG,inserted + " rows inserted to the database succesfully");

        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        }


    }

    public static class AlarmReceiver extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {
            Intent myServiceIntent = new Intent(context,MovieService.class);
            myServiceIntent.putExtra(REQ_TYPE,intent.getStringExtra(REQ_TYPE));
            context.startService(myServiceIntent);

        }
    }
}
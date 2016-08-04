package com.kadirkertis.popularmovies;


import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Fragment;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Toast;

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
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;


/**
 * A simple {@link Fragment} subclass.
 */
public class MoviesFragment extends Fragment {
    private final String POPULARITY_FLAG = "popular";
    private final String RATING_FLAG = "top_rated";
    private final String LIST_POPULAR = "listByPop";
    private final String LIST_VOTE = "listByVote";
    private final String ORDER_PREFERENCE = "order";

    private MovieInfoAdapter mMovieAdapter;
    private ArrayList<MovieInfo> mMoviesByPopularityList;
    private ArrayList<MovieInfo> mMoviesByVoteList;

    public MoviesFragment() {
        // Required empty public constructor
    }

    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        if(savedInstanceState == null  ){
            mMoviesByVoteList = new ArrayList<MovieInfo>();
            mMoviesByPopularityList = new ArrayList<MovieInfo>();
            mMovieAdapter = new MovieInfoAdapter(getActivity(),new ArrayList<MovieInfo>());
            new GetMoviesTask().execute();
        }else{
            if (PreferenceManager
                    .getDefaultSharedPreferences(getActivity())
                    .getString(ORDER_PREFERENCE, "popular").equals("vote")) {
                mMoviesByVoteList = savedInstanceState.getParcelableArrayList(LIST_VOTE);
                mMovieAdapter = new MovieInfoAdapter(getActivity(),mMoviesByVoteList);

            }else{
                mMoviesByPopularityList = savedInstanceState.getParcelableArrayList(LIST_POPULAR);
                mMovieAdapter = new MovieInfoAdapter(getActivity(),mMoviesByPopularityList);
            }


        }
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState){
        View root = inflater.inflate(R.layout.fragment_movies,container,false);

        GridView mMovieGridView = (GridView) root.findViewById(R.id.movies_grid);
        mMovieGridView.setAdapter(mMovieAdapter);
        mMovieGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                MovieInfo selectedMovie = mMovieAdapter.getItem(position);
                Intent intent = new Intent(getActivity(),DetailsActivity.class);
                intent.putExtra(DetailsActivity.MOVIE_TITLE,selectedMovie.getName());
                intent.putExtra(DetailsActivity.MOVIE_IMG,selectedMovie.getImageResource());
                intent.putExtra(DetailsActivity.MOVIE_SYN,selectedMovie.getSynopsis());
                intent.putExtra(DetailsActivity.MOVIE_RATING,selectedMovie.getRating());
                intent.putExtra(DetailsActivity.MOVIE_DATE,selectedMovie.getDate());
                startActivity(intent);
            }
        });
        return root;
    }

    public void onSaveInstanceState(Bundle outState){
        outState.putParcelableArrayList(LIST_POPULAR,mMoviesByPopularityList);
        outState.putParcelableArrayList(LIST_VOTE,mMoviesByVoteList);
        super.onSaveInstanceState(outState);
    }

    public void onCreateOptionsMenu(Menu menu, MenuInflater inflator){
        inflator.inflate(R.menu.main_menu,menu);
    }

    public boolean onOptionsItemSelected(MenuItem item){
        int id = item.getItemId();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        SharedPreferences.Editor editor = prefs.edit();
        if(id == R.id.action_sort_by_pop){
            editor.putString(ORDER_PREFERENCE,"popular");
            editor.apply();
            new GetMoviesTask().execute();
            return true;
        }
        if( id == R.id.action_sort_by_rating){
            editor.putString(ORDER_PREFERENCE,"vote");
            editor.apply();
            new GetMoviesTask().execute();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private class GetMoviesTask extends AsyncTask<Void,Void,ArrayList<MovieInfo>>{
        ProgressDialog netDialog;

        private final String LOG_TAG = GetMoviesTask.class.getSimpleName();
        private ArrayList<MovieInfo> getMoviesDataFromJson(String moviesJsonStr)
        throws JSONException{
            final String TMDB_RESULTS ="results";
            final String TMDB_ID = "id";
            final String TMDB_TITLE = "original_title";
            final String TMDB_RELEASE = "release_date";
            final String TMDB_VOTE="vote_average";
            final String TMDB_OVERVIEW = "overview";
            final String TMDB_POSTER_PATH = "poster_path";

            JSONObject moviesJson = new JSONObject(moviesJsonStr);
            JSONArray resultsArray = moviesJson.getJSONArray(TMDB_RESULTS);

            ArrayList<MovieInfo> movies = new ArrayList<>();
            for (int i = 0; i < resultsArray.length(); i++) {

                JSONObject movie = resultsArray.getJSONObject(i);
                MovieInfo movieInfo = new MovieInfo(movie.getInt(TMDB_ID),
                        movie.getString(TMDB_TITLE),
                        movie.getString(TMDB_VOTE),
                        movie.getString(TMDB_POSTER_PATH),
                        movie.getString(TMDB_RELEASE),
                        movie.getString(TMDB_OVERVIEW)

                        );
                movies.add(movieInfo);

            }

            return  movies;
        }

        private ArrayList<MovieInfo> getMoviesData(String reqType){
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            //raw JSON response string
            String moviesJsonStr = null;

            try{
                final String BASE_URL = "https://api.themoviedb.org/3/movie";
                final String APPID_PARAM = "api_key";

                Uri builtUri = Uri.parse(BASE_URL).buildUpon()
                        .appendPath(reqType)
                        .appendQueryParameter(APPID_PARAM,BuildConfig.MOVIE_DB_API_KEY)
                        .build();

                URL url = new URL(builtUri.toString());

                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if(inputStream == null){
                    return null;
                }

                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;

                while((line = reader.readLine()) != null){
                    buffer.append(line + "\n");
                }

                if(buffer.length() == 0){
                    //Nothing to parse
                    return null;
                }

                moviesJsonStr = buffer.toString();
            }catch (IOException e){
                Log.e(LOG_TAG,"Connection Error",e);
                return null;
            }finally {
                if(urlConnection != null){
                    urlConnection.disconnect();
                }

                if(reader != null){
                    try{
                        reader.close();
                    }catch(final IOException e){
                        Log.e(LOG_TAG,"Error Closing Stream",e);
                    }
                }
            }

            try {
                return getMoviesDataFromJson(moviesJsonStr);
            } catch (JSONException e) {
                Log.e(LOG_TAG, e.getMessage(), e);
                e.printStackTrace();
            }
            return null;
        }

        private boolean isNetworkAvailable(){
            ConnectivityManager cm = (ConnectivityManager) getActivity().getSystemService(
                    getActivity().CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = cm.getActiveNetworkInfo();
            return networkInfo != null && networkInfo.isConnectedOrConnecting();
        }

        protected void onPreExecute(){
            super.onPreExecute();
            netDialog = new ProgressDialog(getActivity());
            netDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            netDialog.setTitle(getString(R.string.progress_title));
            netDialog.show();
        }

        protected ArrayList<MovieInfo> doInBackground(Void... params){
            if(isNetworkAvailable()) {
                if (PreferenceManager
                        .getDefaultSharedPreferences(getActivity())
                        .getString(ORDER_PREFERENCE, "popular").equals("popular")) {
                    mMoviesByPopularityList = getMoviesData(POPULARITY_FLAG);
                    return mMoviesByPopularityList;
                } else if(PreferenceManager
                        .getDefaultSharedPreferences(getActivity())
                        .getString(ORDER_PREFERENCE, "popular").equals("vote")){
                    mMoviesByVoteList = getMoviesData(RATING_FLAG);
                    return mMoviesByVoteList;
                }
                else {
                    mMoviesByPopularityList = getMoviesData(POPULARITY_FLAG);
                    return mMoviesByPopularityList;
                }
            }
            else{
                getActivity().runOnUiThread(new Runnable() {
                    public void run() {
                        Toast.makeText(getActivity(),
                                "No Internet Connection",
                                Toast.LENGTH_SHORT).show();
                    }
                });

            }

            return null;
        }

        protected void onPostExecute(ArrayList<MovieInfo> result){
            if(netDialog.isShowing())
                netDialog.dismiss();
            if(result != null){
                mMovieAdapter.clear();
                for(MovieInfo movieInfo: result){
                    mMovieAdapter.add(movieInfo);
                }

            }


        }
    }

}

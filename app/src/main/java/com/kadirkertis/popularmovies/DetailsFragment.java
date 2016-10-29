package com.kadirkertis.popularmovies;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;
import com.android.volley.AuthFailureError;
import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.kadirkertis.popularmovies.Utilities.Constants;
import com.kadirkertis.popularmovies.Utilities.DbHelpers;
import com.kadirkertis.popularmovies.Utilities.VolleySingleton;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class DetailsFragment extends Fragment implements View.OnClickListener , LoaderManager.LoaderCallbacks<Cursor>,
DbHelpers.DbHelperListener{

    private static final int LOADER_ID =111 ;
    private final String LOG_TAG = this.getClass().getSimpleName();
    public static final String MOVIE_DETAILS = "details";
    private RequestQueue mRequestQueue;
    private TextView mReviewsView;
    private MovieInfo mDetails;
    private ImageButton mAddBtn;
    private ImageButton mShareBtn;
    private DbHelpers mDbHelper;
    private TextView mTitleView;
    private ImageView mImageView;
    private TextView mSynopsisView;
    private TextView mRateView;
    private TextView mDateView;
    private TextView mGenreView;
    private TextView mPopularityTextView;
    private TextView mVotesTextView;
    private TextView mRatingHeader;
    private RatingBar mRatingBar;
    private Uri mUri;
    private boolean mTwoPane;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        if (args != null) {
            mUri = args.getParcelable(MOVIE_DETAILS);
        }

        VolleySingleton mVolley = VolleySingleton.getInstance();
        mRequestQueue = mVolley.getRequestQueue();
        mDbHelper = DbHelpers.getInstance(getActivity());


    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_details, container, false);
        mReviewsView = (TextView) rootView.findViewById(R.id.details_reviews);
        mRatingBar = (RatingBar) rootView.findViewById(R.id.ratingBar);
        mTitleView = (TextView) rootView.findViewById(R.id.details_title);
        mImageView = (ImageView) rootView.findViewById(R.id.details_img);
        mSynopsisView = (TextView) rootView.findViewById(R.id.details_synopsis);
        mRateView = (TextView) rootView.findViewById(R.id.details_rating);
        mDateView = (TextView) rootView.findViewById(R.id.details_date);
        mGenreView = (TextView) rootView.findViewById(R.id.genre_text);
        mPopularityTextView = (TextView) rootView.findViewById(R.id.popularity_text);
        mVotesTextView = (TextView) rootView.findViewById(R.id.votes_text);
        mRatingHeader = (TextView) rootView.findViewById(R.id.rating_header);
        mAddBtn = (ImageButton) rootView.findViewById(R.id.add_to_fav_btn);
        mDbHelper.addDbHelperListener(this);
        mAddBtn.setOnClickListener(this);
        mShareBtn = (ImageButton) rootView.findViewById(R.id.share_btn);
        mShareBtn.setOnClickListener(this);
        return rootView;
    }



    public void populateReviews() {
        JsonObjectRequest requestEndPoint = new JsonObjectRequest(
                Request.Method.GET,
                createURL(Constants.REVIEWS_END_POINT,mDetails),
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(final JSONObject response) {
                        String reviewStr = getReviewsDataFromJson(response);
                        if(reviewStr != null){
                            mReviewsView.setText(reviewStr);
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if (error instanceof TimeoutError || error instanceof NoConnectionError) {
                            Toast.makeText(getActivity(), getString(R.string.connection_error),
                                    Toast.LENGTH_SHORT).show();
                        } else if (error instanceof ParseError) {
                            Toast.makeText(getActivity(),getString(R.string.parse_error), Toast.LENGTH_SHORT).show();

                        } else if (error instanceof ServerError) {
                            Toast.makeText(getActivity(),getString(R.string.connection_error),
                                    Toast.LENGTH_SHORT).show();

                        } else if (error instanceof NetworkError) {
                            Toast.makeText(getActivity(), getString(R.string.connection_error),
                                    Toast.LENGTH_SHORT).show();

                        } else if (error instanceof AuthFailureError) {


                        }

                    }
                }
        );

        mRequestQueue.add(requestEndPoint);

    }



    private  String getReviewsDataFromJson(JSONObject response) {
        StringBuilder formattedReviews = new StringBuilder();

        if (response == null || response.length() == 0) {
            return null;
        }
        try {
            if (response.has("results") && !response.isNull("results")) {
                JSONArray results = response.getJSONArray("results");
                for(int i=0;i<results.length();i++){
                    JSONObject result = results.getJSONObject(i);
                    String author = result.getString("author");
                    formattedReviews.append(author + "\n");
                    String content = result.getString("content");
                    formattedReviews.append(content +"\n");
                }

                return formattedReviews.toString();
            }

        } catch (JSONException e) {
            Log.e(LOG_TAG,e.getStackTrace().toString());
        }

        return null;
    }

    private String createURL(String endPoint, MovieInfo details) {
        String BASE_URL = "https://api.themoviedb.org/3/movie";
        String APPID_PARAM = "api_key";
        Uri uri = Uri.parse(BASE_URL).buildUpon()
                .appendPath(Integer.toString(details.getId()))
                .appendPath(endPoint)
                .appendQueryParameter(APPID_PARAM, BuildConfig.MOVIE_DB_API_KEY)
                .build();
        return uri.toString();
    }


    @Override
    public void onClick(View view) {
        if(view == mShareBtn ){
            Intent intent = new Intent(Intent.ACTION_SEND);
            String message = "Check out this movie : "
                    +mDetails.getName()
                    +"\n "
                    +mDetails.getRating();
            intent.putExtra(Intent.EXTRA_TEXT,message);
            intent.setType("text/plain");
            startActivity(intent);

        }
        if(view == mAddBtn){
            mDbHelper.addToFavDb(mDetails,view);
        }
    }

    public int getCurrentItemId(){
        return mDetails.getId();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        mTwoPane = getActivity().findViewById(R.id.details_fragment_container)!= null;
        getLoaderManager().initLoader(LOADER_ID,null,this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(
                getActivity(),
                mUri,
                Constants.MOVIES_COLUMNS,
                null,
                null,
                null
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if(data.moveToFirst()){
        mDetails = new MovieInfo(
                data.getInt(Constants.COL_ID),
                data.getString(Constants.COL_TITLE),
                data.getDouble(Constants.COL_RATING),
                data.getString(Constants.COL_POSTER),
                data.getString(Constants.COL_DATE),
                data.getString(Constants.COL_OVERVIEW),
                data.getInt(Constants.COL_GENRE),
                data.getInt(Constants.COL_POPULARITY));
        populateReviews();
        }
        if (mDetails != null) {

            mTitleView.setText(mDetails.getName());
            Picasso.with(getActivity()).load(
                    mDetails.getImageResource()).into(mImageView);
            mSynopsisView.setText(mDetails.getSynopsis());
            mRateView.setText("Rating: " + mDetails.getRating());
            mDateView.setText("Release: " + mDetails.getDate());
            if(Constants.GENRES.containsKey(mDetails.getGenre())){
                mGenreView.setText(Constants.GENRES.get(mDetails.getGenre()));
            }else {
                mGenreView.setText("");
            }
            mPopularityTextView.setText(String.format("%1$d",mDetails.getPopularity()));
            mVotesTextView.setText(String.format("%1$.1f",mDetails.getRating()));
            mRatingHeader.setText(String.format("%1$.1f",mDetails.getRating()));
            mRatingBar.setRating((float)mDetails.getRating()/2.0f);
            if(!mTwoPane){
                if(mDbHelper.isInFavDb(mDetails.getId())){
                    mAddBtn.setImageDrawable(ContextCompat.getDrawable(getActivity(),R.drawable.ic_remove));
                }
                else {
                    mAddBtn.setImageDrawable(ContextCompat.getDrawable(getActivity(),R.drawable.ic_action_add));
                }
            }


        } else {
            Log.d(LOG_TAG, "No details");
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    @Override
    public void onDataChanged(boolean isAdded,int id) {
        if(isAdded){
            mAddBtn.setImageDrawable(ContextCompat.getDrawable(getActivity(),R.drawable.ic_remove));
        }
        else {
            mAddBtn.setImageDrawable(ContextCompat.getDrawable(getActivity(),R.drawable.ic_action_add));
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mDbHelper.removeDbHelperListener(this);
    }
}

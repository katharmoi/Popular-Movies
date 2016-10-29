package com.kadirkertis.popularmovies;

import android.annotation.TargetApi;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.NavUtils;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;
import java.util.Random;
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
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.firebase.auth.FirebaseAuth;
import com.kadirkertis.popularmovies.Utilities.Constants;
import com.kadirkertis.popularmovies.Utilities.DbHelpers;
import com.kadirkertis.popularmovies.Utilities.VolleySingleton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class DetailsActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {
    private final static String LOG_TAG = DetailsActivity.class.getSimpleName();
    private static final int LOADER_ID = 232;
    private FloatingActionButton mFab;
    private Uri mUri;
    private FirebaseAuth mAuth;
    private MovieInfo mDetails = null;
    private VolleySingleton volley;
    private ImageLoader mImageLoader;
    private ImageView mToolbarImage;
    private RequestQueue mRequestQueue;
    private DbHelpers mDbHelper;
    private ImageButton mYoutubeView;
    private Toolbar mToolbar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);
        if (savedInstanceState == null) {

            Bundle args = new Bundle();
            mUri = getIntent().getData();
            args.putParcelable(DetailsFragment.MOVIE_DETAILS, mUri);
            DetailsFragment fragment = new DetailsFragment();
            fragment.setArguments(args);


            getSupportFragmentManager().beginTransaction()
                    .add(R.id.movie_detail_container, fragment)
                    .commit();
        }
        getSupportLoaderManager().initLoader(LOADER_ID,null,this);
        mDbHelper = DbHelpers.getInstance(this);
        mDbHelper.addDbHelperListener(new DbHelpers.DbHelperListener() {
            @Override
            public void onDataChanged(boolean isAdded,int id) {
                if(isAdded){
                    mFab.setBackgroundTintList(ColorStateList.valueOf(
                            ContextCompat.getColor(DetailsActivity.this,R.color.colorAccent)));
                }
                else {
                    mFab.setBackgroundTintList(ColorStateList.valueOf(
                            ContextCompat.getColor(DetailsActivity.this,R.color.colorPrimaryLight)));
                }
            }
        });
        volley = VolleySingleton.getInstance();
        mRequestQueue = volley.getRequestQueue();
        mImageLoader = volley.getImageLoader();
        mToolbarImage = (ImageView)findViewById(R.id.details_poster_big);
        mToolbar = (Toolbar) findViewById(R.id.details_ac_toolbar);
        mYoutubeView = (ImageButton) findViewById(R.id.details_fab_youtube);
        mToolbar.setNavigationIcon(ContextCompat.getDrawable(this,R.drawable.ic_action_arrow_left));
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        mAuth = FirebaseAuth.getInstance();
        mFab = (FloatingActionButton)findViewById(R.id.details_fab);

    }



    private void populateHeaderImage() {
        JsonObjectRequest requestEndPoint = new JsonObjectRequest(
                Request.Method.GET,
                mDbHelper.createURL(Constants.IMAGES_END_POINT,mDetails),
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(final JSONObject response) {
                        String imagesEnd = mDbHelper.getImagesDataFromJson(response);
                        mImageLoader.get(Constants.POSTER_BASE_URL + imagesEnd,
                                ImageLoader.getImageListener(mToolbarImage,
                                        R.drawable.poster_placeholder_large,
                                        R.drawable.poster_placeholder_large));

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if (error instanceof TimeoutError || error instanceof NoConnectionError) {
                            Toast.makeText(getApplicationContext(), getString(R.string.connection_error),
                                    Toast.LENGTH_SHORT).show();
                        } else if (error instanceof ParseError) {
                            Toast.makeText(getApplicationContext(),getString(R.string.parse_error), Toast.LENGTH_SHORT).show();

                        } else if (error instanceof ServerError) {
                            Toast.makeText(getApplicationContext(),getString(R.string.connection_error),
                                    Toast.LENGTH_SHORT).show();

                        } else if (error instanceof NetworkError) {
                            Toast.makeText(getApplicationContext(), getString(R.string.connection_error),
                                    Toast.LENGTH_SHORT).show();

                        } else if (error instanceof AuthFailureError) {

                            Toast.makeText(getApplicationContext(), getString(R.string.connection_error),
                                    Toast.LENGTH_SHORT).show();
                        }

                    }
                }
        );

        mRequestQueue.add(requestEndPoint);

    }



    public  void populateTrailer() {
        JsonObjectRequest requestEndPoint = new JsonObjectRequest(
                Request.Method.GET,
                mDbHelper.createURL(Constants.VIDEOS_END_POINT,mDetails),
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(final JSONObject response) {
                        final String responseStr = mDbHelper.getTrailerDataFromJson(response);
                        if(responseStr != null) {
                            mYoutubeView.setVisibility(View.VISIBLE);
                            mYoutubeView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    startActivity(new Intent(Intent.ACTION_VIEW,
                                            Uri.parse(Constants.YOUTUBE_BASE_URL + responseStr)));
                                }
                            });
                        }else {
                            mYoutubeView.setVisibility(View.INVISIBLE);
                        }

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if (error instanceof TimeoutError || error instanceof NoConnectionError) {
                            Toast.makeText(getApplicationContext(), getString(R.string.connection_error),
                                    Toast.LENGTH_SHORT).show();
                        } else if (error instanceof ParseError) {
                            Toast.makeText(getApplicationContext(), getString(R.string.parse_error), Toast.LENGTH_SHORT).show();

                        } else if (error instanceof ServerError) {
                            Toast.makeText(getApplicationContext(),   getString(R.string.connection_error),
                                    Toast.LENGTH_SHORT).show();

                        } else if (error instanceof NetworkError) {
                            Toast.makeText(getApplicationContext(),getString(R.string.connection_error),
                                    Toast.LENGTH_SHORT).show();

                        } else if (error instanceof AuthFailureError) {

                            Toast.makeText(getApplicationContext(),getString(R.string.connection_error),
                                    Toast.LENGTH_SHORT).show();
                        }

                    }
                }
        );

        mRequestQueue.add(requestEndPoint);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(
                this,
                mUri,
                Constants.MOVIES_COLUMNS,
                null,
                null,
                null
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if(data.moveToFirst()) {
            mDetails = new MovieInfo(
                    data.getInt(Constants.COL_ID),
                    data.getString(Constants.COL_TITLE),
                    data.getDouble(Constants.COL_RATING),
                    data.getString(Constants.COL_POSTER),
                    data.getString(Constants.COL_DATE),
                    data.getString(Constants.COL_OVERVIEW),
                    data.getInt(Constants.COL_GENRE),
                    data.getInt(Constants.COL_POPULARITY));
            mToolbar.setTitle(mDetails.getName());
            if(mDbHelper.isInFavDb(mDetails.getId())){
                mFab.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this,R.color.colorAccent)));
            }else {
                mFab.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this,R.color.colorPrimaryLight)));
            }
            mFab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mAuth.getCurrentUser() != null) {

                        if (mDetails != null) {
                            mDbHelper.addToFavDb(mDetails, view);

                        }

                    } else {
                        startActivity(new Intent(getApplicationContext(), SignUpActivity.class));
                    }
                }
            });
        }
        populateHeaderImage();
        populateTrailer();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
}

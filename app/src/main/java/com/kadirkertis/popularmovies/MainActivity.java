package com.kadirkertis.popularmovies;

import android.app.SearchManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.transition.TransitionManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.support.design.widget.TabLayout;

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
import com.kadirkertis.popularmovies.Utilities.Callback;

import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.support.v4.widget.CursorAdapter;
import android.support.v4.widget.SimpleCursorAdapter;
import android.widget.Toast;

import com.kadirkertis.popularmovies.Utilities.Constants;
import com.kadirkertis.popularmovies.Utilities.DbHelpers;
import com.kadirkertis.popularmovies.Utilities.VolleySingleton;
import com.kadirkertis.popularmovies.data.PopMoviesContract;
import com.kadirkertis.popularmovies.data.SearchProvider;
import com.kadirkertis.popularmovies.sync.MovieSyncAdapter;

import org.json.JSONObject;


public class MainActivity extends AppCompatActivity implements Callback, NavigationView.OnNavigationItemSelectedListener {

    private static final String SELECTED_DRAWER_ITEM = "selected";
    private static final String[] FRAGMENTS = {"popular", "top", "favourite"};
    private DetailsFragment[] mFragments = new DetailsFragment[3];
    private ViewPager mPager;
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;
    private int mSelectedDrawerItemId;
    private VolleySingleton mVolley;
    private RequestQueue mRequestQueue;
    private DbHelpers mDbHelper;
    private boolean mTwoPane = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (findViewById(R.id.details_fragment_container) != null) {
            mTwoPane = true;
            NoItemFragment fr = new NoItemFragment();
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.details_fragment_container, fr)
                    .commit();
        }
        Toolbar mToolBar = (Toolbar) findViewById(R.id.app_bar);
        setSupportActionBar(mToolBar);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        NavigationView navigationView = (NavigationView) findViewById(R.id.drawer);
        navigationView.setNavigationItemSelectedListener(this);
        mDrawerToggle = new ActionBarDrawerToggle(this,
                mDrawerLayout,
                mToolBar,
                R.string.drawer_open,
                R.string.drawer_closed
        );
        mDrawerLayout.setDrawerListener(mDrawerToggle);
        mDrawerToggle.syncState();
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tab_layout);
        mPager = (ViewPager) findViewById(R.id.pager);
        mPager.setOffscreenPageLimit(3);
        MyPagerAdapter myPagerAdapter = new MyPagerAdapter(getSupportFragmentManager());
        mPager.setAdapter(myPagerAdapter);
        tabLayout.setupWithViewPager(mPager);
        tabLayout.setupWithViewPager(mPager);
        mPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout) {

            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                if (mTwoPane) {

                    if (mFragments[position] != null) {
                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.details_fragment_container, mFragments[position], FRAGMENTS[position])
                                .commit();
                    } else {
                        loadEmptyDetails();
                    }


                }


            }
        });
        MovieSyncAdapter.initializeSyncAdapter(this);
        mSelectedDrawerItemId = savedInstanceState == null ? R.id.user : savedInstanceState.getInt(SELECTED_DRAWER_ITEM);
        mVolley = VolleySingleton.getInstance();
        mRequestQueue = mVolley.getRequestQueue();
        mDbHelper =DbHelpers.getInstance(this);

    }

    private void loadEmptyDetails(){
        if(mTwoPane){
            NoItemFragment nF = new NoItemFragment();
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.details_fragment_container, nF)
                    .commit();
        }

    }

    @Override
    public void onMovieSelected(long selectedMovie) {
        if (mTwoPane) {
            Bundle args = new Bundle();
            args.putParcelable(DetailsFragment.MOVIE_DETAILS, PopMoviesContract.SavedMoviesTable.buildSavedMovieUri(selectedMovie));

            DetailsFragment fr = new DetailsFragment();
            fr.setArguments(args);

            String tag = FRAGMENTS[mPager.getCurrentItem()];
            mFragments[mPager.getCurrentItem()] = fr;
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.details_fragment_container, fr, tag)
                    .commit();
        } else {

            Intent intent = new Intent(this, DetailsActivity.class)
                    .setData(PopMoviesContract.SavedMoviesTable.buildSavedMovieUri(selectedMovie));
            startActivity(intent);
        }

    }



    public void onMovieLongPressed(long id,int caller) {
        //TODO this is error prone. Rework this method!!!
        if(caller == Constants.CALLER_FAV && mPager.getCurrentItem() == Constants.CALLER_FAV){
            if(mFragments[Constants.CALLER_FAV] != null &&
                    (mFragments[Constants.CALLER_FAV].getCurrentItemId() == id
                    || id == Constants.EMPTY_FAV)){
               loadEmptyDetails();
                return;
            }
        }
        if(caller == Constants.CALLER_POPULAR || caller == Constants.CALLER_TOP){
            getTrailer(id);
        }

    }

    private void showYoutubeDialog(String url, long id) {
        FragmentManager fm = getSupportFragmentManager();
        YoutubeDialogFragment fr = YoutubeDialogFragment.newInstance(url,
                PopMoviesContract.SavedMoviesTable.buildSavedMovieUri(id));
        fr.show(fm, "TAG");
    }

    public void getTrailer(final long id) {
        JsonObjectRequest requestEndPoint = new JsonObjectRequest(
                Request.Method.GET,
                mDbHelper.createURL(Constants.VIDEOS_END_POINT, id),
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(final JSONObject response) {
                        final String resp = mDbHelper.getTrailerDataFromJson(response);
                        showYoutubeDialog(resp, id);

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
                            Toast.makeText(getApplicationContext(), getString(R.string.connection_error),
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

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        Intent intent = null;
        item.setChecked(true);
        mSelectedDrawerItemId = item.getItemId();
        if (item.getItemId() == R.id.user) {
            mDrawerLayout.closeDrawer(GravityCompat.START);
            startActivity(new Intent(this, UserActivity.class));
        }

        if (item.getItemId() == R.id.settings) {
            mDrawerLayout.closeDrawer(GravityCompat.START);
            intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
        }
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);

        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(menu.findItem(R.id.menu_search));
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));

        final CursorAdapter adapter = new SimpleCursorAdapter(this,
                R.layout.custom_suggestions_item,
                null,
                new String[]{SearchManager.SUGGEST_COLUMN_TEXT_1},
                new int[]{R.id.text_custom_search},
                CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);

        searchView.setSuggestionsAdapter(adapter);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                Cursor cursor = fetchSuggestions(newText);
                adapter.changeCursor(cursor);
                return true;
            }
        });

        return true;
    }


    private Cursor fetchSuggestions(String newText) {
        ContentResolver cR = getContentResolver();
        String uri = "content://" + SearchProvider.AUTHORITY + "/" + SearchManager.SUGGEST_URI_PATH_QUERY;
        return cR.query(Uri.parse(uri),
                null,
                "title LIKE ?",
                new String[]{newText},
                null);
    }


    public class MyPagerAdapter extends FragmentPagerAdapter {

        public MyPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {

            if (position == 0) {
                return new PopularMoviesFragment();
            } else if (position == 1) {
                return new TopRatedMoviesFragment();
            } else {
                return new FavMoviesFragment();
            }


        }



        @Override
        public int getCount() {
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            String title = "";
            switch (position) {
                case 0:
                    title = getString(R.string.popular_title);
                    break;
                case 1:
                    title = getString(R.string.top_rated_title);
                    break;
                case 2:
                    title = getString(R.string.favorites_title);
                    break;
            }
            return title;
        }
    }


    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(SELECTED_DRAWER_ITEM, mSelectedDrawerItemId);
    }

    @Override
    public void onBackPressed() {

        if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mDrawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
}

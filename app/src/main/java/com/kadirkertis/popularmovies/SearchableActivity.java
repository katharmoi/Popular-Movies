package com.kadirkertis.popularmovies;


import android.app.SearchManager;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.kadirkertis.popularmovies.data.PopMoviesContract;
import com.kadirkertis.popularmovies.data.SearchProvider;

public class SearchableActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {


    private static final int LOADER_ID = 121;
    private CursorAdapter mCursorAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_searchable);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_searchable);
        toolbar.setTitle("Search");
        toolbar.setNavigationIcon(R.drawable.ic_action_arrow_left);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        handleIntent(getIntent());
    }

    @Override
    public void onNewIntent(Intent intent) {
        setIntent(intent);
        handleIntent(intent);
    }

    private void handleIntent(final Intent intent){

        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            ListView ls = (ListView) findViewById(R.id.list_search);
            mCursorAdapter = new SimpleCursorAdapter(this,
                    R.layout.custom_suggestions_item,
                    null,
                    new String[]{SearchManager.SUGGEST_COLUMN_TEXT_1},
                    new int[]{R.id.text_custom_search},
                    CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);
            ls.setAdapter(mCursorAdapter);
            ls.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    Intent detailsIntent = new Intent(getApplicationContext(),DetailsActivity.class)
                           .setData(PopMoviesContract.SavedMoviesTable
                           .buildSavedMovieUri(l));
                    startActivity(detailsIntent);
                }
            });

            String queryStr = intent.getStringExtra(SearchManager.QUERY);
            Bundle args = new Bundle();
            args.putString("query",queryStr);
            getSupportLoaderManager().initLoader(LOADER_ID,args,this);

        }else if(Intent.ACTION_VIEW.equals(intent.getAction())){
            Uri data = intent.getData();
            String id = data.getLastPathSegment();
            Intent detailsIntent = new Intent(getApplicationContext(),DetailsActivity.class)
                    .setData(PopMoviesContract.SavedMoviesTable.buildSavedMovieUri(Integer.parseInt(id)));
            startActivity(detailsIntent);
            finish();
        }

    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String uri = "content://" + SearchProvider.AUTHORITY + "/" + SearchManager.SUGGEST_URI_PATH_QUERY;
        String query = args.getString("query");
        return new CursorLoader(
                this,
                Uri.parse(uri),
                null,
                "title LIKE ?",
                new String[]{query},
                null
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mCursorAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mCursorAdapter.swapCursor(null);
    }
}

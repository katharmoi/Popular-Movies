package com.kadirkertis.popularmovies;

import android.content.Context;
import android.content.res.Configuration;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.kadirkertis.popularmovies.Utilities.Callback;
import com.kadirkertis.popularmovies.Utilities.Constants;
import com.kadirkertis.popularmovies.Utilities.DbHelpers;
import com.kadirkertis.popularmovies.adapters.MovieRecyclerCursorAdapter;
import com.kadirkertis.popularmovies.data.PopMoviesContract;


public class FavMoviesFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>,
        DbHelpers.DbHelperListener{
    private static final int MOVIES_LOADER_FAV = 102;
    private static final String SELECTED_ITEM = "selected";


    private DbHelpers mDbHelper;
    private MovieRecyclerCursorAdapter mRecyclerAdapter;
    private int mSelectedPosition = -1;
    private boolean mTwoPane;
    private static final String[] MOVIES_COLUMNS = {

            PopMoviesContract.FavMoviesTable._ID,
            PopMoviesContract.FavMoviesTable.COLUMN_TITLE,
            PopMoviesContract.FavMoviesTable.COLUMN_RATING,
            PopMoviesContract.FavMoviesTable.COLUMN_GENRE,
            PopMoviesContract.FavMoviesTable.COLUMN_DATE,
            PopMoviesContract.FavMoviesTable.COLUMN_OVERVIEW,
            PopMoviesContract.FavMoviesTable.COLUMN_POPULARITY,
            PopMoviesContract.FavMoviesTable.COLUMN_LANGUAGE,
            PopMoviesContract.FavMoviesTable.COLUMN_POSTER,
    };


    public FavMoviesFragment() {
        // Required empty public constructor
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        mDbHelper = DbHelpers.getInstance(getActivity());
        mDbHelper.addDbHelperListener(this);
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_movies, container, false);
        RecyclerView recyclerView = (RecyclerView) root.findViewById(R.id.movies_recycler);
        mRecyclerAdapter = new MovieRecyclerCursorAdapter(getActivity(), null);
        recyclerView.setAdapter(mRecyclerAdapter);
        if (getActivity().getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            recyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 3));
        } else {
            recyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 5));
        }
        recyclerView.addOnItemTouchListener(new RecyclerTouchListener(getActivity(), recyclerView, new ClickListener() {
            @Override
            public void onClick(View view, int position) {
                long selectedMovie = mRecyclerAdapter.getItem(position);
                mSelectedPosition = position;
                mRecyclerAdapter.setSelectedPosition(position);
                ((Callback) getActivity()).onMovieSelected(selectedMovie);
            }

            @Override
            public void onClickHold(View view, int position) {
                int selectedMovie = mRecyclerAdapter.getItem(position);
                mDbHelper.removeFromFavDb(selectedMovie, view);
                getLoaderManager().restartLoader(MOVIES_LOADER_FAV, null, FavMoviesFragment.this);
                ((Callback) getActivity()).onMovieLongPressed(selectedMovie, Constants.CALLER_FAV);
            }
        }));

        return root;


    }

    public void onSaveInstanceState(Bundle outState) {
        outState.putInt(SELECTED_ITEM, mSelectedPosition);
        super.onSaveInstanceState(outState);
    }


    @Override
    public void onResume() {
        if (mSelectedPosition > -1 && mTwoPane) {
            int selectedMovie = mRecyclerAdapter.getItem(mSelectedPosition);
            mRecyclerAdapter.setSelectedPosition(mSelectedPosition);
            ((Callback) getActivity()).onMovieSelected(selectedMovie);
        }
        super.onResume();
    }

    private void upDateSelected(int id) {
        //No favorites reset selected
        if(mRecyclerAdapter.getItemCount() == 0){
            mSelectedPosition = -1;
            mRecyclerAdapter.setSelectedPosition(mSelectedPosition);
            if(mTwoPane){
                ((Callback) getActivity()).onMovieLongPressed(Constants.EMPTY_FAV, Constants.CALLER_FAV);
            }
           return;
        }
        if(mSelectedPosition != -1){
            for(int i =0; i< mRecyclerAdapter.getItemCount(); i++){
                if(id == mRecyclerAdapter.getItem(i)){
                    mSelectedPosition = i;
                    mRecyclerAdapter.setSelectedPosition(mSelectedPosition);
                    return;
                }
            }
            mSelectedPosition = -1;
            mRecyclerAdapter.setSelectedPosition(mSelectedPosition);
        }

    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        mTwoPane = getActivity().findViewById(R.id.details_fragment_container) != null;
        getLoaderManager().initLoader(MOVIES_LOADER_FAV, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Uri uri = PopMoviesContract.FavMoviesTable.CONTENT_URI;

        return new CursorLoader(getActivity(),
                uri,
                MOVIES_COLUMNS,
                null,
                null,
                null);

    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mRecyclerAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mRecyclerAdapter.swapCursor(null);

    }

    @Override
    public void onDataChanged(boolean isAdded, int id) {
        upDateSelected(id);
    }


    class RecyclerTouchListener implements RecyclerView.OnItemTouchListener {

        private GestureDetector gestureDetector;
        private ClickListener clickListener;

        public RecyclerTouchListener(Context context, final RecyclerView recyclerView,
                                     final ClickListener clickListener) {
            this.clickListener = clickListener;
            gestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {

                @Override
                public boolean onSingleTapUp(MotionEvent e) {
                    return true;
                }

                @Override
                public void onLongPress(MotionEvent e) {
                    View child = recyclerView.findChildViewUnder(e.getX(), e.getY());
                    if (child != null && clickListener != null) {
                        clickListener.onClickHold(child, recyclerView.getChildAdapterPosition(child));
                    }
                }
            });

        }

        @Override
        public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
            View child = rv.findChildViewUnder(e.getX(), e.getY());
            if (child != null && clickListener != null && gestureDetector.onTouchEvent(e)) {
                clickListener.onClick(child, rv.getChildAdapterPosition(child));
            }
            return false;
        }

        @Override
        public void onTouchEvent(RecyclerView rv, MotionEvent e) {

        }

        @Override
        public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

        }
    }

    public interface ClickListener {
        void onClick(View view, int position);

        void onClickHold(View view, int position);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mDbHelper.removeDbHelperListener(this);
    }
}

package com.kadirkertis.popularmovies;

import android.content.Context;

import android.content.res.Configuration;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
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
import android.widget.Toast;
import com.kadirkertis.popularmovies.Utilities.Callback;
import com.kadirkertis.popularmovies.Utilities.Constants;
import com.kadirkertis.popularmovies.adapters.MovieRecyclerCursorAdapter;
import com.kadirkertis.popularmovies.data.PopMoviesContract;

import java.util.ArrayList;

public class PopularMoviesFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>{
    private static final int MOVIES_LOADER = 100;
    private static final String SELECTED_ITEM ="selected" ;
    private int mSelectedPosition = -1;
    private boolean mTwoPane;

    private MovieRecyclerCursorAdapter mRecyclerAdapter;


    private static final String[] MOVIES_COLUMNS = {
            PopMoviesContract.SavedMoviesTable._ID,
    PopMoviesContract.SavedMoviesTable.COLUMN_TITLE,
    PopMoviesContract.SavedMoviesTable.COLUMN_RATING,
    PopMoviesContract.SavedMoviesTable.COLUMN_GENRE,
    PopMoviesContract.SavedMoviesTable.COLUMN_DATE,
    PopMoviesContract.SavedMoviesTable.COLUMN_OVERVIEW,
    PopMoviesContract.SavedMoviesTable.COLUMN_POPULARITY,
    PopMoviesContract.SavedMoviesTable.COLUMN_LANGUAGE,
    PopMoviesContract.SavedMoviesTable.COLUMN_POSTER,
            PopMoviesContract.SavedMoviesTable.COLUMN_CATEGORY
    };



    public PopularMoviesFragment() {
        // Required empty public constructor
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_movies, container, false);
        RecyclerView recycler = (RecyclerView) root.findViewById(R.id.movies_recycler);
        mRecyclerAdapter = new MovieRecyclerCursorAdapter(getActivity(),null);
        recycler.setAdapter(mRecyclerAdapter);
        if(getActivity().getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT){
            recycler.setLayoutManager(new GridLayoutManager(getActivity(), 3));
        }
        else{
            recycler.setLayoutManager(new GridLayoutManager(getActivity(), 5));
        }

        recycler.addOnItemTouchListener(new RecyclerTouchListener(getActivity(), recycler, new ClickListener() {
            @Override
            public void onClick(View view, int position) {
                int selectedMovie = mRecyclerAdapter.getItem(position);
                mSelectedPosition = position;
                mRecyclerAdapter.setSelectedPosition(position);
                ((Callback) getActivity()).onMovieSelected(selectedMovie);
            }

            @Override
            public void onClickHold(View view, int position) {
                int selectedMovie = mRecyclerAdapter.getItem(position);
                ((Callback) getActivity()).onMovieLongPressed(selectedMovie,Constants.CALLER_POPULAR);
            }
        }));
        if(savedInstanceState != null){
            mSelectedPosition = savedInstanceState.getInt(SELECTED_ITEM);
        }
        return root;



    }

    public void onSaveInstanceState(Bundle outState) {
        outState.putInt(SELECTED_ITEM,mSelectedPosition);
        super.onSaveInstanceState(outState);
    }


    @Override
    public void onResume() {
        if(mSelectedPosition > -1 && mTwoPane ){
            int selectedMovie = mRecyclerAdapter.getItem(mSelectedPosition);
            mRecyclerAdapter.setSelectedPosition(mSelectedPosition);
            ((Callback) getActivity()).onMovieSelected(selectedMovie);
        }
        super.onResume();
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        mTwoPane = getActivity().findViewById(R.id.details_fragment_container)!= null;
        getLoaderManager().initLoader(MOVIES_LOADER,null,this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String order = PopMoviesContract.SavedMoviesTable.COLUMN_POPULARITY + " DESC";
        String selection = PopMoviesContract.SavedMoviesTable.COLUMN_CATEGORY +"=?";
        String[] selectionArgs={Constants.POPULAR_CATEGORY};
        Uri uri = PopMoviesContract.SavedMoviesTable.CONTENT_URI;

        return new CursorLoader(getActivity(),
                uri,
                MOVIES_COLUMNS,
                selection,
                selectionArgs,
                order);

    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mRecyclerAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mRecyclerAdapter.swapCursor(null);

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



}

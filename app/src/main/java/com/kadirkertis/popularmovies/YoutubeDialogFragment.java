package com.kadirkertis.popularmovies;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.os.Bundle;

import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.TextView;

import com.kadirkertis.popularmovies.Utilities.Constants;
import com.kadirkertis.popularmovies.Utilities.DbHelpers;
import com.pierfrancescosoffritti.youtubeplayer.AbstractYouTubeListener;
import com.pierfrancescosoffritti.youtubeplayer.YouTubePlayerView;
import com.squareup.picasso.Picasso;


public class YoutubeDialogFragment extends DialogFragment implements LoaderManager.LoaderCallbacks<Cursor>,
        DbHelpers.DbHelperListener{
    private static final String ID = "id";
    private static final int LOADER_ID = 345;
    private static final String DATA_URI = "uri";
    private Uri mUri;
    private MovieInfo mDetails;
    private YouTubePlayerView yPV;
    private DbHelpers mDBHelper;
    private TextView mMovieTitle;
    private ImageButton mFavBtn;

    public YoutubeDialogFragment() {
        // Required empty public constructor
    }

    public static YoutubeDialogFragment newInstance(String id,Uri uri) {
        YoutubeDialogFragment fr = new YoutubeDialogFragment();
        Bundle args = new Bundle();
        args.putString(ID, id);
        args.putParcelable(DATA_URI,uri);
        fr.setArguments(args);
        return fr;
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_youtube_dialog, container, false);
        yPV = (YouTubePlayerView) root.findViewById(R.id.youtube_player_view);
        mMovieTitle = (TextView) root.findViewById(R.id.youtube_movie_title);
        Bundle args = getArguments();
        final String resource = args.getString(ID);
        mUri = args.getParcelable(DATA_URI);
        mFavBtn = (ImageButton) root.findViewById(R.id.add_to_fav_btn_youtube_dialog);
        ImageButton shareBtn = (ImageButton) root.findViewById(R.id.share_btn_youtube_dialog);
        mDBHelper = DbHelpers.getInstance(getActivity());
        mDBHelper.addDbHelperListener(this);
        shareBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_SEND);
                String message = "Check out this movie "
                        + Constants.YOUTUBE_BASE_URL + resource;
                intent.putExtra(Intent.EXTRA_TEXT, message);
                intent.setType("text/plain");
                startActivity(intent);
            }
        });
        mFavBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mDBHelper.addToFavDb(mDetails,view);

            }
        });
        yPV.initialize(new AbstractYouTubeListener() {
            @Override
            public void onReady() {
                yPV.loadVideo(resource, 0);
            }
        }, true);
        return root;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        return dialog;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getDialog() == null) {
            return;
        }

//        DisplayMetrics dM = new DisplayMetrics();
//        getActivity().getWindowManager().getDefaultDisplay().getMetrics(dM);
//
//        int height = dM.heightPixels;
//        int width = dM.widthPixels;
//        int dialogWidth = width - (width / 10);
//        int dialogHeight = height > width ? (height / 3) : height;
//        getDialog().getWindow().setLayout(dialogWidth, dialogHeight);
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        yPV.release();
        super.onDismiss(dialog);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        getLoaderManager().initLoader(LOADER_ID, null, this);
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
            mMovieTitle.setText(mDetails.getName());
        }
        if(mDBHelper.isInFavDb(mDetails.getId())){
            mFavBtn.setImageDrawable(ContextCompat.getDrawable(getActivity(),R.drawable.ic_remove));
        }
        else {
            mFavBtn.setImageDrawable(ContextCompat.getDrawable(getActivity(),R.drawable.ic_action_add));
        }


    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    @Override
    public void onDataChanged(boolean isAdded,int id) {
        if(isAdded){
            mFavBtn.setImageResource(R.drawable.ic_remove);
        }
        else{
            mFavBtn.setImageResource(R.drawable.ic_action_add);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mDBHelper.removeDbHelperListener(this);
    }
}


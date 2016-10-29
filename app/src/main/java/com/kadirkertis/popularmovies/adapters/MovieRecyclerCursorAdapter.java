package com.kadirkertis.popularmovies.adapters;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.kadirkertis.popularmovies.R;
import com.kadirkertis.popularmovies.Utilities.Constants;
import com.squareup.picasso.Picasso;

/**
 * Created by uyan on 05/09/16.
 */
public class MovieRecyclerCursorAdapter extends RecyclerView.Adapter<MovieRecyclerCursorAdapter.RecyclerViewHolder>
{
    private  LayoutInflater inflater;
    private Cursor mMoviesCursor;
    private Context mContext;
    private int mSelectedPosition =-1;
    //private static final String BASE_URL = "http://image.tmdb.org/t/p/w185/";
    private int prePos = 0;

    public MovieRecyclerCursorAdapter(Context context, Cursor movieInfo){
        mMoviesCursor = movieInfo;
        mContext = context;
        inflater = LayoutInflater.from(context);
    }

    @Override
    public RecyclerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View root = inflater.inflate(R.layout.list_item_movie,parent,false);
        RecyclerViewHolder vh = new RecyclerViewHolder(root);
        return vh;

    }

    @Override
    public void onBindViewHolder(RecyclerViewHolder holder, int position) {
        if(mMoviesCursor.moveToPosition(position) ){
            holder.itemView.setSelected(mSelectedPosition == position);
            holder.nameHolder.setText(mMoviesCursor.getString(Constants.COL_TITLE));
            holder.rateHolder.setText(mMoviesCursor.getString(Constants.COL_RATING));
            Picasso.with(mContext).load(mMoviesCursor.getString(Constants.COL_POSTER)).into(holder.posterHolder);
        }
        if(position > prePos){
            animate(holder,true);

        }else {
            animate(holder,false);
        }
        prePos = position;
    }

    @Override
    public int getItemCount() {
       return mMoviesCursor !=null ? mMoviesCursor.getCount():0;
    }

    public int getItem(int position){
        mMoviesCursor.moveToPosition(position);
        return mMoviesCursor.getInt(Constants.COL_ID);
    }

    public void setSelectedPosition(int position){
        int oldSelected = mSelectedPosition;
        mSelectedPosition = position;
        notifyItemChanged(oldSelected);
        notifyItemChanged(position);
    }

    public int getSelectedPosition(){
        return mSelectedPosition;
    }

    private static void animate(RecyclerView.ViewHolder holder,boolean isDown){
        ObjectAnimator yAxisAnimator = ObjectAnimator.ofFloat(holder.itemView,"translationY",
                isDown?250:-250,0);
        yAxisAnimator.setDuration(500);
        yAxisAnimator.start();
    }

    class RecyclerViewHolder extends RecyclerView.ViewHolder{

        TextView nameHolder;
        TextView rateHolder;
        ImageView posterHolder;
        public RecyclerViewHolder(View itemView) {
            super(itemView);
            nameHolder = (TextView) itemView.findViewById(R.id.list_item_movie_name);
            rateHolder = (TextView) itemView.findViewById(R.id.list_item_movie_rating);
            posterHolder = (ImageView) itemView.findViewById(R.id.list_item_movie_icon);
        }
    }

    public void changeCursor(Cursor cursor) {
        Cursor old = swapCursor(cursor);
        if (old != null) {
            old.close();
        }
    }

    public Cursor swapCursor(Cursor cursor) {
        if (mMoviesCursor == cursor) {
            return null;
        }
        Cursor oldCursor = mMoviesCursor;
        this.mMoviesCursor = cursor;
        if (cursor != null) {
            this.notifyDataSetChanged();
        }
        return oldCursor;
    }

    public Cursor getCursor(){
        return mMoviesCursor;
    }



}

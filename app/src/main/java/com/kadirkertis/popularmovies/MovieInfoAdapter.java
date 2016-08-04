package com.kadirkertis.popularmovies;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Created by uyan on 30/07/16.
 */
public class MovieInfoAdapter extends ArrayAdapter<MovieInfo> {
    private static final String LOG_TAG = MovieInfoAdapter.class.getSimpleName();

    public MovieInfoAdapter(Activity context, List<MovieInfo> movieInfos){
        super(context,0,movieInfos);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        //Iterating the Adapter
        MovieInfo movieInfo = getItem(position);

        // Adapters recycle views to AdapterViews.
        // If this is a new View object we're getting, then inflate the layout.
        // If not, this view already has the layout inflated from a previous call to getView,
        // and we modify the View widgets as usual.
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_item_movie, parent, false);
        }

        ImageView iconView = (ImageView) convertView.findViewById(R.id.list_item_movie_icon);
        Picasso.with(getContext()).load(movieInfo.getImageResource()).into(iconView);

        TextView versionNameView = (TextView) convertView.findViewById(R.id.list_item_movie_name);
        versionNameView.setText(movieInfo.getName());

        TextView versionNumberView = (TextView) convertView.findViewById(R.id.list_item_movie_rating);
        versionNumberView.setText(movieInfo.getRating());
        return convertView;
    }
}

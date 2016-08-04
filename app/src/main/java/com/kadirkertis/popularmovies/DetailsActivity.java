package com.kadirkertis.popularmovies;

import android.content.Intent;
import android.os.Bundle;
import android.app.Activity;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

public class DetailsActivity extends Activity {

    public static final String MOVIE_TITLE="title";
    public static final String MOVIE_IMG="img";
    public static final String MOVIE_SYN="synopsis";
    public static final String MOVIE_RATING="rating";
    public static final String MOVIE_DATE="date";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        Intent intent = getIntent();
        TextView titleView = (TextView)findViewById(R.id.details_title);
        ImageView imageView = (ImageView) findViewById(R.id.details_img);
        TextView synopsisView = (TextView) findViewById(R.id.details_synopsis);
        TextView rateView = (TextView) findViewById(R.id.details_rating);
        TextView dateView = (TextView) findViewById(R.id.details_date);



        if(intent != null){
            if(intent.hasExtra(MOVIE_TITLE)){
                titleView.setText(intent.getStringExtra(MOVIE_TITLE));
                getActionBar().setTitle(intent.getStringExtra(MOVIE_TITLE));
            }

            if(intent.hasExtra(MOVIE_IMG))
                Picasso.with(this).load("http://image.tmdb.org/t/p/w185/" +
                        intent.getStringExtra(MOVIE_IMG)).into(imageView);
            if(intent.hasExtra(MOVIE_SYN))
                synopsisView.setText(intent.getStringExtra(MOVIE_SYN));
            if(intent.hasExtra(MOVIE_RATING))
                rateView.setText("Rating: " +intent.getStringExtra(MOVIE_RATING));
            if(intent.hasExtra(MOVIE_DATE))
                dateView.setText("Release: " +intent.getStringExtra(MOVIE_DATE));
        }
        getActionBar().setDisplayHomeAsUpEnabled(true);

    }

}

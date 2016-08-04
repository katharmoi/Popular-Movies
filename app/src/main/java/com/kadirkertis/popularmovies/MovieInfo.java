package com.kadirkertis.popularmovies;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by uyan on 30/07/16.
 */
public class MovieInfo implements Parcelable {

    private final String IMAGE_BASE_URL="http://image.tmdb.org/t/p/w185/";
    private int id;
    private String name;
    private String rating;
    private String imageResource;
    private String date;
    private String synopsis;

    public MovieInfo(int id,String name, String rating, String resource,String date
                     ,String synopsis){
        this.id = id;
        this.name = name;
        this.rating = rating;
        this.imageResource = resource;
        this.date = date;
        this.synopsis = synopsis;
    }

    private MovieInfo(Parcel in){
        id=in.readInt();
        name = in.readString();
        rating = in.readString();
        imageResource = in.readString();
        date = in.readString();
        synopsis = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public int getId() {
        return id;
    }

    public String getName(){
        return name;
    }

    public String getRating(){
        return rating;
    }

    public String getImageResource(){
        return IMAGE_BASE_URL+imageResource;
    }

    public String getDate() { return date;}

    public String getSynopsis() {
        return synopsis;
    }

    public String toString(){
        return "Movie Id: " +id +" Movie name: " +name +" Movie rating: " + rating +
                " Image Source: " + imageResource +" Date: " +date +" Synopsis: " +synopsis;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(name);
        dest.writeString(rating);
        dest.writeString(imageResource);
        dest.writeString(date);
        dest.writeString(synopsis);
    }

    public final Parcelable.Creator<MovieInfo> CREATOR = new Parcelable.Creator<MovieInfo>(){
        @Override
        public MovieInfo createFromParcel(Parcel source) {
            return new MovieInfo(source);
        }

        @Override
        public MovieInfo[] newArray(int i) {
            return new MovieInfo[i];
        }
    };
}

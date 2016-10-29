package com.kadirkertis.popularmovies;

import android.os.Parcel;
import android.os.Parcelable;

import com.kadirkertis.popularmovies.Utilities.Constants;

/**
 * Created by uyan on 30/07/16.
 */
public class MovieInfo implements Parcelable {

    private int id;
    private String name;
    private double rating;
    private String imageResource;
    private String date;
    private String synopsis;
    private int genre;
    private int popularity;

    public MovieInfo(int id,String name, double rating, String resource,String date
                     ,String synopsis,int genre,int popularity){
        this.id = id;
        this.name = name;
        this.rating = rating;
        this.imageResource = resource;
        this.date = date;
        this.synopsis = synopsis;
        this.genre = genre;
        this.popularity = popularity;
    }

    private MovieInfo(Parcel in){
        id=in.readInt();
        name = in.readString();
        rating = in.readDouble();
        imageResource = in.readString();
        date = in.readString();
        synopsis = in.readString();
        genre = in.readInt();
        popularity = in.readInt();
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

    public double getRating(){
        return rating;
    }

    public String getImageResource(){
        return imageResource;
    }

    public String getDate() { return date;}

    public String getSynopsis() {
        return synopsis;
    }

    public int getGenre(){return genre;}

    public int getPopularity(){ return popularity;}


    public String toString(){
        return "Movie Id: " +id
                +" Movie name: " +name
                +" Movie rating: " + rating +
                " Image Source: " + imageResource
                +" Date: " +date
                +" Synopsis: " +synopsis
                +" Genre: " +genre
                +" Popularity: " +popularity;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(name);
        dest.writeDouble(rating);
        dest.writeString(imageResource);
        dest.writeString(date);
        dest.writeString(synopsis);
        dest.writeInt(genre);
        dest.writeInt(popularity);
    }

    public static final Parcelable.Creator<MovieInfo> CREATOR = new Parcelable.Creator<MovieInfo>(){
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

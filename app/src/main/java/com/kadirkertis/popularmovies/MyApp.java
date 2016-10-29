package com.kadirkertis.popularmovies;

import android.app.Application;
import android.content.Context;

/**
 * Created by uyan on 09/09/16.
 */
public class MyApp extends Application {
    private static MyApp sInstance;

    @Override
    public void onCreate() {
        super.onCreate();
        sInstance = this;
    }

    public static MyApp getInstance(){
        return sInstance;
    }

    public static Context getAppContext(){
        return sInstance.getApplicationContext();
    }
}

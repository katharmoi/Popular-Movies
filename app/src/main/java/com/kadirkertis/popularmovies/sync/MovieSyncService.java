package com.kadirkertis.popularmovies.sync;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.kadirkertis.popularmovies.sync.MovieSyncAdapter;

/**
 * Created by uyan on 19/09/16.
 */
public class MovieSyncService extends Service {

    private static MovieSyncAdapter sMovieSyncAdapter = null;

    private static final Object sLock = new Object();

    @Override
    public void onCreate() {
        synchronized (sLock){
            if(sMovieSyncAdapter == null){
                sMovieSyncAdapter = new MovieSyncAdapter(getApplicationContext(),true);
            }
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return sMovieSyncAdapter.getSyncAdapterBinder();
    }
}

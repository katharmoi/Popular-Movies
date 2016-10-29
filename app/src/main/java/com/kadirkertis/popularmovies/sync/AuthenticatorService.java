package com.kadirkertis.popularmovies.sync;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.kadirkertis.popularmovies.sync.Authenticator;

/**
 * Created by uyan on 19/09/16.
 */
public class AuthenticatorService extends Service {

    private Authenticator mAuth;

    @Override
    public void onCreate() {
        mAuth = new Authenticator(this);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mAuth.getIBinder();
    }
}

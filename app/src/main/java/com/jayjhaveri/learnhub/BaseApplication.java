package com.jayjhaveri.learnhub;

import android.app.Application;

import com.google.firebase.database.FirebaseDatabase;

/**
 * Created by ADMIN-PC on 21-03-2017.
 */

public class BaseApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
    }
}

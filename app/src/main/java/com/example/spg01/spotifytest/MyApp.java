package com.example.spg01.spotifytest;

import android.app.Application;
import android.content.Context;

/**
 * Created by Richard on 12/1/2017.
 */

public class MyApp extends Application {

    private static MyApp instance;

    public static MyApp getInstance() {
        return instance;
    }

    public static Context getContext(){
        return instance;
        // or return instance.getApplicationContext();
    }

    @Override
    public void onCreate() {
        instance = this;
        super.onCreate();
    }
}

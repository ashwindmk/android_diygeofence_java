package com.ashwin.android.diygeofencejavademo;

import android.app.Application;
import android.util.Log;

import com.ashwin.android.diygeofencejava.DiyGeofence;

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        DiyGeofence.init(this);
        DiyGeofence.setLogLevel(Log.VERBOSE);
        DiyGeofence.setAutoLocationUpdates(this, true);
        DiyGeofence.setGeofenceListener(new MyGeofenceListener());
    }
}

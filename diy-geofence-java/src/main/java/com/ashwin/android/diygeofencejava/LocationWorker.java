package com.ashwin.android.diygeofencejava;

import android.content.Context;

class LocationWorker implements Runnable {
    private Context context = null;

    LocationWorker(Context context) {
        this.context = context;
    }

    @Override
    public void run() {
        DiyGeofence.updateLocation(this.context, false);
    }
}

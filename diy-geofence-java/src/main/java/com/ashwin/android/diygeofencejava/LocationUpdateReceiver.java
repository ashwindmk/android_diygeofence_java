package com.ashwin.android.diygeofencejava;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Location;

import com.google.android.gms.location.LocationResult;

import java.util.List;

public class LocationUpdateReceiver extends BroadcastReceiver {
    public static final String ACTION_PROCESS_UPDATES = "com.ashwin.android.diygeofencejava.LOCATION_UPDATES";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (context == null) return;

        Logger.d("Received location update");

        if (ACTION_PROCESS_UPDATES.equals(intent.getAction())) {
            try {
                LocationResult result = LocationResult.extractResult(intent);
                if (result != null) {
                    List<Location> locations = result.getLocations();
                    DiyGeofenceWorker geofenceWorker = new DiyGeofenceWorker(context, locations);
                    DiyServiceManager.runProcess(geofenceWorker);
                } else {
                    Logger.e("Location result is NULL");
                    LocationWorker locationWorker = new LocationWorker(context);
                    DiyServiceManager.runProcess(locationWorker);
                }
            } catch (Exception e) {
                Logger.e("Exception while processing received location update", e);
            }
        }
    }
}

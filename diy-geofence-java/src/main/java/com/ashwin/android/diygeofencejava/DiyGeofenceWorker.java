package com.ashwin.android.diygeofencejava;

import android.content.Context;
import android.location.Location;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

class DiyGeofenceWorker implements Runnable {
    private Context context = null;
    private List<Location> locations = null;

    DiyGeofenceWorker(Context context, List<Location> locations) {
        this.context = context;
        this.locations = locations;
    }

    @Override
    public void run() {
        processGeofences(context, locations);
    }

    private double getDistance(double lat1, double lng1, double lat2, double lng2) {
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lng2 - lng1);
        double a = Math.sin(latDistance / 2.0) * Math.sin(latDistance / 2.0)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) * Math.sin(lonDistance / 2.0) * Math.sin(lonDistance / 2.0);
        double distance = 6371.0 * (2.0 * Math.atan2(Math.sqrt(a), Math.sqrt(1.0 - a))) * 1000.0;
        return Math.abs(distance);
    }

    private void processGeofences(Context context, List<Location> locations) {
        Logger.d("Processing geofences on thread: " + Thread.currentThread().getName());

        if (locations == null || locations.isEmpty()) {
            return;
        }

        SharedPrefsManager sharedPreferences = SharedPrefsManager.get(context);

        Set<DiyGeofenceData> geofences = DiyGeofence.getDb(context).getAllGeofences();

        if (geofences.isEmpty()) {
            DiyGeofence.stopLocationUpdates(context);
            return;
        }

        Set<String> lastEnteredGeofences = sharedPreferences.getStringSet(SharedPrefsManager.LAST_ENTERED_GEOFENCES, new HashSet<String>());
        Set<String> enteredGeofences = new HashSet<String>();
        Set<String> exitedGeofences = new HashSet<String>();

        List<String> geofenceIds = new ArrayList<String>();

        double min = Double.MAX_VALUE;
        String closestId = "null";

        for (DiyGeofenceData geofence : geofences) {
            geofenceIds.add(geofence.getId());
            for (Location location : locations) {
                double distance = getDistance(location.getLatitude(), location.getLongitude(), geofence.getLat(), geofence.getLng());

                if (distance <= geofence.getRad()) {
                    enteredGeofences.add(geofence.getId());
                }

                double edgeDistance = Math.abs(distance - geofence.getRad());
                if (edgeDistance <= min) {
                    min = edgeDistance;
                    closestId = geofence.getId();
                }
            }
        }

        Logger.d(DiyGeofence.DEBUG_TAG, "Closest geofence: " + closestId + ", min distance: " + min);

        // Exited geofences
        exitedGeofences.addAll(geofenceIds);
        exitedGeofences.removeAll(enteredGeofences);
        for (String exitedGeofence : exitedGeofences) {
            if (BuildConfig.DEBUG) {
                Logger.d("Not in geofence: " + exitedGeofence);
            }

            if (lastEnteredGeofences.contains(exitedGeofence)) {
                DiyGeofence.dispatchExitCallback(context, exitedGeofence);
            }
        }

        // Entered geofences
        if (!enteredGeofences.isEmpty()) {
            for (String enteredGeofence : enteredGeofences) {
                if (BuildConfig.DEBUG) {
                    Logger.d("In geofence: " + enteredGeofence);
                }

                if (!lastEnteredGeofences.contains(enteredGeofence)) {
                    DiyGeofence.dispatchEnterCallback(context, enteredGeofence);
                }
            }
        }

        // Update last entered geofences
        sharedPreferences.put(SharedPrefsManager.LAST_ENTERED_GEOFENCES, enteredGeofences);

        // Update location service
        DiyGeofence.updateLocationUpdates(context, min);
    }
}

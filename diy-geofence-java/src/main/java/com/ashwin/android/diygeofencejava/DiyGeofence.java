package com.ashwin.android.diygeofencejava;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DiyGeofence {
    public static final String TAG = "diy-geofence";

    private static final int REQUEST_CODE = TAG.hashCode();

    private static DiyGeofenceListener mDiyGeofenceListener = null;

    private static Handler mMainHandler = new Handler(Looper.getMainLooper());

    private static boolean FUSED_LOCATION_AVAILABLE;
    static {
        try {
            Class.forName("com.google.android.gms.location.LocationServices");
            FUSED_LOCATION_AVAILABLE = true;
        } catch (Exception e) {
            Logger.e("Location dependency not found", e);
            FUSED_LOCATION_AVAILABLE = false;
        }
    }

    private static PendingIntent mPendingIntent = null;

    public static void init(Context context) {
        LocationWorker locationWorker = new LocationWorker(context);
        long delay = SharedPrefsManager.get(context).getLong(SharedPrefsManager.INITIAL_LOCATION_UPDATE_DELAY, SharedPrefsManager.DEFAULT_INITIAL_LOCATION_UPDATE_DELAY);
        mMainHandler.postDelayed(locationWorker, delay);
        Logger.d("DIY Geofence is initialized successfully");
    }

    // Configurations
    public static void setLogLevel(int level) {
        Logger.setLevel(level);
        Logger.d("Log level set to " + level + " successfully");
    }

    public static void setGeofenceListener(DiyGeofenceListener listener) {
        mDiyGeofenceListener = listener;
        Logger.d("Geofence listener set successfully");
    }

    public static void setAutoLocationUpdates(Context context, boolean enable) {
        SharedPrefsManager.get(context).put(SharedPrefsManager.ENABLE_LOCATION_UPDATES, enable);
        Logger.d("Location updates set to " + enable + " successfully");
    }

    public static void setInitialLocationUpdateDelay(Context context, long delay) {
        SharedPrefsManager.get(context).put(SharedPrefsManager.INITIAL_LOCATION_UPDATE_DELAY, delay);
        Logger.d("Location update initial delay set to " + delay + " milliseconds successfully");
    }

    // APIs
    public static boolean addGeofence(Context context, String id, double lat, double lng, double rad) {
        if (id == null || id.isEmpty() || (id.trim()).isEmpty()) {
            Logger.e("Invalid ID, must be non-empty string.");
            return false;
        }

        if (rad <= 0) {
            Logger.e("Invalid radius: " + rad + " for " + id + ". Radius must be greater than 0.");
            return false;
        }

        if (lat < -90d || lat > 90D) {
            Logger.e("Invalid latitude: " + lat + " for id: " + id);
            return false;
        }

        if (lng < -180d || lng > 180d) {
            Logger.e("Invalid longitude: " + lng + " for id: " + id);
            return false;
        }

        if (isGeofenceAdded(context, id, lat, lng, rad)) {
            Logger.d("Geofence " + id + " is already added");
            return true;
        }

        boolean isAdded = getDb(context).addGeofence(id, lat, lng, rad);
        if (isAdded) {
            updateLocation(context, true);
            Logger.d("Geofence " + id + " added successfully");
        } else {
            Logger.e("Geofence " + id + " could not be added");
        }
        return isAdded;
    }

    public static boolean removeGeofence(Context context, String id) {
        boolean isRemoved = getDb(context).removeGeofence(id);
        if (isRemoved) {
            updateLocation(context, true);
            Logger.d("Geofence " + id + " is removed successfully");
        }
        return isRemoved;
    }

    public static boolean isGeofenceAdded(Context context, String id, double lat, double lng, double rad) {
        DiyGeofenceData prevGeofence = getDb(context).getGeofence(id);
        if (prevGeofence == null) {
            return false;
        }
        DiyGeofenceData newGeofence = new DiyGeofenceData(id, lat, lng, rad);
        return newGeofence.equals(prevGeofence);
    }

    public static boolean removeAllGeofences(Context context) {
        boolean areRemoved = getDb(context).removeAllGeofences();
        if (areRemoved) {
            stopLocationUpdates(context);
            SharedPrefsManager.get(context).put(SharedPrefsManager.LAST_ENTERED_GEOFENCES, new HashSet<String>());
            Logger.d("All geofences removed successfully");
        }
        return areRemoved;
    }

    public static JSONObject getGeofence(Context context, String id) {
        DiyGeofenceData diyGeofenceData = getDb(context).getGeofence(id);
        return diyGeofenceData.toJson();
    }

    public static JSONArray getAllGeofences(Context context) {
        Set<DiyGeofenceData> diyGeofenceDataSet = getDb(context).getAllGeofences();
        JSONArray jsonArray = new JSONArray();
        for (DiyGeofenceData diyGeofenceData : diyGeofenceDataSet) {
            jsonArray.put(diyGeofenceData.toJson());
        }
        return jsonArray;
    }

    public static void setLocation(Context context, Location location) {
        if (context != null && location != null) {
            List<Location> locations = new ArrayList<>();
            locations.add(location);
            DiyGeofenceWorker geofenceWorker = new DiyGeofenceWorker(context, locations);
            DiyServiceManager.runProcess(geofenceWorker);
        }
    }

    public static void updateLocation(final Context context, final boolean force) {
        if (!canUpdateLocation(context)) {
            return;
        }

        Set<DiyGeofenceData> geofences = getDb(context).getAllGeofences();
        if (geofences.isEmpty()) {
            stopLocationUpdates(context);
            return;
        }

        FusedLocationProviderClient fusedLocationProvider = LocationServices.getFusedLocationProviderClient(context);
        fusedLocationProvider.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null) {
                    SharedPrefsManager sharedPrefsManager = SharedPrefsManager.get(context);

                    double latitude = location.getLatitude();
                    double longitude = location.getLongitude();

                    if (!force) {
                        double lastLatitude = sharedPrefsManager.getDouble(SharedPrefsManager.LAST_LATITUDE, 0);
                        double lastLongitude = sharedPrefsManager.getDouble(SharedPrefsManager.LAST_LONGITUDE, 0);

                        if (latitude == lastLatitude && longitude == lastLongitude) {
                            Logger.d("Same as previous location: latitude: " + latitude + ", longitude: " + longitude);
                            return;
                        }
                    }

                    SharedPrefsManager.get(context).put(SharedPrefsManager.LAST_LATITUDE, latitude);
                    SharedPrefsManager.get(context).put(SharedPrefsManager.LAST_LONGITUDE, longitude);
                    Logger.d("Updated location: latitude: " + latitude + ", longitude: " + longitude);

                    List<Location> locations = new ArrayList<>();
                    locations.add(location);

                    DiyGeofenceWorker geofenceWorker = new DiyGeofenceWorker(context, locations);
                    DiyServiceManager.runProcess(geofenceWorker);
                } else {
                    Logger.e("Location is NULL");
                }
            }
        });
    }

    // Helpers
    private static DatabaseHandler mDatabaseHandler = null;

    static DatabaseHandler getDb(Context context) {
        if (mDatabaseHandler == null) {
            mDatabaseHandler = DatabaseHandler.getInstance(context);
        }
        return mDatabaseHandler;
    }

    // Internal
    private static boolean canUpdateLocation(Context context) {
        boolean canUpdateLocation = true;

        if (!FUSED_LOCATION_AVAILABLE) {
            Logger.e("Location dependency library not added");
            canUpdateLocation = false;
        }

        if (context.getPackageManager().checkPermission(android.Manifest.permission.ACCESS_FINE_LOCATION, context.getPackageName()) != PackageManager.PERMISSION_GRANTED) {
            Logger.e("Location permission not granted");
            canUpdateLocation = false;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (context.checkSelfPermission(android.Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                Logger.e("Background location permission not granted");
            }
        }

        if (!canUpdateLocation) {
            stopLocationUpdates(context);
        }

        return canUpdateLocation;
    }

    private static PendingIntent getPendingIntent(Context context) {
        if (mPendingIntent == null) {
            Intent intent = new Intent(context, LocationUpdateReceiver.class);
            intent.setAction(LocationUpdateReceiver.ACTION_PROCESS_UPDATES);
            mPendingIntent = PendingIntent.getBroadcast(context, REQUEST_CODE, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        }
        return mPendingIntent;
    }

    static void updateLocationUpdates(Context context, double min) {
        SharedPrefsManager sharedPrefsManager = SharedPrefsManager.get(context);
        boolean enableLocationUpdates = sharedPrefsManager.getBoolean(SharedPrefsManager.ENABLE_LOCATION_UPDATES, true);

        if (!enableLocationUpdates) {
            stopLocationUpdates(context);
            return;
        }

        int newAccuracy;
        if (min == Double.MAX_VALUE) {
            newAccuracy = SharedPrefsManager.ACCURACY_NONE;
        } else if (min >= SharedPrefsManager.DISPLACEMENT_COUNTRY) {
            newAccuracy = SharedPrefsManager.ACCURACY_COUNTRY;
        } else if (min >= SharedPrefsManager.DISPLACEMENT_CITY) {
            newAccuracy = SharedPrefsManager.ACCURACY_CITY;
        } else {
            newAccuracy = SharedPrefsManager.ACCURACY_STREET;
        }

        int prevAccuracy = sharedPrefsManager.getInteger(SharedPrefsManager.LOCATION_ACCURACY, SharedPrefsManager.ACCURACY_NONE);
        if (prevAccuracy == newAccuracy) {
            return;
        }

        startLocationUpdates(context, newAccuracy);
    }

    static void startLocationUpdates(final Context context, final int accuracy) {
        if (!canUpdateLocation(context)) {
            return;
        }

        if (accuracy == SharedPrefsManager.ACCURACY_NONE) {
            stopLocationUpdates(context);
            return;
        }

        // Redundant check to avoid infinite loop. This method should never be called if accuracy == prev-accuracy
        int prevAccuracy = SharedPrefsManager.get(context).getInteger(SharedPrefsManager.LOCATION_ACCURACY, SharedPrefsManager.ACCURACY_NONE);
        if (accuracy == prevAccuracy) {
            return;
        }

        long interval;
        switch (accuracy) {
            case SharedPrefsManager.ACCURACY_STREET:
                interval = SharedPrefsManager.INTERVAL_STREET;
                break;

            case SharedPrefsManager.ACCURACY_COUNTRY:
                interval = SharedPrefsManager.INTERVAL_COUNTRY;
                break;

            default:
                interval = SharedPrefsManager.INTERVAL_CITY;
                break;
        }

        LocationRequest request = new LocationRequest();
        request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        request.setInterval(interval);
        request.setFastestInterval(interval / 3);
        request.setMaxWaitTime(interval * 3);

        try {
            FusedLocationProviderClient client = LocationServices.getFusedLocationProviderClient(context);
            Task<Void> requestTask = client.requestLocationUpdates(request, getPendingIntent(context));
            requestTask.addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(Task<Void> task) {
                    if (task.isSuccessful()) {
                        Logger.d("Location updates set successfully");
                        SharedPrefsManager.get(context).put(SharedPrefsManager.LOCATION_ACCURACY, accuracy);
                    } else {
                        Logger.e("Location updates set failed");
                    }
                }
            });
        } catch (Exception e) {
            Logger.e("Exception while setting location updates", e);
        }
    }

    static void stopLocationUpdates(final Context context) {
        try {
            int accuracy = SharedPrefsManager.get(context).getInteger(SharedPrefsManager.LOCATION_ACCURACY, SharedPrefsManager.ACCURACY_NONE);
            if (accuracy == SharedPrefsManager.ACCURACY_NONE) {
                return;
            }

            FusedLocationProviderClient client = LocationServices.getFusedLocationProviderClient(context);
            Task<Void> requestTask = client.removeLocationUpdates(getPendingIntent(context));
            requestTask.addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(Task<Void> task) {
                    if (task.isSuccessful()) {
                        Logger.d("Location updates removed successfully");
                        SharedPrefsManager.get(context).put(SharedPrefsManager.LOCATION_ACCURACY, SharedPrefsManager.ACCURACY_NONE);
                    } else {
                        Logger.d("Failed to remove location updates");
                    }
                }
            });
        } catch (Exception e) {
            Logger.e("Exception while removing location updates", e);
        }
    }

    static void dispatchEnterCallback(final Context context, final String id) {
        if (mDiyGeofenceListener != null) {
            mMainHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (mDiyGeofenceListener != null) {
                        mDiyGeofenceListener.onEnter(context, id);
                    }
                }
            });
        }
    }

    static void dispatchExitCallback(final Context context, final String id) {
        if (mDiyGeofenceListener != null) {
            mMainHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (mDiyGeofenceListener != null) {
                        mDiyGeofenceListener.onExit(context, id);
                    }
                }
            });
        }
    }
}

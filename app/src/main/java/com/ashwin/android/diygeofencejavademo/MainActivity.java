package com.ashwin.android.diygeofencejavademo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.ashwin.android.diygeofencejava.DiyGeofence;
import com.ashwin.android.diygeofencejava.DiyGeofenceData;

import java.util.Arrays;
import java.util.Set;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = DiyGeofence.DEBUG_TAG;
    private static final int REQUEST_PERMISSIONS_REQUEST_CODE = 34;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    protected void onStart() {
        super.onStart();
        registerGeofences();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (!checkPermission(Manifest.permission.ACCESS_FINE_LOCATION) || !checkPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION)) {
                startPermissionRequest(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_BACKGROUND_LOCATION});
            }
        } else {
            if (!checkPermission(Manifest.permission.ACCESS_FINE_LOCATION)) {
                startPermissionRequest(new String[]{Manifest.permission.ACCESS_FINE_LOCATION});
            }
        }
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        getRegisteredGeofences();
    }

    private boolean checkPermission(String permission) {
        int permissionState = ActivityCompat.checkSelfPermission(this, permission);
        return permissionState == PackageManager.PERMISSION_GRANTED;
    }

    private void startPermissionRequest(String[] permissions) {
        ActivityCompat.requestPermissions(MainActivity.this, permissions, REQUEST_PERMISSIONS_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.i(TAG, "on-request-permission-results: " + Arrays.toString(grantResults));
        if (requestCode == REQUEST_PERMISSIONS_REQUEST_CODE) {
            if (grantResults.length <= 0) {
                Log.i(TAG, "User interaction was cancelled.");
                Toast.makeText(MainActivity.this, "Permission cancelled", Toast.LENGTH_LONG).show();
            } else if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.i(TAG, "Permission granted");
                Toast.makeText(MainActivity.this, "Permission granted", Toast.LENGTH_LONG).show();
                DiyGeofence.updateLocation(getBaseContext(), true);
            } else {
                Log.i(TAG, "Permission denied");
                Toast.makeText(MainActivity.this, "Permission denied", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void registerGeofences() {
        Log.w(TAG, "app: Registering geofences...");

        Context context = getBaseContext();
        double rad = 500;
        DiyGeofence.addGeofence(context, "bandra", 19.0607, 72.8416, rad);
        DiyGeofence.addGeofence(context, "home", 19.0816, 72.8556, 600d);
        DiyGeofence.addGeofence(context, "santacruz", 19.0817025, 72.8414561, rad);
        DiyGeofence.addGeofence(context, "vile_parle", 19.0995335, 72.8439462, rad);
        DiyGeofence.addGeofence(context, "andheri", 19.1188514, 72.8472434, rad);
        DiyGeofence.addGeofence(context, "jogeshwari", 19.1361473, 72.8488414, rad);
        DiyGeofence.addGeofence(context, "ram_mandir", 19.151618, 72.8501295, rad);
        DiyGeofence.addGeofence(context, "lotus_corporate_park", 19.14519, 72.8528428, rad);
        DiyGeofence.addGeofence(context, "goregaon", 19.1648, 72.8493, rad);
    }

    private void getRegisteredGeofences() {
        Set<DiyGeofenceData> geofences = DiyGeofence.getAllGeofences(getBaseContext());
        Log.w(TAG, "app: registered geofences: " + geofences.size());
        for (DiyGeofenceData geofence : geofences) {
            Log.w(TAG, "app: " + geofence.toString());
        }
    }
}

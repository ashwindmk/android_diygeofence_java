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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = DiyGeofence.DEBUG_TAG + ": app";
    private static final int REQUEST_PERMISSIONS_REQUEST_CODE = 34;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
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

        registerGeofences();

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
            } else if (permissions.length == grantResults.length) {
                 int len = permissions.length;

                 for (int i = 0; i < len; i++) {
                     String permission = permissions[i];
                     int grantResult = grantResults[i];

                     if (grantResult == PackageManager.PERMISSION_GRANTED) {
                         Log.i(TAG, permission + " permission granted");
                         Toast.makeText(MainActivity.this, permission + " permission granted", Toast.LENGTH_LONG).show();

                         if (permission.equals(Manifest.permission.ACCESS_FINE_LOCATION)) {
                             DiyGeofence.updateLocation(getBaseContext(), true);
                         }
                     } else {
                         Log.i(TAG, permission + " permission denied");
                         Toast.makeText(MainActivity.this, permission + " permission denied", Toast.LENGTH_LONG).show();
                     }
                 }
            } else {
                // This should never happen
                Log.e(TAG, "number of permissions and granted-results mismatch");
            }
        }
    }

    private void registerGeofences() {
        Log.w(TAG, "Registering geofences...");

        Context context = getBaseContext();
        double rad = 500;
        DiyGeofence.addGeofence(context, "bandra", 19.0607, 72.8416, rad);
        DiyGeofence.addGeofence(context, "vakola", 19.0816, 72.8556, rad);
        DiyGeofence.addGeofence(context, "santacruz", 19.0817, 72.8415, rad);
        DiyGeofence.addGeofence(context, "vile_parle", 19.0995, 72.8439, rad);
        DiyGeofence.addGeofence(context, "andheri", 19.1189, 72.8472, rad);
        DiyGeofence.addGeofence(context, "jogeshwari", 19.1361, 72.8488, rad);
        DiyGeofence.addGeofence(context, "ram_mandir", 19.1516, 72.8501, rad);
        DiyGeofence.addGeofence(context, "lotus_corporate_park", 19.1447, 72.8533, rad);
        DiyGeofence.addGeofence(context, "goregaon", 19.1648, 72.8493, rad);
    }

    private void getRegisteredGeofences() {
        JSONArray geofences = DiyGeofence.getAllGeofences(getBaseContext());
        int len = geofences.length();
        Log.w(TAG, "registered geofences: " + len);
        for (int i = 0; i < len; i++) {
            try {
                JSONObject geofence = geofences.getJSONObject(i);
                Log.w(TAG, String.valueOf(geofence));
            } catch (JSONException e) {
                Log.e(TAG, "Exception while logging geofence", e);
            }
        }
    }
}

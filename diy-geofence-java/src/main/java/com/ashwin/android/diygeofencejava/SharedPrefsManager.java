package com.ashwin.android.diygeofencejava;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.Set;

class SharedPrefsManager {
    private static final String DIY_GEOFENCE_PREFERENCES = "diy_geofence_preferences";
    private static SharedPreferences.Editor mEditor;
    private static SharedPreferences mSharedPrefs;
    private static SharedPrefsManager mSharedPrefsManager;

    /**
     * Keys
     */
    static final String INITIAL_LOCATION_UPDATE_DELAY = "initial_location_update_delay";
    static final String ENABLE_LOCATION_UPDATES = "enable_location_updates";
    static final String LAST_ENTERED_GEOFENCES = "last_entered_geofences";
    static final String LOCATION_ACCURACY = "location_accuracy";
    static final String LAST_LATITUDE = "last_latitude";
    static final String LAST_LONGITUDE = "last_longitude";

    /**
     * Values
     */
    private static final long ONE_SECOND = 1000;
    private static final long ONE_MINUTE = 60 * ONE_SECOND;
    private static final long ONE_HOUR = 60 * ONE_MINUTE;
    private static final float ONE_KILOMETER = 1000f;

    static final long DEFAULT_INITIAL_LOCATION_UPDATE_DELAY = 10 * ONE_SECOND;

    // Street
    static final long INTERVAL_STREET = 10L * ONE_MINUTE;
    static final float DISPLACEMENT_STREET = ONE_KILOMETER;
    static final int ACCURACY_STREET = 1;

    // City
    static final long INTERVAL_CITY = 3L * ONE_HOUR;
    static final float DISPLACEMENT_CITY = 10 * ONE_KILOMETER;
    static final int ACCURACY_CITY = 2;

    // Country
    static final long INTERVAL_COUNTRY = 12L * ONE_HOUR;
    static final float DISPLACEMENT_COUNTRY = 50 * ONE_KILOMETER;
    static final int ACCURACY_COUNTRY = 3;

    static final int ACCURACY_NONE = 0;

    private SharedPrefsManager(Context context) {
        mSharedPrefs = context.getSharedPreferences(DIY_GEOFENCE_PREFERENCES, Context.MODE_PRIVATE);
    }

    private static void initialize(Context context) {
        synchronized (SharedPrefsManager.class) {
            if (mSharedPrefsManager == null) {
                mSharedPrefsManager = new SharedPrefsManager(context);
            }
        }
    }

    public static SharedPrefsManager get(Context context) {
        if (mSharedPrefsManager == null) {
            initialize(context);
        }
        return mSharedPrefsManager;
    }

    private void doEdit() {
        if (mEditor == null) {
            mEditor = mSharedPrefs.edit();
        }
    }

    private void doCommit() {
        if (mEditor != null) {
            mEditor.apply();
            mEditor = null;
        }
    }

    public void put(String key, String value) {
        doEdit();
        mEditor.putString(key, value).apply();
        doCommit();
    }

    public void put(String key, Set<String> value) {
        doEdit();
        mEditor.putStringSet(key, value);
        doCommit();
    }

    public void put(String key, boolean value) {
        doEdit();
        mEditor.putBoolean(key, value);
        doCommit();
    }

    public void put(String key, int value) {
        doEdit();
        mEditor.putInt(key, value);
        doCommit();
    }

    public void put(String key, long value) {
        doEdit();
        mEditor.putLong(key, value);
        doCommit();
    }

    public void put(String key, double value) {
        doEdit();
        mEditor.putString(key, String.valueOf(value));
        doCommit();
    }

    public String getString(String key, String defaultValue) {
        return mSharedPrefs.getString(key, defaultValue);
    }

    public Set<String> getStringSet(String key, Set<String> defaultValue) {
        return mSharedPrefs.getStringSet(key, defaultValue);
    }

    public boolean getBoolean(String key, boolean defaultValue) {
        return mSharedPrefs.getBoolean(key, defaultValue);
    }

    public int getInteger(String key, int defaultValue) {
        return mSharedPrefs.getInt(key, defaultValue);
    }

    public long getLong(String key, long defaultValue) {
        return mSharedPrefs.getLong(key, defaultValue);
    }

    public double getDouble(String key, double defaultValue) {
        String str = mSharedPrefs.getString(key, null);

        if (str != null && !str.isEmpty()) {
            try {
                return Double.valueOf(str);
            } catch (Exception e) {
                Logger.e("Exception while getting double from shared-prefs", e);
            }
        }

        return defaultValue;
    }

    public void remove(String... keys) {
        doEdit();
        for (String key : keys) {
            mEditor.remove(key);
        }
        doCommit();
    }

    public void clear() {
        doEdit();
        mEditor.clear();
        doCommit();
    }
}

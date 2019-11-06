package com.ashwin.android.diygeofencejava;

import android.util.Log;

class Logger {
    private static int sLevel = Log.DEBUG;

    static void setLevel(int level) {
        sLevel = level;
    }

    static void d(String msg) {
        d(DiyGeofence.TAG, msg);
    }

    static void d(String tag, String msg) {
        if (sLevel <= Log.DEBUG) {
            Log.w(tag, msg);
        }
    }

    static void e(String msg) {
        if (sLevel <= Log.ERROR) {
            Log.e(DiyGeofence.TAG, msg);
        }
    }

    static void e(String msg, Throwable t) {
        if (sLevel <= Log.ERROR) {
            Log.e(DiyGeofence.TAG, msg, t);
        }
    }
}

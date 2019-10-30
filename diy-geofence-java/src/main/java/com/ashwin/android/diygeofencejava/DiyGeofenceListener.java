package com.ashwin.android.diygeofencejava;

import android.content.Context;

public interface DiyGeofenceListener {
    void onEnter(Context context, String id);
    void onExit(Context context, String id);
}

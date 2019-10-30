package com.ashwin.android.diygeofencejava;

import android.os.Handler;
import android.os.HandlerThread;

class DiyServiceManager {
    private static final String BG_THREAD = "diy-geofence-bg";

    private static HandlerThread mHandlerThread = new HandlerThread(BG_THREAD);
    private static Handler mHandler;

    static {
        startHandlerThread();
    }

    private static void startHandlerThread() {
        try {
            mHandlerThread.start();
        } catch (Throwable t) {
            Logger.e("Error while starting handler-thread", t);
        }
    }

    private synchronized static Handler getHandler() {
        if (mHandler == null) {
            if (!mHandlerThread.isAlive()) {
                startHandlerThread();
            }
            mHandler = new Handler(mHandlerThread.getLooper());
        }
        return mHandler;
    }

    static void runProcess(Runnable runnable) {
        getHandler().post(runnable);
    }
}

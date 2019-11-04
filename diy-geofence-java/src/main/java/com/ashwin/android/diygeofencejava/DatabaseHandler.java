package com.ashwin.android.diygeofencejava;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.HashSet;
import java.util.Set;

public class DatabaseHandler extends SQLiteOpenHelper {
    static final int DATABASE_VERSION = 1;
    static final String DATABASE_NAME = "geofence.db";
    static final String TABLE_GEOFENCE = "geofence";
    static final String COLUMN_ID = "id";
    static final String COLUMN_LAT = "lat";
    static final String COLUMN_LNG = "lng";
    static final String COLUMN_RAD = "rad";

    private static DatabaseHandler instance = null;

    private DatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    static DatabaseHandler getInstance(Context context) {
        if (instance == null) {
            synchronized (DatabaseHandler.class) {
                if (instance == null) {
                    instance = new DatabaseHandler(context);
                }
            }
        }
        return instance;
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        String geofenceQuery = ("CREATE TABLE " +
                TABLE_GEOFENCE + "("
                + COLUMN_ID + " TEXT PRIMARY KEY,"
                + COLUMN_LAT + " REAL,"
                + COLUMN_LNG + " REAL,"
                + COLUMN_RAD + " REAL"
                + ")");

        sqLiteDatabase.execSQL(geofenceQuery);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_GEOFENCE);
        onCreate(sqLiteDatabase);
    }

    boolean addGeofence(String id, double lat, double lng, double rad) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_ID, id);
        values.put(COLUMN_LAT, lat);
        values.put(COLUMN_LNG, lng);
        values.put(COLUMN_RAD, rad);

        long result = -1;
        try {
            SQLiteDatabase db = this.getWritableDatabase();
            result = db.insertWithOnConflict(TABLE_GEOFENCE, null, values, SQLiteDatabase.CONFLICT_REPLACE);
            db.close();
        } catch (Throwable t) {
            Logger.e("Exception while adding geofence: " + id, t);
        }
        return result != -1L;
    }

    boolean removeGeofence(String id) {
        boolean result = false;
        String query = "SELECT * FROM " + TABLE_GEOFENCE + " WHERE " + COLUMN_ID  + " = '" + id + "'";
        try {
            SQLiteDatabase db = this.getWritableDatabase();
            Cursor cursor = db.rawQuery(query, null);
            if (cursor != null && cursor.moveToFirst()) {
                db.delete(TABLE_GEOFENCE, COLUMN_ID + " = ?", new String[]{id});
                cursor.close();
                result = true;
            }

            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }

            db.close();

        } catch (Throwable t) {
            Logger.e("Exception while removing geofence: " + id, t);
        }
        return result;
    }

    boolean removeAllGeofences() {
        try {
            SQLiteDatabase db = this.getWritableDatabase();
            db.delete(TABLE_GEOFENCE, null, null);
            db.close();
            return true;
        } catch (Throwable t) {
            Logger.e("Exception while removing all geofences", t);
        }
        return false;
    }

    DiyGeofenceData getGeofence(String id) {
        DiyGeofenceData result = null;
        String query = "SELECT * FROM " + TABLE_GEOFENCE + " WHERE " + COLUMN_ID + " = '" + id + "'";
        try {
            SQLiteDatabase db = this.getWritableDatabase();
            Cursor cursor = db.rawQuery(query, null);
            if (cursor != null && cursor.moveToFirst()) {
                double lat = cursor.getDouble(cursor.getColumnIndex(COLUMN_LAT));
                double lng = cursor.getDouble(cursor.getColumnIndex(COLUMN_LNG));
                double rad = cursor.getDouble(cursor.getColumnIndex(COLUMN_RAD));
                result = new DiyGeofenceData(id, lat, lng, rad);
            }

            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }

            db.close();
        } catch (Throwable t) {
            Logger.e("Exception while getting geofence: " + id, t);
        }
        return result;
    }

    Set<DiyGeofenceData> getAllGeofences() {
        Set<DiyGeofenceData> result = new HashSet<DiyGeofenceData>();
        String query = "SELECT * FROM " + TABLE_GEOFENCE;
        try {
            SQLiteDatabase db = this.getWritableDatabase();
            Cursor cursor = db.rawQuery(query, null);
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    String id = cursor.getString(cursor.getColumnIndex(COLUMN_ID));
                    double lat = cursor.getDouble(cursor.getColumnIndex(COLUMN_LAT));
                    double lng = cursor.getDouble(cursor.getColumnIndex(COLUMN_LNG));
                    double rad = cursor.getDouble(cursor.getColumnIndex(COLUMN_RAD));
                    result.add(new DiyGeofenceData(id, lat, lng, rad));
                } while (cursor.moveToNext());
            }

            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }

            db.close();
        } catch (Throwable t) {
            Logger.e("Exception while getting all geofences", t);
        }
        return result;
    }
}

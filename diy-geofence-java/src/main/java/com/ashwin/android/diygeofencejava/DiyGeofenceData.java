package com.ashwin.android.diygeofencejava;

public class DiyGeofenceData {
    private String id;
    private double lat;
    private double lng;
    private double rad;

    public DiyGeofenceData(String id, double lat, double lng, double rad) {
        this.id = id;
        this.lat = lat;
        this.lng = lng;
        this.rad = rad;
    }

    public String getId() {
        return id;
    }

    public double getLat() {
        return lat;
    }

    public double getLng() {
        return lng;
    }

    public double getRad() {
        return rad;
    }

    @Override
    public String toString() {
        return "{id: " + this.id + ", lat: " + this.lat + ", lng: " + this.lng + ", rad: " + this.rad + "}";
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof DiyGeofenceData) {
            DiyGeofenceData other = (DiyGeofenceData) obj;
            return (this.id.equals(other.id)
                    && this.lat == other.lat
                    && this.lng == other.lng
                    && this.rad == other.rad);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        Object[] a = new Object[]{this.id, this.lat, this.lng, this.rad};

        /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            return java.util.Objects.hash(a);
        }*/

        int result = 1;
        for (Object element : a) {
            result = 31 * result + (element == null ? 0 : element.hashCode());
        }
        return result;
    }
}

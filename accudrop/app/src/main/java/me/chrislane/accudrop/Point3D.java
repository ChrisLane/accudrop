package me.chrislane.accudrop;

import com.google.android.gms.maps.model.LatLng;

public class Point3D {

    private LatLng latLng;
    private double altitude;

    public Point3D(LatLng latLng, double altitude) {
        this.latLng = latLng;
        this.altitude = altitude;
    }

    public LatLng getLatLng() {
        return latLng;
    }

    public double getAltitude() {
        return altitude;
    }

    public void set(LatLng latLng, double altitude) {
        this.latLng = latLng;
        this.altitude = altitude;
    }
}

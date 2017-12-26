package me.chrislane.accudrop;

import com.google.android.gms.maps.model.LatLng;

public class Point3D {

    private LatLng latLng;
    private float altitude;

    public Point3D(LatLng latLng, float altitude) {
        this.latLng = latLng;
        this.altitude = altitude;
    }

    public LatLng getLatLng() {
        return latLng;
    }

    public double getAltitude() {
        return altitude;
    }

    public void set(LatLng latLng, float altitude) {
        this.latLng = latLng;
        this.altitude = altitude;
    }
}

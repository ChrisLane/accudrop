package me.chrislane.accudrop;

import com.google.android.gms.maps.model.LatLng;

public class Point3D {

    private LatLng latLng;
    private double altitude;

    public Point3D(LatLng latLng, double altitude) {
        this.latLng = latLng;
        this.altitude = altitude;
    }

    /**
     * Get the latitude and longitude from the point.
     *
     * @return The latitude and longitude from the point.
     */
    public LatLng getLatLng() {
        return latLng;
    }

    /**
     * Get the altitude from the point.
     *
     * @return The altitude from the point.
     */
    public double getAltitude() {
        return altitude;
    }

    /**
     * Set the latitude, longitude and altitude for the point.
     *
     * @param latLng   The latitude and longitude to be set.
     * @param altitude The altitude to be set.
     */
    public void set(LatLng latLng, double altitude) {
        this.latLng = latLng;
        this.altitude = altitude;
    }

    @Override
    public String toString() {
        return "(" + latLng + " Alt: " + altitude + "m)";
    }
}

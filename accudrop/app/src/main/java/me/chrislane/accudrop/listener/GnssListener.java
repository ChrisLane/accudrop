package me.chrislane.accudrop.listener;

import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

import me.chrislane.accudrop.viewmodel.GnssViewModel;

public class GnssListener implements android.location.LocationListener {
    private static final String TAG = GnssListener.class.getSimpleName();
    private final GnssViewModel gnssViewModel;
    private final LocationManager locationManager;

    public GnssListener(GnssViewModel gnssViewModel) {
        this.gnssViewModel = gnssViewModel;
        locationManager = (LocationManager) gnssViewModel.getApplication()
                .getSystemService(Context.LOCATION_SERVICE);
    }

    /**
     * Tell the location manager to start collecting location updates.
     */
    @SuppressLint("MissingPermission")
    public void startListening() {
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            Log.d(TAG, "Listening on location.");
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
        } else {
            // TODO: Do something if GPS is disabled
        }
    }

    /**
     * Tell the location manager to stop getting location updates.
     */
    public void stopListening() {
        Log.d(TAG, "Stopped listening on Position.");
        locationManager.removeUpdates(this);
    }

    /**
     * Called to notify the app of a location change.
     *
     * @param location The new location of the device.
     */
    @Override
    public void onLocationChanged(Location location) {
        gnssViewModel.setLastLocation(location);
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }
}

package me.chrislane.accudrop.listener;

import android.location.Location;
import android.os.Bundle;

import me.chrislane.accudrop.viewmodel.LocationViewModel;

public class GnssListener implements android.location.LocationListener {
    private final LocationViewModel locationViewModel;

    public GnssListener(LocationViewModel locationViewModel) {
        this.locationViewModel = locationViewModel;
    }

    /**
     * Called to notify the app of a location change.
     *
     * @param location The new location of the device.
     */
    @Override
    public void onLocationChanged(Location location) {
        locationViewModel.setLastLocation(location);
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

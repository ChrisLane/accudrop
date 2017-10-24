package me.chrislane.accudrop;

import android.location.Location;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;

public class LocationManager {

    private final GoogleApiClient googleApiClient;
    private final MainActivity mainActivity;
    private boolean connected = false;
    private List<LocationListener> listenerQueue = new ArrayList<>();

    public LocationManager(MainActivity mainActivity, GoogleApiClient googleApiClient) {
        this.mainActivity = mainActivity;
        this.googleApiClient = googleApiClient;
    }

    public void startLocationUpdates() {
        connected = true;
        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        mainActivity.getPermissionManager().checkLocationPermission();

        for (LocationListener listener : listenerQueue)
            LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, listener);
    }

    public void stopLocationUpdates() {
        connected = false;

        for (LocationListener listener : listenerQueue) {
            LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, listener);
        }
    }

    public void requestLocationUpdates(LocationListener listener) {
        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        mainActivity.getPermissionManager().checkLocationPermission();
        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, listener);
    }

    public void addListener(LocationListener listener) {
        if (connected) {
            requestLocationUpdates(listener);
            listenerQueue.add(listener);
        } else {
            listenerQueue.add(listener);
        }
    }

    public void removeListener(LocationListener listener) {
        LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, listener);
    }

    public LatLng getLatLng(Location location) {
        return new LatLng(location.getLatitude(), location.getLongitude());
    }

}

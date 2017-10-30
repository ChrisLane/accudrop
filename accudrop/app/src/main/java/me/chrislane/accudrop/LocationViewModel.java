package me.chrislane.accudrop;

import android.arch.lifecycle.ViewModel;
import android.location.Location;
import android.util.Log;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;

public class LocationViewModel extends ViewModel {

    private boolean connected = false;
    private GoogleApiClient googleApiClient;
    private List<LocationListener> listenerQueue = new ArrayList<>();


    public void startLocationUpdates(GoogleApiClient googleApiClient) {
        this.googleApiClient = googleApiClient;
        connected = true;
        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        for (LocationListener listener : listenerQueue) {
            LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, listener);
        }
    }

    public void stopLocationUpdates() {
        Log.d("LocMgr", "Stopping location updates");
        connected = false;

        for (LocationListener listener : listenerQueue) {
            LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, listener);
        }
    }

    public void requestLocationUpdates(LocationListener listener) {
        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

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

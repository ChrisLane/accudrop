package me.chrislane.accudrop;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.location.Location;
import android.util.Log;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;

public class LocationViewModel extends ViewModel implements LocationListener {
    private GoogleApiClient googleApiClient;
    private MutableLiveData<Location> lastLocation = new MutableLiveData<>();

    public LocationViewModel() {
        Location loc = new Location("");
        loc.setLatitude(51.52);
        loc.setLongitude(0.08);
        lastLocation.setValue(loc);
    }


    public void startLocationUpdates(GoogleApiClient googleApiClient) {
        this.googleApiClient = googleApiClient;

        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);

    }

    public void stopLocationUpdates() {
        Log.d("LocMgr", "Stopping location updates");

        LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, this);

    }

    public LatLng getLatLng(Location location) {
        return new LatLng(location.getLatitude(), location.getLongitude());
    }

    public LiveData<Location> getLastLocation() {
        return lastLocation;
    }

    @Override
    public void onLocationChanged(Location location) {
        lastLocation.setValue(location);
    }
}

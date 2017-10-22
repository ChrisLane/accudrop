package me.chrislane.accudrop;

import android.location.Location;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;

public class LocationManager implements LocationListener {

    private final GoogleApiClient googleApiClient;
    private final MainActivity mainActivity;
    private Location lastLocation;

    public LocationManager(MainActivity mainActivity, GoogleApiClient googleApiClient) {
        this.mainActivity = mainActivity;
        this.googleApiClient = googleApiClient;
    }

    @Override
    public void onLocationChanged(Location location) {
        this.lastLocation = location;
    }

    public void startLocationUpdates() {
        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        mainActivity.getPermissionManager().checkLocationPermission();
        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, mLocationRequest, this);
    }

    public void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, this);
    }

    public Location getLastLocation() {
        return lastLocation;
    }

    public LatLng getLastLatLng() {
        return new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude());
    }
}

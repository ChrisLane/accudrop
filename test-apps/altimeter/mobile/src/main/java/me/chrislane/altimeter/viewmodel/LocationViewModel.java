package me.chrislane.altimeter.viewmodel;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

public class LocationViewModel extends ViewModel implements LocationListener {
    private static final String TAG = "location_view_model";
    private GoogleApiClient googleApiClient;
    private MutableLiveData<Location> lastLocation = new MutableLiveData<>();
    private MutableLiveData<Location> groundLocation = new MutableLiveData<>();
    private MutableLiveData<Double> lastAltitude = new MutableLiveData<>();
    private LocationManager locationManager;
    private boolean listening;

    public void initialise(Context context) {
        Log.d(TAG, "Initialising.");
        Context applicationContext = context.getApplicationContext();

        ApiClientListener apiClientListener = new ApiClientListener();
        googleApiClient = new GoogleApiClient.Builder(applicationContext)
                .addConnectionCallbacks(apiClientListener)
                .addOnConnectionFailedListener(apiClientListener)
                .addApi(LocationServices.API)
                .build();
        googleApiClient.connect();
    }

    public void startListening() {
        if (googleApiClient.isConnected() && !listening) {
            listening = true;
            Log.d(TAG, "Listening on location.");
            LocationRequest request = LocationRequest.create();
            request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

            LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, request, this);
        }
    }

    public void stopListening() {
        Log.d(TAG, "Stopped listening on Location.");
        listening = false;
        LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, this);
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.d(TAG, "GPS Altitude given: " + location.hasAltitude());
        Log.d(TAG, "Altitude is: " + location.getAltitude());
        lastLocation.setValue(location);
        updateAltitude();
    }

    public LiveData<Location> getLastLocation() {
        return lastLocation;
    }

    public void setGroundLocation(Location groundLocation) {
        this.groundLocation.setValue(groundLocation);
    }

    public void setGroundLocation() {
        if (groundLocation.getValue() != null) {
            setGroundLocation(groundLocation.getValue());
        }
    }

    public LiveData<Double> getLastAltitude() {
        return lastAltitude;
    }

    public void setLastAltitude(double lastAltitude) {
        this.lastAltitude.setValue(lastAltitude);
    }

    private void updateAltitude() {
        Location ground = groundLocation.getValue();
        Location last = lastLocation.getValue();
        double altitude;

        if (ground == null && last != null) {
            altitude = last.getAltitude();
        } else if (ground != null && last != null) {
            altitude = last.getAltitude() - ground.getAltitude();
        } else {
            return;
        }
        setLastAltitude(altitude);
    }

    private class ApiClientListener implements GoogleApiClient.OnConnectionFailedListener,
            GoogleApiClient.ConnectionCallbacks {
        private static final String TAG = "api_client_listener";

        @Override
        public void onConnected(@Nullable Bundle bundle) {
            Log.d(TAG, "Connected to Google API.");
            startListening();
        }

        @Override
        public void onConnectionSuspended(int i) {
            Log.d(TAG, "Connection to Google API suspended.");
            stopListening();
        }

        @Override
        public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
            Log.d(TAG, "Connection to Google API failed: " + connectionResult.toString());
        }
    }
}

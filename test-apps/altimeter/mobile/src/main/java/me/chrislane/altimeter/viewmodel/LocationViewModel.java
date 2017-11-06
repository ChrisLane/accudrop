package me.chrislane.altimeter.viewmodel;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;

public class LocationViewModel extends AndroidViewModel implements LocationListener {
    private static final String TAG = "location_view_model";
    private MutableLiveData<Location> lastLocation = new MutableLiveData<>();
    private MutableLiveData<Location> groundLocation = new MutableLiveData<>();
    private MutableLiveData<Double> lastAltitude = new MutableLiveData<>();
    private LocationManager locationManager;

    public LocationViewModel(@NonNull Application application) {
        super(application);
    }

    public void startListening() {
        Log.d(TAG, "Listening on location.");

        locationManager = (LocationManager) getApplication().getSystemService(Context.LOCATION_SERVICE);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
    }

    public void stopListening() {
        Log.d(TAG, "Stopped listening on Location.");
        locationManager.removeUpdates(this);
    }

    @Override
    public void onLocationChanged(Location location) {
        lastLocation.setValue(location);
        updateAltitude();
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    public LiveData<Location> getLastLocation() {
        return lastLocation;
    }

    public LiveData<Double> getLastAltitude() {
        return lastAltitude;
    }

    public void setLastAltitude(double lastAltitude) {
        this.lastAltitude.setValue(lastAltitude);
    }

    public void setGroundLocation(Location groundLocation) {
        this.groundLocation.setValue(groundLocation);
    }

    public void setGroundLocation() {
        if (lastLocation.getValue() != null) {
            setGroundLocation(lastLocation.getValue());
        }
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
}

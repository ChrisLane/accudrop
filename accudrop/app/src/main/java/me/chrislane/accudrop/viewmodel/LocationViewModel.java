package me.chrislane.accudrop.viewmodel;

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
import com.google.android.gms.maps.model.LatLng;

public class LocationViewModel extends AndroidViewModel implements LocationListener {
    private static final String TAG = "location_view_model";
    private MutableLiveData<Location> lastLocation = new MutableLiveData<>();
    private LocationManager locationManager;

    public LocationViewModel(@NonNull Application application) {
        super(application);

        Location loc = new Location("");
        loc.setLatitude(51.52);
        loc.setLongitude(0.08);
        lastLocation.setValue(loc);
    }

    /**
     * Tell the location manager to start collecting location updates.
     */
    public void startListening() {
        Log.d(TAG, "Listening on location.");

        locationManager = (LocationManager) getApplication().getSystemService(Context.LOCATION_SERVICE);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
    }

    /**
     * Tell the location manager to stop getting location updates.
     */
    public void stopListening() {
        Log.d(TAG, "Stopped listening on Location.");
        locationManager.removeUpdates(this);
    }

    /**
     * Get a LatLng object from a Location object.
     *
     * @param location The location to get latitude and longitude from.
     * @return A LatLng object with latitude and longitude of the given location.
     */
    public LatLng getLatLng(Location location) {
        return new LatLng(location.getLatitude(), location.getLongitude());
    }

    /**
     * Get the device location for the last location update.
     *
     * @return The last known device location.
     */
    public LiveData<Location> getLastLocation() {
        return lastLocation;
    }

    /**
     * Called to notify the app of a location change.
     *
     * @param location The new location of the device.
     */
    @Override
    public void onLocationChanged(Location location) {
        lastLocation.setValue(location);
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
}

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
import me.chrislane.accudrop.db.AccudropDb;
import me.chrislane.accudrop.db.Position;

import java.util.Date;

public class LocationViewModel extends AndroidViewModel implements LocationListener {
    private static final String TAG = LocationViewModel.class.getSimpleName();
    private MutableLiveData<Location> lastLocation = new MutableLiveData<>();
    private LocationManager locationManager;
    private AccudropDb db;

    public LocationViewModel(@NonNull Application application) {
        super(application);
        db = AccudropDb.getInMemoryDatabase(application);

        Location loc = new Location("");
        loc.setLatitude(51.52);
        loc.setLongitude(0.08);
        lastLocation.setValue(loc);
        locationManager = (LocationManager) getApplication().getSystemService(Context.LOCATION_SERVICE);
    }

    /**
     * Tell the location manager to start collecting location updates.
     */
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
     * Get a LatLng object from a Position object.
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
        addPosition(location);
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

    private void addPosition(Location location) {
        Integer jumpId = db.jumpModel().findLastJumpId().getValue();
        if (jumpId == null) {
            Log.e(TAG, "Attempted to add a position to a non-existent jump.");
            return;
        }

        Position pos = new Position();
        pos.latitude = location.getLatitude();
        pos.longitude = location.getLongitude();
        pos.time = new Date();
        pos.jumpId = jumpId;

        db.locationModel().insertPosition(pos);
    }

    private void subscribeToDbChanges() {

    }
}

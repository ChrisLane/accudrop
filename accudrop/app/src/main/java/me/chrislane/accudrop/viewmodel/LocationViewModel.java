package me.chrislane.accudrop.viewmodel;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.location.Location;
import android.support.annotation.NonNull;

import com.google.android.gms.maps.model.LatLng;

import me.chrislane.accudrop.listener.GnssListener;

public class LocationViewModel extends AndroidViewModel {
    private static final String TAG = LocationViewModel.class.getSimpleName();
    private final MutableLiveData<Location> lastLocation = new MutableLiveData<>();
    private final GnssListener gnssListener;

    public LocationViewModel(@NonNull Application application) {
        super(application);
        gnssListener = new GnssListener(this);

        Location loc = new Location("");
        loc.setLatitude(51.52);
        loc.setLongitude(0.08);
        lastLocation.setValue(loc);
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

    public void setLastLocation(Location location) {
        lastLocation.setValue(location);
    }

    public void startListening() {
        gnssListener.startListening();
    }

    public void stopListening() {
        gnssListener.stopListening();
    }
}

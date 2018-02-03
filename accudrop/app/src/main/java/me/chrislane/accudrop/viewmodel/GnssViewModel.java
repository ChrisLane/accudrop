package me.chrislane.accudrop.viewmodel;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.android.gms.maps.model.LatLng;

import me.chrislane.accudrop.listener.GnssListener;

public class GnssViewModel extends AndroidViewModel {

    private static final String TAG = GnssViewModel.class.getSimpleName();
    private final MutableLiveData<Location> lastLocation = new MutableLiveData<>();
    private final GnssListener gnssListener;

    public GnssViewModel(@NonNull Application application) {
        super(application);
        gnssListener = new GnssListener(this);
    }

    /**
     * Get a LatLng object from a Position object.
     *
     * @param location The location to get latitude and longitude from.
     * @return A LatLng object with latitude and longitude of the given location.
     */
    @Nullable
    public LatLng getLatLng(Location location) {
        if (location != null) {
            return new LatLng(location.getLatitude(), location.getLongitude());
        }
        return null;
    }

    /**
     * Get the device location for the last location updateDrawable.
     *
     * @return The last known device location.
     */
    public LiveData<Location> getLastLocation() {
        return lastLocation;
    }

    /**
     * Set the last location.
     *
     * @param location The last location.
     */
    public void setLastLocation(Location location) {
        lastLocation.setValue(location);
    }

    /**
     * Get the GNSS listener.
     *
     * @return The GNSS listener.
     */
    public GnssListener getGnssListener() {
        return gnssListener;
    }
}

package me.chrislane.accudrop.viewmodel

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.location.Location
import android.util.Log

import com.google.android.gms.maps.model.LatLng

import me.chrislane.accudrop.BuildConfig
import me.chrislane.accudrop.listener.GnssListener

class GnssViewModel(application: Application) : AndroidViewModel(application) {
    private val lastLocation = MutableLiveData<Location>()
    val gnssListener: GnssListener = GnssListener(this)

    /**
     * Get the device location for the last location updateDrawable.
     *
     * @return The last known device location.
     */
    fun getLastLocation(): LiveData<Location> {
        return lastLocation
    }

    /**
     * Set the last location.
     *
     * @param location The last location.
     */
    fun setLastLocation(location: Location) {
        lastLocation.setValue(location)

        if (BuildConfig.DEBUG) {
            Log.v(TAG, "Location set: $location")
        }
    }

    companion object {

        private val TAG = GnssViewModel::class.java.simpleName

        /**
         * Get a LatLng object from a Position object.
         *
         * @param location The location to get latitude and longitude from.
         * @return A LatLng object with latitude and longitude of the given location.
         */
        fun getLatLng(location: Location): LatLng {
            return LatLng(location.latitude, location.longitude)
        }
    }
}

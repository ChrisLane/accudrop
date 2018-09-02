package me.chrislane.accudrop.listener

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast

import me.chrislane.accudrop.viewmodel.GnssViewModel

class GnssListener(private val gnssViewModel: GnssViewModel) : android.location.LocationListener {
    private val locationManager: LocationManager = gnssViewModel.getApplication<Application>()
            .getSystemService(Context.LOCATION_SERVICE) as LocationManager

    /**
     * Tell the location manager to start collecting location updates.
     */
    @SuppressLint("MissingPermission")
    fun startListening() {
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            Log.d(TAG, "Listening on location.")
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0f, this)
        } else {
            // TODO #49: Change to more appropriate notification. Be more useful.
            Toast.makeText(gnssViewModel.getApplication<Application>(), "GPS is disabled.", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Tell the location manager to stop getting location updates.
     */
    fun stopListening() {
        Log.d(TAG, "Stopped listening on Position.")
        locationManager.removeUpdates(this)
    }

    /**
     * Called to notify the app of a location change.
     *
     * @param location The new location of the device.
     */
    override fun onLocationChanged(location: Location) {
        gnssViewModel.setLastLocation(location)
    }

    override fun onStatusChanged(s: String, i: Int, bundle: Bundle) {

    }

    override fun onProviderEnabled(s: String) {

    }

    override fun onProviderDisabled(s: String) {

    }

    companion object {
        private val TAG = GnssListener::class.java.simpleName
    }
}

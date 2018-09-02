package me.chrislane.accudrop.fragment

import android.graphics.Color
import android.location.Location
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.util.Pair
import android.util.Log
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.PolylineOptions
import me.chrislane.accudrop.R
import me.chrislane.accudrop.presenter.ReplayMapPresenter
import me.chrislane.accudrop.viewmodel.GnssViewModel
import java.util.*

class ReplayMapFragment : Fragment(), OnMapReadyCallback {
    lateinit var map: GoogleMap
        private set
    private lateinit var replayMapPresenter: ReplayMapPresenter
    private lateinit var camPosBuilder: CameraPosition.Builder
    private var bearing: Float = 0F

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        replayMapPresenter = ReplayMapPresenter(this)

        camPosBuilder = CameraPosition.Builder()
                .zoom(15.5f)
                .bearing(0f)
                .tilt(0f)

        val parentFragment = parentFragment
        if (parentFragment != null) {
            val mapFragment = parentFragment
                    .childFragmentManager.findFragmentById(R.id.replay_map) as SupportMapFragment
            mapFragment.getMapAsync(this)
        }
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        Log.i(TAG, "Map ready")
        map = googleMap

        setupMap()
    }

    /**
     * Set up the GoogleMap with initial settings and get last jump data.
     */
    private fun setupMap() {
        // Set map settings
        map.uiSettings.isMapToolbarEnabled = false
        map.isBuildingsEnabled = true
        map.mapType = GoogleMap.MAP_TYPE_HYBRID
        map.setOnCameraMoveListener { this.onCameraMove() }

        replayMapPresenter.addObservers()
    }

    /**
     *
     * Called when the Google Maps camera is moved.
     *
     * This method updates the side view if there are any changes in the camera bearing.
     */
    private fun onCameraMove() {
        val newBearing = map.cameraPosition.bearing

        if (bearing != newBearing) {
            bearing = newBearing

            updateSideView()
        }
    }

    /**
     * Send screen coordinates to the side view to be updated.
     */
    private fun updateSideView() {
        val replayFragment = parentFragment as ReplayFragment?
        replayFragment?.replaySideView?.updateDrawable(true)
    }

    /**
     * Place a jump route on the map.
     */
    fun updateMapRoutes(usersAndLocs: MutableList<Pair<UUID, MutableList<Location>>>) {
        map.clear()

        // Set camera location
        // TODO: Make this use the 'subject' user rather than just the first
        if (!usersAndLocs.isEmpty()) {
            val subjectRoute = usersAndLocs[0].second
            if (subjectRoute != null && !subjectRoute.isEmpty()) {
                val lastPos = GnssViewModel.getLatLng(subjectRoute[subjectRoute.size - 1])
                Log.d(TAG, lastPos.toString())
                val camPos = camPosBuilder.target(lastPos).build()
                map.moveCamera(CameraUpdateFactory.newCameraPosition(camPos))
            }
        }

        for (userAndLocs in usersAndLocs) {
            val route = userAndLocs.second

            if (route != null && !route.isEmpty()) {
                val options = PolylineOptions()
                options.width(5f).color(Color.RED)
                for (i in 0 until route.size - 1) {
                    val point1 = route[i]
                    val point2 = route[i + 1]

                    options.add(GnssViewModel.getLatLng(point1), GnssViewModel.getLatLng(point2))
                }
                map.addPolyline(options)
            } else {
                Log.w(TAG, "No route available for this jump.")
            }
        }

        updateSideView()
    }

    companion object {
        private val TAG = ReplayMapFragment::class.java.simpleName
    }
}

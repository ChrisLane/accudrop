package me.chrislane.accudrop.fragment

import android.content.SharedPreferences
import android.graphics.Color
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.appcompat.content.res.AppCompatResources
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.preference.PreferenceManager
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import com.mapbox.maps.*
import com.mapbox.maps.plugin.LocationPuck2D
import com.mapbox.maps.plugin.locationcomponent.location
import me.chrislane.accudrop.MainActivity
import me.chrislane.accudrop.R
import me.chrislane.accudrop.UnitType
import me.chrislane.accudrop.util.DistanceAndSpeedUtil
import me.chrislane.accudrop.presenter.PlanPresenter
import me.chrislane.accudrop.viewmodel.GnssViewModel
import me.chrislane.accudrop.viewmodel.RouteViewModel
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.MapView
import com.mapbox.maps.Style
import com.mapbox.maps.extension.style.expressions.dsl.generated.interpolate

class PlanFragment : Fragment(), LifecycleOwner, SharedPreferences.OnSharedPreferenceChangeListener {
    private lateinit var mapView: MapView
    private lateinit var map: MapboxMap
    private lateinit var gnssViewModel: GnssViewModel
    private lateinit var routeViewModel: RouteViewModel
    private lateinit var planPresenter: PlanPresenter
    private lateinit var preferences: SharedPreferences
    private val camPosBuilder: CameraPosition.Builder = CameraPosition.Builder().zoom(15.5f).bearing(0f).tilt(0f)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        retainInstance = true

        // Add this as a location listener
        if (savedInstanceState == null) {
            val main = requireActivity() as MainActivity
            gnssViewModel = ViewModelProvider(main).get(GnssViewModel::class.java)
            gnssViewModel.gnssListener.startListening()
            routeViewModel = ViewModelProvider(main).get(RouteViewModel::class.java)
        }

        preferences = PreferenceManager.getDefaultSharedPreferences(context)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mapView = MapView(
            inflater.context,
            MapInitOptions(inflater.context)
        )

        onMapReady(mapView)

        // Inflate the layout for this fragment
/*        val view = inflater.inflate(R.layout.fragment_plan, container, false)

        // Set up the map
        val mapFragment = childFragmentManager.findFragmentById(R.id.plan_map_fragment) as SupportMapFragment
        mapFragment.getMapAsync(this)

        preferences.registerOnSharedPreferenceChangeListener(this)*/

        return mapView
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    fun onMapReady(mapView: MapView) {
        Log.i(TAG, "Map ready")
        map = mapView.getMapboxMap()

        setupMap()
        subscribeToRoute()

        val lastLocation = gnssViewModel.getLastLocation().value
        planPresenter = if (lastLocation != null) {
            val gnssLocation = GnssViewModel.getLatLng(lastLocation)
            PlanPresenter(this, gnssLocation)
        } else {
            PlanPresenter(this)
        }
    }

    /**
     * Set up the GoogleMap with initial settings and location.
     */
    private fun setupMap() {
        val main = requireActivity() as MainActivity
        val permissionManager = main.permissionManager

        // Initial map setup
        if (permissionManager.checkLocationPermission()) {
        } else {
            permissionManager.requestLocationPermission("Location access is required to find your location.")
            return
        }
        mapView.getMapboxMap().setCamera(
            CameraOptions.Builder()
                .zoom(14.0)
                .build()
        )
        mapView.getMapboxMap().loadStyleUri(
            Style.SATELLITE_STREETS
        ) {
            initLocationComponent()
        }
    }

    private fun initLocationComponent() {
        val main = requireActivity() as MainActivity
        val locationComponentPlugin = mapView.location
        locationComponentPlugin.updateSettings {
            this.enabled = true
            this.locationPuck = LocationPuck2D(
                bearingImage = AppCompatResources.getDrawable(
                    main,
                    R.drawable.mapbox_user_puck_icon,
                ),
                shadowImage = AppCompatResources.getDrawable(
                    main,
                    R.drawable.mapbox_user_icon_shadow,
                ),
                scaleExpression = interpolate {
                    linear()
                    zoom()
                    stop {
                        literal(0.0)
                        literal(0.6)
                    }
                    stop {
                        literal(20.0)
                        literal(1.0)
                    }
                }.toJson()
            )
        }
    }

    /**
     *
     * Called when the user touches and holds on the map.
     *
     * This will set a new landing target for route calculations.
     *
     * @param latLng The new landing target location.
     */
    private fun onMapLongClick(latLng: LatLng) {
        // Update map camera position
        val camPos = camPosBuilder.target(latLng).build()
        //map.animateCamera(CameraUpdateFactory.newCameraPosition(camPos))

        // Update route
        routeViewModel.setTarget(latLng)
        planPresenter.calcRoute(latLng)
    }

    /**
     * Subscribes to any changes to the route in the `RouteViewModel` and
     * updates the map to display these changes.
     */
    private fun subscribeToRoute() {
        routeViewModel.getRoute().observe(viewLifecycleOwner, Observer<MutableList<Location>> {
            if (it != null) {
                this.updateRoute(it)
            }
        })
    }

    override fun onDetach() {
        super.onDetach()
        preferences.registerOnSharedPreferenceChangeListener(this)
    }

    private fun updateRoute(route: MutableList<Location>) {
/*        map.clear()

        val unitString = preferences.getString("general_unit", "")!!
        val unit = UnitType.valueOf(unitString.toUpperCase())

        for (i in 0 until route.size - 1) {
            val point1 = route[i]
            val point2 = route[i + 1]
            map.addPolyline(PolylineOptions()
                    .add(GnssViewModel.getLatLng(point1), GnssViewModel.getLatLng(point2))
                    .width(5f)
                    .color(Color.RED))
            map.addMarker(MarkerOptions()
                    .position(GnssViewModel.getLatLng(point1))
                    .title(
                        DistanceAndSpeedUtil.getAltitudeText(
                            DistanceAndSpeedUtil.getAltitudeInUnit(point1.altitude, unit), unit))
            )
        }

        map.addMarker(MarkerOptions()
                .position(GnssViewModel.getLatLng(route[route.size - 1]))
                .title("Landing"))*/
    }

    /**
     * Sets the visibility of the progress bar shown while routes are being calculated.
     *
     * @param showBar Set to `true` to show the progress bar and
     * `false` to hide it.
     */
    fun setProgressBarVisibility(showBar: Boolean) {
        val view = view
        if (view != null) {
            val progressBar = view.findViewById<ProgressBar>(R.id.progressbar)
            progressBar.visibility = if (showBar) {
                View.VISIBLE
            } else {
                View.GONE
            }
        }
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
        when (key) {
            "general_unit" -> {
                val route = routeViewModel.getRoute().value
                if (route != null) {
                    updateRoute(route)
                }
            }
            "landing_pattern_downwind_altitude", "landing_pattern_crosswind_altitude", "landing_pattern_upwind_altitude" -> {
                val target = routeViewModel.getTarget().value
                if (target != null) {
                    planPresenter.calcRoute(target)
                }
            }
        }
    }

    companion object {
        private val TAG: String = PlanFragment::class.java.simpleName
    }
}

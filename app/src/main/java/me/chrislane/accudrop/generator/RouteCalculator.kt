package me.chrislane.accudrop.generator

import android.content.SharedPreferences
import android.content.res.Resources
import android.location.Location
import android.util.Log
import android.util.Pair
import android.util.TypedValue
import com.google.android.gms.maps.model.LatLng
import me.chrislane.accudrop.BuildConfig
import me.chrislane.accudrop.R
import me.chrislane.accudrop.util.DistanceAndSpeedUtil
import me.chrislane.accudrop.viewmodel.GnssViewModel

// TODO: Remove prefs and resources to pass in only basic data
class RouteCalculator(sharedPreferences: SharedPreferences, resources: Resources,
                      wind: Pair<Double, Double>, private var target: LatLng) {
    private val route = mutableListOf<Location>()
    private var airspeed: Double = 0.toDouble() // Metres per second
    private var descentRate: Double = 0.toDouble() // Metres per second
    private val windDirection: Double = wind.second
    private val windSpeed: Double = wind.first
    private var p3Altitude: Double = 0.toDouble()
    private var p2Altitude: Double = 0.toDouble()
    private var p1Altitude: Double = 0.toDouble()
    private lateinit var p3: Location
    private lateinit var p2: Location
    private lateinit var p1: Location

    init {
        setFromPreferences(sharedPreferences, resources)
    }

    private fun setFromPreferences(sharedPreferences: SharedPreferences, resources: Resources) {
        var glideRatio: Double
        try {
            // Unfortunately EditTextPreferences don't save in number format
            p1Altitude = Integer
                    .valueOf(
                            sharedPreferences.getString(
                                    "landing_pattern_downwind_altitude",
                                    resources.getInteger(R.integer.pref_default_downwind_altitude).toString())!!)
                    .toDouble()
            p2Altitude = Integer.valueOf(
                    sharedPreferences.getString(
                            "landing_pattern_crosswind_altitude",
                            resources.getInteger(R.integer.pref_default_crosswind_altitude).toString())!!)
                    .toDouble()
            p3Altitude = Integer.valueOf(
                    sharedPreferences.getString(
                            "landing_pattern_upwind_altitude",
                            resources.getInteger(R.integer.pref_default_upwind_altitude).toString())!!)
                    .toDouble()

            val typedValue = TypedValue()
            resources.getValue(R.integer.pref_default_airspeed, typedValue, false)
            airspeed = java.lang.Float.valueOf(sharedPreferences.getString("canopy_airspeed",
                    typedValue.float.toString())!!).toDouble()

            resources.getValue(R.integer.pref_default_glide_ratio, typedValue, false)
            glideRatio = java.lang.Float.valueOf(sharedPreferences.getString("canopy_glide_ratio",
                    typedValue.float.toString())!!).toDouble()
        } catch (e: NumberFormatException) {
            // The user entered something silly as a preference value
            Log.e(TAG, "Invalid number in preferences", e)
            // Set to some sane defaults
            p1Altitude = 300.0
            p2Altitude = 180.0
            p3Altitude = 90.0
            airspeed = 15.0
            glideRatio = 2.5
        }

        descentRate = getSinkSpeed(airspeed, glideRatio)
    }

    /**
     * Calculate the route.
     *
     * @return The route calculated.
     */
    fun calcRoute(): MutableList<Location> {
        route.clear()

        calcP3()
        calcP2()
        calcP1()

        route.add(p1)
        route.add(p2)
        route.add(p3)

        val ground = Location("")
        ground.latitude = target.latitude
        ground.longitude = target.longitude
        ground.altitude = 0.0
        route.add(ground)

        if (BuildConfig.DEBUG) {
            Log.v(TAG, "Route calculated: $route")
        }

        return route
    }

    /**
     * Calculate the position of the start location of the route.
     */
    private fun calcP1() {
        val altitudeChange = p1Altitude - p2Altitude
        val distance = distanceFromHeight(airspeed + windSpeed, altitudeChange)

        val p2LatLng = GnssViewModel.getLatLng(p2)
        val loc = getPosAfterMove(p2LatLng, distance, windDirection)
        val location = Location("")
        location.latitude = loc.latitude
        location.longitude = loc.longitude
        location.altitude = p1Altitude
        p1 = location
    }

    /**
     * Calculate the position of the second turn in the route.
     */
    private fun calcP2() {
        // TODO: Add sideways movement on base leg
        val altitudeChange = p2Altitude - p3Altitude
        val distance = distanceFromHeight(airspeed, altitudeChange)

        val p3LatLng = GnssViewModel.getLatLng(p3)
        val loc = getPosAfterMove(p3LatLng, distance, get270Bearing(windDirection))
        val location = Location("")
        location.latitude = loc.latitude
        location.longitude = loc.longitude
        location.altitude = p2Altitude
        p2 = location
    }

    /**
     * Calculate the position of the final turn in the route.
     */
    private fun calcP3() {
        val distance = distanceFromHeight(airspeed - windSpeed, p3Altitude)

        // Subtract distance along upwind direction from coordinates
        val loc = getPosAfterMove(target, distance, getOppositeBearing(windDirection))
        val location = Location("")
        location.latitude = loc.latitude
        location.longitude = loc.longitude
        location.altitude = p3Altitude
        p3 = location
    }

    /**
     * Calculate the distance that a canopy can travel horizontally from an altitude to the ground.
     *
     * @param groundSpeed The ground speed of the canopy in metres per second.
     * @param altitude    The altitude of the canopy in metres.
     * @return The distance in metres that can be travelled.
     */
    private fun distanceFromHeight(groundSpeed: Double, altitude: Double): Double {
        // We know descent rate, calculate time to ground
        val seconds = altitude / descentRate

        // Calculate distance travelled at ground speed after time
        return groundSpeed * seconds
    }

    /**
     * Get the opposite to a bearing.
     *
     * @param bearing The initial bearing.
     * @return The opposite bearing to the input bearing.
     */
    private fun getOppositeBearing(bearing: Double): Double {
        var opposite = bearing - 180
        if (opposite < 0) {
            opposite += 360.0
        }

        return opposite
    }

    /**
     * Get the bearing 90 degrees from the input bearing.
     *
     * @param bearing The initial bearing.
     * @return The bearing 90 degrees from the input.
     */
    fun get90Bearing(bearing: Double): Double {
        var result = bearing + 90
        if (result > 360) {
            result = 360 - result
        }
        return result
    }

    /**
     * Get the bearing 270 degrees from the input bearing.
     *
     * @param bearing The initial bearing.
     * @return The bearing 270 degrees from the input.
     */
    private fun get270Bearing(bearing: Double): Double {
        var result = bearing - 90
        if (result < 0) {
            result += 360
        }
        return result
    }

    private fun getSinkSpeed(airspeed: Double, glideRatio: Double): Double {
        return airspeed / glideRatio
    }

    companion object {

        private val TAG = RouteCalculator::class.java.simpleName

        /**
         *
         * Get coordinates after a move of a certain distance in a bearing from an initial location.
         *
         * Adapted from a [StackOverflow answer](https://stackoverflow.com/a/7835325)
         *
         * @param initialPosition The initial position before a move.
         * @param distance        The distance to be travelled from the initial position.
         * @param bearing         The bearing to travel in from the initial position.
         * @return The coordinates after the move.
         */
        fun getPosAfterMove(initialPosition: LatLng, distance: Double, bearing: Double): LatLng {
            val R = 6378.1 // Radius of Earth
            val b = Math.toRadians(bearing) // Bearing is converted to radians.
            val d = DistanceAndSpeedUtil.metresToKilometres(distance) // Distance in km

            val lat1 = Math.toRadians(initialPosition.latitude) // Initial latitude converted to radians
            val lon1 = Math.toRadians(initialPosition.longitude) // Initial longitude converted to radians

            var lat2 = Math.asin(Math.sin(lat1) * Math.cos(d / R) + Math.cos(lat1) * Math.sin(d / R) * Math.cos(b))

            var lon2 = lon1 + Math.atan2(Math.sin(b) * Math.sin(d / R) * Math.cos(lat1),
                    Math.cos(d / R) - Math.sin(lat1) * Math.sin(lat2))

            lat2 = Math.toDegrees(lat2)
            lon2 = Math.toDegrees(lon2)

            return LatLng(lat2, lon2)
        }
    }
}

package me.chrislane.accudrop.task

import android.content.SharedPreferences
import android.content.res.Resources
import android.location.Location
import android.os.AsyncTask
import android.util.Pair

import com.google.android.gms.maps.model.LatLng

import me.chrislane.accudrop.generator.RouteCalculator

/**
 * Calculate a route for a given target argument.
 */
class RouteTask(private val listener: (MutableList<Location>) -> Any, private val sharedPreferences: SharedPreferences,
                private val resources: Resources, private val windTuple: Pair<Double, Double>) : AsyncTask<LatLng, Void, MutableList<Location>>() {

    override fun doInBackground(vararg Latlngs: LatLng): MutableList<Location> {
        val target = Latlngs[0]
        val routeCalculator = RouteCalculator(sharedPreferences, resources,
                windTuple, target)
        return routeCalculator.calcRoute()
    }

    override fun onPostExecute(route: MutableList<Location>) {
        super.onPostExecute(route)
        listener(route)
    }

    interface RouteTaskListener {
        fun onFinished(route: MutableList<Location>)
    }
}

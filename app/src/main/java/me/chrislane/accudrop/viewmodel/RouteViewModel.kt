package me.chrislane.accudrop.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import android.location.Location

import com.google.android.gms.maps.model.LatLng

class RouteViewModel : ViewModel() {

    private val route = MutableLiveData<MutableList<Location>>()
    private val minAltitude = MutableLiveData<Int>()
    private val maxAltitude = MutableLiveData<Int>()
    private val target = MutableLiveData<LatLng>()

    /**
     * Get the recommended route.
     *
     * @return A `LiveData` object containing the route.
     */
    fun getRoute(): LiveData<MutableList<Location>> {
        return route
    }

    /**
     * Set the route.
     *
     * @param route The route to be set.
     */
    fun setRoute(route: MutableList<Location>) {
        this.route.value = route
    }

    fun getMinAltitude(): LiveData<Int> {
        return minAltitude
    }

    fun getMaxAltitude(): LiveData<Int> {
        return maxAltitude
    }

    fun setMinAltitude(minAltitude: Int) {
        this.minAltitude.value = minAltitude
    }

    fun setMaxAltitude(maxAltitude: Int) {
        this.maxAltitude.value = maxAltitude
    }

    fun setTarget(target: LatLng) {
        this.target.value = target
    }

    fun getTarget(): LiveData<LatLng> {
        return target
    }
}
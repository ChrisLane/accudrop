package me.chrislane.accudrop.viewmodel

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel

class WindViewModel : ViewModel() {

    private val windDirection = MutableLiveData<Double>()
    private val windSpeed = MutableLiveData<Double>()

    /**
     * Get the wind speed.
     *
     * @return The wind speed.
     */
    fun getWindSpeed(): LiveData<Double> {
        return windSpeed
    }

    /**
     * Set the wind speed.
     *
     * @param windSpeed The wind speed to set.
     */
    fun setWindSpeed(windSpeed: Double) {
        this.windSpeed.value = windSpeed
    }

    /**
     * Get the wind direction.
     *
     * @return The wind direction.
     */
    fun getWindDirection(): LiveData<Double> {
        return windDirection
    }

    /**
     * Set the wind direction.
     *
     * @param windDirection The bearing in degrees of the wind direction.
     */
    fun setWindDirection(windDirection: Double) {
        this.windDirection.value = windDirection
    }
}

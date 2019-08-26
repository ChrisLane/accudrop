package me.chrislane.accudrop.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import android.hardware.SensorManager
import android.util.Log

import me.chrislane.accudrop.BuildConfig
import me.chrislane.accudrop.listener.PressureListener

class PressureViewModel(application: Application) : AndroidViewModel(application) {
    /**
     * Get the pressure listener.
     *
     * @return The pressure listener.
     */
    val pressureListener: PressureListener = PressureListener(this)
    private val lastPressure = MutableLiveData<Float>()
    private val groundPressure = MutableLiveData<Float>()
    private val lastAltitude = MutableLiveData<Float>()

    /**
     * Find the ground pressure.
     *
     * @return A `LiveData` object containing the ground pressure.
     */
    fun getGroundPressure(): LiveData<Float> {
        return groundPressure
    }

    /**
     * Set the ground pressure.
     *
     * @param groundPressure The ground pressure value to set.
     */
    fun setGroundPressure(groundPressure: Float) {
        this.groundPressure.value = groundPressure
    }

    /**
     * Find the last pressure.
     *
     * @return A `LiveData` object containing the last pressure.
     */
    fun getLastPressure(): LiveData<Float> {
        return lastPressure
    }

    /**
     * Set the last pressure.
     *
     * @param lastPressure The pressure value to set.
     */
    fun setLastPressure(lastPressure: Float) {
        this.lastPressure.value = lastPressure
        updateAltitude()
    }

    /**
     * Find the last altitude in metres.
     *
     * @return A `LiveData` object containing the last altitude.
     */
    fun getLastAltitude(): LiveData<Float> {
        return lastAltitude
    }

    /**
     * Set the last altitude, measured in metres.
     *
     * @param lastAltitude The altitude in metres to set.
     */
    private fun setLastAltitude(lastAltitude: Float) {
        this.lastAltitude.value = lastAltitude

        if (BuildConfig.DEBUG) {
            Log.v(TAG, "Altitude set: $lastAltitude")
        }
    }

    /**
     * Set the ground pressure to the current pressure reading.
     */
    fun setGroundPressure() {
        if (lastPressure.value != null) {
            setGroundPressure(lastPressure.value!!)
        }
    }

    /**
     * Update the altitude from ground pressure and current pressure.
     */
    private fun updateAltitude() {
        val ground = groundPressure.value
        val last = lastPressure.value
        val altitude: Float

        altitude = if (ground == null && last != null) {
            SensorManager.getAltitude(SensorManager.PRESSURE_STANDARD_ATMOSPHERE, last)
        } else if (ground != null && last != null) {
            SensorManager.getAltitude(ground, last)
        } else {
            return
        }
        setLastAltitude(altitude)
    }

    companion object {

        private val TAG = PressureViewModel::class.java.simpleName
    }
}
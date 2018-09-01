package me.chrislane.accudrop.listener

import android.app.Application
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log

import me.chrislane.accudrop.viewmodel.PressureViewModel

class PressureListener(private val pressureViewModel: PressureViewModel) : SensorEventListener {
    private val sensorManager: SensorManager?
    private var barometer: Sensor? = null

    init {

        sensorManager = pressureViewModel.getApplication<Application>()
                .getSystemService(Context.SENSOR_SERVICE) as SensorManager?
        if (sensorManager != null) {
            barometer = sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE)
        }
    }

    /**
     * Start listening to pressure sensor events.
     */
    fun startListening() {
        Log.i(TAG, "Listening on pressure.")
        sensorManager!!.registerListener(this, barometer, ONE_SECOND_DELAY)
    }

    /**
     * Stop listening to pressure sensor events.
     */
    fun stopListening() {
        Log.i(TAG, "Stopped listening on pressure.")
        sensorManager!!.unregisterListener(this)
    }

    /**
     * Set pressure values in the `PressureViewModel`.
     */
    override fun onSensorChanged(sensorEvent: SensorEvent) {
        pressureViewModel.setLastPressure(sensorEvent.values[0])
    }

    override fun onAccuracyChanged(sensor: Sensor, i: Int) {

    }

    companion object {

        private val TAG = PressureListener::class.java!!.getSimpleName()
        private val ONE_SECOND_DELAY = 1000000
    }
}

package me.chrislane.accudrop.listener;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

import me.chrislane.accudrop.viewmodel.PressureViewModel;

public class PressureListener implements SensorEventListener {

    private static final String TAG = PressureListener.class.getSimpleName();
    private static final int ONE_SECOND_DELAY = 1000000;
    private final PressureViewModel pressureViewModel;
    private final SensorManager sensorManager;
    private Sensor barometer = null;

    public PressureListener(PressureViewModel pressureViewModel) {
        this.pressureViewModel = pressureViewModel;

        sensorManager = (SensorManager) pressureViewModel.getApplication()
                .getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager != null) {
            barometer = sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE);
        }
    }

    /**
     * Start listening to pressure sensor events.
     */
    public void startListening() {
        Log.i(TAG, "Listening on pressure.");
        sensorManager.registerListener(this, barometer, ONE_SECOND_DELAY);
    }

    /**
     * Stop listening to pressure sensor events.
     */
    public void stopListening() {
        Log.i(TAG, "Stopped listening on pressure.");
        sensorManager.unregisterListener(this);
    }

    /**
     * Set pressure values in the <code>PressureViewModel</code>.
     */
    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        pressureViewModel.setLastPressure(sensorEvent.values[0]);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }
}

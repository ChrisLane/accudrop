package me.chrislane.altimeter.viewmodel;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

public class PressureViewModel extends ViewModel implements SensorEventListener {
    private SensorManager sensorManager;
    private Sensor barometer;
    private static final String TAG = "pressure_view_model";
    private static final int ONE_SECOND_DELAY = 1000000;
    private MutableLiveData<Float> lastPressure = new MutableLiveData<>();
    private MutableLiveData<Float> groundPressure = new MutableLiveData<>();
    private MutableLiveData<Float> lastAltitude = new MutableLiveData<>();

    public void initialise(SensorManager sensorManager) {
        this.sensorManager = sensorManager;
        barometer = sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE);
    }

    public void startListening() {
        Log.d(TAG, "Listening on pressure.");
        sensorManager.registerListener(this, barometer, ONE_SECOND_DELAY);
    }

    public void stopListening() {
        Log.d(TAG, "Stopped listening on pressure.");
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        Log.d(TAG, "Sensor changed.");
        lastPressure.setValue(event.values[0]);
        updateAltitude();
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    public void setGroundPressure(float groundPressure) {
        this.groundPressure.setValue(groundPressure);
    }

    public void setGroundPressure() {
        if (lastPressure.getValue() != null) {
            groundPressure.setValue(lastPressure.getValue());
        }
    }

    public LiveData<Float> getGroundPressure() {
        return groundPressure;
    }

    public LiveData<Float> getLastPressure() {
        return lastPressure;
    }

    public MutableLiveData<Float> getLastAltitude() {
        return lastAltitude;
    }

    private void updateAltitude() {
        Float ground = groundPressure.getValue();
        Float last = lastPressure.getValue();
        float altitude;

        if (ground == null && last != null) {
            altitude = SensorManager.getAltitude(SensorManager.PRESSURE_STANDARD_ATMOSPHERE, last);
        } else if (ground != null && last != null) {
            altitude = SensorManager.getAltitude(ground, last);
        } else {
            return;
        }
        lastAltitude.setValue(altitude);
    }
}

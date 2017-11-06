package me.chrislane.altimeter.viewmodel;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.annotation.NonNull;
import android.util.Log;

public class PressureViewModel extends AndroidViewModel implements SensorEventListener {
    private static final String TAG = "pressure_view_model";
    private static final int ONE_SECOND_DELAY = 1000000;
    private SensorManager sensorManager;
    private Sensor barometer;
    private MutableLiveData<Float> lastPressure = new MutableLiveData<>();
    private MutableLiveData<Float> groundPressure = new MutableLiveData<>();
    private MutableLiveData<Float> lastAltitude = new MutableLiveData<>();

    public PressureViewModel(@NonNull Application application) {
        super(application);

        sensorManager = (SensorManager) application.getSystemService(Context.SENSOR_SERVICE);
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
        Log.v(TAG, "Sensor changed.");
        lastPressure.setValue(event.values[0]);
        updateAltitude();
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    public LiveData<Float> getGroundPressure() {
        return groundPressure;
    }

    public void setGroundPressure(float groundPressure) {
        this.groundPressure.setValue(groundPressure);
    }

    public LiveData<Float> getLastPressure() {
        return lastPressure;
    }

    public LiveData<Float> getLastAltitude() {
        return lastAltitude;
    }

    public void setLastAltitude(float lastAltitude) {
        this.lastAltitude.setValue(lastAltitude);
    }

    public void setGroundPressure() {
        if (lastPressure.getValue() != null) {
            setGroundPressure(lastPressure.getValue());
        }
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
        setLastAltitude(altitude);
    }
}

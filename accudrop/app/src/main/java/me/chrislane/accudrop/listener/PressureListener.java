package me.chrislane.accudrop.listener;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;

import me.chrislane.accudrop.viewmodel.PressureViewModel;

public class PressureListener implements SensorEventListener {
    private PressureViewModel pressureViewModel;

    public PressureListener(PressureViewModel pressureViewModel) {
        this.pressureViewModel = pressureViewModel;
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        pressureViewModel.setLastPressure(sensorEvent.values[0]);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }
}

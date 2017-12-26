package me.chrislane.accudrop.viewmodel;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.hardware.SensorManager;
import android.support.annotation.NonNull;

import me.chrislane.accudrop.listener.PressureListener;

public class PressureViewModel extends AndroidViewModel {
    private static final String TAG = PressureViewModel.class.getSimpleName();
    private final PressureListener pressureListener;
    private final MutableLiveData<Float> lastPressure = new MutableLiveData<>();
    private final MutableLiveData<Float> groundPressure = new MutableLiveData<>();
    private final MutableLiveData<Float> lastAltitude = new MutableLiveData<>();

    public PressureViewModel(@NonNull Application application) {
        super(application);

        pressureListener = new PressureListener(application, this);

    }

    public PressureListener getPressureListener() {
        return pressureListener;
    }

    public LiveData<Float> getGroundPressure() {
        return groundPressure;
    }

    private void setGroundPressure(float groundPressure) {
        this.groundPressure.setValue(groundPressure);
    }

    public LiveData<Float> getLastPressure() {
        return lastPressure;
    }

    public void setLastPressure(float lastPressure) {
        this.lastPressure.setValue(lastPressure);
        updateAltitude();
    }

    public LiveData<Float> getLastAltitude() {
        return lastAltitude;
    }

    private void setLastAltitude(float lastAltitude) {
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
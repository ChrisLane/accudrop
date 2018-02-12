package me.chrislane.accudrop.viewmodel;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.hardware.SensorManager;
import android.support.annotation.NonNull;
import android.util.Log;

import me.chrislane.accudrop.listener.PressureListener;

public class PressureViewModel extends AndroidViewModel {

    private static final String TAG = PressureViewModel.class.getSimpleName();
    private final PressureListener pressureListener;
    private final MutableLiveData<Float> lastPressure = new MutableLiveData<>();
    private final MutableLiveData<Float> groundPressure = new MutableLiveData<>();
    private final MutableLiveData<Float> lastAltitude = new MutableLiveData<>();

    public PressureViewModel(@NonNull Application application) {
        super(application);

        pressureListener = new PressureListener(this);
    }

    /**
     * Get the pressure listener.
     *
     * @return The pressure listener.
     */
    public PressureListener getPressureListener() {
        return pressureListener;
    }

    /**
     * Find the ground pressure.
     *
     * @return A <code>LiveData</code> object containing the ground pressure.
     */
    public LiveData<Float> getGroundPressure() {
        return groundPressure;
    }

    /**
     * Set the ground pressure.
     *
     * @param groundPressure The ground pressure value to set.
     */
    public void setGroundPressure(float groundPressure) {
        this.groundPressure.setValue(groundPressure);
    }

    /**
     * Find the last pressure.
     *
     * @return A <code>LiveData</code> object containing the last pressure.
     */
    public LiveData<Float> getLastPressure() {
        return lastPressure;
    }

    /**
     * Set the last pressure.
     *
     * @param lastPressure The pressure value to set.
     */
    public void setLastPressure(float lastPressure) {
        this.lastPressure.setValue(lastPressure);
        updateAltitude();
    }

    /**
     * Find the last altitude in metres.
     *
     * @return A <code>LiveData</code> object containing the last altitude.
     */
    public LiveData<Float> getLastAltitude() {
        return lastAltitude;
    }

    /**
     * Set the last altitude, measured in metres.
     *
     * @param lastAltitude The altitude in metres to set.
     */
    private void setLastAltitude(float lastAltitude) {
        this.lastAltitude.setValue(lastAltitude);
        Log.d(TAG, "Altitude set: " + lastAltitude);
    }

    /**
     * Set the ground pressure to the current pressure reading.
     */
    public void setGroundPressure() {
        if (lastPressure.getValue() != null) {
            setGroundPressure(lastPressure.getValue());
        }
    }

    /**
     * Update the altitude from ground pressure and current pressure.
     */
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
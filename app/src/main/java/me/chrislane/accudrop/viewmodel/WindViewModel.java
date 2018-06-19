package me.chrislane.accudrop.viewmodel;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;

public class WindViewModel extends ViewModel {

    private final MutableLiveData<Double> windDirection = new MutableLiveData<>();
    private final MutableLiveData<Double> windSpeed = new MutableLiveData<>();

    /**
     * Get the wind speed.
     *
     * @return The wind speed.
     */
    public LiveData<Double> getWindSpeed() {
        return windSpeed;
    }

    /**
     * Set the wind speed.
     *
     * @param windSpeed The wind speed to set.
     */
    public void setWindSpeed(double windSpeed) {
        this.windSpeed.setValue(windSpeed);
    }

    /**
     * Get the wind direction.
     *
     * @return The wind direction.
     */
    public LiveData<Double> getWindDirection() {
        return windDirection;
    }

    /**
     * Set the wind direction.
     *
     * @param windDirection The bearing in degrees of the wind direction.
     */
    public void setWindDirection(double windDirection) {
        this.windDirection.setValue(windDirection);
    }
}

package me.chrislane.accudrop.viewmodel;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;

public class WindViewModel extends ViewModel {
    private MutableLiveData<Double> windDirection = new MutableLiveData<>();
    private MutableLiveData<Double> windSpeed = new MutableLiveData<>();

    public LiveData<Double> getWindSpeed() {
        return windSpeed;
    }

    public void setWindSpeed(double windSpeed) {
        this.windSpeed.setValue(windSpeed);
    }

    public LiveData<Double> getWindDirection() {
        return windDirection;
    }

    public void setWindDirection(double windDirection) {
        this.windDirection.setValue(windDirection);
    }
}

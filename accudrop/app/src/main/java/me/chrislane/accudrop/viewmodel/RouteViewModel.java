package me.chrislane.accudrop.viewmodel;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.location.Location;

import java.util.List;

public class RouteViewModel extends ViewModel {

    private final MutableLiveData<List<Location>> route = new MutableLiveData<>();
    private final MutableLiveData<Integer> minAltitude = new MutableLiveData<>();
    private final MutableLiveData<Integer> maxAltitude =  new MutableLiveData<>();

    /**
     * Get the recommended route.
     *
     * @return A <code>LiveData</code> object containing the route.
     */
    public LiveData<List<Location>> getRoute() {
        return route;
    }

    /**
     * Set the route.
     *
     * @param route The route to be set.
     */
    public void setRoute(List<Location> route) {
        this.route.setValue(route);
    }

    /**
     * Set a point in the route.
     *
     * @param index The index position in the route to set.
     * @param point The point to set in the route.
     */
    public void setPoint(int index, Location point) {
        List<Location> route = this.route.getValue();
        if (route != null) {
            route.set(index, point);
        }
    }

    public LiveData<Integer> getMinAltitude() {
        return minAltitude;
    }

    public LiveData<Integer> getMaxAltitude() {
        return maxAltitude;
    }

    public void setMinAltitude(int minAltitude) {
        this.minAltitude.setValue(minAltitude);
    }

    public void setMaxAltitude(int maxAltitude) {
        this.maxAltitude.setValue(maxAltitude);
    }
}
package me.chrislane.accudrop.viewmodel;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;

import java.util.List;

import me.chrislane.accudrop.Point3D;

public class RouteViewModel extends ViewModel {

    private final MutableLiveData<List<Point3D>> route = new MutableLiveData<>();

    /**
     * Get the recommended route.
     *
     * @return A <code>LiveData</code> object containing the route.
     */
    public LiveData<List<Point3D>> getRoute() {
        return route;
    }

    /**
     * Set the route.
     *
     * @param route The route to be set.
     */
    public void setRoute(List<Point3D> route) {
        this.route.setValue(route);
    }

    /**
     * Set a point in the route.
     *
     * @param index The index position in the route to set.
     * @param point The point to set in the route.
     */
    public void setPoint(int index, Point3D point) {
        List<Point3D> route = this.route.getValue();
        if (route != null) {
            route.set(index, point);
        }
    }
}
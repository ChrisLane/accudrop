package me.chrislane.accudrop.viewmodel;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;

import java.util.List;

import me.chrislane.accudrop.Point3D;

public class RouteViewModel extends ViewModel {
    private MutableLiveData<List<Point3D>> route = new MutableLiveData<>();

    public LiveData<List<Point3D>> getRoute() {
        return route;
    }

    public void setRoute(List<Point3D> route) {
        this.route.setValue(route);
    }

    public void setPoint(int index, Point3D point) {
        List<Point3D> route = this.route.getValue();
        if (route != null) {
            route.set(index, point);
        }
    }
}
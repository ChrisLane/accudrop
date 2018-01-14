package me.chrislane.accudrop.presenter;

import com.google.android.gms.maps.model.LatLng;

import me.chrislane.accudrop.RouteCalculator;
import me.chrislane.accudrop.viewmodel.RouteViewModel;

public class RoutePlanPresenter {
    RouteCalculator routeCalculator;
    RouteViewModel routeViewModel;

    public RoutePlanPresenter(RouteViewModel routeViewModel, LatLng target) {
        this.routeViewModel = routeViewModel;
        routeCalculator = new RouteCalculator(target);
    }

    /**
     * Calculate the route and update the model with the result.
     */
    public void calcRoute() {
        routeViewModel.setRoute(routeCalculator.calcRoute());
    }

    /**
     * Set the route target.
     *
     * @param latLng The target location.
     */
    public void setTarget(LatLng latLng) {
        // Update the target location
        routeCalculator.setTarget(latLng);
        // Recalculate the route
        calcRoute();
    }
}

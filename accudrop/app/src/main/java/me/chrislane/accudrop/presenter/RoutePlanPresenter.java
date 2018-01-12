package me.chrislane.accudrop.presenter;

import me.chrislane.accudrop.RouteCalculator;
import me.chrislane.accudrop.viewmodel.RouteViewModel;

public class RoutePlanPresenter {
    RouteCalculator routeCalculator = new RouteCalculator();
    RouteViewModel routeViewModel;

    public RoutePlanPresenter(RouteViewModel routeViewModel) {
        this.routeViewModel = routeViewModel;
    }

    public void calcRoute() {
        routeViewModel.setRoute(routeCalculator.calcRoute());
    }
}

package me.chrislane.accudrop.presenter;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.location.Location;
import android.support.v4.app.Fragment;

import java.util.List;

import me.chrislane.accudrop.fragment.ReplayMapFragment;
import me.chrislane.accudrop.viewmodel.RouteViewModel;

public class ReplayMapPresenter {

    private final ReplayMapFragment replayMapFragment;
    private final Fragment parentFragment;
    private RouteViewModel routeViewModel;

    public ReplayMapPresenter(ReplayMapFragment replayMapFragment) {
        this.replayMapFragment = replayMapFragment;

        parentFragment = replayMapFragment.getParentFragment();
        if (parentFragment != null) {
            routeViewModel = ViewModelProviders.of(parentFragment).get(RouteViewModel.class);
        }
    }

    /**
     * Add all observers.
     */
    public void addObservers() {
        subscribeToRoute();
    }

    /**
     * Set the new route being displayed.
     */
    private void subscribeToRoute() {
        final Observer<List<Location>> routeObserver = replayMapFragment::updateMapRoute;
        routeViewModel.getRoute().observe(parentFragment, routeObserver);
    }
}

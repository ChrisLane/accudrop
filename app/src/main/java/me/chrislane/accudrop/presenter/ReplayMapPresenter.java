package me.chrislane.accudrop.presenter;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.location.Location;
import android.support.v4.app.Fragment;
import android.support.v4.util.Pair;

import java.util.List;
import java.util.UUID;

import me.chrislane.accudrop.fragment.ReplayMapFragment;
import me.chrislane.accudrop.viewmodel.ReplayViewModel;

public class ReplayMapPresenter {

    private final ReplayMapFragment replayMapFragment;
    private final Fragment parentFragment;
    private ReplayViewModel replayViewModel;

    public ReplayMapPresenter(ReplayMapFragment replayMapFragment) {
        this.replayMapFragment = replayMapFragment;

        parentFragment = replayMapFragment.getParentFragment();
        if (parentFragment != null) {
            replayViewModel = ViewModelProviders.of(parentFragment).get(ReplayViewModel.class);
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
        final Observer<List<Pair<UUID, List<Location>>>> routeObserver = replayMapFragment::updateMapRoutes;
        replayViewModel.getUsersAndLocs().observe(parentFragment, routeObserver);
    }
}

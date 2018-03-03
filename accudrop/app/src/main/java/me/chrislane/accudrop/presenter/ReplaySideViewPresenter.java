package me.chrislane.accudrop.presenter;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.graphics.Point;
import android.location.Location;

import com.google.android.gms.maps.GoogleMap;

import java.util.ArrayList;
import java.util.List;

import me.chrislane.accudrop.fragment.ReplayFragment;
import me.chrislane.accudrop.fragment.ReplaySideViewFragment;
import me.chrislane.accudrop.task.MinMaxAltiTask;
import me.chrislane.accudrop.task.ProduceSideViewTask;
import me.chrislane.accudrop.viewmodel.GnssViewModel;
import me.chrislane.accudrop.viewmodel.JumpViewModel;
import me.chrislane.accudrop.viewmodel.RouteViewModel;

public class ReplaySideViewPresenter {

    private static final String TAG = ReplaySideViewPresenter.class.getSimpleName();
    private final ReplaySideViewFragment fragment;
    private final ReplayFragment parentFragment;
    private RouteViewModel routeViewModel;
    private JumpViewModel jumpViewModel;

    public ReplaySideViewPresenter(ReplaySideViewFragment fragment) {
        this.fragment = fragment;

        parentFragment = (ReplayFragment) fragment.getParentFragment();
        if (parentFragment != null) {
            jumpViewModel = ViewModelProviders.of(parentFragment).get(JumpViewModel.class);
            routeViewModel = ViewModelProviders.of(parentFragment).get(RouteViewModel.class);

            subscribeToRoute();
        }
    }

    /**
     * Get and set the latest jump minimum/maximum altitude and position data.
     */
    private void findJumpData() {
        MinMaxAltiTask.Listener altitudeListener = (min, max) -> {
            setMinMaxAltitude(min, max);
            fragment.updateDrawable(true);
        };
        new MinMaxAltiTask(altitudeListener, jumpViewModel).execute();
    }

    /**
     * Set minimum and maximum altitude values.
     *
     * @param min The minimum altitude.
     * @param max The maximum altitude.
     */
    private void setMinMaxAltitude(int min, int max) {
        routeViewModel.setMinAltitude(min);
        routeViewModel.setMaxAltitude(max);
    }

    /**
     * Update data for a new route.
     */
    private void subscribeToRoute() {
        final Observer<List<Location>> routeObserver = route -> findJumpData();
        routeViewModel.getRoute().observe(parentFragment, routeObserver);
    }

    private List<Point> getMapPoints() {
        GoogleMap map = parentFragment.getReplayMap().getMap();
        List<Point> mapPoints = new ArrayList<>();

        List<Location> route = routeViewModel.getRoute().getValue();
        if (route != null) {
            for (Location location : route) {
                mapPoints.add(map.getProjection().toScreenLocation(GnssViewModel.getLatLng(location)));
            }
        }

        return mapPoints;
    }

    public void produceViewPositions(int width, int height, int margin) {
        List<Point> mapPoints = getMapPoints();
        ProduceSideViewTask.Listener listener = fragment::setScreenPoints;
        new ProduceSideViewTask(width, height, margin, routeViewModel, listener).execute(mapPoints);
    }
}

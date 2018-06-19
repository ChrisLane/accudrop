package me.chrislane.accudrop.presenter;

import android.arch.lifecycle.ViewModelProviders;
import android.graphics.Point;
import android.location.Location;
import android.os.AsyncTask;
import android.support.v4.util.Pair;

import com.google.android.gms.maps.GoogleMap;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import me.chrislane.accudrop.fragment.ReplayFragment;
import me.chrislane.accudrop.fragment.ReplaySideViewFragment;
import me.chrislane.accudrop.task.ProduceSideViewTask;
import me.chrislane.accudrop.viewmodel.GnssViewModel;
import me.chrislane.accudrop.viewmodel.ReplayViewModel;

public class ReplaySideViewPresenter {

    private static final String TAG = ReplaySideViewPresenter.class.getSimpleName();
    private final ReplaySideViewFragment fragment;
    private final ReplayFragment parentFragment;
    private ReplayViewModel replayViewModel;

    public ReplaySideViewPresenter(ReplaySideViewFragment fragment) {
        this.fragment = fragment;

        parentFragment = (ReplayFragment) fragment.getParentFragment();
        if (parentFragment != null) {
            replayViewModel = ViewModelProviders.of(parentFragment).get(ReplayViewModel.class);
        }
    }

    private List<List<Point>> getMapPoints() {
        GoogleMap map = parentFragment.getReplayMap().getMap();
        List<List<Point>> mapPoints = new ArrayList<>();

        List<Pair<UUID, List<Location>>> usersAndLocs = replayViewModel.getUsersAndLocs().getValue();

        if (usersAndLocs != null) {
            for (Pair<UUID, List<Location>> userAndLocs : usersAndLocs) {
                List<Location> locations = userAndLocs.second;
                if (locations != null) {
                    List<Point> points = new ArrayList<>();
                    for (Location location : locations) {
                        points.add(map.getProjection()
                                .toScreenLocation(GnssViewModel.getLatLng(location)));
                    }
                    mapPoints.add(points);
                }
            }
        }

        return mapPoints;
    }

    public void produceViewPositions(int width, int height, int margin) {
        List<List<Point>> mapPointsList = getMapPoints();
        ProduceSideViewTask.Listener listener = fragment::setScreenPointsList;
        new ProduceSideViewTask(width, height, margin, mapPointsList, replayViewModel, listener)
                .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }
}

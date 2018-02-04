package me.chrislane.accudrop.presenter;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.graphics.Point;
import android.graphics.PointF;
import android.util.Log;

import com.google.android.gms.maps.GoogleMap;

import java.util.ArrayList;
import java.util.List;

import me.chrislane.accudrop.Point3D;
import me.chrislane.accudrop.fragment.ReplayFragment;
import me.chrislane.accudrop.fragment.ReplaySideViewFragment;
import me.chrislane.accudrop.task.FetchJumpTask;
import me.chrislane.accudrop.task.MinMaxAltiTask;
import me.chrislane.accudrop.viewmodel.JumpViewModel;
import me.chrislane.accudrop.viewmodel.RouteViewModel;

public class ReplaySideViewPresenter {

    private static final String TAG = ReplaySideViewFragment.class.getSimpleName();
    private final ReplaySideViewFragment fragment;
    private final ReplayFragment parentFragment;
    private RouteViewModel routeViewModel;
    private JumpViewModel jumpViewModel;
    private List<Point3D> jump;
    private int minAltitude, maxAltitude;
    private int tasksRunning;
    private List<Point3D> route;

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
     * Returns a value scaled to be between 0 and 100 for an input value and an input's minimum
     * and maximum possible value.
     *
     * @param input      The value to be scaled.
     * @param min        An input's minimum possible value.
     * @param max        An input's maximum possible value.
     * @param allowedMin The output's minimum possible value.
     * @param allowedMax The output's maximum possible value.
     * @return The input value scaled between allowedMin and allowedMax.
     */
    private double getScaledValue(double input, double min, double max, double allowedMin, double allowedMax) {
        return ((allowedMax - allowedMin) * (input - min) / (max - min)) + allowedMin;
    }

    /**
     * Get and set the latest jump minimum/maximum altitude and position data.
     */
    private void findJumpData() {
        tasksRunning++;
        MinMaxAltiTask.Listener altitudeListener = (min, max) -> {
            setMinMaxAltitude(min, max);
            taskFinished();
        };
        new MinMaxAltiTask(altitudeListener, jumpViewModel).execute();

        tasksRunning++;
        FetchJumpTask.FetchJumpListener jumpListener = result -> {
            setJump(result);
            taskFinished();
        };
        new FetchJumpTask(jumpListener, jumpViewModel).execute();
    }

    private List<Point> getMapPoints() {
        GoogleMap map = parentFragment.getReplayMap().getMap();
        List<Point> screenPoints = new ArrayList<>();

        for (Point3D point : route) {
            screenPoints.add(map.getProjection().toScreenLocation(point.getLatLng()));
        }

        return screenPoints;
    }

    /**
     * Convert map coordinates into side view coordinates.
     *
     * @param width  The canvas width.
     * @param height The canvas height.
     * @param margin The margin to leave in the canvas.
     * @return The screen coordinates converted for the side view.
     */
    public List<PointF> produceViewPositions(int width, int height, int margin) {
        List<Point> mapPoints = getMapPoints();
        List<PointF> screenPos = new ArrayList<>();

        int min = 0;
        int max = width > height ? height : width;
        int diff = Math.abs(width - height);
        max -= margin;
        min += margin;

        // Set the minimum and maximum x coordinate
        int minX = mapPoints.get(0).x;
        int maxX = minX;
        for (Point point : mapPoints) {
            int x = point.x;

            if (x < minX) {
                minX = x;
            } else if (x > maxX) {
                maxX = x;
            }
        }

        // Generate screen points
        for (int i = 0; i < mapPoints.size(); i++) {
            double x =
                    getScaledValue(mapPoints.get(i).x, minX, maxX, min, max);
            double y =
                    getScaledValue(jump.get(i).getAltitude(), minAltitude, maxAltitude, min, max);
            x += diff / 2;
            screenPos.add(new PointF((float) x, (float) (height - y)));
        }

        Log.d(TAG, "Generated screen positions: " + screenPos);

        return screenPos;
    }

    /**
     * Set the current jump being viewed.
     *
     * @param jump The current jump.
     */
    private void setJump(List<Point3D> jump) {
        this.jump = jump;
    }

    /**
     * Set minimum and maximum altitude values.
     *
     * @param min The minimum altitude.
     * @param max The maximum altitude.
     */
    private void setMinMaxAltitude(int min, int max) {
        minAltitude = min;
        maxAltitude = max;
    }

    /**
     * Reduce the count of tasks running.
     */
    private void taskFinished() {
        Log.d(TAG, "Task finished");
        tasksRunning--;
        if (tasksRunning == 0) {
            fragment.updateDrawable();
        }
    }

    /**
     * Update data for a new route.
     */
    private void subscribeToRoute() {
        final Observer<List<Point3D>> routeObserver = route -> findJumpData();
        routeViewModel.getRoute().observe(parentFragment, routeObserver);
    }

    /**
     * Get the active route.
     *
     * @return The active route.
     */
    public List<Point3D> getRoute() {
        return routeViewModel.getRoute().getValue();
    }
}

package me.chrislane.accudrop.presenter;

import android.arch.lifecycle.ViewModelProviders;
import android.graphics.PointF;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;

import me.chrislane.accudrop.MainActivity;
import me.chrislane.accudrop.Point3D;
import me.chrislane.accudrop.fragment.ReplaySideViewFragment;
import me.chrislane.accudrop.task.FetchJumpTask;
import me.chrislane.accudrop.task.MinMaxAltiTask;
import me.chrislane.accudrop.task.MinMaxLatLngTask;
import me.chrislane.accudrop.viewmodel.JumpViewModel;

public class ReplaySideViewPresenter {
    private static final String TAG = ReplaySideViewFragment.class.getSimpleName();
    private JumpViewModel jumpViewModel;
    private final ReplaySideViewFragment fragment;
    private List<Point3D> jump;
    private int minAltitude, maxAltitude;
    private double minLat, maxLat, minLong, maxLong;
    private int tasksRunning;
    private double bearing;

    public ReplaySideViewPresenter(ReplaySideViewFragment fragment) {
        this.fragment = fragment;

        MainActivity main = (MainActivity) fragment.getActivity();
        if (main != null) {
            jumpViewModel = ViewModelProviders.of(main).get(JumpViewModel.class);
        }

        findJumpData();
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

/*        tasksRunning++;
        MinMaxLatLngTask.Listener longListener = (min, max) -> {
            setMinMaxLong(min, max);
            taskFinished();
        };
        new MinMaxLatLngTask(longListener, jumpViewModel, false).execute();

        tasksRunning++;
        MinMaxLatLngTask.Listener latListener = (min, max) -> {
            setMinMaxLat(min, max);
            taskFinished();
        };
        new MinMaxLatLngTask(latListener, jumpViewModel, true).execute();*/
    }

    public void updateRotation(double bearing) {
        if (tasksRunning == 0) {
            this.bearing = bearing;
            Log.d(TAG, "Bearing = " + bearing);

            fragment.updateDrawable();
        }
    }

    public List<PointF> produceViewPositions(int width, int height, int margin) {
        List<PointF> screenPos = new ArrayList<>();
        int min = 0;
        int max = width > height ? height : width;
        int diff = Math.abs(width - height);
        max -= margin;
        min += margin;

        // Hold coordinates in new object
        List<Point3D> rotated = rotatePointsAboutY(jump, bearing);
        minLong = rotated.get(0).getLatLng().longitude;
        maxLong = minLong;
        for (Point3D point : rotated) {
            double lng = point.getLatLng().longitude;

            if (lng < minLong) {
                minLong = lng;
            } else if (lng > maxLong) {
                maxLong = lng;
            }
        }
        for (Point3D point : rotated) {
            double x =
                    getScaledValue(point.getLatLng().longitude, minLong, maxLong, min, max);
            double y =
                    getScaledValue(point.getAltitude(), minAltitude, maxAltitude, min, max);
            x += diff / 2;
            screenPos.add(new PointF((float) x, (float) (height - y)));
        }

        Log.d(TAG, "Generated screen positions: " + screenPos);

        return screenPos;
    }

    private List<Point3D> rotatePointsAboutY(List<Point3D> points, double rotation) {
        // [cosθ     sinθ] [x(longitude)]
        // [-sinθ    cosθ] [y(latitude) ]

        List<Point3D> newPoints = new ArrayList<>();
        for (Point3D point : points) {
            LatLng latLng = point.getLatLng();
            double lat = latLng.latitude;
            double lng = latLng.longitude;

            double radians = Math.toRadians(rotation);

            double newLat = lng * Math.cos(radians) - lat * Math.sin(radians);
            double newLng = lng * Math.sin(radians) + lat * Math.cos(radians);
            Point3D newPoint = new Point3D(new LatLng(newLng, newLat), point.getAltitude());
            newPoints.add(newPoint);
        }

        return newPoints;
    }

    private void setMinMaxLat(double min, double max) {
        minLat = min;
        maxLat = max;
    }

    private void setMinMaxLong(double min, double max) {
        minLong = min;
        maxLong = max;
    }

    private void setJump(List<Point3D> jump) {
        this.jump = jump;
    }

    private void setMinMaxAltitude(int min, int max) {
        minAltitude = min;
        maxAltitude = max;
    }

    private void taskFinished() {
        Log.d(TAG, "Task finished");
        tasksRunning--;
        if (tasksRunning == 0) {
            fragment.updateDrawable();
        }
    }
}

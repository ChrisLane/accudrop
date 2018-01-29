package me.chrislane.accudrop.presenter;

import android.arch.lifecycle.ViewModelProviders;
import android.graphics.PointF;
import android.util.Log;

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
    private int tasksFinished;
    private long direction;

    public ReplaySideViewPresenter(ReplaySideViewFragment fragment) {
        this.fragment = fragment;

        MainActivity main = (MainActivity) fragment.getActivity();
        if (main != null) {
            jumpViewModel = ViewModelProviders.of(main).get(JumpViewModel.class);
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
        return (allowedMax - allowedMin) * (input - min) / (max - min) + allowedMin;
    }

    private void findJumpData(long direction) {
        tasksFinished = 0;
        MinMaxAltiTask.Listener altitudeListener = (min, max) -> {
            setMinMaxAltitude(min, max);
            taskFinished();
        };
        new MinMaxAltiTask(altitudeListener, jumpViewModel).execute();

        FetchJumpTask.FetchJumpListener jumpListener = result -> {
            setJump(result);
            taskFinished();
        };
        new FetchJumpTask(jumpListener, jumpViewModel).execute();

        if (direction == 0 || direction == 180 || direction == 360) {
            // Get min and max longitude for jump
            MinMaxLatLngTask.Listener longListener = (min, max) -> {
                setMinMaxLong(min, max);
                taskFinished();
            };
            new MinMaxLatLngTask(longListener, jumpViewModel, false).execute();
        } else {
            MinMaxLatLngTask.Listener latListener = (min, max) -> {
                setMinMaxLat(min, max);
                taskFinished();
            };
            new MinMaxLatLngTask(latListener, jumpViewModel, true).execute();
        }
    }

    public void updateRotation(double bearing) {

        direction = Math.round(bearing / 90);
        Log.d(TAG, "Direction = " + direction);

        findJumpData(direction);
    }

    public List<PointF> produceViewPositions(int width, int height) {
        List<PointF> screenPos = new ArrayList<>();

        if (direction == 0 || direction == 180 || direction == 360) {
            // Get min and max longitude for jump
            if (direction == 0 || direction == 360) {
                // Do stuff for north
                for (Point3D point : jump) {
                    float x =
                            (float) getScaledValue(point.getLatLng().longitude, minLong, maxLong, 0, width);
                    float y =
                            (float) getScaledValue(point.getAltitude(), minAltitude, maxAltitude, 0, height);
                    screenPos.add(new PointF(x, height - y));
                }
            } else {
                // Do stuff for south

            }
        } else {
            // Get min and max latitude for jump
            if (direction == 90) {
                // Do stuff for east

            } else if (direction == 270) {
                // Do stuff for west

            } else {
                Log.wtf(TAG, "Direction not N,E,S,W");
            }
        }

        Log.d(TAG, "Generated screen positions: " + screenPos);

        return screenPos;
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
        if (tasksFinished >= 2) {
            fragment.updateDrawable();

            tasksFinished = 0;
        } else {
            tasksFinished++;
        }
    }
}

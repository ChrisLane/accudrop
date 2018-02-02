package me.chrislane.accudrop.presenter;

import android.arch.lifecycle.ViewModelProviders;
import android.graphics.Point;
import android.graphics.PointF;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import me.chrislane.accudrop.MainActivity;
import me.chrislane.accudrop.Point3D;
import me.chrislane.accudrop.fragment.ReplaySideViewFragment;
import me.chrislane.accudrop.task.FetchJumpTask;
import me.chrislane.accudrop.task.MinMaxAltiTask;
import me.chrislane.accudrop.viewmodel.JumpViewModel;

public class ReplaySideViewPresenter {
    private static final String TAG = ReplaySideViewFragment.class.getSimpleName();
    private final ReplaySideViewFragment fragment;
    private JumpViewModel jumpViewModel;
    private List<Point3D> jump;
    private int minAltitude, maxAltitude;
    private int tasksRunning;
    private List<Point> mapPoints;

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
    }

    public void updateRotation(List<Point> mapPoints) {
        if (tasksRunning == 0) {
            this.mapPoints = mapPoints;
            Log.d(TAG, "Map points: " + mapPoints);

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
    }
}

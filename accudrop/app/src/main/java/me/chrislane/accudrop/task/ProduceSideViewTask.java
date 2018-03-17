package me.chrislane.accudrop.task;

import android.graphics.Point;
import android.graphics.PointF;
import android.location.Location;
import android.os.AsyncTask;
import android.support.v4.util.Pair;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import me.chrislane.accudrop.Util;
import me.chrislane.accudrop.viewmodel.ReplayViewModel;

public class ProduceSideViewTask extends AsyncTask<Void, Void, List<List<PointF>>> {

    private static final String TAG = ProduceSideViewTask.class.getSimpleName();
    private final int width;
    private final int height;
    private final int margin;
    private final ReplayViewModel model;
    private final Listener listener;
    private final List<List<Point>> mapPointList;

    public ProduceSideViewTask(int width, int height, int margin,
                               List<List<Point>> mapPointList, ReplayViewModel model, Listener listener) {
        this.width = width;
        this.height = height;
        this.margin = margin;
        this.mapPointList = mapPointList;
        this.model = model;
        this.listener = listener;
    }

    /**
     * Convert map coordinates into side view coordinates.
     *
     * @return The screen coordinates converted for the side view.
     */
    public List<List<PointF>> produceViewPositions() {
        List<List<PointF>> screenPosList = new ArrayList<>();

        List<Pair<UUID, List<Location>>> usersAndLocs = model.getUsersAndLocs().getValue();
        if (usersAndLocs == null || usersAndLocs.isEmpty()) {
            Log.e(TAG, "Users and locations list is null or empty.");
            return screenPosList;
        }

        for (int i = 0; i < mapPointList.size() && i < usersAndLocs.size(); i++) {
            List<Point> mapPoints = mapPointList.get(i);
            List<Location> locations = usersAndLocs.get(i).second;
            List<PointF> screenPos = new ArrayList<>();

            // Return an empty list if the route is empty.
            if (mapPoints.isEmpty()) {
                Log.d(TAG, "No points in the route.");
                return new ArrayList<>();
            }

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

            if (locations != null) {
                Double minAltitude = locations.get(0).getAltitude();
                Double maxAltitude = locations.get(locations.size() - 1).getAltitude();

                // Generate screen points
                for (int j = 0; j < mapPoints.size() && j < locations.size(); j++) {
                    double x =
                            Util.getScaledValue(mapPoints.get(j).x, minX, maxX, min, max);
                    double y =
                            Util.getScaledValue(locations.get(j).getAltitude(), minAltitude, maxAltitude, min, max);
                    x += diff / 2f;
                    screenPos.add(new PointF((float) x, (float) (height - y)));
                }

                Log.v(TAG, "Generated screen positions: " + screenPos);
            }

            screenPosList.add(screenPos);

        }

        return screenPosList;
    }

    @Override
    protected List<List<PointF>> doInBackground(Void... voids) {
        return produceViewPositions();
    }

    @Override
    protected void onPostExecute(List<List<PointF>> screenPoints) {
        super.onPostExecute(screenPoints);
        listener.onFinished(screenPoints);
    }

    public interface Listener {
        void onFinished(List<List<PointF>> screenPoints);
    }
}

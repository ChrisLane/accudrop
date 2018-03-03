package me.chrislane.accudrop.task;

import android.graphics.Point;
import android.graphics.PointF;
import android.location.Location;
import android.os.AsyncTask;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import me.chrislane.accudrop.Util;
import me.chrislane.accudrop.viewmodel.RouteViewModel;

public class ProduceSideViewTask extends AsyncTask<List<Point>, Void, List<PointF>> {

    private static final String TAG = ProduceSideViewTask.class.getSimpleName();
    private final int width;
    private final int height;
    private final int margin;
    private final RouteViewModel model;
    private final Listener listener;

    public ProduceSideViewTask(int width, int height, int margin,
                               RouteViewModel model, Listener listener) {
        this.width = width;
        this.height = height;
        this.margin = margin;
        this.model = model;
        this.listener = listener;
    }

    /**
     * Convert map coordinates into side view coordinates.
     *
     * @param mapPoints
     * @return The screen coordinates converted for the side view.
     */
    public List<PointF> produceViewPositions(List<Point> mapPoints) {
        List<PointF> screenPos = new ArrayList<>();

        // Return an empty list if the route is empty.
        if (mapPoints.size() == 0) {
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

        List<Location> route = model.getRoute().getValue();
        Integer minAltitude = model.getMinAltitude().getValue();
        Integer maxAltitude = model.getMaxAltitude().getValue();
        if (route != null && minAltitude != null && maxAltitude != null) {
            // Generate screen points
            for (int i = 0; i < mapPoints.size() && i < route.size(); i++) {

                double x =
                        Util.getScaledValue(mapPoints.get(i).x, minX, maxX, min, max);
                double y =
                        Util.getScaledValue(route.get(i).getAltitude(), minAltitude, maxAltitude, min, max);
                x += diff / 2f;
                screenPos.add(new PointF((float) x, (float) (height - y)));
            }

            Log.v(TAG, "Generated screen positions: " + screenPos);
        }

        return screenPos;
    }

    @Override
    protected List<PointF> doInBackground(List<Point>... pointLists) {
        return produceViewPositions(pointLists[0]);
    }

    @Override
    protected void onPostExecute(List<PointF> screenPoints) {
        super.onPostExecute(screenPoints);
        listener.onFinished(screenPoints);
    }

    public interface Listener {
        void onFinished(List<PointF> screenPoints);
    }
}

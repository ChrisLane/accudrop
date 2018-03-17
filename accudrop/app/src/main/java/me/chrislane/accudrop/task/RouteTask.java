package me.chrislane.accudrop.task;

import android.content.SharedPreferences;
import android.content.res.Resources;
import android.location.Location;
import android.os.AsyncTask;
import android.util.Pair;

import com.google.android.gms.maps.model.LatLng;

import java.util.List;

import me.chrislane.accudrop.generator.RouteCalculator;

/**
 * Calculate a route for a given target argument.
 */
public class RouteTask extends AsyncTask<LatLng, Void, List<Location>> {

    private final RouteTaskListener listener;
    private final Pair<Double, Double> windTuple;
    private final SharedPreferences sharedPreferences;
    private final Resources resources;

    public RouteTask(RouteTaskListener listener, SharedPreferences sharedPreferences,
                     Resources resources, Pair<Double, Double> windTuple) {
        this.listener = listener;
        this.sharedPreferences = sharedPreferences;
        this.resources = resources;
        this.windTuple = windTuple;
    }

    @Override
    protected List<Location> doInBackground(LatLng... Latlngs) {
        LatLng target = Latlngs[0];
        RouteCalculator routeCalculator = new RouteCalculator(sharedPreferences, resources,
                windTuple, target);
        return routeCalculator.calcRoute();
    }

    @Override
    protected void onPostExecute(List<Location> route) {
        super.onPostExecute(route);
        listener.onFinished(route);
    }

    public interface RouteTaskListener {
        void onFinished(List<Location> route);
    }
}

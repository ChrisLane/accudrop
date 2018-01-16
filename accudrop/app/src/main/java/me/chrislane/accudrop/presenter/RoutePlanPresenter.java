package me.chrislane.accudrop.presenter;

import android.arch.lifecycle.ViewModelProviders;

import com.google.android.gms.maps.model.LatLng;

import me.chrislane.accudrop.MainActivity;
import me.chrislane.accudrop.R;
import me.chrislane.accudrop.fragment.MapFragment;
import me.chrislane.accudrop.task.RouteTask;
import me.chrislane.accudrop.task.WindTask;
import me.chrislane.accudrop.viewmodel.RouteViewModel;
import me.chrislane.accudrop.viewmodel.WindViewModel;

public class RoutePlanPresenter {
    private static final String TAG = RoutePlanPresenter.class.getSimpleName();
    private final MapFragment mapFragment;
    private RouteViewModel routeViewModel = null;
    private WindViewModel windViewModel = null;
    private String apiKey;
    private boolean taskRunning;

    public RoutePlanPresenter(MapFragment mapFragment, LatLng target) {
        this.mapFragment = mapFragment;

        MainActivity main = (MainActivity) mapFragment.getActivity();
        if (main != null) {
            routeViewModel = ViewModelProviders.of(main).get(RouteViewModel.class);
            windViewModel = ViewModelProviders.of(main).get(WindViewModel.class);
        }

        apiKey = mapFragment.getResources().getString(R.string.owmApiKey);

        // Calculate a route to the target
        calcRoute(target);
    }

    /**
     * Update wind data and calculate a route
     *
     * @param target
     */
    public void calcRoute(LatLng target) {
        WindTask.WeatherTaskListener windListener = windTuple -> {
            // Update the windViewModel
            windViewModel.setWindSpeed(windTuple.windSpeed);
            windViewModel.setWindDirection(windTuple.windDirection);

            // Run a route calculation task with the updated wind
            RouteTask.RouteTaskListener routeListener = route -> routeViewModel.setRoute(route);
            new RouteTask(routeListener, windTuple).execute(target);
        };
        new WindTask(windListener, this, apiKey).execute(target);
    }

    public void setTaskRunning(boolean taskRunning) {
        this.taskRunning = taskRunning;
        mapFragment.setProgressBarVisibility(taskRunning);
    }
}

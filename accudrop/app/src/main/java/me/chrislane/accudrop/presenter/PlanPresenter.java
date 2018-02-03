package me.chrislane.accudrop.presenter;

import android.arch.lifecycle.ViewModelProviders;

import com.google.android.gms.maps.model.LatLng;

import me.chrislane.accudrop.MainActivity;
import me.chrislane.accudrop.R;
import me.chrislane.accudrop.fragment.PlanFragment;
import me.chrislane.accudrop.task.RouteTask;
import me.chrislane.accudrop.task.WindTask;
import me.chrislane.accudrop.viewmodel.RouteViewModel;
import me.chrislane.accudrop.viewmodel.WindViewModel;

public class PlanPresenter {

    private static final String TAG = PlanPresenter.class.getSimpleName();
    private final PlanFragment planFragment;
    private final String apiKey;
    private RouteViewModel routeViewModel = null;
    private WindViewModel windViewModel = null;

    public PlanPresenter(PlanFragment planFragment) {
        this.planFragment = planFragment;

        MainActivity main = (MainActivity) planFragment.getActivity();
        if (main != null) {
            routeViewModel = ViewModelProviders.of(main).get(RouteViewModel.class);
            windViewModel = ViewModelProviders.of(main).get(WindViewModel.class);
        }

        // Get the OpenWeatherMap API key
        apiKey = planFragment.getResources().getString(R.string.owmApiKey);
    }

    public PlanPresenter(PlanFragment planFragment, LatLng target) {
        this.planFragment = planFragment;

        MainActivity main = (MainActivity) planFragment.getActivity();
        if (main != null) {
            routeViewModel = ViewModelProviders.of(main).get(RouteViewModel.class);
            windViewModel = ViewModelProviders.of(main).get(WindViewModel.class);
        }

        // Get the OpenWeatherMap API key
        apiKey = planFragment.getResources().getString(R.string.owmApiKey);

        // Calculate a route to the target
        calcRoute(target);
    }

    /**
     * Update wind data and calculate a route.
     *
     * @param target The target landing coordinates.
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

    /**
     * Set the visibility of the map fragment's progress bar.
     *
     * @param taskRunning Whether to show the progress bar.
     */
    public void setTaskRunning(boolean taskRunning) {
        planFragment.setProgressBarVisibility(taskRunning);
    }
}

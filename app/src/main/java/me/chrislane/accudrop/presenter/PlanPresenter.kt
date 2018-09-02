package me.chrislane.accudrop.presenter

import android.arch.lifecycle.ViewModelProviders
import android.location.Location
import android.preference.PreferenceManager
import android.util.Pair

import com.google.android.gms.maps.model.LatLng

import me.chrislane.accudrop.MainActivity
import me.chrislane.accudrop.R
import me.chrislane.accudrop.fragment.PlanFragment
import me.chrislane.accudrop.task.RouteTask
import me.chrislane.accudrop.task.WindTask
import me.chrislane.accudrop.viewmodel.RouteViewModel
import me.chrislane.accudrop.viewmodel.WindViewModel

class PlanPresenter {
    private val planFragment: PlanFragment
    private val apiKey: String
    private var routeViewModel: RouteViewModel
    private var windViewModel: WindViewModel

    constructor(planFragment: PlanFragment) {
        this.planFragment = planFragment

        val main = planFragment.requireActivity() as MainActivity
        routeViewModel = ViewModelProviders.of(main).get(RouteViewModel::class.java)
        windViewModel = ViewModelProviders.of(main).get(WindViewModel::class.java)

        // Get the OpenWeatherMap API key
        apiKey = planFragment.resources.getString(R.string.owmApiKey)
    }

    constructor(planFragment: PlanFragment, target: LatLng) {
        this.planFragment = planFragment

        val main = planFragment.requireActivity() as MainActivity
        routeViewModel = ViewModelProviders.of(main).get(RouteViewModel::class.java)
        windViewModel = ViewModelProviders.of(main).get(WindViewModel::class.java)

        // Get the OpenWeatherMap API key
        apiKey = planFragment.resources.getString(R.string.owmApiKey)

        // Calculate a route to the target
        calcRoute(target)
    }

    /**
     * Update wind data and calculate a route.
     *
     * @param target The target landing coordinates.
     */
    fun calcRoute(target: LatLng) {
        val windListener = { windTuple: Pair<Double, Double> ->
            // Update the windViewModel
            windViewModel.setWindSpeed(windTuple.first)
            windViewModel.setWindDirection(windTuple.second)

            // Run a route calculation task with the updated wind
            val routeListener = { route: MutableList<Location> -> routeViewModel.setRoute(route) }
            val context = planFragment.requireContext()
            val sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context)
            val resources = context.resources
            RouteTask(routeListener, sharedPrefs, resources, windTuple).execute(target)
        }
        WindTask(windListener, this, apiKey).execute(target)
    }

    /**
     * Set the visibility of the map fragment's progress bar.
     *
     * @param taskRunning Whether to show the progress bar.
     */
    fun setTaskRunning(taskRunning: Boolean) {
        planFragment.setProgressBarVisibility(taskRunning)
    }

    companion object {
        private val TAG = PlanPresenter::class.java.simpleName
    }
}

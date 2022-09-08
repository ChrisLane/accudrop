package me.chrislane.accudrop.presenter

import android.location.Location
import androidx.preference.PreferenceManager
import android.util.Pair
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.maps.model.LatLng
import me.chrislane.accudrop.MainActivity
import me.chrislane.accudrop.R
import me.chrislane.accudrop.fragment.PlanFragment
import me.chrislane.accudrop.task.RouteTask
import me.chrislane.accudrop.task.WindTask
import me.chrislane.accudrop.viewmodel.RouteViewModel
import me.chrislane.accudrop.viewmodel.WindViewModel

class PlanPresenter(private val planFragment: PlanFragment) {
    private val apiKey: String = planFragment.resources.getString(R.string.owmApiKey)
    private val routeViewModel: RouteViewModel
    private val windViewModel: WindViewModel

    init {
        val main = planFragment.requireActivity() as MainActivity
        routeViewModel = ViewModelProvider(main).get(RouteViewModel::class.java)
        windViewModel = ViewModelProvider(main).get(WindViewModel::class.java)
    }

    constructor(planFragment: PlanFragment, target: LatLng) : this(planFragment) {
        // Calculate a route to the target
        calcRoute(target)
    }

    /**
     * Update wind data and calculate a route.
     *
     * @param target The target landing coordinates.
     */
    fun calcRoute(target: LatLng) {
        val callback = { windTuple: Pair<Double, Double>? -> windTaskCallback(target, windTuple) }
        WindTask(callback, this, apiKey).execute(target)
    }

    /**
     * Set the visibility of the map fragment's progress bar.
     *
     * @param taskRunning Whether to show the progress bar.
     */
    fun setTaskRunning(taskRunning: Boolean) {
        planFragment.setProgressBarVisibility(taskRunning)
    }


    private fun windTaskCallback(target: LatLng, windTuple: Pair<Double, Double>?) {
        if (windTuple != null) {
            windViewModel.setWindSpeed(windTuple.first)
            windViewModel.setWindDirection(windTuple.second)

            // Run a route calculation task with the updated wind
            val routeListener = { route: MutableList<Location> -> routeViewModel.setRoute(route) }
            val context = planFragment.requireContext()
            val sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context)
            val resources = context.resources
            RouteTask(routeListener, sharedPrefs, resources, windTuple).execute(target)
        } else {
            Toast.makeText(planFragment.requireContext(), "Failed to get wind data", Toast.LENGTH_SHORT).show()
        }
    }

    companion object {
        private val TAG = PlanPresenter::class.java.simpleName
    }
}

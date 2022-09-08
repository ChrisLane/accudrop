package me.chrislane.accudrop.presenter

import android.location.Location
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import me.chrislane.accudrop.fragment.ReplayMapFragment
import me.chrislane.accudrop.viewmodel.ReplayViewModel
import java.util.*

class ReplayMapPresenter(private val replayMapFragment: ReplayMapFragment) {
    private val parentFragment: Fragment? = replayMapFragment.parentFragment
    private var replayViewModel: ReplayViewModel

    init {

        if (parentFragment == null) {
            throw Exception("Missing parent fragment (Replay)")
        }
        replayViewModel = ViewModelProvider(parentFragment).get(ReplayViewModel::class.java)
    }

    /**
     * Add all observers.
     */
    fun addObservers() {
        subscribeToRoute()
    }

    /**
     * Set the new route being displayed.
     */
    private fun subscribeToRoute() {
        val routeObserver = Observer<MutableList<Pair<UUID, MutableList<Location>>>> {
            if (it != null) {
                replayMapFragment.updateMapRoutes(it)
            }
        }
        replayViewModel.getUsersAndLocs().observe(parentFragment!!, routeObserver)
    }
}

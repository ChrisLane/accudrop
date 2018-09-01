package me.chrislane.accudrop.presenter

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.location.Location
import android.support.v4.app.Fragment
import android.support.v4.util.Pair
import me.chrislane.accudrop.fragment.ReplayMapFragment
import me.chrislane.accudrop.viewmodel.ReplayViewModel
import java.util.*

class ReplayMapPresenter(private val replayMapFragment: ReplayMapFragment) {
    private val parentFragment: Fragment?
    private var replayViewModel: ReplayViewModel? = null

    init {

        parentFragment = replayMapFragment.parentFragment
        if (parentFragment != null) {
            replayViewModel = ViewModelProviders.of(parentFragment).get(ReplayViewModel::class.java)
        }
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
        replayViewModel?.getUsersAndLocs()?.observe(parentFragment!!, routeObserver)
    }
}

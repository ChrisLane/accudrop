package me.chrislane.accudrop.presenter

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.location.Location
import android.os.AsyncTask
import android.support.v4.util.Pair
import android.util.Log

import me.chrislane.accudrop.BuildConfig
import me.chrislane.accudrop.db.FallType
import me.chrislane.accudrop.fragment.ReplayFragment
import me.chrislane.accudrop.task.FetchLastJumpIdTask
import me.chrislane.accudrop.task.FetchUsersAndTypePositionsTask
import me.chrislane.accudrop.viewmodel.DatabaseViewModel
import me.chrislane.accudrop.viewmodel.ReplayViewModel
import java.util.*

class ReplayPresenter(private val replayFragment: ReplayFragment) {
    private val replayViewModel: ReplayViewModel = ViewModelProviders.of(replayFragment).get(ReplayViewModel::class.java)
    private val databaseViewModel: DatabaseViewModel = ViewModelProviders.of(replayFragment).get(DatabaseViewModel::class.java)

    init {

        subscribeToJumpId()
        subscribeToJumpRange()
        subscribeToButtonData()

        val listener = { jumpId: Int? ->
            if (jumpId != null) {
                replayViewModel.setJumpId(jumpId)
            }
        }
        FetchLastJumpIdTask(listener, databaseViewModel)
                .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
    }

    /**
     * Set the route to be displayed in replay views.
     *
     * @param jumpID The jump ID to get the route from.
     */
    private fun setRoutes(jumpID: Int) {
        val listener = { result: MutableList<Pair<UUID, MutableList<Location>>> ->
            replayViewModel.setUsersAndLocs(result)
        }
        FetchUsersAndTypePositionsTask(listener, FallType.CANOPY, databaseViewModel)
                .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, jumpID)
    }

    /**
     * Set the new route when the jump ID changes.
     */
    private fun subscribeToJumpId() {
        val jumpIdObserver = Observer<Int> { jumpId ->
            if (jumpId != null) {
                setRoutes(jumpId)
            }
        }
        replayViewModel.getJumpId().observe(replayFragment, jumpIdObserver)
    }

    private fun subscribeToJumpRange() {
        val firstJumpIdObserver = Observer<Int> { firstJumpId ->
            if (firstJumpId != null) {
                replayViewModel.setFirstJumpId(firstJumpId)
            }
        }
        databaseViewModel.findFirstJumpId().observe(replayFragment, firstJumpIdObserver)

        val lastJumpIdObserver = Observer<Int>{ lastJumpId ->
            if (lastJumpId != null) {
                replayViewModel.setLastJumpId(lastJumpId)
            }
        }
        databaseViewModel.findLastJumpId().observe(replayFragment, lastJumpIdObserver)
    }

    private fun subscribeToButtonData() {
        val buttonDataObserver = Observer<Int>{
            val jumpId = replayViewModel.getJumpId().value
            val firstJumpId = replayViewModel.getFirstJumpId().value
            val lastJumpId = replayViewModel.getLastJumpId().value

            if (BuildConfig.DEBUG) {
                Log.d(TAG, "Button data: $jumpId, $firstJumpId, $lastJumpId")
            }

            if (jumpId != null && firstJumpId != null && lastJumpId != null) {
                replayFragment.updateButtons(jumpId, firstJumpId, lastJumpId)
            }
        }
        replayViewModel.getJumpId().observe(replayFragment, buttonDataObserver)
        replayViewModel.getFirstJumpId().observe(replayFragment, buttonDataObserver)
        replayViewModel.getLastJumpId().observe(replayFragment, buttonDataObserver)
    }

    fun prevJump() {
        val jumpId = replayViewModel.getJumpId().value
        val firstJumpId = replayViewModel.getFirstJumpId().value

        if (jumpId != null && firstJumpId != null) {
            if (jumpId > firstJumpId) {
                replayViewModel.setJumpId(jumpId - 1)
            }
        }
    }

    fun nextJump() {
        val jumpId = replayViewModel.getJumpId().value
        val lastJumpId = replayViewModel.getLastJumpId().value

        if (jumpId != null && lastJumpId != null) {
            if (jumpId < lastJumpId) {
                replayViewModel.setJumpId(jumpId + 1)
            }
        }
    }

    companion object {
        private val TAG = ReplayPresenter::class.java.simpleName
    }
}
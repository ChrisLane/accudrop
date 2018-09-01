package me.chrislane.accudrop.presenter

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.os.AsyncTask
import android.support.v4.app.FragmentActivity
import android.util.Log
import me.chrislane.accudrop.BuildConfig
import me.chrislane.accudrop.db.FallType
import me.chrislane.accudrop.fragment.JumpStatsFragment
import me.chrislane.accudrop.task.*
import me.chrislane.accudrop.viewmodel.DatabaseViewModel
import me.chrislane.accudrop.viewmodel.JumpStatsViewModel
import java.util.*

class JumpStatsPresenter(private val fragment: JumpStatsFragment) {
    private val dbViewModel: DatabaseViewModel
    private val viewModel: JumpStatsViewModel
    private val main: FragmentActivity
    private lateinit var uuid: UUID

    init {

        main = fragment.requireActivity()

        dbViewModel = ViewModelProviders.of(fragment).get(DatabaseViewModel::class.java)
        viewModel = ViewModelProviders.of(fragment).get(JumpStatsViewModel::class.java)

        initialise()

        subscribeToJumpId()
        subscribeToJumpRange()
        subscribeToButtonData()
    }

    private fun initialise() {
        val settings = main.getSharedPreferences("userInfo", Context.MODE_PRIVATE)
        val stringUuid = settings.getString("userUUID", "")
        uuid = UUID.fromString(stringUuid)

        val listener = { jumpId: Int? ->
            if (jumpId != null) {
                viewModel.setJumpId(jumpId)
            }
        }
        FetchLastJumpIdTask(listener, dbViewModel).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
    }

    private fun subscribeToJumpId() {
        val jumpObserver = Observer<Int> { jumpId ->
            if (jumpId != null) {
                fragment.updateJumpId(jumpId)
                updateDurations(jumpId)
                updateSpeeds(jumpId)
                updateExitAltitude(jumpId)
            }
        }
        viewModel.getJumpId().observe(fragment, jumpObserver)
    }

    private fun updateExitAltitude(jumpId: Int) {
        val listener = { _: Int, max: Int ->
            fragment.updateExitAltitude(max)
        }

        MinMaxAltiTask(listener, dbViewModel)
                .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, jumpId)
    }

    private fun updateSpeeds(jumpId: Int) {
        updateFreefallSpeeds(jumpId)
        updateCanopySpeeds(jumpId)
    }

    private fun updateCanopySpeeds(jumpId: Int) {
        // Update vertical speed
        val vListener = { vSpeed: Double? ->
            if (vSpeed != null) {
                fragment.updateCanopyVSpeed(vSpeed)
            }
        }
        FetchFallTypeMaxVSpeed(vListener, FallType.CANOPY, uuid, dbViewModel)
                .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, jumpId)

        // Update horizontal speed
        val hListener = { hSpeed: Float? ->
            if (hSpeed != null) {
                fragment.updateCanopyHSpeed(hSpeed)
            }
        }
        FetchFallTypeMaxHSpeed(hListener, FallType.CANOPY, uuid, dbViewModel)
                .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, jumpId)
    }

    private fun updateFreefallSpeeds(jumpId: Int) {
        // Update vertical speed
        val vListener = { vSpeed: Double? ->
            if (vSpeed != null) {
                fragment.updateFreefallVSpeed(vSpeed)
            } else {
                fragment.updateFreefallVSpeed(0.0)
            }
        }
        FetchFallTypeMaxVSpeed(vListener, FallType.FREEFALL, uuid, dbViewModel)
                .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, jumpId)

        // Update horizontal speed
        val hListener = { hSpeed: Float? ->
            if (hSpeed != null) {
                fragment.updateFreefallHSpeed(hSpeed)
            } else {
                fragment.updateFreefallHSpeed(0f)
            }
        }
        FetchFallTypeMaxHSpeed(hListener, FallType.FREEFALL, uuid, dbViewModel)
                .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, jumpId)
    }

    private fun updateDurations(jumpId: Int) {
        updateTotalDuration(jumpId)
        updateFreefallDuration(jumpId)
        updateCanopyDuration(jumpId)
    }

    private fun updateCanopyDuration(jumpId: Int) {
        val listener = { millis: Long? ->
            if (millis != null) {
                fragment.updateCanopyDuration(millis)
            } else {
                fragment.updateCanopyDuration(0)
            }
        }
        FetchFallTypeDuration(listener, FallType.CANOPY, uuid, dbViewModel)
                .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, jumpId)
    }

    private fun updateFreefallDuration(jumpId: Int) {
        val listener = { millis: Long? ->
            if (millis != null) {
                fragment.updateFreefallDuration(millis)
            } else {
                fragment.updateFreefallDuration(0)
            }
        }
        FetchFallTypeDuration(listener, FallType.FREEFALL, uuid, dbViewModel)
                .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, jumpId)
    }

    private fun updateTotalDuration(jumpId: Int) {
        val listener = { millis: Long? ->
            if (millis != null) {
                fragment.updateTotalDuration(millis)
            } else {
                fragment.updateTotalDuration(0)
            }
        }
        FetchTotalDuration(listener, uuid, dbViewModel)
                .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, jumpId)
    }

    private fun subscribeToJumpRange() {
        val firstJumpIdObserver = Observer<Int> {
            if (it != null) {
                viewModel.setFirstJumpId(it)
            }
        }
        dbViewModel.findFirstJumpId().observe(fragment, firstJumpIdObserver)

        val lastJumpIdObserver = Observer<Int> {
            if (it != null) {
                viewModel.setLastJumpId(it)
            }
        }
        dbViewModel.findLastJumpId().observe(fragment, lastJumpIdObserver)
    }

    private fun subscribeToButtonData() {
        val buttonDataObserver = Observer<Int> {
            val jumpId = viewModel.getJumpId().value
            val firstJumpId = viewModel.getFirstJumpId().value
            val lastJumpId = viewModel.getLastJumpId().value

            if (BuildConfig.DEBUG) {
                Log.v(TAG, "Button data: $jumpId, $firstJumpId, $lastJumpId")
            }
            if (jumpId != null && firstJumpId != null && lastJumpId != null) {
                fragment.updateButtons(jumpId, firstJumpId, lastJumpId)
            }
        }
        viewModel.getJumpId().observe(fragment, buttonDataObserver)
        viewModel.getFirstJumpId().observe(fragment, buttonDataObserver)
        viewModel.getLastJumpId().observe(fragment, buttonDataObserver)
    }

    fun prevJump() {
        val jumpId = viewModel.getJumpId().value
        val firstJumpId = viewModel.getFirstJumpId().value

        if (jumpId != null && firstJumpId != null) {
            if (jumpId > firstJumpId) {
                viewModel.setJumpId(jumpId - 1)
            }
        }
    }

    fun nextJump() {
        val jumpId = viewModel.getJumpId().value
        val lastJumpId = viewModel.getLastJumpId().value

        if (jumpId != null && lastJumpId != null) {
            if (jumpId < lastJumpId) {
                viewModel.setJumpId(jumpId + 1)
            }
        }
    }

    companion object {

        private val TAG = JumpStatsPresenter::class.java.simpleName
    }
}

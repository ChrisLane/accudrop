package me.chrislane.accudrop.presenter

import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import android.content.Intent
import android.location.Location
import android.os.AsyncTask
import android.util.Log
import me.chrislane.accudrop.MainActivity
import me.chrislane.accudrop.fragment.JumpFragment
import me.chrislane.accudrop.service.LocationService
import me.chrislane.accudrop.task.CreateAndInsertJumpTask
import me.chrislane.accudrop.task.FetchJumpTask
import me.chrislane.accudrop.viewmodel.DatabaseViewModel
import me.chrislane.accudrop.viewmodel.GnssViewModel
import me.chrislane.accudrop.viewmodel.JumpViewModel
import me.chrislane.accudrop.viewmodel.PressureViewModel

class JumpPresenter(private val jumpFragment: JumpFragment) {
    private val databaseViewModel: DatabaseViewModel
    private val jumpViewModel: JumpViewModel
    private var pressureViewModel: PressureViewModel
    private var gnssViewModel: GnssViewModel
    /**
     * Get whether there is an active jump or not.
     *
     * @return Whether there an active jump or not.
     */
    var isJumping = false
        private set

    init {

        val main = jumpFragment.requireActivity() as MainActivity
        pressureViewModel = ViewModelProvider(main).get(PressureViewModel::class.java)
        gnssViewModel = ViewModelProvider(main).get(GnssViewModel::class.java)
        databaseViewModel = ViewModelProvider(main).get(DatabaseViewModel::class.java)
        jumpViewModel = ViewModelProvider(main).get(JumpViewModel::class.java)

        subscribeToPressure()
    }

    /**
     *
     * Start a jump.
     *
     * Inserts a new jump and starts the foreground location tracking service.
     */
    fun startJump() {
        Log.i(TAG, "Starting jump.")
        isJumping = true

        gnssViewModel.gnssListener.stopListening()
        val createListener = { jumpId: Int ->
            jumpViewModel.setJumpId(jumpId)
        }
        val insertListener = {
            startLocationService()
        }
        CreateAndInsertJumpTask(databaseViewModel, createListener, insertListener).execute()
    }

    /**
     * Start the foreground location tracking service.
     */
    private fun startLocationService() {
        val main = jumpFragment.requireActivity() as MainActivity
        // Get ground pressure
        val pressureViewModel = ViewModelProvider(main).get<PressureViewModel>(PressureViewModel::class.java)
        val groundPressure = pressureViewModel.getGroundPressure().value

        // Create intent and add ground pressure
        val locationService = Intent(main, LocationService::class.java)
        if (groundPressure != null) {
            locationService.putExtra("groundPressure", groundPressure)

        }

        // Start the service
        main.startService(locationService)
    }

    /**
     * Stop the jump and foreground location tracking service.
     */
    fun stopJump() {
        Log.i(TAG, "Stopping jump.")
        isJumping = false

        val main = jumpFragment.requireActivity() as MainActivity
        val intent = Intent(main, LocationService::class.java)
        main.stopService(intent)


        // Remove the jump if no positional data was logged
        val jumpId = jumpViewModel.getJumpId().value
        if (jumpId != null) {
            val listener = { locations: MutableList<Location> ->
                if (locations.isEmpty()) {
                    AsyncTask.execute { databaseViewModel.deleteJump(jumpId) }
                }

            }
            FetchJumpTask(listener, databaseViewModel)
                    .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
        }


        gnssViewModel.gnssListener.startListening()
    }

    /**
     * Subscribe to altitude changes and update the fragment view.
     */
    private fun subscribeToPressure() {
        val altitudeObserver = Observer<Float> {
            // Update altitude text
            if (it != null) {
                jumpFragment.updatePressureAltitude(it)
            }
        }

        pressureViewModel.getLastAltitude().observe(jumpFragment, altitudeObserver)
    }

    /**
     * Zero the ground pressure.
     */
    fun calibrate() {
        pressureViewModel.setGroundPressure()
    }

    /**
     * Resume listening on pressure and altitude events.
     */
    fun resume() {
        if (!isJumping) {
            pressureViewModel.pressureListener.startListening()
            val main = jumpFragment.activity as MainActivity?

            if (main != null) {
                val permissionManager = main.permissionManager
                if (permissionManager.checkLocationPermission()) {
                    gnssViewModel.gnssListener.startListening()
                } else {
                    val reason = "Location access is required to track your jump location."
                    permissionManager.requestLocationPermission(reason)
                }
            }
        }
    }

    /**
     * Stop listening on pressure and altitude changes.
     */
    fun pause() {
        if (!isJumping) {
            pressureViewModel.pressureListener.stopListening()
            gnssViewModel.gnssListener.stopListening()
        }
    }

    companion object {
        private val TAG = JumpPresenter::class.java.simpleName
    }
}

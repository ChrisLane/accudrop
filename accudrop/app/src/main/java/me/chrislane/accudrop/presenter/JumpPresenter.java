package me.chrislane.accudrop.presenter;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.util.Log;

import me.chrislane.accudrop.MainActivity;
import me.chrislane.accudrop.PermissionManager;
import me.chrislane.accudrop.Util;
import me.chrislane.accudrop.fragment.JumpFragment;
import me.chrislane.accudrop.service.LocationService;
import me.chrislane.accudrop.task.CreateAndInsertJumpTask;
import me.chrislane.accudrop.task.InsertJumpTask;
import me.chrislane.accudrop.viewmodel.GnssViewModel;
import me.chrislane.accudrop.viewmodel.PressureViewModel;

public class JumpPresenter {

    private static final String TAG = JumpPresenter.class.getSimpleName();
    private final JumpFragment jumpFragment;
    private PressureViewModel pressureViewModel = null;
    private GnssViewModel gnssViewModel = null;
    private boolean isJumping = false;

    public JumpPresenter(JumpFragment jumpFragment) {
        this.jumpFragment = jumpFragment;

        MainActivity main = (MainActivity) jumpFragment.requireActivity();
        pressureViewModel = ViewModelProviders.of(main).get(PressureViewModel.class);
        gnssViewModel = ViewModelProviders.of(main).get(GnssViewModel.class);

        subscribeToPressure();
    }

    /**
     * <p>Start a jump.</p>
     * <p>Inserts a new jump and starts the foreground location tracking service.</p>
     */
    public void startJump() {
        Log.i(TAG, "Starting jump.");
        isJumping = true;

        MainActivity main = (MainActivity) jumpFragment.requireActivity();
        gnssViewModel.getGnssListener().stopListening();
        CreateAndInsertJumpTask.Listener createListener = jumpId -> {
        };
        InsertJumpTask.Listener insertListener = this::startLocationService;
        new CreateAndInsertJumpTask(main, createListener, insertListener).execute();
    }

    /**
     * Start the foreground location tracking service.
     */
    private void startLocationService() {
        MainActivity main = (MainActivity) jumpFragment.requireActivity();
        // Get ground pressure
        PressureViewModel pressureViewModel = ViewModelProviders.of(main).get(PressureViewModel.class);
        Float groundPressure = pressureViewModel.getGroundPressure().getValue();

        // Create intent and add ground pressure
        Intent locationService = new Intent(main, LocationService.class);
        if (groundPressure != null) {
            locationService.putExtra("groundPressure", groundPressure);

        }

        // Start the service
        main.startService(locationService);
    }

    /**
     * Stop the jump and foreground location tracking service.
     */
    public void stopJump() {
        Log.i(TAG, "Stopping jump.");
        isJumping = false;

        MainActivity main = (MainActivity) jumpFragment.requireActivity();
        Intent intent = new Intent(main, LocationService.class);
        main.stopService(intent);
        gnssViewModel.getGnssListener().startListening();
    }

    /**
     * Subscribe to altitude changes and update the fragment view.
     */
    private void subscribeToPressure() {
        final Observer<Float> altitudeObserver = altitude -> {
            // Update altitude text
            if (altitude != null) {
                jumpFragment.updatePressureAltitude(Util.metresToFeet(altitude), Util.Unit.IMPERIAL);
            }
        };

        pressureViewModel.getLastAltitude().observe(jumpFragment, altitudeObserver);
    }

    /**
     * Zero the ground pressure.
     */
    public void calibrate() {
        pressureViewModel.setGroundPressure();
    }

    /**
     * Resume listening on pressure and altitude events.
     */
    public void resume() {
        if (!isJumping) {
            pressureViewModel.getPressureListener().startListening();
            MainActivity main = (MainActivity) jumpFragment.getActivity();

            if (main != null) {
                PermissionManager permissionManager = main.getPermissionManager();
                if (permissionManager.checkLocationPermission()) {
                    gnssViewModel.getGnssListener().startListening();
                } else {
                    String reason = "Location access is required to track your jump location.";
                    permissionManager.requestLocationPermission(reason);
                }
            }
        }
    }

    /**
     * Stop listening on pressure and altitude changes.
     */
    public void pause() {
        if (!isJumping) {
            pressureViewModel.getPressureListener().stopListening();
            gnssViewModel.getGnssListener().stopListening();
        }
    }

    /**
     * Get whether there is an active jump or not.
     *
     * @return Whether there an active jump or not.
     */
    public boolean isJumping() {
        return isJumping;
    }
}

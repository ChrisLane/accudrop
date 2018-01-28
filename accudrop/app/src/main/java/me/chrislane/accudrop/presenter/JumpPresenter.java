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
        MainActivity main = (MainActivity) jumpFragment.getActivity();
        if (main != null) {
            pressureViewModel = ViewModelProviders.of(main).get(PressureViewModel.class);
            gnssViewModel = ViewModelProviders.of(main).get(GnssViewModel.class);
        }

        subscribeToPressure();
    }

    public void startJump() {
        Log.i(TAG, "Starting jump.");
        isJumping = true;
        MainActivity main = (MainActivity) jumpFragment.getActivity();
        if (main != null) {
            gnssViewModel.getGnssListener().stopListening();
            CreateAndInsertJumpTask.Listener createListener = jumpId -> {
            };
            InsertJumpTask.Listener insertListener = this::startLocationService;
            new CreateAndInsertJumpTask(main, createListener, insertListener).execute();
        } else {
            Log.e(TAG, "Could not get main activity.");
        }
    }

    private void startLocationService() {
        MainActivity main = (MainActivity) jumpFragment.getActivity();
        if (main != null) {
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
    }

    public void stopJump() {
        Log.i(TAG, "Stopping jump.");
        isJumping = false;
        MainActivity main = (MainActivity) jumpFragment.getActivity();
        if (main != null) {
            Intent intent = new Intent(main, LocationService.class);
            main.stopService(intent);
            gnssViewModel.getGnssListener().startListening();
        } else {
            Log.e(TAG, "Could not get main activity.");
        }
    }

    /**
     * Subscribe to altitude changes.
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

    public void calibrate() {
        pressureViewModel.setGroundPressure();
    }

    public void resume() {
        if (!isJumping) {
            MainActivity main = (MainActivity) jumpFragment.getActivity();
            if (main != null) {
                PermissionManager permissionManager = main.getPermissionManager();
                pressureViewModel.getPressureListener().startListening();
                if (permissionManager.checkLocationPermission()) {
                    gnssViewModel.getGnssListener().startListening();
                } else {
                    String reason = "Location access is required to track your jump location.";
                    permissionManager.requestLocationPermission(reason);
                }
            }
        }
    }

    public void pause() {
        if (!isJumping) {
            pressureViewModel.getPressureListener().stopListening();
            gnssViewModel.getGnssListener().stopListening();
        }
    }

    public boolean isJumping() {
        return isJumping;
    }
}

package me.chrislane.accudrop.presenter;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.os.AsyncTask;
import android.util.Log;

import java.util.Date;

import me.chrislane.accudrop.MainActivity;
import me.chrislane.accudrop.PermissionManager;
import me.chrislane.accudrop.Util;
import me.chrislane.accudrop.db.AccudropDb;
import me.chrislane.accudrop.db.Jump;
import me.chrislane.accudrop.fragment.JumpFragment;
import me.chrislane.accudrop.viewmodel.JumpViewModel;
import me.chrislane.accudrop.viewmodel.LocationViewModel;
import me.chrislane.accudrop.viewmodel.PressureViewModel;

public class JumpPresenter {

    private static final String TAG = JumpPresenter.class.getSimpleName();
    private final PressureViewModel pressureViewModel;
    private final JumpFragment jumpFragment;
    private final LocationViewModel locationViewModel;
    private final JumpViewModel jumpViewModel;
    private AccudropDb db;

    public JumpPresenter(JumpFragment jumpFragment) {
        this.jumpFragment = jumpFragment;
        pressureViewModel = ViewModelProviders.of(jumpFragment.getActivity()).get(PressureViewModel.class);
        locationViewModel = ViewModelProviders.of(jumpFragment.getActivity()).get(LocationViewModel.class);
        jumpViewModel = ViewModelProviders.of(jumpFragment.getActivity()).get(JumpViewModel.class);

        db = AccudropDb.getDatabase(jumpFragment.getContext());

        subscribeToPressure();
    }

    public void startJump() {
        Log.i(TAG, "Starting jump.");
        MainActivity main = (MainActivity) jumpFragment.getActivity();
        if (main != null) {
            Integer result = jumpViewModel.getLastJumpId().getValue();
            int jumpId;

            if (result != null) {
                Log.d(TAG, "Previous jump id: " + result);
                jumpId = result + 1;
            } else {
                Log.d(TAG, "No previous jump id.");
                jumpId = 1;
            }

            Jump jump = new Jump();
            jump.id = jumpId;
            jump.time = new Date();

            AsyncTask.execute(() -> db.jumpModel().insertJump(jump));

            main.getReadingListener().enableLogging();
        } else {
            Log.e(TAG, "Could not get main activity.");
        }
    }

    public void stopJump() {
        Log.i(TAG, "Stopping jump.");
        MainActivity main = (MainActivity) jumpFragment.getActivity();
        if (main != null) {
            main.getReadingListener().disableLogging();
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
        MainActivity main = (MainActivity) jumpFragment.getActivity();
        PermissionManager permissionManager = main.getPermissionManager();
        pressureViewModel.startListening();
        if (permissionManager.checkLocationPermission()) {
            locationViewModel.startListening();
        } else {
            String reason = "Location access is required to track your jump location.";
            permissionManager.requestLocationPermission(reason);
        }
    }

    public void pause() {
        pressureViewModel.stopListening();
        locationViewModel.stopListening();
    }
}

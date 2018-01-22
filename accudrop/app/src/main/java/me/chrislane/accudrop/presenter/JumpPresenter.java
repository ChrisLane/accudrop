package me.chrislane.accudrop.presenter;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;

import java.lang.ref.WeakReference;
import java.util.Date;

import me.chrislane.accudrop.MainActivity;
import me.chrislane.accudrop.PermissionManager;
import me.chrislane.accudrop.Util;
import me.chrislane.accudrop.db.Jump;
import me.chrislane.accudrop.fragment.JumpFragment;
import me.chrislane.accudrop.service.LocationService;
import me.chrislane.accudrop.viewmodel.GnssViewModel;
import me.chrislane.accudrop.viewmodel.JumpViewModel;
import me.chrislane.accudrop.viewmodel.PressureViewModel;

public class JumpPresenter {

    private static final String TAG = JumpPresenter.class.getSimpleName();
    private final JumpFragment jumpFragment;
    private PressureViewModel pressureViewModel = null;
    private GnssViewModel gnssViewModel = null;
    private JumpViewModel jumpViewModel = null;
    private boolean isJumping = false;

    public JumpPresenter(JumpFragment jumpFragment) {
        this.jumpFragment = jumpFragment;
        MainActivity main = (MainActivity) jumpFragment.getActivity();
        if (main != null) {
            pressureViewModel = ViewModelProviders.of(main).get(PressureViewModel.class);
            gnssViewModel = ViewModelProviders.of(main).get(GnssViewModel.class);
            jumpViewModel = ViewModelProviders.of(main).get(JumpViewModel.class);
        }

        subscribeToPressure();
    }

    public void startJump() {
        Log.i(TAG, "Starting jump.");
        isJumping = true;
        MainActivity main = (MainActivity) jumpFragment.getActivity();
        if (main != null) {
            gnssViewModel.getGnssListener().stopListening();
            new CreateAndInsertJumpTask(main).execute();
        } else {
            Log.e(TAG, "Could not get main activity.");
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

    public static class CreateAndInsertJumpTask extends AsyncTask<Void, Void, Integer> {

        private final WeakReference<MainActivity> mainRef;
        private final JumpViewModel jumpViewModel;

        CreateAndInsertJumpTask(MainActivity main) {
            mainRef = new WeakReference<>(main);
            jumpViewModel = ViewModelProviders.of(main).get(JumpViewModel.class);

        }

        @Override
        protected Integer doInBackground(Void... params) {
            return jumpViewModel.getLastJumpId();
        }

        @Override
        protected void onPostExecute(Integer result) {
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

            new InsertJumpTask(mainRef, jumpViewModel).execute(jump);
        }
    }

    public static class InsertJumpTask extends AsyncTask<Jump, Void, Void> {
        private final WeakReference<MainActivity> mainRef;
        private final JumpViewModel jumpViewModel;


        InsertJumpTask(WeakReference<MainActivity> mainRef, JumpViewModel jumpViewModel) {
            this.mainRef = mainRef;
            this.jumpViewModel = jumpViewModel;
        }

        @Override
        protected Void doInBackground(Jump... jumps) {
            jumpViewModel.addJump(jumps[0]);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            MainActivity main = mainRef.get();
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
    }
}

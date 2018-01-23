package me.chrislane.accudrop.fragment;


import android.app.Activity;
import android.app.ActivityManager;
import android.arch.lifecycle.DefaultLifecycleObserver;
import android.arch.lifecycle.LifecycleOwner;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.ToggleButton;

import me.chrislane.accudrop.MainActivity;
import me.chrislane.accudrop.R;
import me.chrislane.accudrop.Util;
import me.chrislane.accudrop.Util.Unit;
import me.chrislane.accudrop.presenter.JumpPresenter;
import me.chrislane.accudrop.service.LocationService;

public class JumpFragment extends Fragment implements DefaultLifecycleObserver {

    private static final String TAG = JumpFragment.class.getSimpleName();
    private View view;
    private JumpPresenter jumpPresenter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        jumpPresenter = new JumpPresenter(this);

        MainActivity main = (MainActivity) getActivity();
        if (main != null) {
            main.getLifecycle().addObserver(this);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment.
        view = inflater.inflate(R.layout.fragment_jump, container, false);

        // Add click listener for fragment view.
        Button calibrateButton = view.findViewById(R.id.calibrate_button);
        calibrateButton.setOnClickListener(this::onClickCalibrate);

        // Set the jump button toggle state
        ToggleButton jumpButton = view.findViewById(R.id.jump_button);
        jumpButton.setOnCheckedChangeListener(null);
        Log.d(TAG, "LocationService running: " + isServiceRunning(LocationService.class));
        jumpButton.setChecked(isServiceRunning(LocationService.class));
        jumpButton.setOnCheckedChangeListener(onClickJump());

        return view;
    }

    /**
     * Zeros the altitude.
     *
     * @param view The view calling the method.
     */
    private void onClickCalibrate(View view) {
        Log.d(TAG, "Calibrating.");
        jumpPresenter.calibrate();
    }

    private CompoundButton.OnCheckedChangeListener onClickJump() {
        return (compoundButton, isChecked) -> {
            if (isChecked) {
                jumpPresenter.startJump();
            } else {
                jumpPresenter.stopJump();
            }
        };
    }

    /**
     * Update the altitude text.
     *
     * @param altitude The altitude to set.
     * @param unit     The unit to display after the altitude.
     */
    public void updatePressureAltitude(Double altitude, Unit unit) {
        Log.v(TAG, "Updating pressure altitude text.");
        TextView text = view.findViewById(R.id.pressure_altitude);
        text.setText(Util.getAltitudeText(altitude, unit));
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putBoolean("jumpButton", jumpPresenter.isJumping());
    }

    @Override
    public void onResume(@NonNull LifecycleOwner owner) {
        jumpPresenter.resume();
    }

    @Override
    public void onPause(@NonNull LifecycleOwner owner) {
        jumpPresenter.pause();
    }

    /**
     * <p>Check if a service is running.</p>
     * <p>Code taken from <a href="https://stackoverflow.com/a/5921190">a StackOverflow answer.</a></p>
     *
     * @param serviceClass The service class to check for an instance of.
     * @return Whether the service is running.
     */
    private boolean isServiceRunning(Class<?> serviceClass) {
        Activity activity = getActivity();
        if (activity != null) {
            ActivityManager manager = (ActivityManager) activity.getSystemService(Context.ACTIVITY_SERVICE);

            if (manager != null) {
                for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
                    if (serviceClass.getName().equals(service.service.getClassName())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}

package me.chrislane.accudrop.fragment;


import android.arch.lifecycle.DefaultLifecycleObserver;
import android.arch.lifecycle.LifecycleOwner;
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

public class JumpFragment extends Fragment implements DefaultLifecycleObserver {

    private static final String TAG = JumpFragment.class.getSimpleName();
    private View view;
    private JumpPresenter jumpPresenter;
    private Boolean isJumping;

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

        ToggleButton jumpButton = view.findViewById(R.id.jump_button);
        jumpButton.setOnCheckedChangeListener(null);
        if (savedInstanceState != null) {
            jumpButton.setChecked(savedInstanceState.getBoolean("jumpButton", false));
        }
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
                isJumping = true;
                jumpPresenter.startJump();
            } else {
                isJumping = false;
                jumpPresenter.stopJump();
            }
        };
    }

    /**
     * Update the altitude text.
     *
     * @param altitude The altitude to set.
     * @param unit The unit to display after the altitude.
     */
    public void updatePressureAltitude(Double altitude, Unit unit) {
        Log.v(TAG, "Updating pressure altitude text.");
        TextView text = view.findViewById(R.id.pressure_altitude);
        text.setText(Util.getAltitudeText(altitude, unit));
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putBoolean("jumpButton", isJumping);
    }

    @Override
    public void onResume(@NonNull LifecycleOwner owner) {
        jumpPresenter.resume();
    }

    @Override
    public void onPause(@NonNull LifecycleOwner owner) {
        jumpPresenter.pause();
    }
}

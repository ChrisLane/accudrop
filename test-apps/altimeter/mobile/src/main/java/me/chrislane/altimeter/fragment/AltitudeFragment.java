package me.chrislane.altimeter.fragment;


import android.arch.lifecycle.*;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import me.chrislane.altimeter.R;
import me.chrislane.altimeter.viewmodel.PressureViewModel;

import java.util.Locale;


/**
 * A simple {@link Fragment} subclass.
 */
public class AltitudeFragment extends Fragment implements LifecycleObserver {

    public static final String TAG = "altitude_fragment";
    private PressureViewModel pressureViewModel;
    private View view;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment.
        view = inflater.inflate(R.layout.fragment_altitude, container, false);
        setRetainInstance(true);

        // Get view models.
        pressureViewModel = ViewModelProviders.of(getActivity()).get(PressureViewModel.class);

        // Add click listener for fragment view.
        Button calibrateButton = view.findViewById(R.id.calibrate_button);
        calibrateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickCalibrate(v);
            }
        });

        // Subscribe to data changes
        subscribeToPressure();

        getActivity().getLifecycle().addObserver(this);

        return view;
    }

    public void onClickCalibrate(View view) {
        Log.d(TAG, "Calibrating.");
        pressureViewModel.setGroundPressure();
    }

    /**
     * Subscribe to altitude changes.
     */
    private void subscribeToPressure() {
        final Observer<Float> altitudeObserver = new Observer<Float>() {
            @Override
            public void onChanged(@Nullable final Float altitude) {
                // Update altitude text
                updatePressureAltitude(altitude);
            }
        };

        final Observer<Float> pressureObserver = new Observer<Float>() {
            @Override
            public void onChanged(@Nullable final Float pressure) {
                // Update pressure text
                updatePressure(pressure);
            }
        };

        pressureViewModel.getLastAltitude().observe(this, altitudeObserver);
        pressureViewModel.getLastPressure().observe(this, pressureObserver);
    }

    public void updatePressureAltitude(Float altitude) {
        Log.d(TAG, "Updating pressure altitude text.");
        TextView text = view.findViewById(R.id.pressure_altitude);
        text.setText(String.format(Locale.ENGLISH, "%.0f m", altitude));
    }

    public void updatePressure(Float pressure) {
        Log.d(TAG, "Updating pressure altitude text.");
        TextView text = view.findViewById(R.id.pressure);
        text.setText(String.format(Locale.ENGLISH, "%.0f hPa", pressure));
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    public void startPressure() {
        pressureViewModel.startListening();
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    public void stopPressure() {
        pressureViewModel.stopListening();
    }
}

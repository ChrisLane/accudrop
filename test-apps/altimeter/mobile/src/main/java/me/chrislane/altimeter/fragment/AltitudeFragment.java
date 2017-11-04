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
import me.chrislane.altimeter.Util;
import me.chrislane.altimeter.Util.Unit;
import me.chrislane.altimeter.viewmodel.LocationViewModel;
import me.chrislane.altimeter.viewmodel.PressureViewModel;

import java.util.Locale;


/**
 * A simple {@link Fragment} subclass.
 */
public class AltitudeFragment extends Fragment implements LifecycleObserver {

    public static final String TAG = "altitude_fragment";
    private PressureViewModel pressureViewModel;
    private LocationViewModel locationViewModel;
    private View view;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment.
        view = inflater.inflate(R.layout.fragment_altitude, container, false);
        setRetainInstance(true);

        // Get view models.
        pressureViewModel = ViewModelProviders.of(getActivity()).get(PressureViewModel.class);
        locationViewModel = ViewModelProviders.of(getActivity()).get(LocationViewModel.class);

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
        subscribeToLocation();

        getActivity().getLifecycle().addObserver(this);

        return view;
    }

    public void onClickCalibrate(View view) {
        Log.d(TAG, "Calibrating.");
        pressureViewModel.setGroundPressure();
        locationViewModel.setGroundLocation();
    }

    /**
     * Subscribe to altitude changes.
     */
    private void subscribeToPressure() {
        final Observer<Float> altitudeObserver = new Observer<Float>() {
            @Override
            public void onChanged(@Nullable final Float altitude) {
                // Update altitude text
                if (altitude != null) {
                    updatePressureAltitude(Util.metresToFeet(altitude), Unit.IMPERIAL);
                }
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

    /**
     * Subscribe to location changes.
     */
    private void subscribeToLocation() {
        final Observer<Double> locationObserver = new Observer<Double>() {
            @Override
            public void onChanged(@Nullable final Double altitude) {
                // Update altitude text
                if (altitude != null) {
                    updateLocationAltitude(Util.metresToFeet(altitude.floatValue()), Unit.IMPERIAL);
                }
            }
        };

        locationViewModel.getLastAltitude().observe(this, locationObserver);
    }

    public void updateLocationAltitude(Float altitude, Unit unit) {
        Log.v(TAG, "Updating GPS altitude text");
        TextView text = view.findViewById(R.id.gps_altitude);
        text.setText(getAltitudeText(altitude, unit));

    }

    public void updatePressureAltitude(Float altitude, Unit unit) {
        Log.v(TAG, "Updating pressure altitude text.");
        TextView text = view.findViewById(R.id.pressure_altitude);
        text.setText(getAltitudeText(altitude, unit));
    }

    public String getAltitudeText(Float altitude, Unit unit) {
        String unitSymbol = "";
        switch (unit) {
            case METRIC:
                unitSymbol = "m";
                break;
            case IMPERIAL:
                unitSymbol = "ft";
                break;
        }

        return String.format(Locale.ENGLISH, "%.0f %s", altitude, unitSymbol);
    }

    public void updatePressure(Float pressure) {
        Log.v(TAG, "Updating pressure altitude text.");
        TextView text = view.findViewById(R.id.pressure);
        text.setText(String.format(Locale.ENGLISH, "%.0f hPa", pressure));
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    public void startListening() {
        pressureViewModel.startListening();
        locationViewModel.startListening();
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    public void stopListening() {
        pressureViewModel.stopListening();
        locationViewModel.stopListening();
    }
}

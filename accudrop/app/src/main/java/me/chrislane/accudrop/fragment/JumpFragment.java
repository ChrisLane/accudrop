package me.chrislane.accudrop.fragment;


import android.arch.lifecycle.*;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import me.chrislane.accudrop.MainActivity;
import me.chrislane.accudrop.PermissionManager;
import me.chrislane.accudrop.R;
import me.chrislane.accudrop.Util;
import me.chrislane.accudrop.Util.Unit;
import me.chrislane.accudrop.viewmodel.LocationViewModel;
import me.chrislane.accudrop.viewmodel.PressureViewModel;

import java.util.Locale;

/**
 * A simple {@link Fragment} subclass.
 */
public class JumpFragment extends Fragment implements LifecycleObserver {

    public static final String TAG = JumpFragment.class.getSimpleName();
    private PressureViewModel pressureViewModel;
    private LocationViewModel locationViewModel;
    private View view;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        // Get view models.
        if (savedInstanceState == null) {
            pressureViewModel = ViewModelProviders.of(getActivity()).get(PressureViewModel.class);
            locationViewModel = ViewModelProviders.of(getActivity()).get(LocationViewModel.class);
        }

        getActivity().getLifecycle().addObserver(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment.
        view = inflater.inflate(R.layout.fragment_jump, container, false);

        // Add click listener for fragment view.
        Button calibrateButton = view.findViewById(R.id.calibrate_button);
        calibrateButton.setOnClickListener(this::onClickCalibrate);

        // Subscribe to data changes
        subscribeToPressure();

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
        final Observer<Float> altitudeObserver = altitude -> {
            // Update altitude text
            if (altitude != null) {
                updatePressureAltitude(Util.metresToFeet(altitude), Util.Unit.IMPERIAL);
            }
        };

        pressureViewModel.getLastAltitude().observe(this, altitudeObserver);
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

    public void updatePressureAltitude(Float altitude, Unit unit) {
        Log.v(TAG, "Updating pressure altitude text.");
        TextView text = view.findViewById(R.id.pressure_altitude);
        text.setText(getAltitudeText(altitude, unit));
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    public void startListening() {
        MainActivity main = (MainActivity) getActivity();
        PermissionManager permissionManager = main.getPermissionManager();
        pressureViewModel.startListening();
        if (permissionManager.checkLocationPermission()) {
            locationViewModel.startListening();
        } else {
            String reason = "Location access is required to track your jump location.";
            permissionManager.requestLocationPermission(reason);
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    public void stopListening() {
        pressureViewModel.stopListening();
        locationViewModel.stopListening();
    }
}

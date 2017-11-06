package me.chrislane.altimeter;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;
import me.chrislane.altimeter.fragment.AltitudeFragment;
import me.chrislane.altimeter.viewmodel.LocationViewModel;
import me.chrislane.altimeter.viewmodel.PressureViewModel;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Check that the device has a barometer.
        SensorManager sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE) == null) {
            // No barometer, do not continue.
            Toast.makeText(this, "No barometer in device.", Toast.LENGTH_SHORT).show();
            //return;
        }

        // Create or get ViewModels.
        ViewModelProviders.of(this).get(PressureViewModel.class);
        ViewModelProviders.of(this).get(LocationViewModel.class);

        // Set the fragment view, passing whether the fragment should exist or not.
        setFragment(savedInstanceState != null);
    }

    /**
     * Set the altitude fragment.
     *
     * @param exists Whether an instance of the fragment exists already.
     */
    private void setFragment(Boolean exists) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment fragment;

        if (exists) {
            fragment = fragmentManager.findFragmentByTag(AltitudeFragment.TAG);
        } else {
            fragment = new AltitudeFragment();
        }

        fragmentManager.beginTransaction()
                .replace(R.id.frame, fragment, AltitudeFragment.TAG)
                .commit();
    }
}

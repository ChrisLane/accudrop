package me.chrislane.accudrop.listener;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.location.Location;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import java.util.Date;
import java.util.Locale;

import me.chrislane.accudrop.db.AccudropDb;
import me.chrislane.accudrop.db.Position;
import me.chrislane.accudrop.viewmodel.JumpViewModel;
import me.chrislane.accudrop.viewmodel.LocationViewModel;
import me.chrislane.accudrop.viewmodel.PressureViewModel;

public class ReadingListener {

    private static final String TAG = ReadingListener.class.getSimpleName();
    private final LocationViewModel locationViewModel;
    private final AccudropDb db;
    private final AppCompatActivity activity;
    private final PressureViewModel pressureViewModel;
    private final JumpViewModel jumpViewModel;
    private boolean logging = false;

    public ReadingListener(AppCompatActivity activity) {
        this.activity = activity;
        db = AccudropDb.getDatabase(activity);
        pressureViewModel = ViewModelProviders.of(activity).get(PressureViewModel.class);
        locationViewModel = ViewModelProviders.of(activity).get(LocationViewModel.class);
        jumpViewModel = ViewModelProviders.of(activity).get(JumpViewModel.class);

        subscribeToLocation();
        subscribeToAltitude();
    }

    /**
     * Subscribe to altitude changes.
     */
    private void subscribeToAltitude() {
        final Observer<Float> altitudeObserver = altitude -> {
            // Add entry to db
            if (altitude != null && logging) {
                Location location = locationViewModel.getLastLocation().getValue();
                Integer jumpId = jumpViewModel.getLastJumpId().getValue();
                if (location != null && jumpId != null) {
                    addPositionToDb(jumpId, location, altitude);
                }
            }
        };

        pressureViewModel.getLastAltitude().observe(activity, altitudeObserver);

    }

    private void subscribeToLocation() {
        final Observer<Location> locationObserver = location -> {
            // Add entry to db
            if (location != null && logging) {
                Float altitude = pressureViewModel.getLastAltitude().getValue();
                Integer jumpId = jumpViewModel.getLastJumpId().getValue();
                if (altitude != null && jumpId != null) {
                    addPositionToDb(jumpId, location, altitude);
                }
            }
        };

        locationViewModel.getLastLocation().observe(activity, locationObserver);
    }

    private void addPositionToDb(Integer jumpId, Location location, Float altitude) {
        Position pos = new Position();
        pos.latitude = location.getLatitude();
        pos.longitude = location.getLongitude();
        pos.altitude = altitude.intValue();
        pos.time = new Date();
        pos.jumpId = jumpId;

        String msg = String.format(Locale.ENGLISH, "Inserting position:\n" +
                "\tJump ID: %d" +
                "\t(Lat, Long): (%f,%f)\n" +
                "\tAltitude: %d\n" +
                "\tTime: %s", pos.jumpId, pos.latitude, pos.longitude, pos.altitude, pos.time);
        Log.d(TAG, msg);

        AsyncTask.execute(() -> db.locationModel().insertPosition(pos));
    }

    public void enableLogging() {
        logging = true;
    }

    public void disableLogging() {
        logging = false;
    }
}

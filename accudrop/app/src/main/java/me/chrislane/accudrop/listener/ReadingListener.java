package me.chrislane.accudrop.listener;

import android.arch.lifecycle.Observer;
import android.location.Location;
import android.os.AsyncTask;
import android.util.Log;

import java.util.Date;
import java.util.Locale;

import me.chrislane.accudrop.db.Position;
import me.chrislane.accudrop.viewmodel.GnssViewModel;
import me.chrislane.accudrop.viewmodel.JumpViewModel;
import me.chrislane.accudrop.viewmodel.PressureViewModel;

public class ReadingListener {

    private static final String TAG = ReadingListener.class.getSimpleName();
    private final GnssViewModel gnssViewModel;
    private final PressureViewModel pressureViewModel;
    private final JumpViewModel jumpViewModel;
    private boolean logging = false;
    private Integer jumpId;

    public ReadingListener(GnssViewModel gnssViewModel, PressureViewModel pressureViewModel,
                           JumpViewModel jumpViewModel) {
        this.pressureViewModel = pressureViewModel;
        this.gnssViewModel = gnssViewModel;
        this.jumpViewModel = jumpViewModel;

        subscribeToJumpId();
        subscribeToLocation();
        subscribeToAltitude();
    }

    /**
     * Subscribe to the latest jump ID and store it in this object.
     */
    private void subscribeToJumpId() {
        final Observer<Integer> jumpIdObserver = jumpId -> {
            if (jumpId != null) {
                this.jumpId = jumpId;
            }
        };

        jumpViewModel.findLastJumpId().observeForever(jumpIdObserver);
    }

    /**
     * Subscribe to altitude changes.
     */
    private void subscribeToAltitude() {
        final Observer<Float> altitudeObserver = altitude -> {
            // Add entry to db
            if (altitude != null && logging) {
                Location location = gnssViewModel.getLastLocation().getValue();
                if (location != null && jumpId != null) {
                    addPositionToDb(jumpId, location, altitude);
                }
            }
        };

        pressureViewModel.getLastAltitude().observeForever(altitudeObserver);
    }

    /**
     * Subscribe to location changes and add positions to the database.
     */
    private void subscribeToLocation() {
        final Observer<Location> locationObserver = location -> {
            // Add entry to db
            if (location != null && logging) {
                Float altitude = pressureViewModel.getLastAltitude().getValue();
                if (altitude != null && jumpId != null) {
                    addPositionToDb(jumpId, location, altitude);
                }
            }
        };

        gnssViewModel.getLastLocation().observeForever(locationObserver);
    }

    /**
     * Add a new position to the database.
     *
     * @param jumpId   The jump id for the position.
     * @param location The location of the position.
     * @param altitude The altitude of the position.
     */
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

        AsyncTask.execute(() -> jumpViewModel.addPosition(pos));
    }

    /**
     * Enable position logging.
     */
    public void enableLogging() {
        logging = true;
    }

    /**
     * Disable position logging.
     */
    public void disableLogging() {
        logging = false;
    }
}

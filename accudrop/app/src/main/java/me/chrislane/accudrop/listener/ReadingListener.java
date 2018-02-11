package me.chrislane.accudrop.listener;

import android.arch.lifecycle.Observer;
import android.content.Context;
import android.content.SharedPreferences;
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
    private Float prevAlt;
    private Long prevTime;

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
     * <p>Subscribe to altitude changes.</p>
     * <p>This handles checks for whether logging should be enabled/disabled and adding
     * position entries to the database.</p>
     */
    private void subscribeToAltitude() {
        pressureViewModel.getLastAltitude().observeForever(this::checkAltitude);
    }

    /**
     * Checks and actions to be performed on new altitude data.
     *
     * @param altitude The new altitude data.
     */
    private void checkAltitude(Float altitude) {
        // Exit if no altitude is given
        if (altitude == null) {
            return;
        }

        // Should we start logging?
        // Check we have an altitude and aren't already logging.
        if (!logging) {
            if (hasReachedSpeed(altitude, 20)) {
                enableLogging();
            }
        } else {
            // Add entry to the db.
            Location location = gnssViewModel.getLastLocation().getValue();
            Float speed = getFallRate(altitude);
            if (location != null && jumpId != null) {
                if (speed != null) {
                    location.setSpeed(speed);
                }
                addPositionToDb(jumpId, location, altitude);
            }

            // Should we stop logging?
            if (altitude < 5) {
                disableLogging();
            }
        }
    }

    /**
     * <p>Get the fall rate of the user.</p>
     * <p>This depends on the method being called twice to get time periods between altitudes.</p>
     *
     * @param altitude The current altitude of the user.
     * @return The fall rate of the user.
     */
    private Float getFallRate(float altitude) {
        long now = new Date().getTime();
        Float speed = null;

        // Check if this is our first run
        if (prevAlt != null && prevTime != null) {
            long period = now - prevTime;
            float distance = prevAlt - altitude; // Distance in metres
            speed = distance / period; // Speed in m/s
        }

        prevTime = now;
        prevAlt = altitude;

        return speed;
    }

    /**
     * <p>Check if the user has reached at least a certain speed.</p>
     * <p>This check depends on the speed of the user having been previously checked.</p>
     *
     * @param altitude The current altitude.
     * @param minSpeed The minimum speed.
     * @return If the user has reached at least the minimum speed.
     */
    private boolean hasReachedSpeed(Float altitude, int minSpeed) {
        Float speed = getFallRate(altitude);
        return speed != null && speed >= minSpeed;
    }

    /**
     * Subscribe to location changes and add positions to the database.
     */
    private void subscribeToLocation() {
        gnssViewModel.getLastLocation().observeForever(this::checkLocation);
    }

    /**
     * Checks and actions performed on new location data.
     *
     * @param location The new location data.
     */
    private void checkLocation(Location location) {
        // Add entry to db
        if (location != null && logging) {
            Float altitude = pressureViewModel.getLastAltitude().getValue();
            if (altitude != null && jumpId != null) {
                addPositionToDb(jumpId, location, altitude);
            }
        }
    }

    /**
     * Add a new position to the database.
     *
     * @param jumpId   The jump id for the position.
     * @param location The location of the position.
     * @param altitude The altitude of the position.
     */
    private void addPositionToDb(Integer jumpId, Location location, Float altitude) {
        SharedPreferences settings = jumpViewModel.getApplication()
                .getSharedPreferences("userInfo", Context.MODE_PRIVATE);
        String uuid = settings.getString("userUUID", "");

        Position pos = new Position();
        pos.latitude = location.getLatitude();
        pos.longitude = location.getLongitude();
        pos.altitude = altitude.intValue();
        pos.time = new Date();
        pos.jumpId = jumpId;
        pos.useruuid = uuid;

        String msg = String.format(Locale.ENGLISH, "Inserting position:\n" +
                        "\tUser UUID: %s\n" +
                        "\tJump ID: %d\n" +
                        "\t(Lat, Long): (%f,%f)\n" +
                        "\tAltitude: %d\n" +
                        "\tTime: %s",
                pos.useruuid, pos.jumpId, pos.latitude, pos.longitude, pos.altitude, pos.time);
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

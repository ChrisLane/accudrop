package me.chrislane.accudrop.listener;

import android.arch.lifecycle.Observer;
import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.Date;
import java.util.Locale;

import me.chrislane.accudrop.BuildConfig;
import me.chrislane.accudrop.db.FallType;
import me.chrislane.accudrop.db.Position;
import me.chrislane.accudrop.network.CoordSender;
import me.chrislane.accudrop.viewmodel.DatabaseViewModel;
import me.chrislane.accudrop.viewmodel.GnssViewModel;
import me.chrislane.accudrop.viewmodel.PressureViewModel;

public class ReadingListener {

    private static final String TAG = ReadingListener.class.getSimpleName();
    private final GnssViewModel gnssViewModel;
    private final PressureViewModel pressureViewModel;
    private final DatabaseViewModel databaseViewModel;
    private final boolean isGuidanceEnabled;
    private boolean logging = false;
    private CoordSender coordSender;
    private Integer jumpId;
    private Float prevAlt;
    private Long prevTime;
    private Double vSpeed;
    private boolean hasFreefallen = false;
    private boolean isUnderCanopy = false;
    private int fallToggle = 20;
    private int canopyToggle = 15;

    public ReadingListener(GnssViewModel gnssViewModel, PressureViewModel pressureViewModel,
                           DatabaseViewModel databaseViewModel) {
        this.pressureViewModel = pressureViewModel;
        this.gnssViewModel = gnssViewModel;
        this.databaseViewModel = databaseViewModel;

        SharedPreferences preferences = PreferenceManager
                .getDefaultSharedPreferences(databaseViewModel.getApplication());
        isGuidanceEnabled = preferences.getBoolean("guidance_enabled", false);

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

        databaseViewModel.findLastJumpId().observeForever(jumpIdObserver);
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
            //if (hasReachedSpeed(altitude, 20)) {
            if (altitude >= 600) {
                enableLogging();
            }
        } else {
            // Add entry to the db.
            Location location = gnssViewModel.getLastLocation().getValue();
            vSpeed = getFallRate(altitude);

            // Decide the fall type
            if (vSpeed != null) {
                if (vSpeed > fallToggle && !hasFreefallen) {
                    // TODO: Set this boolean value when enabling logging
                    hasFreefallen = true;
                } else if (vSpeed < canopyToggle && hasFreefallen) {
                    isUnderCanopy = true;
                }
            }

            // Add the position to the database
            if (jumpId != null) {
                addPositionToDb(jumpId, location, altitude, vSpeed);
            }

            if (isGuidanceEnabled) {
                if (coordSender != null && location != null) {
                    // TODO: Only send new location after a set distance moved
                    String send = String.format(Locale.ENGLISH, "%f %f %f", location.getLatitude(),
                            location.getLongitude(), altitude);
                    coordSender.write(send.getBytes());
                }
            }

            // Should we stop logging?
            if (altitude < 3) {
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
    @Nullable
    private synchronized Double getFallRate(float altitude) {
        long now = new Date().getTime();
        Double speed = null;

        // Check if this is our first run
        if (prevAlt != null && prevTime != null) {
            double period = (now - prevTime) * 0.001; // Period in seconds
            float distance = prevAlt - altitude; // Distance in metres
            speed = distance / period; // Speed in m/s
        }

        prevTime = now;
        prevAlt = altitude;

        if (BuildConfig.DEBUG) {
            Log.v(TAG, "Fall Rate: " + speed + "m/s");
        }
        return speed;
    }

    /**
     * <p>Check if the user has reached at least a certain vSpeed.</p>
     * <p>This check depends on the vSpeed of the user having been previously checked.</p>
     *
     * @param altitude The current altitude.
     * @param minSpeed The minimum vSpeed.
     * @return If the user has reached at least the minimum vSpeed.
     */
    private boolean hasReachedSpeed(Float altitude, double minSpeed) {
        Double speed = getFallRate(altitude);
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
            if (jumpId != null) {
                addPositionToDb(jumpId, location, altitude, vSpeed);
            }

            if (isGuidanceEnabled) {
                if (coordSender != null && altitude != null) {
                    // TODO: Only send new location after a set distance moved
                    String send = String.format(Locale.ENGLISH, "%f %f %f", location.getLatitude(),
                            location.getLongitude(), altitude);
                    coordSender.write(send.getBytes());
                }
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
    private void addPositionToDb(Integer jumpId, Location location, Float altitude, Double vSpeed) {
        SharedPreferences settings = databaseViewModel.getApplication()
                .getSharedPreferences("userInfo", Context.MODE_PRIVATE);
        String uuid = settings.getString("userUUID", "");

        Position pos = new Position();
        pos.latitude = location != null ? location.getLatitude() : null;
        pos.longitude = location != null ? location.getLongitude() : null;
        pos.hspeed = location != null ? location.getSpeed() : null;
        pos.vspeed = vSpeed;
        pos.altitude = altitude != null ? altitude.intValue() : null;
        pos.time = new Date();
        pos.jumpId = jumpId;
        pos.useruuid = uuid;

        if (isUnderCanopy) {
            pos.fallType = FallType.CANOPY;
        } else {
            pos.fallType = FallType.FREEFALL;
        }

        String msg = String.format(Locale.ENGLISH, "Inserting position:%n" +
                        "\tUser UUID: %s%n" +
                        "\tJump ID: %d%n" +
                        "\t(Lat, Long): (%f,%f)%n" +
                        "\tAltitude: %d%n" +
                        "\tTime: %s%n" +
                        "\tHorizontal Speed: %f%n" +
                        "\tVertical Speed: %f",
                pos.useruuid, pos.jumpId, pos.latitude, pos.longitude, pos.altitude, pos.time,
                pos.hspeed, pos.vspeed);
        Log.v(TAG, msg);

        AsyncTask.execute(() -> databaseViewModel.addPosition(pos));
    }

    /**
     * Enable position logging.
     */
    public void enableLogging() {
        logging = true;
        Log.i(TAG, "Logging enabled.");
    }

    /**
     * Disable position logging.
     */
    public void disableLogging() {
        logging = false;
        Log.i(TAG, "Logging disabled.");
    }

    public void setCoordSender(CoordSender coordSender) {
        this.coordSender = coordSender;
    }
}

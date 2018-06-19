package me.chrislane.accudrop.task;

import android.location.Location;
import android.os.AsyncTask;
import android.util.Log;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import me.chrislane.accudrop.BuildConfig;
import me.chrislane.accudrop.db.FallType;
import me.chrislane.accudrop.db.Position;
import me.chrislane.accudrop.viewmodel.DatabaseViewModel;

public class AddGeneratedPositionsTask extends AsyncTask<Void, Void, Void> {

    private static final String TAG = AddGeneratedPositionsTask.class.getSimpleName();
    private final DatabaseViewModel databaseViewModel;
    private final int jumpId;
    private final UUID uuid;
    private final List<Location> route;

    public AddGeneratedPositionsTask(int jumpId, UUID uuid, List<Location> route,
                                     DatabaseViewModel databaseViewModel) {
        this.jumpId = jumpId;
        this.uuid = uuid;
        this.route = route;
        this.databaseViewModel = databaseViewModel;
    }

    private static Double getFallRate(float newAlti, Long newTime, Double prevAlt, Long prevTime) {
        Double speed = null;

        // Check if this is our first run
        if (prevAlt != null && prevTime != null) {
            double period = (newTime - prevTime) * 0.001; // Period in seconds
            double distance = prevAlt - newAlti; // Distance in metres
            speed = distance / period; // Speed in m/s
        }

        if (BuildConfig.DEBUG) {
            Log.v(TAG, "Fall Rate: " + speed + "m/s");
        }
        return speed;
    }

    /**
     * Add positions in a route to a jump.
     */
    private void addPositions() {
        // Set time to be equal for all jumpers
        Date date = new Date();
        date.setTime(0L);

        // Add "Freefall" data (shift landing pattern up in altitude)
        for (int i = 0; i < route.size(); i++) {
            Location location = route.get(i);
            Position pos = new Position();
            pos.latitude = location.getLatitude();
            pos.longitude = location.getLongitude();
            pos.altitude = (int) location.getAltitude() + 1000;
            pos.time = (Date) date.clone();
            //pos.time = new Date();
            pos.jumpId = jumpId;
            pos.useruuid = uuid.toString();
            pos.vspeed = 54.0;
            pos.hspeed = 4f;
            pos.fallType = FallType.FREEFALL;

            databaseViewModel.addPosition(pos);

            // Increment time by a second for next position
            date.setTime(date.getTime() + 450L);
        }

        // Add canopy data
        for (int i = 0; i < route.size(); i++) {
            Location location = route.get(i);
            Position pos = new Position();
            pos.latitude = location.getLatitude();
            pos.longitude = location.getLongitude();
            pos.altitude = (int) location.getAltitude();
            pos.time = (Date) date.clone();
            //pos.time = new Date();
            pos.jumpId = jumpId;
            pos.useruuid = uuid.toString();
            pos.vspeed = 15.4 / 2.5;
            pos.hspeed = 15.4f + 2f;
            pos.fallType = FallType.CANOPY;

            databaseViewModel.addPosition(pos);

            // Increment time by a second for next position
            date.setTime(date.getTime() + 1000L);
        }
    }

    @Override
    protected Void doInBackground(Void... voids) {
        addPositions();
        return null;
    }
}

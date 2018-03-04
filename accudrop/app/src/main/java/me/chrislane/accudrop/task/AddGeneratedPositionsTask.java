package me.chrislane.accudrop.task;

import android.location.Location;
import android.os.AsyncTask;

import java.util.Date;
import java.util.List;
import java.util.UUID;

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

    /**
     * Add positions in a route to a jump.
     */
    private void addPositions() {
        // Set time to be equal for all jumpers
        Date date = new Date();
        date.setTime(0L);

        for (Location location : route) {
            Position pos = new Position();
            pos.latitude = location.getLatitude();
            pos.longitude = location.getLongitude();
            pos.altitude = (int) location.getAltitude();
            pos.time = (Date) date.clone();
            //pos.time = new Date();
            pos.jumpId = jumpId;
            pos.useruuid = uuid.toString();

            /*String msg = String.format(Locale.ENGLISH, "Inserting position:%n" +
                            "\tUser UUID: %s%n" +
                            "\tJump ID: %d%n" +
                            "\t(Lat, Long): (%f,%f)%n" +
                            "\tAltitude: %d%n" +
                            "\tTime: %s",
                    pos.useruuid, pos.jumpId, pos.latitude, pos.longitude, pos.altitude, pos.time);
            Log.v(TAG, msg);*/

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

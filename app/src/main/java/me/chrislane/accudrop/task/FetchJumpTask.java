package me.chrislane.accudrop.task;

import android.location.Location;
import android.os.AsyncTask;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import me.chrislane.accudrop.db.Position;
import me.chrislane.accudrop.viewmodel.DatabaseViewModel;

/**
 * Get the latest jump.
 */
public class FetchJumpTask extends AsyncTask<Integer, Void, List<Location>> {

    private static final String TAG = FetchJumpTask.class.getSimpleName();
    private final DatabaseViewModel databaseViewModel;
    private final FetchJumpListener listener;

    public FetchJumpTask(FetchJumpListener listener, DatabaseViewModel databaseViewModel) {
        this.listener = listener;
        this.databaseViewModel = databaseViewModel;
    }

    @Override
    protected List<Location> doInBackground(Integer... integers) {
        Integer jumpNumber;
        if (integers.length > 0) {
            jumpNumber = integers[0];
            Log.d(TAG, "Fetching jump " + jumpNumber);
        } else {
            jumpNumber = databaseViewModel.getLastJumpId();
            Log.d(TAG, "Fetching last jump (" + jumpNumber + ")");
        }

        List<Location> locations = new ArrayList<>();
        if (jumpNumber != null) {
            List<Position> positions = databaseViewModel.getPositionsForJump(jumpNumber);
            for (Position position : positions) {
                if (position.latitude != null && position.longitude != null &&
                        position.altitude != null) {
                    Location location = new Location("");
                    location.setLatitude(position.latitude);
                    location.setLongitude(position.longitude);
                    location.setAltitude(position.altitude);
                    locations.add(location);

                    Log.v(TAG, location.toString());
                }
            }
        } else {
            Log.e(TAG, "No last jump id found.");
        }

        return locations;
    }

    @Override
    protected void onPostExecute(List<Location> locations) {
        super.onPostExecute(locations);

        Log.d(TAG, "Finished getting jump data.");
        listener.onFinished(locations);
    }

    public interface FetchJumpListener {
        void onFinished(List<Location> locations);
    }
}

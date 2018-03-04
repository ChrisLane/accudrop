package me.chrislane.accudrop.task;

import android.os.AsyncTask;
import android.util.Pair;

import me.chrislane.accudrop.viewmodel.DatabaseViewModel;

/**
 * Get the minimum and maximum latitude or longitude for the latest jump.
 */
public class MinMaxLatLngTask extends AsyncTask<Void, Void, Pair<Double, Double>> {

    private final Listener listener;
    private final DatabaseViewModel databaseViewModel;
    private final boolean getLatitude;

    public MinMaxLatLngTask(Listener listener, DatabaseViewModel databaseViewModel, boolean getLatitude) {
        this.listener = listener;
        this.databaseViewModel = databaseViewModel;
        this.getLatitude = getLatitude;
    }

    @Override
    protected Pair<Double, Double> doInBackground(Void... aVoid) {
        Integer jumpId = databaseViewModel.getLastJumpId();
        if (jumpId != null) {
            Double min, max;
            if (getLatitude) {
                min = databaseViewModel.getMinLatitudeForJump(jumpId);
                max = databaseViewModel.getMaxLatitudeForJump(jumpId);
            } else {
                min = databaseViewModel.getMinLongitudeForJump(jumpId);
                max = databaseViewModel.getMaxLongitudeForJump(jumpId);
            }

            if (min != null && max != null) {
                return new Pair<>(min, max);
            }
        }

        return null;
    }

    @Override
    protected void onPostExecute(Pair<Double, Double> result) {
        super.onPostExecute(result);

        if (result != null) {
            listener.onFinished(result.first, result.second);
        }
    }

    public interface Listener {
        void onFinished(double min, double max);
    }
}

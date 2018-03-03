package me.chrislane.accudrop.task;

import android.os.AsyncTask;
import android.util.Pair;

import me.chrislane.accudrop.viewmodel.JumpViewModel;

/**
 * Get the minimum and maximum latitude or longitude for the latest jump.
 */
public class MinMaxLatLngTask extends AsyncTask<Void, Void, Pair<Double, Double>> {

    private final Listener listener;
    private final JumpViewModel jumpViewModel;
    private final boolean getLatitude;

    public MinMaxLatLngTask(Listener listener, JumpViewModel jumpViewModel, boolean getLatitude) {
        this.listener = listener;
        this.jumpViewModel = jumpViewModel;
        this.getLatitude = getLatitude;
    }

    @Override
    protected Pair<Double, Double> doInBackground(Void... aVoid) {
        Integer jumpId = jumpViewModel.getLastJumpId();
        if (jumpId != null) {
            Double min, max;
            if (getLatitude) {
                min = jumpViewModel.getMinLatitudeForJump(jumpId);
                max = jumpViewModel.getMaxLatitudeForJump(jumpId);
            } else {
                min = jumpViewModel.getMinLongitudeForJump(jumpId);
                max = jumpViewModel.getMaxLongitudeForJump(jumpId);
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

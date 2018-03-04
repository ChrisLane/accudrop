package me.chrislane.accudrop.task;

import android.os.AsyncTask;
import android.util.Pair;

import me.chrislane.accudrop.viewmodel.DatabaseViewModel;

/**
 * Get the minimum and maximum altitude for the latest jump.
 */
public class MinMaxAltiTask extends AsyncTask<Void, Void, Pair<Integer, Integer>> {

    private final MinMaxAltiTask.Listener listener;
    private final DatabaseViewModel databaseViewModel;

    public MinMaxAltiTask(MinMaxAltiTask.Listener listener, DatabaseViewModel databaseViewModel) {
        this.listener = listener;
        this.databaseViewModel = databaseViewModel;
    }

    @Override
    protected Pair<Integer, Integer> doInBackground(Void... aVoid) {
        Integer jumpId = databaseViewModel.getLastJumpId();
        if (jumpId != null) {
            Integer min = databaseViewModel.getMinAltitudeForJump(jumpId);
            Integer max = databaseViewModel.getMaxAltitudeForJump(jumpId);

            if (min != null && max != null) {
                return new Pair<>(min, max);
            }
        }

        return null;
    }

    @Override
    protected void onPostExecute(Pair<Integer, Integer> result) {
        super.onPostExecute(result);

        if (result != null) {
            listener.onFinished(result.first, result.second);
        }
    }

    public interface Listener {
        void onFinished(int min, int max);
    }
}

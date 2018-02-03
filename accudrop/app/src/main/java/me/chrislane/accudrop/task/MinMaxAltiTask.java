package me.chrislane.accudrop.task;

import android.os.AsyncTask;

import me.chrislane.accudrop.viewmodel.JumpViewModel;

/**
 * Get the minimum and maximum altitude for the latest jump.
 */
public class MinMaxAltiTask extends AsyncTask<Void, Void, MinMaxAltiTask.Result> {

    private final MinMaxAltiTask.Listener listener;
    private final JumpViewModel jumpViewModel;

    public MinMaxAltiTask(MinMaxAltiTask.Listener listener, JumpViewModel jumpViewModel) {
        this.listener = listener;
        this.jumpViewModel = jumpViewModel;
    }

    @Override
    protected MinMaxAltiTask.Result doInBackground(Void... aVoid) {
        Integer jumpId = jumpViewModel.getLastJumpId();
        if (jumpId != null) {
            Integer min = jumpViewModel.getMinAltitudeForJump(jumpId);
            Integer max = jumpViewModel.getMaxAltitudeForJump(jumpId);

            if (min != null && max != null) {
                return new MinMaxAltiTask.Result(min, max);
            }
        }

        return null;
    }

    @Override
    protected void onPostExecute(MinMaxAltiTask.Result result) {
        super.onPostExecute(result);

        if (result != null) {
            listener.onFinished(result.min, result.max);
        }
    }

    public interface Listener {
        void onFinished(int min, int max);
    }

    class Result {
        final int min;
        final int max;

        Result(int min, int max) {
            this.min = min;
            this.max = max;
        }
    }
}

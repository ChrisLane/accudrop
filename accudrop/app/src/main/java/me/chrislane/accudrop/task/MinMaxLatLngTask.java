package me.chrislane.accudrop.task;

import android.os.AsyncTask;

import me.chrislane.accudrop.viewmodel.JumpViewModel;

public class MinMaxLatLngTask extends AsyncTask<Void, Void, MinMaxLatLngTask.Result> {
    private final Listener listener;
    private final JumpViewModel jumpViewModel;
    private final boolean getLatitude;

    class Result {
        final double min;
        final double max;

        Result(double min, double max) {
            this.min = min;
            this.max = max;
        }
    }

    public interface Listener {
        void onFinished(double min, double max);
    }

    public MinMaxLatLngTask(Listener listener, JumpViewModel jumpViewModel, boolean getLatitude) {
        this.listener = listener;
        this.jumpViewModel = jumpViewModel;
        this.getLatitude = getLatitude;
    }

    @Override
    protected Result doInBackground(Void... aVoid) {
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
                return new Result(min, max);
            }
        }

        return null;
    }

    @Override
    protected void onPostExecute(Result result) {
        super.onPostExecute(result);

        if (result != null) {
            listener.onFinished(result.min, result.max);
        }
    }
}

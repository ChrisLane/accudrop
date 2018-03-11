package me.chrislane.accudrop.task;

import android.os.AsyncTask;

import java.util.UUID;

import me.chrislane.accudrop.db.FallType;
import me.chrislane.accudrop.viewmodel.DatabaseViewModel;


public class FetchFallTypeMaxVSpeed extends AsyncTask<Integer, Void, Double> {

    private final DatabaseViewModel viewModel;
    private final FetchFallTypeMaxVSpeed.Listener listener;
    private final UUID uuid;
    private final FallType fallType;

    public FetchFallTypeMaxVSpeed(FetchFallTypeMaxVSpeed.Listener listener, FallType fallType,
                                 UUID uuid, DatabaseViewModel viewModel) {
        this.listener = listener;
        this.fallType = fallType;
        this.uuid = uuid;
        this.viewModel = viewModel;
    }

    @Override
    protected Double doInBackground(Integer... integers) {
        Integer jumpId = integers[0];

        if (jumpId == null) {
            return null;
        }

        return viewModel.getMaxVSpeedOfFallType(fallType, uuid, jumpId);
    }

    @Override
    protected void onPostExecute(Double vSpeed) {
        super.onPostExecute(vSpeed);

        listener.onFinished(vSpeed);
    }

    public interface Listener {
        void onFinished(Double vSpeed);
    }
}

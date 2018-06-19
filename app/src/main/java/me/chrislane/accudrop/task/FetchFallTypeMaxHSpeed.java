package me.chrislane.accudrop.task;

import android.os.AsyncTask;

import java.util.UUID;

import me.chrislane.accudrop.db.FallType;
import me.chrislane.accudrop.viewmodel.DatabaseViewModel;


public class FetchFallTypeMaxHSpeed extends AsyncTask<Integer, Void, Float> {

    private final DatabaseViewModel viewModel;
    private final FetchFallTypeMaxHSpeed.Listener listener;
    private final UUID uuid;
    private final FallType fallType;

    public FetchFallTypeMaxHSpeed(FetchFallTypeMaxHSpeed.Listener listener, FallType fallType,
                                  UUID uuid, DatabaseViewModel viewModel) {
        this.listener = listener;
        this.fallType = fallType;
        this.uuid = uuid;
        this.viewModel = viewModel;
    }

    @Override
    protected Float doInBackground(Integer... integers) {
        Integer jumpId = integers[0];

        if (jumpId == null) {
            return null;
        }

        return viewModel.getMaxHSpeedOfFallType(fallType, uuid, jumpId);
    }

    @Override
    protected void onPostExecute(Float hSpeed) {
        super.onPostExecute(hSpeed);

        listener.onFinished(hSpeed);
    }

    public interface Listener {
        void onFinished(Float hSpeed);
    }
}

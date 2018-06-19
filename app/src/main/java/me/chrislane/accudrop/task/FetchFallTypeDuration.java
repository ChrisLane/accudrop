package me.chrislane.accudrop.task;

import android.os.AsyncTask;

import java.util.Date;
import java.util.UUID;

import me.chrislane.accudrop.db.FallType;
import me.chrislane.accudrop.viewmodel.DatabaseViewModel;


public class FetchFallTypeDuration extends AsyncTask<Integer, Void, Long> {

    private final DatabaseViewModel viewModel;
    private final Listener listener;
    private final UUID uuid;
    private final FallType fallType;

    public FetchFallTypeDuration(FetchFallTypeDuration.Listener listener, FallType fallType,
                                 UUID uuid, DatabaseViewModel viewModel) {
        this.listener = listener;
        this.fallType = fallType;
        this.uuid = uuid;
        this.viewModel = viewModel;
    }

    @Override
    protected Long doInBackground(Integer... integers) {
        Integer jumpId = integers[0];

        if (jumpId == null) {
            return null;
        }

        Date first = viewModel.getFirstDateOfFallType(fallType, uuid, jumpId);
        Date last = viewModel.getLastDateOfFallType(fallType, uuid, jumpId);

        if (first == null || last == null) {
            return null;
        }

        return last.getTime() - first.getTime();
    }

    @Override
    protected void onPostExecute(Long millis) {
        super.onPostExecute(millis);

        listener.onFinished(millis);
    }

    public interface Listener {
        void onFinished(Long millis);
    }
}

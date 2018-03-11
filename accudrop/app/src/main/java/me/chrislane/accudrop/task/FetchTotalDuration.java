package me.chrislane.accudrop.task;

import android.os.AsyncTask;

import java.util.Date;
import java.util.UUID;

import me.chrislane.accudrop.viewmodel.DatabaseViewModel;


public class FetchTotalDuration extends AsyncTask<Integer, Void, Long> {

    private final DatabaseViewModel viewModel;
    private final Listener listener;
    private final UUID uuid;

    public FetchTotalDuration(Listener listener, UUID uuid, DatabaseViewModel viewModel) {
        this.listener = listener;
        this.uuid = uuid;
        this.viewModel = viewModel;
    }

    @Override
    protected Long doInBackground(Integer... integers) {
        Integer jumpId = integers[0];

        if (jumpId == null) {
            return null;
        }

        Date first = viewModel.getFirstDate(uuid, jumpId);
        Date last = viewModel.getLastDate(uuid, jumpId);

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

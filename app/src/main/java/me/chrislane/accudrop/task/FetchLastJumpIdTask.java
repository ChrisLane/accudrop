package me.chrislane.accudrop.task;

import android.os.AsyncTask;

import me.chrislane.accudrop.viewmodel.DatabaseViewModel;

public class FetchLastJumpIdTask extends AsyncTask<Void, Void, Integer> {

    private final DatabaseViewModel databaseViewModel;
    private final Listener listener;

    public FetchLastJumpIdTask(FetchLastJumpIdTask.Listener listener, DatabaseViewModel databaseViewModel) {
        this.listener = listener;
        this.databaseViewModel = databaseViewModel;
    }

    @Override
    protected Integer doInBackground(Void... voids) {
        return databaseViewModel.getLastJumpId();
    }

    @Override
    protected void onPostExecute(Integer jumpId) {
        super.onPostExecute(jumpId);
        listener.onFinished(jumpId);
    }

    public interface Listener {
        void onFinished(Integer jumpId);
    }
}

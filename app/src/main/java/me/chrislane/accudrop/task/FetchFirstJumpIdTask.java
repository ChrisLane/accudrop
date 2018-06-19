package me.chrislane.accudrop.task;

import android.os.AsyncTask;

import me.chrislane.accudrop.viewmodel.DatabaseViewModel;


public class FetchFirstJumpIdTask extends AsyncTask<Void, Void, Integer> {

    private final DatabaseViewModel databaseViewModel;
    private final FetchFirstJumpIdTask.Listener listener;

    public FetchFirstJumpIdTask(FetchFirstJumpIdTask.Listener listener, DatabaseViewModel databaseViewModel) {
        this.listener = listener;
        this.databaseViewModel = databaseViewModel;
    }

    @Override
    protected Integer doInBackground(Void... voids) {
        return databaseViewModel.getFirstJumpId();
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

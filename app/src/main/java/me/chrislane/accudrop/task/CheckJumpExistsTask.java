package me.chrislane.accudrop.task;

import android.os.AsyncTask;

import me.chrislane.accudrop.viewmodel.DatabaseViewModel;

public class CheckJumpExistsTask extends AsyncTask<Integer, Void, Boolean> {

    private final Listener listener;
    private final DatabaseViewModel databaseViewModel;

    public CheckJumpExistsTask(Listener listener, DatabaseViewModel databaseViewModel) {
        this.listener = listener;
        this.databaseViewModel = databaseViewModel;
    }

    @Override
    protected Boolean doInBackground(Integer... integers) {
        return databaseViewModel.jumpExists(integers[0]);
    }

    @Override
    protected void onPostExecute(Boolean jumpExists) {
        super.onPostExecute(jumpExists);

        if (jumpExists == null) {
            listener.onFinished(false);
        } else {
            listener.onFinished(jumpExists);
        }
    }

    public interface Listener {
        void onFinished(Boolean jumpExists);
    }
}

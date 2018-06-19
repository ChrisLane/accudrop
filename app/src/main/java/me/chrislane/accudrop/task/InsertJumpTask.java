package me.chrislane.accudrop.task;

import android.os.AsyncTask;

import me.chrislane.accudrop.db.Jump;
import me.chrislane.accudrop.viewmodel.DatabaseViewModel;

/**
 * Insert a new jump into the database.
 */
public class InsertJumpTask extends AsyncTask<Jump, Void, Void> {

    private final DatabaseViewModel databaseViewModel;
    private final Listener listener;


    InsertJumpTask(DatabaseViewModel databaseViewModel, Listener listener) {
        this.databaseViewModel = databaseViewModel;
        this.listener = listener;
    }

    @Override
    protected Void doInBackground(Jump... jumps) {
        databaseViewModel.addJump(jumps[0]);
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);

        listener.onFinished();
    }

    public interface Listener {
        void onFinished();
    }
}

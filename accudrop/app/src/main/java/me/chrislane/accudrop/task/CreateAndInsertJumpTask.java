package me.chrislane.accudrop.task;

import android.os.AsyncTask;
import android.util.Log;

import java.util.Date;

import me.chrislane.accudrop.db.Jump;
import me.chrislane.accudrop.viewmodel.DatabaseViewModel;

/**
 * Create and insert a new jump into the database.
 */
public class CreateAndInsertJumpTask extends AsyncTask<Void, Void, Integer> {

    private static final String TAG = CreateAndInsertJumpTask.class.getSimpleName();
    private final DatabaseViewModel databaseViewModel;
    private final Listener listener;
    private final InsertJumpTask.Listener insertListener;

    public CreateAndInsertJumpTask(DatabaseViewModel databaseViewModel, Listener listener,
                                   InsertJumpTask.Listener insertListener) {
        this.databaseViewModel = databaseViewModel;
        this.listener = listener;
        this.insertListener = insertListener;
    }

    @Override
    protected Integer doInBackground(Void... params) {
        Integer jumpId = databaseViewModel.getLastJumpId();

        if (jumpId != null) {
            Log.d(TAG, "Previous jump id: " + jumpId);
            jumpId = jumpId + 1;
        } else {
            Log.d(TAG, "No previous jump id.");
            jumpId = 1;
        }
        return jumpId;
    }

    @Override
    protected void onPostExecute(Integer result) {
        Jump jump = new Jump();
        jump.id = result;
        jump.time = new Date();

        listener.onFinished(result);

        new InsertJumpTask(databaseViewModel, insertListener).execute(jump);
    }

    public interface Listener {
        void onFinished(int jumpId);
    }
}
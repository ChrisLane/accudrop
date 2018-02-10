package me.chrislane.accudrop.task;

import android.arch.lifecycle.ViewModelProviders;
import android.os.AsyncTask;
import android.util.Log;

import java.util.Date;

import me.chrislane.accudrop.MainActivity;
import me.chrislane.accudrop.db.Jump;
import me.chrislane.accudrop.viewmodel.JumpViewModel;

/**
 * Create and insert a new jump into the database.
 */
public class CreateAndInsertJumpTask extends AsyncTask<Void, Void, Integer> {

    private static final String TAG = CreateAndInsertJumpTask.class.getSimpleName();
    private final JumpViewModel jumpViewModel;
    private final Listener listener;
    private final InsertJumpTask.Listener insertListener;

    public CreateAndInsertJumpTask(MainActivity main, Listener listener,
                                   InsertJumpTask.Listener insertListener) {
        jumpViewModel = ViewModelProviders.of(main).get(JumpViewModel.class);
        this.listener = listener;
        this.insertListener = insertListener;
    }

    @Override
    protected Integer doInBackground(Void... params) {
        Integer jumpId = jumpViewModel.getLastJumpId();

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

        new InsertJumpTask(jumpViewModel, insertListener).execute(jump);
    }

    public interface Listener {
        void onFinished(int jumpId);
    }
}
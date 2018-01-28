package me.chrislane.accudrop.task;

import android.os.AsyncTask;

import me.chrislane.accudrop.db.Jump;
import me.chrislane.accudrop.viewmodel.JumpViewModel;

public class InsertJumpTask extends AsyncTask<Jump, Void, Void> {
    private final JumpViewModel jumpViewModel;
    private final Listener listener;


    InsertJumpTask(JumpViewModel jumpViewModel, Listener listener) {
        this.jumpViewModel = jumpViewModel;
        this.listener = listener;
    }

    public interface Listener {
        void onFinished();
    }

    @Override
    protected Void doInBackground(Jump... jumps) {
        jumpViewModel.addJump(jumps[0]);
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);

        listener.onFinished();
    }
}

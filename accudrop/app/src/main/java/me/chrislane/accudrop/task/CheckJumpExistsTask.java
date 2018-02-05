package me.chrislane.accudrop.task;

import android.os.AsyncTask;

import me.chrislane.accudrop.viewmodel.JumpViewModel;

public class CheckJumpExistsTask extends AsyncTask<Integer, Void, Boolean> {

    private final Listener listener;
    private final JumpViewModel jumpViewModel;

    public CheckJumpExistsTask(Listener listener, JumpViewModel jumpViewModel) {
        this.listener = listener;
        this.jumpViewModel = jumpViewModel;
    }

    @Override
    protected Boolean doInBackground(Integer... integers) {
        return jumpViewModel.jumpExists(integers[0]);
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

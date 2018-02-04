package me.chrislane.accudrop.task;

import android.os.AsyncTask;

import me.chrislane.accudrop.viewmodel.JumpViewModel;

public class FetchJumpIdTask extends AsyncTask<Void, Void, Integer> {

    private final JumpViewModel jumpViewModel;
    private final Listener listener;

    public FetchJumpIdTask(FetchJumpIdTask.Listener listener, JumpViewModel jumpViewModel) {
        this.listener = listener;
        this.jumpViewModel = jumpViewModel;
    }

    @Override
    protected Integer doInBackground(Void... voids) {
        return jumpViewModel.getLastJumpId();
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

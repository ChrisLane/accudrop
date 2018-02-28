package me.chrislane.accudrop.task;

import android.os.AsyncTask;

import me.chrislane.accudrop.viewmodel.JumpViewModel;


public class FetchFirstJumpIdTask extends AsyncTask<Void, Void, Integer> {

    private final JumpViewModel jumpViewModel;
    private final FetchFirstJumpIdTask.Listener listener;

    public FetchFirstJumpIdTask(FetchFirstJumpIdTask.Listener listener, JumpViewModel jumpViewModel) {
        this.listener = listener;
        this.jumpViewModel = jumpViewModel;
    }

    @Override
    protected Integer doInBackground(Void... voids) {
        return jumpViewModel.getFirstJumpId();
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

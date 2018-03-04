package me.chrislane.accudrop.presenter;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.os.AsyncTask;
import android.util.Log;

import me.chrislane.accudrop.fragment.ReplayFragment;
import me.chrislane.accudrop.task.CheckJumpExistsTask;
import me.chrislane.accudrop.task.FetchFirstJumpIdTask;
import me.chrislane.accudrop.task.FetchLastJumpIdTask;
import me.chrislane.accudrop.task.FetchUsersAndPositionsTask;
import me.chrislane.accudrop.viewmodel.JumpViewModel;
import me.chrislane.accudrop.viewmodel.ReplayViewModel;

public class ReplayPresenter {

    private static final String TAG = ReplayPresenter.class.getSimpleName();
    private final ReplayFragment replayFragment;
    private ReplayViewModel replayViewModel;
    private JumpViewModel jumpViewModel;
    int tasksRunning = 0;

    public ReplayPresenter(ReplayFragment replayFragment) {
        this.replayFragment = replayFragment;

        replayViewModel = ViewModelProviders.of(replayFragment).get(ReplayViewModel.class);
        jumpViewModel = ViewModelProviders.of(replayFragment).get(JumpViewModel.class);

        subscribeToJumpId();

        FetchLastJumpIdTask.Listener listener = jumpId -> {
            if (jumpId != null) {
                replayViewModel.setJumpId(jumpId);
            }
        };
        new FetchLastJumpIdTask(listener, jumpViewModel)
                .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    /**
     * Set the route to be displayed in replay views.
     *
     * @param jumpID The jump ID to get the route from.
     */
    public void setRoutes(int jumpID) {
        FetchUsersAndPositionsTask.Listener listener = result -> replayViewModel.setUsersAndLocs(result);
        new FetchUsersAndPositionsTask(listener, jumpViewModel)
                .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, jumpID);
    }

    /**
     * Set the new route when the jump ID changes.
     */
    private void subscribeToJumpId() {
        final Observer<Integer> jumpIdObserver = jumpID -> {
            if (jumpID != null) {
                setRoutes(jumpID);
                updateButtons(jumpID);
            }
        };
        replayViewModel.getJumpId().observe(replayFragment, jumpIdObserver);
    }

    public void prevJump() {
        Integer jumpId = replayViewModel.getJumpId().getValue();
        if (jumpId != null) {
            CheckJumpExistsTask.Listener listener = jumpExists -> {
                if (jumpExists) {
                    replayViewModel.setJumpId(jumpId - 1);
                }
            };
            new CheckJumpExistsTask(listener, jumpViewModel)
                    .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, jumpId - 1);
        }
    }

    public void nextJump() {
        Integer jumpId = replayViewModel.getJumpId().getValue();
        if (jumpId != null) {
            CheckJumpExistsTask.Listener listener = jumpExists -> {
                if (jumpExists) {
                    replayViewModel.setJumpId(jumpId + 1);
                }
            };
            new CheckJumpExistsTask(listener, jumpViewModel)
                    .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, jumpId + 1);
        }
    }

    public void updateButtons(int jumpId) {
        final Integer[] first = new Integer[1];
        final Integer[] last = new Integer[1];

        tasksRunning = 2;
        FetchFirstJumpIdTask.Listener firstListener = firstJumpId -> {
            Log.d(TAG, "First Jump ID: " + firstJumpId);
            first[0] = firstJumpId;
            decTasksRunning();

            if (tasksRunning == 0) {
                if (first[0] != null && last[0] != null) {
                    replayFragment.updateButtons(jumpId, first[0], last[0]);
                }
            }
        };
        new FetchFirstJumpIdTask(firstListener, jumpViewModel)
                .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

        FetchLastJumpIdTask.Listener lastListener = lastJumpId -> {
            Log.d(TAG, "Last Jump ID: " + lastJumpId);
            last[0] = lastJumpId;
            decTasksRunning();

            if (tasksRunning == 0) {
                if (first[0] != null && last[0] != null) {
                    replayFragment.updateButtons(jumpId, first[0], last[0]);
                }
            }
        };
        new FetchLastJumpIdTask(lastListener, jumpViewModel)
                .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    public synchronized void decTasksRunning() {
        tasksRunning--;
    }
}
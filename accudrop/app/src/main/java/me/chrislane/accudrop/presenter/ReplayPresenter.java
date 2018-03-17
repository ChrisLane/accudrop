package me.chrislane.accudrop.presenter;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.os.AsyncTask;
import android.util.Log;

import me.chrislane.accudrop.BuildConfig;
import me.chrislane.accudrop.fragment.ReplayFragment;
import me.chrislane.accudrop.task.FetchLastJumpIdTask;
import me.chrislane.accudrop.task.FetchUsersAndPositionsTask;
import me.chrislane.accudrop.viewmodel.DatabaseViewModel;
import me.chrislane.accudrop.viewmodel.ReplayViewModel;

public class ReplayPresenter {

    private static final String TAG = ReplayPresenter.class.getSimpleName();
    private final ReplayFragment replayFragment;
    private ReplayViewModel replayViewModel;
    private DatabaseViewModel databaseViewModel;

    public ReplayPresenter(ReplayFragment replayFragment) {
        this.replayFragment = replayFragment;

        replayViewModel = ViewModelProviders.of(replayFragment).get(ReplayViewModel.class);
        databaseViewModel = ViewModelProviders.of(replayFragment).get(DatabaseViewModel.class);

        subscribeToJumpId();
        subscribeToJumpRange();
        subscribeToButtonData();

        FetchLastJumpIdTask.Listener listener = jumpId -> {
            if (jumpId != null) {
                replayViewModel.setJumpId(jumpId);
            }
        };
        new FetchLastJumpIdTask(listener, databaseViewModel)
                .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    /**
     * Set the route to be displayed in replay views.
     *
     * @param jumpID The jump ID to get the route from.
     */
    private void setRoutes(int jumpID) {
        FetchUsersAndPositionsTask.Listener listener = result -> replayViewModel.setUsersAndLocs(result);
        new FetchUsersAndPositionsTask(listener, databaseViewModel)
                .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, jumpID);
    }

    /**
     * Set the new route when the jump ID changes.
     */
    private void subscribeToJumpId() {
        final Observer<Integer> jumpIdObserver = jumpId -> {
            if (jumpId != null) {
                setRoutes(jumpId);
            }
        };
        replayViewModel.getJumpId().observe(replayFragment, jumpIdObserver);
    }

    private void subscribeToJumpRange() {
        final Observer<Integer> firstJumpIdObserver = firstJumpId -> {
            if (firstJumpId != null) {
                replayViewModel.setFirstJumpId(firstJumpId);
            }
        };
        databaseViewModel.findFirstJumpId().observe(replayFragment, firstJumpIdObserver);

        final Observer<Integer> lastJumpIdObserver = lastJumpId -> {
            if (lastJumpId != null) {
                replayViewModel.setLastJumpId(lastJumpId);
            }
        };
        databaseViewModel.findLastJumpId().observe(replayFragment, lastJumpIdObserver);
    }

    private void subscribeToButtonData() {
        final Observer<Integer> buttonDataObserver = ignored -> {
            Integer jumpId = replayViewModel.getJumpId().getValue();
            Integer firstJumpId = replayViewModel.getFirstJumpId().getValue();
            Integer lastJumpId = replayViewModel.getLastJumpId().getValue();

            if (BuildConfig.DEBUG) {
                Log.d(TAG, "Button data: " + jumpId + ", " + firstJumpId + ", " + lastJumpId);
            }

            if (jumpId != null && firstJumpId != null && lastJumpId != null) {
                replayFragment.updateButtons(jumpId, firstJumpId, lastJumpId);
            }
        };
        replayViewModel.getJumpId().observe(replayFragment, buttonDataObserver);
        replayViewModel.getFirstJumpId().observe(replayFragment, buttonDataObserver);
        replayViewModel.getLastJumpId().observe(replayFragment, buttonDataObserver);
    }

    public void prevJump() {
        Integer jumpId = replayViewModel.getJumpId().getValue();
        Integer firstJumpId = replayViewModel.getFirstJumpId().getValue();

        if (jumpId != null && firstJumpId != null) {
            if (jumpId > firstJumpId) {
                replayViewModel.setJumpId(jumpId - 1);
            }
        }
    }

    public void nextJump() {
        Integer jumpId = replayViewModel.getJumpId().getValue();
        Integer lastJumpId = replayViewModel.getLastJumpId().getValue();

        if (jumpId != null && lastJumpId != null) {
            if (jumpId < lastJumpId) {
                replayViewModel.setJumpId(jumpId + 1);
            }
        }
    }
}
package me.chrislane.accudrop.presenter;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

import java.util.UUID;

import me.chrislane.accudrop.BuildConfig;
import me.chrislane.accudrop.db.FallType;
import me.chrislane.accudrop.fragment.JumpStatsFragment;
import me.chrislane.accudrop.task.FetchFallTypeDuration;
import me.chrislane.accudrop.task.FetchFallTypeMaxHSpeed;
import me.chrislane.accudrop.task.FetchFallTypeMaxVSpeed;
import me.chrislane.accudrop.task.FetchLastJumpIdTask;
import me.chrislane.accudrop.task.FetchTotalDuration;
import me.chrislane.accudrop.viewmodel.DatabaseViewModel;
import me.chrislane.accudrop.viewmodel.JumpStatsViewModel;

public class JumpStatsPresenter {

    private static final String TAG = JumpStatsPresenter.class.getSimpleName();
    private final DatabaseViewModel dbViewModel;
    private final JumpStatsFragment fragment;
    private final JumpStatsViewModel viewModel;
    private final FragmentActivity main;
    private UUID uuid;

    public JumpStatsPresenter(JumpStatsFragment fragment) {
        this.fragment = fragment;

        main = fragment.requireActivity();

        dbViewModel = ViewModelProviders.of(fragment).get(DatabaseViewModel.class);
        viewModel = ViewModelProviders.of(fragment).get(JumpStatsViewModel.class);

        initialise();

        subscribeToJumpId();
        subscribeToJumpRange();
        subscribeToButtonData();
    }

    private void initialise() {
        SharedPreferences settings = main.getSharedPreferences("userInfo", Context.MODE_PRIVATE);
        String stringUuid = settings.getString("userUUID", "");
        uuid = UUID.fromString(stringUuid);

        FetchLastJumpIdTask.Listener listener = jumpId -> {
            if (jumpId != null) {
                viewModel.setJumpId(jumpId);
            }
        };
        new FetchLastJumpIdTask(listener, dbViewModel).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void subscribeToJumpId() {
        final Observer<Integer> jumpIdObserver = jumpId -> {
            if (jumpId != null) {
                fragment.updateJumpId(jumpId);
                updateDurations(jumpId);
                updateSpeeds(jumpId);
            }
        };
        viewModel.getJumpId().observe(fragment, jumpIdObserver);
    }

    private void updateSpeeds(int jumpId) {
        updateFreefallSpeeds(jumpId);
        updateCanopySpeeds(jumpId);
    }

    private void updateCanopySpeeds(int jumpId) {
        // Update vertical speed
        FetchFallTypeMaxVSpeed.Listener vListener = vSpeed -> {
            if (vSpeed != null) {
                fragment.updateCanopyVSpeed(vSpeed);
            }
        };
        new FetchFallTypeMaxVSpeed(vListener, FallType.CANOPY, uuid, dbViewModel)
                .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, jumpId);

        // Update horizontal speed
        FetchFallTypeMaxHSpeed.Listener hListener = hSpeed -> {
            if (hSpeed != null) {
                fragment.updateCanopyHSpeed(hSpeed);
            }
        };
        new FetchFallTypeMaxHSpeed(hListener, FallType.CANOPY, uuid, dbViewModel)
                .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, jumpId);
    }

    private void updateFreefallSpeeds(int jumpId) {
        // Update vertical speed
        FetchFallTypeMaxVSpeed.Listener vListener = vSpeed -> {
            if (vSpeed != null) {
                fragment.updateFreefallVSpeed(vSpeed);
            } else {
                fragment.updateFreefallVSpeed(0);
            }
        };
        new FetchFallTypeMaxVSpeed(vListener, FallType.FREEFALL, uuid, dbViewModel)
                .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, jumpId);

        // Update horizontal speed
        FetchFallTypeMaxHSpeed.Listener hListener = hSpeed -> {
            if (hSpeed != null) {
                fragment.updateFreefallHSpeed(hSpeed);
            } else {
                fragment.updateFreefallHSpeed(0);
            }
        };
        new FetchFallTypeMaxHSpeed(hListener, FallType.FREEFALL, uuid, dbViewModel)
                .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, jumpId);
    }

    private void updateDurations(int jumpId) {
        updateTotalDuration(jumpId);
        updateFreefallDuration(jumpId);
        updateCanopyDuration(jumpId);
    }

    private void updateCanopyDuration(int jumpId) {
        FetchFallTypeDuration.Listener listener = millis -> {
            if (millis != null) {
                fragment.updateCanopyDuration(millis);
            } else {
                fragment.updateCanopyDuration(0);
            }
        };
        new FetchFallTypeDuration(listener, FallType.CANOPY, uuid, dbViewModel)
                .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, jumpId);
    }

    private void updateFreefallDuration(int jumpId) {
        FetchFallTypeDuration.Listener listener = millis -> {
            if (millis != null) {
                fragment.updateFreefallDuration(millis);
            } else {
                fragment.updateFreefallDuration(0);
            }
        };
        new FetchFallTypeDuration(listener, FallType.FREEFALL, uuid, dbViewModel)
                .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, jumpId);
    }

    private void updateTotalDuration(int jumpId) {
        FetchTotalDuration.Listener listener = millis -> {
            if (millis != null) {
                fragment.updateTotalDuration(millis);
            } else {
                fragment.updateTotalDuration(0);
            }
        };
        new FetchTotalDuration(listener, uuid, dbViewModel)
                .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, jumpId);
    }

    private void subscribeToJumpRange() {
        final Observer<Integer> firstJumpIdObserver = firstJumpId -> {
            if (firstJumpId != null) {
                viewModel.setFirstJumpId(firstJumpId);
            }
        };
        dbViewModel.findFirstJumpId().observe(fragment, firstJumpIdObserver);

        final Observer<Integer> lastJumpIdObserver = lastJumpId -> {
            if (lastJumpId != null) {
                viewModel.setLastJumpId(lastJumpId);
            }
        };
        dbViewModel.findLastJumpId().observe(fragment, lastJumpIdObserver);
    }

    private void subscribeToButtonData() {
        final Observer<Integer> buttonDataObserver = ignored -> {
            Integer jumpId = viewModel.getJumpId().getValue();
            Integer firstJumpId = viewModel.getFirstJumpId().getValue();
            Integer lastJumpId = viewModel.getLastJumpId().getValue();

            if (BuildConfig.DEBUG) {
                Log.v(TAG, "Button data: " + jumpId + ", " + firstJumpId + ", " + lastJumpId);
            }
            if (jumpId != null && firstJumpId != null && lastJumpId != null) {
                fragment.updateButtons(jumpId, firstJumpId, lastJumpId);
            }
        };
        viewModel.getJumpId().observe(fragment, buttonDataObserver);
        viewModel.getFirstJumpId().observe(fragment, buttonDataObserver);
        viewModel.getLastJumpId().observe(fragment, buttonDataObserver);
    }

    public void prevJump() {
        Integer jumpId = viewModel.getJumpId().getValue();
        Integer firstJumpId = viewModel.getFirstJumpId().getValue();

        if (jumpId != null && firstJumpId != null) {
            if (jumpId > firstJumpId) {
                viewModel.setJumpId(jumpId - 1);
            }
        }
    }

    public void nextJump() {
        Integer jumpId = viewModel.getJumpId().getValue();
        Integer lastJumpId = viewModel.getLastJumpId().getValue();

        if (jumpId != null && lastJumpId != null) {
            if (jumpId < lastJumpId) {
                viewModel.setJumpId(jumpId + 1);
            }
        }
    }
}

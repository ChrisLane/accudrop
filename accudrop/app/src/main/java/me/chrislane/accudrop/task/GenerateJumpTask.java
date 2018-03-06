package me.chrislane.accudrop.task;

import android.location.Location;
import android.os.AsyncTask;
import android.util.Log;
import android.util.Pair;

import com.google.android.gms.maps.model.LatLng;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import me.chrislane.accudrop.JumpGenerator;
import me.chrislane.accudrop.viewmodel.DatabaseViewModel;

public class GenerateJumpTask extends AsyncTask<Integer, Void, Void> {
    private static final String TAG = GenerateJumpTask.class.getSimpleName();
    private final LatLng target;
    private final UUID subjectUUID;
    private final DatabaseViewModel databaseViewModel;

    public GenerateJumpTask(UUID subjectUUID, LatLng target, DatabaseViewModel databaseViewModel) {
        this.subjectUUID = subjectUUID;
        this.target = target;
        this.databaseViewModel = databaseViewModel;

    }

    @Override
    protected Void doInBackground(Integer... guestCounts) {
        int guestCount = guestCounts[0];

        // Add a new jump to the database
        databaseViewModel.addJump();

        Integer jumpId = databaseViewModel.getLastJumpId();
        if (jumpId == null) {
            Log.e(TAG, "Could not get last jump ID.");
            return null;
        }
        Log.d(TAG, "Generating jump " + jumpId);

        // Generate a route for the subject
        RouteTask.RouteTaskListener routeListener = route -> {
            // Add intermediary points to route
            List<Location> subjectRoute = JumpGenerator.addIntermediaryPoints(route);
            // Add subject route to database
            new AddGeneratedPositionsTask(jumpId, subjectUUID, subjectRoute, databaseViewModel)
                    .executeOnExecutor(THREAD_POOL_EXECUTOR);
        };
        // Generate random wind stats
        double randSpeed = ThreadLocalRandom.current().nextInt(0, 10);
        double randDir = ThreadLocalRandom.current().nextInt(0, 360);
        // Execute subject task
        new RouteTask(routeListener, databaseViewModel.getApplication().getApplicationContext(), new Pair<>(randSpeed, randDir))
                .executeOnExecutor(THREAD_POOL_EXECUTOR, target);

        for (int i = 0; i < guestCount; i++) {
            // Generate a route for the subject
            RouteTask.RouteTaskListener guestRouteListener = route -> {
                // Add intermediary points to route
                List<Location> guestRoute = JumpGenerator.addIntermediaryPoints(route);
                // Add subject route to database
                new AddGeneratedPositionsTask(jumpId, UUID.randomUUID(), guestRoute, databaseViewModel)
                        .executeOnExecutor(THREAD_POOL_EXECUTOR);
            };
            // Generate random wind stats
            double guestRandSpeed = ThreadLocalRandom.current().nextInt(0, 10);
            double guestRandDir = ThreadLocalRandom.current().nextInt(0, 360);
            // Execute subject task
            new RouteTask(guestRouteListener, databaseViewModel.getApplication().getApplicationContext(), new Pair<>(guestRandSpeed, guestRandDir))
                    .executeOnExecutor(THREAD_POOL_EXECUTOR, target);
        }
        return null;
    }
}

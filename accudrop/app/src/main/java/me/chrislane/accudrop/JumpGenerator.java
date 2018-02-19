package me.chrislane.accudrop;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import me.chrislane.accudrop.db.Position;
import me.chrislane.accudrop.task.CreateAndInsertJumpTask;
import me.chrislane.accudrop.task.InsertJumpTask;
import me.chrislane.accudrop.task.RouteTask;
import me.chrislane.accudrop.task.WindTask;
import me.chrislane.accudrop.viewmodel.GnssViewModel;
import me.chrislane.accudrop.viewmodel.JumpViewModel;

public class JumpGenerator {

    private static final String TAG = JumpGenerator.class.getSimpleName();
    private final MainActivity main;
    private final JumpViewModel jumpViewModel;
    int jumpId;

    public JumpGenerator(MainActivity main) {
        this.main = main;
        jumpViewModel = ViewModelProviders.of(main).get(JumpViewModel.class);
    }

    /**
     * Calculate a route and add it as a jump.
     *
     * @param target The target landing coordinates.
     */
    public void calcLandingPattern(LatLng target) {
        // Run a route calculation task with the updated wind
        RouteTask.RouteTaskListener routeListener = this::addJump;
        int randSpeed = ThreadLocalRandom.current().nextInt(0, 10);
        int randDir = ThreadLocalRandom.current().nextInt(0, 360);
        new RouteTask(routeListener, new WindTask.WindTuple(randSpeed, randDir)).execute(target);
    }

    public void calcRadarJumpers(LatLng target) {
        // Run a route calculation task with the updated wind
        // Subject
        RouteTask.RouteTaskListener routeListener = this::addJump;
        int randSpeed = ThreadLocalRandom.current().nextInt(0, 10);
        int randDir = ThreadLocalRandom.current().nextInt(0, 360);
        new RouteTask(routeListener, new WindTask.WindTuple(randSpeed, randDir)).execute(target);
    }

    public void calcGuestJumpers(int jumpId, LatLng target) {
        int randSpeed;
        int randDir;
        for (int i = 0; i < 10; i++) {
            randSpeed = ThreadLocalRandom.current().nextInt(0, 10);
            randDir = ThreadLocalRandom.current().nextInt(0, 360);
            RouteTask.RouteTaskListener guestRouteListener = route -> addFakeToJump(jumpId, route);
            new RouteTask(guestRouteListener, new WindTask.WindTuple(randSpeed, randDir)).execute(target);
        }
    }

    private void addPositions(int jumpId, List<Location> route) {
        SharedPreferences settings = main.getSharedPreferences("userInfo", Context.MODE_PRIVATE);
        String stringUuid = settings.getString("userUUID", "");
        UUID uuid = UUID.fromString(stringUuid);

        addPositions(jumpId, uuid, route);
    }

    /**
     * Add positions in a route to a jump.
     *
     * @param jumpId The jump ID to add positions for.
     * @param uuid   The user's uuid.
     * @param route  The route containing positions.
     */
    private void addPositions(int jumpId, UUID uuid, List<Location> route) {
        for (Location location : route) {
            Position pos = new Position();
            pos.latitude = location.getLatitude();
            pos.longitude = location.getLongitude();
            pos.altitude = (int) location.getAltitude();
            pos.time = new Date();
            pos.jumpId = jumpId;
            pos.useruuid = uuid.toString();

            String msg = String.format(Locale.ENGLISH, "Inserting position:%n" +
                            "\tUser UUID: %s%n" +
                            "\tJump ID: %d%n" +
                            "\t(Lat, Long): (%f,%f)%n" +
                            "\tAltitude: %d%n" +
                            "\tTime: %s",
                    pos.useruuid, pos.jumpId, pos.latitude, pos.longitude, pos.altitude, pos.time);
            Log.d(TAG, msg);

            AsyncTask.execute(() -> jumpViewModel.addPosition(pos));
        }
    }

    /**
     * Create a new a jump and add a route's positions to it.
     *
     * @param route The route containing jump positions.
     */
    private void addJump(List<Location> route) {
        List<Location> finalRoute = addIntermediaryPoints(route);

        CreateAndInsertJumpTask.Listener createListener = result -> {
            jumpId = result;
            calcGuestJumpers(result, new LatLng(51.52, 0.08));
        };
        InsertJumpTask.Listener insertListener = () -> addPositions(jumpId, finalRoute);
        new CreateAndInsertJumpTask(main, createListener, insertListener).execute();
    }

    /**
     * Create a new a jump and add a route's positions to it.
     *
     * @param route The route containing jump positions.
     */
    private void addFakeToJump(int jumpId, List<Location> route) {
        List<Location> finalRoute = addIntermediaryPoints(route);

        addPositions(jumpId, UUID.randomUUID(), finalRoute);
    }

    /**
     * Add intermediary points to a route with random tweaks to the bearing.
     *
     * @param route The route to add to.
     * @return The route containing additional points.
     */
    private List<Location> addIntermediaryPoints(List<Location> route) {
        List<Location> result = new ArrayList<>();

        for (int i = 0; i < route.size() - 1; i++) {
            Location loc1 = route.get(i);
            Location loc2 = route.get(i + 1);
            double totalDistance = loc1.distanceTo(loc2);
            double altitude = loc1.getAltitude();
            int split = (int) (totalDistance / 5);
            double altitudeDec = (altitude - loc2.getAltitude()) / split;

            result.add(loc1);
            LatLng prevPos = GnssViewModel.getLatLng(loc1);
            Location prevLoc = loc1;
            for (int j = 0; j < split - 1; j++) {
                double bearing = prevLoc.bearingTo(loc2);

                int randBear = ThreadLocalRandom.current().nextInt(-15, 15);
                prevLoc = new Location("");
                prevPos = RouteCalculator.getPosAfterMove(prevPos, 5, bearing + randBear);
                prevLoc.setLatitude(prevPos.latitude);
                prevLoc.setLongitude(prevPos.longitude);
                altitude -= altitudeDec;
                prevLoc.setAltitude(altitude);

                result.add(prevLoc);
            }
        }
        result.add(route.get(route.size() - 1));

        return result;
    }
}

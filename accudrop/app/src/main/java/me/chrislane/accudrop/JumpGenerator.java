package me.chrislane.accudrop;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.AsyncTask;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import me.chrislane.accudrop.task.GenerateJumpTask;
import me.chrislane.accudrop.viewmodel.GnssViewModel;
import me.chrislane.accudrop.viewmodel.JumpViewModel;

public class JumpGenerator {

    private static final String TAG = JumpGenerator.class.getSimpleName();
    private final MainActivity main;
    private final JumpViewModel jumpViewModel;
    private Observer<Integer> jumpIdObserver;

    public JumpGenerator(MainActivity main) {
        this.main = main;
        jumpViewModel = ViewModelProviders.of(main).get(JumpViewModel.class);
    }

    public void removeJumpIdObserver() {
        jumpViewModel.findLastJumpId().removeObserver(jumpIdObserver);
    }

    public void generateJump(LatLng target, int noOfGuests) {
        SharedPreferences settings = main.getSharedPreferences("userInfo", Context.MODE_PRIVATE);
        String stringUuid = settings.getString("userUUID", "");
        UUID uuid = UUID.fromString(stringUuid);

        new GenerateJumpTask(uuid, target, jumpViewModel)
                .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, noOfGuests);
    }

    /**
     * Add intermediary points to a route with random tweaks to the bearing.
     *
     * @param route The route to add to.
     * @return The route containing additional points.
     */
    public static List<Location> addIntermediaryPoints(List<Location> route) {
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

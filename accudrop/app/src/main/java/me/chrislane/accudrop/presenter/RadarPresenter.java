package me.chrislane.accudrop.presenter;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.support.v4.util.Pair;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import me.chrislane.accudrop.MainActivity;
import me.chrislane.accudrop.fragment.RadarFragment;
import me.chrislane.accudrop.task.FetchUsersAndPositionsTask;
import me.chrislane.accudrop.viewmodel.JumpViewModel;

public class RadarPresenter {

    private static final String TAG = RadarPresenter.class.getSimpleName();
    private final RadarFragment fragment;
    private JumpViewModel jumpViewModel = null;
    private UUID subject;
    private int maxHDistance = 500; // In metres
    private int maxVDistance = 50; // In metres
    private List<Location> subjectLocs;
    private List<Pair<UUID, List<Location>>> guestLocs;
    private List<Pair<Float, Float>> positions = new ArrayList<>();
    private List<Double> heightDiffs = new ArrayList<>();

    public RadarPresenter(RadarFragment fragment) {
        this.fragment = fragment;

        MainActivity main = (MainActivity) fragment.getActivity();
        if (main != null) {
            jumpViewModel = ViewModelProviders.of(main).get(JumpViewModel.class);
        }

        // Set the current user as the subject
        SharedPreferences settings = jumpViewModel.getApplication()
                .getSharedPreferences("userInfo", Context.MODE_PRIVATE);
        String stringUuid = settings.getString("userUUID", "");
        UUID uuid = UUID.fromString(stringUuid);
        setSubject(uuid);

        // Get users and positions of the last jump
        FetchUsersAndPositionsTask.Listener listener = userEntries -> {
            for (int i = 0; i < userEntries.size(); i++) {
                Pair<UUID, List<Location>> userEntry = userEntries.get(i);
                if (userEntry.first != null && userEntry.first.equals(subject)) {
                    subjectLocs = userEntry.second;
                    userEntries.remove(i);
                }
            }

            // None of the users were the subject.
            if (subjectLocs == null) {
                Log.e(TAG, "Subject does not exist in jump data.");
                return;
            }

            guestLocs = userEntries;
            List<Location> locations = gatherLocations();
            generatePositions(locations);
        };
        new FetchUsersAndPositionsTask(listener, jumpViewModel).execute();
    }

    private List<Location> gatherLocations() {
        List<Location> result = new ArrayList<>();
        for (Pair<UUID, List<Location>> userEntry : guestLocs) {
            // TODO: Get the same entry for relative time for all guests
            if (userEntry.second != null && userEntry.second.size() > 0) {
                result.add(userEntry.second.get(25));
            }
        }
        return result;
    }

    public void setSubject(UUID uuid) {
        subject = uuid;
    }

    public void generatePositions(List<Location> locations) {
        if (subjectLocs.isEmpty() || locations.isEmpty()) {
            return;
        }
        Location subjectLoc = subjectLocs.get(25);

        positions.clear();
        heightDiffs.clear();

        // Loop over user position arrays
        for (Location guest : locations) {
            float hDistanceTo = subjectLoc.distanceTo(guest);
            double vDistanceTo = guest.getAltitude() - subjectLoc.getAltitude();
            Log.v(TAG, "Horizontal Distance: " + hDistanceTo);
            Log.v(TAG, "Vertical Distance: " + vDistanceTo);

            // Check if distance from subject further than maxHDistance
            if (maxHDistance >= hDistanceTo && maxVDistance >= vDistanceTo) {
                // Add to list of positions to draw
                float bearingTo = subjectLoc.bearingTo(guest);
                Log.v(TAG, "Bearing: " + bearingTo);
                positions.add(new Pair<>(bearingTo, hDistanceTo));
                heightDiffs.add(vDistanceTo);
            }
        }

        fragment.updateRadarPoints();
    }

    public List<Pair<Float, Float>> getPositions() {
        return positions;
    }

    public List<Double> getHeightDiffs() {
        return heightDiffs;
    }

    public int getMaxVDistance() {
        return maxVDistance;
    }

    public int getMaxHDistance() {
        return maxHDistance;
    }
}

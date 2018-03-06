package me.chrislane.accudrop.presenter;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.AsyncTask;
import android.support.annotation.Nullable;
import android.support.v4.util.Pair;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import me.chrislane.accudrop.MainActivity;
import me.chrislane.accudrop.fragment.RadarFragment;
import me.chrislane.accudrop.task.FetchLastJumpIdTask;
import me.chrislane.accudrop.task.FetchUsersAndPositionsTask;
import me.chrislane.accudrop.viewmodel.DatabaseViewModel;
import me.chrislane.accudrop.viewmodel.RadarViewModel;

public class RadarPresenter {

    private static final String TAG = RadarPresenter.class.getSimpleName();
    private final RadarFragment fragment;
    private final RadarViewModel radarViewModel;
    private DatabaseViewModel databaseViewModel = null;
    // TODO #48: Move data below to the view model
    private int maxHDistance = 500; // In metres
    private int maxVDistance = 50; // In metres
    private List<Location> subjectLocs;
    private List<Pair<Float, Float>> positions = new ArrayList<>();
    private List<Double> heightDiffs = new ArrayList<>();
    private List<Pair<UUID, Location>> guestLocations;
    private List<Pair<UUID, List<Location>>> guestEntries;
    private ArrayList<UUID> uuids = new ArrayList<>();
    private Pair<UUID, List<Location>> subjectEntry;
    private long time;

    public RadarPresenter(RadarFragment fragment) {
        this.fragment = fragment;

        radarViewModel = ViewModelProviders.of(fragment).get(RadarViewModel.class);
        MainActivity main = (MainActivity) fragment.getActivity();
        if (main != null) {
            databaseViewModel = ViewModelProviders.of(main).get(DatabaseViewModel.class);
        }

        // Set the current user as the subject
        SharedPreferences settings = databaseViewModel.getApplication()
                .getSharedPreferences("userInfo", Context.MODE_PRIVATE);
        String stringUuid = settings.getString("userUUID", "");
        UUID uuid = UUID.fromString(stringUuid);
        radarViewModel.setSubject(uuid);

        subscribeToSubject();

        FetchLastJumpIdTask.Listener listener = jumpId -> {
            if (jumpId != null) {
                radarViewModel.setJumpId(jumpId);
            }
        };
        new FetchLastJumpIdTask(listener, databaseViewModel)
                .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    public void test() {
        guestEntries = new ArrayList<>();
        List<Location> locationList1 = new ArrayList<>();
        Location location1 = new Location("");
        location1.setLatitude(38.696680);
        location1.setLongitude(1.448990);
        location1.setAltitude(1);
        locationList1.add(location1);
        subjectEntry = new Pair<>(UUID.fromString("a4f4231a-1a1a-40be-8fa2-632943fb1411"), locationList1);

        List<Location> locationList2 = new ArrayList<>();
        Location location2 = new Location("");
        location2.setLatitude(38.696249);
        location2.setLongitude(1.449623);
        location2.setAltitude(2);
        locationList2.add(location2);
        guestEntries.add(new Pair<>(UUID.fromString("6ad2769d-5031-4ac3-8b63-ce3d8e779c3b"), locationList2));

        List<Location> locationList3 = new ArrayList<>();
        Location location3 = new Location("");
        location3.setLatitude(38.695948);
        location3.setLongitude(1.448914);
        location3.setAltitude(3);
        locationList3.add(location3);
        guestEntries.add(new Pair<>(UUID.fromString("c22284cf-cd7d-49f8-8c96-bcdc3dcd61b8"), locationList3));

        List<Location> locationList4 = new ArrayList<>();
        Location location4 = new Location("");
        location4.setLatitude(38.696358);
        location4.setLongitude(1.448222);
        location4.setAltitude(4);
        locationList4.add(location4);
        guestEntries.add(new Pair<>(UUID.fromString("bc832712-894d-4be4-8440-18aa1c90a7a7"), locationList4));
    }

    public void subscribeToSubject() {
        final Observer<UUID> subjectObserver = uuid -> {
            Integer jumpId = radarViewModel.getJumpId().getValue();
            if (jumpId == null) {
                generateLastJumpPositions(uuid);
            } else {
                updateSubject(uuid);
            }
        };
        radarViewModel.getSubject().observe(fragment, subjectObserver);
    }

    public void updateSubject(UUID subject) {
        guestEntries.add(subjectEntry);
        guestEntries = extractSubject(subject, guestEntries);

        if (subjectLocs != null) {
            guestLocations = getGuestLocations(guestEntries, time);
            updateGuestRelatives(guestLocations, time);
        }
    }

    public List<Pair<UUID, List<Location>>> extractSubject(UUID subject,
                                                           List<Pair<UUID, List<Location>>> userEntries) {
        for (int i = 0; i < userEntries.size(); i++) {
            Pair<UUID, List<Location>> userEntry = userEntries.get(i);
            if (userEntry.first != null &&
                    userEntry.first.equals(subject)) {
                subjectEntry = userEntry;
                subjectLocs = userEntry.second;
                userEntries.remove(i);
            }
        }

        // None of the users were the subject.
        if (subjectLocs == null) {
            Log.e(TAG, "Subject does not exist in jump data.");
            return null;
        }

        return userEntries;
    }

    public void generateLastJumpPositions(UUID subject) {
        FetchUsersAndPositionsTask.Listener listener = userEntries -> {
            if (userEntries == null) {
                return;
            }

            guestEntries = extractSubject(subject, userEntries);

            if (subjectLocs != null) {
                long startTime = subjectLocs.get(0).getTime();
                guestLocations = getGuestLocations(guestEntries, startTime);
                updateGuestRelatives(guestLocations, startTime);
            }
        };
        new FetchUsersAndPositionsTask(listener, databaseViewModel).execute();
    }

    private List<Pair<UUID, Location>> getGuestLocations(List<Pair<UUID, List<Location>>> guestLocs, long time) {
        List<Pair<UUID, Location>> result = new ArrayList<>();
        for (Pair<UUID, List<Location>> userEntry : guestLocs) {
            List<Location> locations = userEntry.second;
            if (locations != null && locations.size() > 0) {
                Location nearest = getLocationByTime(locations, time);

                result.add(new Pair<>(userEntry.first, nearest));
            }
        }
        return result;
    }

    public void updateTime(long time) {
        this.time = time;
        guestLocations = getGuestLocations(guestEntries, time);
        updateGuestRelatives(guestLocations, time);
    }

    private Location getLocationByTime(List<Location> locations, long time) {
        // Time is less than the first time in the sorted list
        if (time < locations.get(0).getTime()) {
            return locations.get(0);
        }
        // Time is greater than the last element in the sorted list
        if (time > locations.get(locations.size() - 1).getTime()) {
            return locations.get(locations.size() - 1);
        }

        int low = 0;
        int high = locations.size() - 1;

        while (low <= high) {
            int mid = (high + low) / 2;

            if (time < locations.get(mid).getTime()) {
                high = mid - 1;
            } else if (time > locations.get(mid).getTime()) {
                low = mid + 1;
            } else {
                return locations.get(mid);
            }
        }

        return (locations.get(low).getTime() - time) < (time - locations.get(high).getTime())
                ? locations.get(low) : locations.get(high);
    }

    @Nullable
    public List<Location> getSubjectLocations() {
        return subjectEntry.second;
    }

    public void updateGuestRelatives(List<Pair<UUID, Location>> locations, long time) {
        if (subjectLocs.isEmpty() || locations.isEmpty()) {
            return;
        }

        Location subjectLoc = null;
        for (Location loc : subjectLocs) {
            if (loc.getTime() == time) {
                subjectLoc = loc;
                break;
            }
        }

        // Check we got a value for the subject location
        if (subjectLoc == null) {
            Log.e(TAG, "No subject location matching timestamp");
            return;
        }

        uuids.clear();
        positions.clear();
        heightDiffs.clear();

        // Loop over user position arrays
        for (Pair<UUID, Location> guest : locations) {
            if (guest.first != null && guest.second != null) {
                float hDistanceTo = subjectLoc.distanceTo(guest.second);
                double vDistanceTo = guest.second.getAltitude() - subjectLoc.getAltitude();
                Log.v(TAG, "Horizontal Distance: " + hDistanceTo);
                Log.v(TAG, "Vertical Distance: " + vDistanceTo);

                // Check if distance from subject further than maxHDistance
                if (maxHDistance >= hDistanceTo && maxVDistance >= vDistanceTo) {
                    // Add to list of positions to draw
                    float bearingTo = subjectLoc.bearingTo(guest.second);
                    Log.v(TAG, "Bearing: " + bearingTo);
                    uuids.add(guest.first);
                    positions.add(new Pair<>(bearingTo, hDistanceTo));
                    heightDiffs.add(vDistanceTo);
                }
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

    public List<UUID> getUuids() {
        return uuids;
    }

    public int getMaxVDistance() {
        return maxVDistance;
    }

    public int getMaxHDistance() {
        return maxHDistance;
    }
}

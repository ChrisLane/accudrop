package me.chrislane.accudrop.presenter;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.AsyncTask;
import android.support.v4.util.Pair;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import me.chrislane.accudrop.BuildConfig;
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
    private int maxHDistance = 500; // In metres
    private int maxVDistance = 50; // In metres

    public RadarPresenter(RadarFragment fragment) {
        this.fragment = fragment;

        radarViewModel = ViewModelProviders.of(fragment).get(RadarViewModel.class);
        MainActivity main = (MainActivity) fragment.requireActivity();
        databaseViewModel = ViewModelProviders.of(main).get(DatabaseViewModel.class);

        // Set the current user as the subject
        setOwnerAsSubject();

        FetchLastJumpIdTask.Listener listener = jumpId -> {
            if (jumpId != null) {
                radarViewModel.setJumpId(jumpId);
            }
        };
        new FetchLastJumpIdTask(listener, databaseViewModel)
                .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

        subscribeToSubject();
        subscribeToTime();
        subscribeToButtonData();
        subscribeToJumpId();
        subscribeToJumpRange();
    }

    public void setOwnerAsSubject() {
        SharedPreferences settings = databaseViewModel.getApplication()
                .getSharedPreferences("userInfo", Context.MODE_PRIVATE);
        String stringUuid = settings.getString("userUUID", "");
        UUID uuid = UUID.fromString(stringUuid);
        radarViewModel.setSubject(uuid);
    }

    public void test() {
        radarViewModel.setGuestEntries(new ArrayList<>());
        List<Pair<UUID, List<Location>>> guestEntries = radarViewModel.getGuestEntries().getValue();
        if (guestEntries == null) {
            return;
        }

        List<Location> locationList1 = new ArrayList<>();
        Location location1 = new Location("");
        location1.setLatitude(38.696680);
        location1.setLongitude(1.448990);
        location1.setAltitude(1);
        locationList1.add(location1);
        radarViewModel.setSubjectEntry(
                new Pair<>(UUID.fromString("a4f4231a-1a1a-40be-8fa2-632943fb1411"), locationList1));

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

    private void subscribeToJumpRange() {
        final Observer<Integer> firstJumpIdObserver = firstJumpId -> {
            if (firstJumpId != null) {
                radarViewModel.setFirstJumpId(firstJumpId);
            }
        };
        databaseViewModel.findFirstJumpId().observe(fragment, firstJumpIdObserver);

        final Observer<Integer> lastJumpIdObserver = lastJumpId -> {
            if (lastJumpId != null) {
                radarViewModel.setLastJumpId(lastJumpId);
            }
        };
        databaseViewModel.findLastJumpId().observe(fragment, lastJumpIdObserver);
    }

    private void subscribeToJumpId() {
        final Observer<Integer> jumpIdObserver = jumpId -> {
            if (jumpId != null) {
                setOwnerAsSubject();
            }
        };
        radarViewModel.getJumpId().observe(fragment, jumpIdObserver);
    }

    private void subscribeToSubject() {
        final Observer<UUID> subjectObserver = subject -> {
            Integer jumpId = radarViewModel.getJumpId().getValue();
            if (subject != null && jumpId != null) {
                start(subject);
                generateJumpPositions(jumpId, subject);
            }
        };
        radarViewModel.getSubject().observe(fragment, subjectObserver);
    }

    public void start(UUID subject) {
        Pair<UUID, List<Location>> subjectEntry = radarViewModel.getSubjectEntry().getValue();
        List<Pair<UUID, List<Location>>> guestEntries = radarViewModel.getGuestEntries().getValue();
        if (subjectEntry == null || guestEntries == null) {
            return;
        }

        guestEntries.add(subjectEntry);
        guestEntries = separateEntries(subject, guestEntries);

        Long time = radarViewModel.getSubjectTime().getValue();
        List<Location> subjectLocs = subjectEntry.second;
        if (subjectLocs != null && time != null) {
            radarViewModel.setGuestLocations(getGuestLocations(guestEntries, time));
            updateGuestRelatives(radarViewModel.getGuestLocations().getValue(), time);
        }
    }

    public List<Pair<UUID, List<Location>>> separateEntries(UUID subject,
                                                            List<Pair<UUID, List<Location>>> userEntries) {
        for (int i = 0; i < userEntries.size(); i++) {
            Pair<UUID, List<Location>> userEntry = userEntries.get(i);
            if (userEntry.first != null &&
                    userEntry.first.equals(subject)) {
                radarViewModel.setSubjectEntry(userEntry);
                userEntries.remove(i);
                break;
            }
        }

        Pair<UUID, List<Location>> subjectEntry = radarViewModel.getSubjectEntry().getValue();
        // None of the users were the subject.
        if (subjectEntry == null || subjectEntry.second == null) {
            Log.e(TAG, "Subject does not exist in jump data.");
            return null;
        }

        return userEntries;
    }

    public void generateJumpPositions(int jumpId, UUID subject) {
        FetchUsersAndPositionsTask.Listener listener = userEntries -> {
            if (userEntries == null) {
                return;
            }

            radarViewModel.setGuestEntries(separateEntries(subject, userEntries));
            Pair<UUID, List<Location>> subjectEntry = radarViewModel.getSubjectEntry().getValue();
            List<Pair<UUID, List<Location>>> guestEntries = radarViewModel.getGuestEntries().getValue();
            if (subjectEntry != null && guestEntries != null) {
                List<Location> subjectLocs = subjectEntry.second;
                if (subjectLocs != null) {
                    long startTime;
                    Long time = radarViewModel.getSubjectTime().getValue();
                    if (time != null) {
                        startTime = time;
                    } else {
                        startTime = subjectLocs.get(0).getTime();
                    }
                    radarViewModel.setGuestLocations(getGuestLocations(guestEntries, startTime));
                    updateGuestRelatives(radarViewModel.getGuestLocations().getValue(), startTime);
                }
            }
        };
        new FetchUsersAndPositionsTask(listener, databaseViewModel).execute(jumpId);
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
        radarViewModel.setSubjectTime(time);
    }

    private void subscribeToTime() {
        final Observer<Long> timeObserver = time -> {
            if (time != null) {
                List<Pair<UUID, List<Location>>> guestEntries = radarViewModel.getGuestEntries().getValue();
                if (guestEntries != null) {
                    radarViewModel.setGuestLocations(getGuestLocations(guestEntries, time));
                    updateGuestRelatives(radarViewModel.getGuestLocations().getValue(), time);
                }
            }
        };
        radarViewModel.getSubjectTime().observe(fragment, timeObserver);
    }

    private void subscribeToButtonData() {
        final Observer<Integer> buttonDataObserver = ignored -> {
            Integer jumpId = radarViewModel.getJumpId().getValue();
            Integer firstJumpId = radarViewModel.getFirstJumpId().getValue();
            Integer lastJumpId = radarViewModel.getLastJumpId().getValue();

            if (BuildConfig.DEBUG) {
                Log.d(TAG, "Button data: " + jumpId + ", " + firstJumpId + ", " + lastJumpId);
            }

            if (jumpId != null && firstJumpId != null && lastJumpId != null) {
                fragment.updateButtons(jumpId, firstJumpId, lastJumpId);
            }
        };
        radarViewModel.getJumpId().observe(fragment, buttonDataObserver);
        radarViewModel.getFirstJumpId().observe(fragment, buttonDataObserver);
        radarViewModel.getLastJumpId().observe(fragment, buttonDataObserver);
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

    public void updateGuestRelatives(List<Pair<UUID, Location>> locations, long time) {
        List<Double> guestHeightDiffs = radarViewModel.getGuestHeightDiffs().getValue();
        List<Pair<Float, Float>> relativeGuestPositions =
                radarViewModel.getRelativeGuestPositions().getValue();
        Pair<UUID, List<Location>> subjectEntry = radarViewModel.getSubjectEntry().getValue();
        List<UUID> guestsInView = radarViewModel.getGuestsInView().getValue();

        if (guestHeightDiffs == null || relativeGuestPositions == null
                || subjectEntry == null || guestsInView == null) {
            return;
        }
        List<Location> subjectLocs = subjectEntry.second;

        if (subjectLocs == null || subjectLocs.isEmpty() || locations.isEmpty()) {
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

        guestsInView.clear();
        relativeGuestPositions.clear();
        guestHeightDiffs.clear();

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
                    guestsInView.add(guest.first);
                    relativeGuestPositions.add(new Pair<>(bearingTo, hDistanceTo));
                    guestHeightDiffs.add(vDistanceTo);
                }
            }
        }

        fragment.updateRadarPoints();
    }

    public int getMaxVDistance() {
        return maxVDistance;
    }

    public int getMaxHDistance() {
        return maxHDistance;
    }

    public void prevJump() {
        Integer jumpId = radarViewModel.getJumpId().getValue();
        Integer firstJumpId = radarViewModel.getFirstJumpId().getValue();

        if (jumpId != null && firstJumpId != null) {
            if (jumpId > firstJumpId) {
                Log.d(TAG, "Setting jump ID to " + (jumpId - 1));
                radarViewModel.setJumpId(jumpId - 1);
            }
        }
    }

    public void nextJump() {
        Integer jumpId = radarViewModel.getJumpId().getValue();
        Integer lastJumpId = radarViewModel.getLastJumpId().getValue();

        if (jumpId != null && lastJumpId != null) {
            if (jumpId < lastJumpId) {
                Log.d(TAG, "Setting jump ID to " + (jumpId + 1));
                radarViewModel.setJumpId(jumpId + 1);
            }
        }
    }
}

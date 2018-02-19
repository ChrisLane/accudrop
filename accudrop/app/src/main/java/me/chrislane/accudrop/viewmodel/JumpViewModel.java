package me.chrislane.accudrop.viewmodel;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.util.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import me.chrislane.accudrop.db.AccudropDb;
import me.chrislane.accudrop.db.Jump;
import me.chrislane.accudrop.db.Position;

public class JumpViewModel extends AndroidViewModel {

    private final AccudropDb db;

    public JumpViewModel(@NonNull Application application) {
        super(application);

        db = AccudropDb.getDatabase(application);
    }

    /**
     * Find the last jump ID.
     *
     * @return A <code>LiveData</code> object containing the last jump ID.
     */
    public LiveData<Integer> findLastJumpId() {
        return db.jumpModel().findLastJumpId();
    }

    /**
     * Get the last jump ID.
     *
     * @return The last jump ID.
     */
    @Nullable
    public Integer getLastJumpId() {
        return db.jumpModel().getLastJumpId();
    }

    /**
     * Add a jump to the database.
     *
     * @param jump The jump to add to the database.
     */
    public void addJump(Jump jump) {
        db.jumpModel().insertJump(jump);
    }

    /**
     * Get positions for a jump.
     *
     * @param jumpId The jump ID.
     * @return A list of positions for the jump ID.
     */
    public List<Position> getPositionsForJump(int jumpId) {
        return db.locationModel().getLocationsByJumpNumber(jumpId);
    }

    /**
     * Get the maximum latitude for a jump.
     *
     * @param jumpId The jump ID.
     * @return The maximum latitude for a jump.
     */
    @Nullable
    public Double getMaxLatitudeForJump(int jumpId) {
        return db.locationModel().getMaxLatitudeByJumpNumber(jumpId);
    }

    /**
     * Get the minimum latitude for a jump.
     *
     * @param jumpId The jump ID.
     * @return The minimum latitude for a jump.
     */
    @Nullable
    public Double getMinLatitudeForJump(int jumpId) {
        return db.locationModel().getMinLatitudeByJumpNumber(jumpId);
    }

    /**
     * Get the maximum longitude for a jump.
     *
     * @param jumpId The jump ID.
     * @return The maximum longitude for a jump.
     */
    @Nullable
    public Double getMaxLongitudeForJump(int jumpId) {
        return db.locationModel().getMaxLongitudeByJumpNumber(jumpId);
    }

    /**
     * Get the minimum longitude for a jump.
     *
     * @param jumpId The jump ID.
     * @return The maximum longitude for a jump.
     */
    @Nullable
    public Double getMinLongitudeForJump(int jumpId) {
        return db.locationModel().getMinLongitudeByJumpNumber(jumpId);
    }

    /**
     * Get the maximum altitude for a jump.
     *
     * @param jumpId The jump ID.
     * @return The maximum altitude for a jump.
     */
    @Nullable
    public Integer getMaxAltitudeForJump(int jumpId) {
        return db.locationModel().getMaxAltitudeByJumpNumber(jumpId);
    }

    /**
     * Get the minimum altitude for a jump.
     *
     * @param jumpId The jump ID.
     * @return The minimum altitude for a jump.
     */
    @Nullable
    public Integer getMinAltitudeForJump(int jumpId) {
        return db.locationModel().getMinAltitudeByJumpNumber(jumpId);
    }

    /**
     * Get all users with data for a jump.
     *
     * @param jumpId The jump ID.
     * @return A list of users.
     */
    public List<UUID> getUsersForJump(int jumpId) {
        List<String> dbResult = db.locationModel().getUsersForJump(jumpId);
        List<UUID> uuids = new ArrayList<>();
        for (String uuidString : dbResult) {
            uuids.add(UUID.fromString(uuidString));
        }

        return uuids;
    }

    /**
     * Get the positions of a user during a jump.
     *
     * @param uuid   The user ID.
     * @param jumpId The jump ID.
     * @return A list of positions for a user during a jump.
     */
    public List<Position> getPositionsForUserForJump(UUID uuid, int jumpId) {
        return db.locationModel().getLocationsByUserByJumpNumber(uuid, jumpId);
    }

    /**
     * Get a list of users and their positions for a jump.
     *
     * @param jumpId The jump ID.
     * @return A list of users and their positions for a jump.
     */
    @Nullable
    public List<Pair<UUID, List<Location>>> getUsersAndPositionsForJump(int jumpId) {
        List<Pair<UUID, List<Location>>> result = new ArrayList<>();

        // Get users in a jump
        List<UUID> users = getUsersForJump(jumpId);

        // Get positions for each user and add to return value
        for (UUID user : users) {
            List<Position> positions = getPositionsForUserForJump(user, jumpId);
            List<Location> locations = new ArrayList<>();
            for (Position position : positions) {
                Location location = new Location("");
                location.setLatitude(position.latitude);
                location.setLongitude(position.longitude);
                location.setAltitude(position.altitude);
                location.setTime(position.time.getTime());
                locations.add(location);
            }
            result.add(new Pair<>(user, locations));
        }

        return result;
    }

    /**
     * Get whether a jump with a given ID exists in the database.
     *
     * @param jumpId The jump ID to check for.
     * @return Whether a jump with the given ID exists in the database.
     */
    @Nullable
    public Boolean jumpExists(int jumpId) {
        return db.jumpModel().jumpExists(jumpId);
    }

    /**
     * Add a jump position to the database.
     *
     * @param position The position to add to the database.
     */
    public void addPosition(Position position) {
        db.locationModel().insertPosition(position);
    }
}

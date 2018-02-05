package me.chrislane.accudrop.viewmodel;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.List;

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

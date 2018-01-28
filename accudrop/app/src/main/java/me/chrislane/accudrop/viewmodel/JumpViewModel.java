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

    public LiveData<Integer> findLastJumpId() {
        return db.jumpModel().findLastJumpId();
    }

    @Nullable
    public Integer getLastJumpId() {
        return db.jumpModel().getLastJumpId();
    }

    public void addJump(Jump jump) {
        db.jumpModel().insertJump(jump);
    }

    public List<Position> getPositionsForJump(int jumpId) {
        return db.locationModel().getLocationsByJumpNumber(jumpId);
    }

    @Nullable
    public Double getMaxLatitudeForJump(int jumpId) {
        return db.locationModel().getMaxLatitudeByJumpNumber(jumpId);
    }

    @Nullable
    public Double getMinLatitudeForJump(int jumpId) {
        return db.locationModel().getMinLatitudeByJumpNumber(jumpId);
    }

    @Nullable
    public Double getMaxLongitudeForJump(int jumpId) {
        return db.locationModel().getMaxLongitudeByJumpNumber(jumpId);
    }

    @Nullable
    public Double getMinLongitudeForJump(int jumpId) {
        return db.locationModel().getMinLongitudeByJumpNumber(jumpId);
    }

    @Nullable
    public Integer getMaxAltitudeForJump(int jumpId) {
        return db.locationModel().getMaxAltitudeByJumpNumber(jumpId);
    }

    @Nullable
    public Integer getMinAltitudeForJump(int jumpId) {
        return db.locationModel().getMinAltitudeByJumpNumber(jumpId);
    }

    public void addPosition(Position position) {
        db.locationModel().insertPosition(position);
    }
}

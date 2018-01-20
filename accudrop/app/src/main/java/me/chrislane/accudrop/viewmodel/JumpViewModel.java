package me.chrislane.accudrop.viewmodel;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.support.annotation.NonNull;

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

    public Integer getLastJumpId() {
        return db.jumpModel().getLastJumpId();
    }

    public void addJump(Jump jump) {
        db.jumpModel().insertJump(jump);
    }

    public List<Position> getPositionsForJump(int jumpId) {
        return db.locationModel().getLocationsByJumpNumber(jumpId);
    }
}

package me.chrislane.accudrop.viewmodel;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.support.annotation.NonNull;

import me.chrislane.accudrop.db.AccudropDb;

public class JumpViewModel extends AndroidViewModel {
    private AccudropDb db;

    public JumpViewModel(@NonNull Application application) {
        super(application);

        db = AccudropDb.getDatabase(application);
    }

    public LiveData<Integer> getLastJumpId() {
        return db.jumpModel().findLastJumpId();
    }
}

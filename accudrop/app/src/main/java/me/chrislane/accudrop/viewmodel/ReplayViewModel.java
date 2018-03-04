package me.chrislane.accudrop.viewmodel;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.location.Location;
import android.support.v4.util.Pair;

import java.util.List;
import java.util.UUID;

public class ReplayViewModel extends ViewModel {

    private MutableLiveData<Integer> jumpId = new MutableLiveData<>();
    private MutableLiveData<Integer> firstJumpId = new MutableLiveData<>();
    private MutableLiveData<Integer> lastJumpId = new MutableLiveData<>();
    private MutableLiveData<List<Pair<UUID, List<Location>>>> usersAndLocs = new MutableLiveData<>();

    /**
     * Get the jump ID for the replay.
     *
     * @return The jump ID for the replay.
     */
    public LiveData<Integer> getJumpId() {
        return jumpId;
    }

    /**
     * Set the jump ID for the replay.
     *
     * @param jumpId The jump ID for the replay.
     */
    public void setJumpId(int jumpId) {
        this.jumpId.setValue(jumpId);
    }

    public LiveData<List<Pair<UUID, List<Location>>>> getUsersAndLocs() {
        return usersAndLocs;
    }

    public void setUsersAndLocs(List<Pair<UUID, List<Location>>> usersAndLocs) {
        this.usersAndLocs.setValue(usersAndLocs);
    }

    public void setFirstJumpId(int firstJumpId) {
        this.firstJumpId.setValue(firstJumpId);
    }

    public void setLastJumpId(int lastJumpId) {
        this.lastJumpId.setValue(lastJumpId);
    }

    public LiveData<Integer> getLastJumpId() {
        return lastJumpId;
    }

    public MutableLiveData<Integer> getFirstJumpId() {
        return firstJumpId;
    }
}

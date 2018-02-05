package me.chrislane.accudrop.viewmodel;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;

public class ReplayViewModel extends ViewModel {

    private MutableLiveData<Integer> jumpId = new MutableLiveData<>();

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
}

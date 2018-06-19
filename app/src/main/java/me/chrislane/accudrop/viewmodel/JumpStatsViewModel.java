package me.chrislane.accudrop.viewmodel;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;

public class JumpStatsViewModel extends ViewModel {

    private MutableLiveData<Integer> jumpId = new MutableLiveData<>();
    private MutableLiveData<Integer> firstJumpId = new MutableLiveData<>();
    private MutableLiveData<Integer> lastJumpId = new MutableLiveData<>();

    public void setJumpId(int jumpId) {
        this.jumpId.setValue(jumpId);
    }

    public LiveData<Integer> getJumpId() {
        return jumpId;
    }

    public void setFirstJumpId(int firstJumpId) {
        this.firstJumpId.setValue(firstJumpId);
    }

    public void setLastJumpId(int lastJumpId) {
        this.lastJumpId.setValue(lastJumpId);
    }

    public LiveData<Integer> getFirstJumpId() {
        return firstJumpId;
    }

    public LiveData<Integer> getLastJumpId() {
        return lastJumpId;
    }
}

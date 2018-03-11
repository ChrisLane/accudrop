package me.chrislane.accudrop.viewmodel;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;

public class JumpStatsViewModel extends ViewModel {

    private MutableLiveData<Integer> jumpId = new MutableLiveData<>();

    public void setJumpId(int jumpId) {
        this.jumpId.setValue(jumpId);
    }

    public LiveData<Integer> getJumpId() {
        return jumpId;
    }
}

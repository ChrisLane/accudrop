package me.chrislane.accudrop.viewmodel

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel

class JumpStatsViewModel : ViewModel() {

    private val jumpId = MutableLiveData<Int>()
    private val firstJumpId = MutableLiveData<Int>()
    private val lastJumpId = MutableLiveData<Int>()

    fun setJumpId(jumpId: Int) {
        this.jumpId.value = jumpId
    }

    fun getJumpId(): LiveData<Int> {
        return jumpId
    }

    fun setFirstJumpId(firstJumpId: Int) {
        this.firstJumpId.value = firstJumpId
    }

    fun setLastJumpId(lastJumpId: Int) {
        this.lastJumpId.value = lastJumpId
    }

    fun getFirstJumpId(): LiveData<Int> {
        return firstJumpId
    }

    fun getLastJumpId(): LiveData<Int> {
        return lastJumpId
    }
}

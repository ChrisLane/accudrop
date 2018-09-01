package me.chrislane.accudrop.viewmodel

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel


class JumpViewModel : ViewModel() {

    private val jumpId = MutableLiveData<Int>()

    fun getJumpId(): LiveData<Int> {
        return jumpId
    }

    fun setJumpId(jumpId: Int) {
        this.jumpId.value = jumpId
    }
}

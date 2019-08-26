package me.chrislane.accudrop.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel


class JumpViewModel : ViewModel() {

    private val jumpId = MutableLiveData<Int>()

    fun getJumpId(): LiveData<Int> {
        return jumpId
    }

    fun setJumpId(jumpId: Int) {
        this.jumpId.value = jumpId
    }
}

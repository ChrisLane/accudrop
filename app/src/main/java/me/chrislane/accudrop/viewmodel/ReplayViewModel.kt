package me.chrislane.accudrop.viewmodel

import android.location.Location
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import java.util.*

class ReplayViewModel : ViewModel() {

    private val jumpId = MutableLiveData<Int>()
    private val firstJumpId = MutableLiveData<Int>()
    private val lastJumpId = MutableLiveData<Int>()
    private val usersAndLocs = MutableLiveData<MutableList<Pair<UUID, MutableList<Location>>>>()

    /**
     * Get the jump ID for the replay.
     *
     * @return The jump ID for the replay.
     */
    fun getJumpId(): LiveData<Int> {
        return jumpId
    }

    /**
     * Set the jump ID for the replay.
     *
     * @param jumpId The jump ID for the replay.
     */
    fun setJumpId(jumpId: Int) {
        this.jumpId.value = jumpId
    }

    fun getUsersAndLocs(): LiveData<MutableList<Pair<UUID, MutableList<Location>>>> {
        return usersAndLocs
    }

    fun setUsersAndLocs(usersAndLocs: MutableList<Pair<UUID, MutableList<Location>>>) {
        this.usersAndLocs.value = usersAndLocs
    }

    fun setFirstJumpId(firstJumpId: Int) {
        this.firstJumpId.value = firstJumpId
    }

    fun setLastJumpId(lastJumpId: Int) {
        this.lastJumpId.value = lastJumpId
    }

    fun getLastJumpId(): LiveData<Int> {
        return lastJumpId
    }

    fun getFirstJumpId(): LiveData<Int> {
        return firstJumpId
    }
}

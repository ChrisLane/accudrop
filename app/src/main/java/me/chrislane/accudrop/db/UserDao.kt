package me.chrislane.accudrop.db

import android.arch.lifecycle.LiveData
import android.arch.persistence.room.Dao
import android.arch.persistence.room.Query
import java.util.*

@Dao
interface UserDao {

    @Query("SELECT * FROM user")
    fun findAllUsers(): LiveData<User>

    @Query("SELECT name FROM user " + "WHERE uuid = :uuid")
    fun findNameFromUUID(uuid: UUID): LiveData<String>
}

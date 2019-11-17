package me.chrislane.accudrop.db

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Query
import java.util.*

@Dao
interface UserDao {

  @Query("SELECT * FROM user")
  fun findAllUsers(): LiveData<User>

  @Query("SELECT first_name, last_name FROM user WHERE uuid = :uuid")
  fun findNameFromUUID(uuid: UUID): LiveData<String>

}

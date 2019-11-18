package me.chrislane.accudrop.db

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import java.util.*

@Dao
interface UserDao {

  @Query("SELECT * FROM user")
  fun findAllUsers(): LiveData<User>

  @Query("SELECT * FROM user WHERE uuid = :uuid")
  fun getUserById(uuid: UUID): User

  @Insert
  fun insertUser(user: User)

}

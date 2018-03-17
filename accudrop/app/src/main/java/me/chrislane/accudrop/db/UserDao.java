package me.chrislane.accudrop.db;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Query;

import java.util.UUID;

@Dao
public interface UserDao {

    @Query("SELECT * FROM user")
    LiveData<User> findAllUsers();

    @Query("SELECT name FROM user " +
            "WHERE uuid = :uuid")
    LiveData<String> findNameFromUUID(UUID uuid);
}

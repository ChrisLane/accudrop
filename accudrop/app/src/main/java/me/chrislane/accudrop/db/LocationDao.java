package me.chrislane.accudrop.db;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import java.util.List;

@Dao
public interface LocationDao {

    @Query("SELECT * FROM Location")
    LiveData<List<Location>> findAllLocations();

    @Query("SELECT * FROM Location " +
            "WHERE jump_id = :jumpNumber ")
    LiveData<List<Location>> findLocationsByJumpNumber(int jumpNumber);

    @Insert
    void insertLocation(Location location);

    @Query("DELETE FROM Location")
    void deleteAll();
}
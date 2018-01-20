package me.chrislane.accudrop.db;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import java.util.List;

@Dao
public interface PositionDao {

    @Query("SELECT * FROM Position")
    LiveData<List<Position>> findAllLocations();

    @Query("SELECT * FROM Position " +
            "WHERE jump_id = :jumpNumber ")
    LiveData<List<Position>> findLocationsByJumpNumber(int jumpNumber);

    @Query("SELECT * FROM Position " +
            "WHERE jump_id = :jumpNumber ")
    List<Position> getLocationsByJumpNumber(int jumpNumber);

    @Insert
    void insertPosition(Position position);

    @Query("DELETE FROM Position")
    void deleteAll();
}
package me.chrislane.accudrop.db;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import java.util.List;

@Dao
public interface PositionDao {

    @Query("SELECT * FROM position")
    LiveData<List<Position>> findAllLocations();

    @Query("SELECT * FROM position " +
            "WHERE jump_id = :jumpNumber ")
    LiveData<List<Position>> findLocationsByJumpNumber(int jumpNumber);

    @Query("SELECT * FROM position " +
            "WHERE jump_id = :jumpNumber ")
    List<Position> getLocationsByJumpNumber(int jumpNumber);

    @Query("SELECT MAX(latitude) FROM position " +
            "WHERE jump_id = :jumpNumber")
    Double getMaxLatitudeByJumpNumber(int jumpNumber);

    @Query("SELECT MIN(longitude) FROM position " +
            "WHERE jump_id = :jumpNumber")
    Double getMinLatitudeByJumpNumber(int jumpNumber);

    @Query("SELECT MAX(longitude) FROM position " +
            "WHERE jump_id = :jumpNumber")
    Double getMaxLongitudeByJumpNumber(int jumpNumber);

    @Query("SELECT MIN(longitude) FROM position " +
            "WHERE jump_id = :jumpNumber")
    Double getMinLongitudeByJumpNumber(int jumpNumber);

    @Query("SELECT MAX(altitude) FROM position " +
            "WHERE jump_id = :jumpNumber")
    Integer getMaxAltitudeByJumpNumber(int jumpNumber);

    @Query("SELECT MIN(altitude) FROM position " +
            "WHERE jump_id = :jumpNumber")
    Integer getMinAltitudeByJumpNumber(int jumpNumber);

    @Insert
    void insertPosition(Position position);

    @Query("DELETE FROM Position")
    void deleteAll();
}
package me.chrislane.accudrop.db;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import java.util.Date;
import java.util.List;
import java.util.UUID;

@Dao
public interface PositionDao {

    /**
     * Find all location records.
     *
     * @return A <code>LiveData</code> object containing a list of all position records.
     */
    @Query("SELECT * FROM position")
    LiveData<List<Position>> findAllLocations();

    /**
     * Find location records for a jump number.
     *
     * @param jumpNumber The jump number to find locations for.
     * @return A <code>LiveData</code> object containing a list of location records for the jump number.
     */
    @Query("SELECT * FROM position " +
            "WHERE jump_id = :jumpNumber ")
    LiveData<List<Position>> findLocationsByJumpNumber(int jumpNumber);

    /**
     * Get location records for a jump number.
     *
     * @param jumpNumber The jump number to find locations for.
     * @return A list of location records for the jump number.
     */
    @Query("SELECT * FROM position " +
            "WHERE jump_id = :jumpNumber ")
    List<Position> getLocationsByJumpNumber(int jumpNumber);


    @Query("SELECT useruuid FROM position " +
            "WHERE jump_id = :jumpNumber " +
            "GROUP BY useruuid")
    List<String> getUsersForJump(int jumpNumber);

    /**
     * Get the maximum latitude for a jump number.
     *
     * @param jumpNumber The jump number to find the maximum latitude for.
     * @return The maximum latitude for a jump number.
     */
    @Query("SELECT MAX(latitude) FROM position " +
            "WHERE jump_id = :jumpNumber")
    Double getMaxLatitudeByJumpNumber(int jumpNumber);

    /**
     * Get the minimum latitude for a jump number.
     *
     * @param jumpNumber The jump number to find the minimum latitude for.
     * @return The minimum latitude for a jump number.
     */
    @Query("SELECT MIN(latitude) FROM position " +
            "WHERE jump_id = :jumpNumber")
    Double getMinLatitudeByJumpNumber(int jumpNumber);

    /**
     * Get the maximum longitude for a jump number.
     *
     * @param jumpNumber The jump number to find the maximum longitude for.
     * @return The maximum longitude for a jump number.
     */
    @Query("SELECT MAX(longitude) FROM position " +
            "WHERE jump_id = :jumpNumber")
    Double getMaxLongitudeByJumpNumber(int jumpNumber);

    /**
     * Get the minimum longitude for a jump number.
     *
     * @param jumpNumber The jump number to find the minimum longitude for.
     * @return The minimum longitude for a jump number.
     */
    @Query("SELECT MIN(longitude) FROM position " +
            "WHERE jump_id = :jumpNumber")
    Double getMinLongitudeByJumpNumber(int jumpNumber);

    /**
     * Get the maximum altitude for a jump number.
     *
     * @param jumpNumber The jump number to find the maximum altitude for.
     * @return The maximum altitude for a jump number.
     */
    @Query("SELECT MAX(altitude) FROM position " +
            "WHERE jump_id = :jumpNumber")
    Integer getMaxAltitudeByJumpNumber(int jumpNumber);

    /**
     * Get the minimum altitude for a jump number.
     *
     * @param jumpNumber The jump number to find the minimum altitude for.
     * @return The minimum altitude for a jump number.
     */
    @Query("SELECT MIN(altitude) FROM position " +
            "WHERE jump_id = :jumpNumber")
    Integer getMinAltitudeByJumpNumber(int jumpNumber);

    /**
     * Insert a new position.
     *
     * @param position The position to insert.
     */
    @Insert
    void insertPosition(Position position);

    /**
     * Delete all position records.
     */
    @Query("DELETE FROM Position")
    void deleteAll();

    /**
     * Gets a list of positions ordered by ascending timestamp for a user for a jump.
     *
     * @param uuid       The user's UUID.
     * @param jumpNumber The jump number.
     * @return A list of positions ordered by timestamp.
     */
    @Query("SELECT * FROM position " +
            "WHERE useruuid = :uuid AND jump_id = :jumpNumber " +
            "ORDER BY time ASC")
    List<Position> getOrderedLocationsByUserByJumpNumber(UUID uuid, int jumpNumber);

    @Query("SELECT MIN(time) FROM position " +
            "WHERE useruuid = :uuid AND jump_id = :jumpNumber")
    Date getFirstDateForUser(UUID uuid, int jumpNumber);

    @Query("SELECT MAX(time) FROM position " +
            "WHERE useruuid = :uuid AND jump_id = :jumpNumber")
    Date getLastDateForUser(UUID uuid, int jumpNumber);

    @Query("SELECT MIN(time) FROM position " +
            "WHERE fallType = :fallType AND useruuid = :uuid AND jump_id = :jumpNumber")
    Date getFirstDateForUser(FallType fallType, UUID uuid, int jumpNumber);

    @Query("SELECT MAX(time) FROM position " +
            "WHERE fallType = :fallType AND useruuid = :uuid AND jump_id = :jumpNumber")
    Date getLastDateForUser(FallType fallType, UUID uuid, int jumpNumber);

    @Query("SELECT MAX(vspeed) FROM position " +
            "WHERE fallType = :fallType AND useruuid = :uuid AND jump_id = :jumpNumber ")
    Double getMaxVSpeed(FallType fallType, UUID uuid, int jumpNumber);

    @Query("SELECT MAX(hspeed) FROM position " +
            "WHERE fallType = :fallType AND useruuid = :uuid AND jump_id = :jumpNumber")
    Float getMaxHSpeed(FallType fallType, UUID uuid, int jumpNumber);
}
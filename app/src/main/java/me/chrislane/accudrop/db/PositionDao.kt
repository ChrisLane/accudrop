package me.chrislane.accudrop.db

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import java.util.*

@Dao
interface PositionDao {

    /**
     * Find all location records.
     *
     * @return A `LiveData` object containing a list of all position records.
     */
    @Query("SELECT * FROM position")
    fun findAllLocations(): LiveData<MutableList<Position>>

    /**
     * Find location records for a jump number.
     *
     * @param jumpNumber The jump number to find locations for.
     * @return A `LiveData` object containing a list of location records for the jump number.
     */
    @Query("SELECT * FROM position WHERE jump_id = :jumpNumber ")
    fun findLocationsByJumpNumber(jumpNumber: Int): LiveData<MutableList<Position>>

    /**
     * Get location records for a jump number.
     *
     * @param jumpNumber The jump number to find locations for.
     * @return A list of location records for the jump number.
     */
    @Query("SELECT * FROM position WHERE jump_id = :jumpNumber ")
    fun getLocationsByJumpNumber(jumpNumber: Int): MutableList<Position>


    @Query("SELECT user_id FROM position " +
            "WHERE jump_id = :jumpNumber " +
            "GROUP BY user_id")
    fun getUsersForJump(jumpNumber: Int): MutableList<String>

    /**
     * Get the maximum latitude for a jump number.
     *
     * @param jumpNumber The jump number to find the maximum latitude for.
     * @return The maximum latitude for a jump number.
     */
    @Query("SELECT MAX(latitude) FROM position WHERE jump_id = :jumpNumber")
    fun getMaxLatitudeByJumpNumber(jumpNumber: Int): Double?

    /**
     * Get the minimum latitude for a jump number.
     *
     * @param jumpNumber The jump number to find the minimum latitude for.
     * @return The minimum latitude for a jump number.
     */
    @Query("SELECT MIN(latitude) FROM position WHERE jump_id = :jumpNumber")
    fun getMinLatitudeByJumpNumber(jumpNumber: Int): Double?

    /**
     * Get the maximum longitude for a jump number.
     *
     * @param jumpNumber The jump number to find the maximum longitude for.
     * @return The maximum longitude for a jump number.
     */
    @Query("SELECT MAX(longitude) FROM position WHERE jump_id = :jumpNumber")
    fun getMaxLongitudeByJumpNumber(jumpNumber: Int): Double?

    /**
     * Get the minimum longitude for a jump number.
     *
     * @param jumpNumber The jump number to find the minimum longitude for.
     * @return The minimum longitude for a jump number.
     */
    @Query("SELECT MIN(longitude) FROM position WHERE jump_id = :jumpNumber")
    fun getMinLongitudeByJumpNumber(jumpNumber: Int): Double?

    /**
     * Get the maximum altitude for a jump number.
     *
     * @param jumpNumber The jump number to find the maximum altitude for.
     * @return The maximum altitude for a jump number.
     */
    @Query("SELECT MAX(altitude) FROM position WHERE jump_id = :jumpNumber")
    fun getMaxAltitudeByJumpNumber(jumpNumber: Int): Int?

    /**
     * Get the minimum altitude for a jump number.
     *
     * @param jumpNumber The jump number to find the minimum altitude for.
     * @return The minimum altitude for a jump number.
     */
    @Query("SELECT MIN(altitude) FROM position WHERE jump_id = :jumpNumber")
    fun getMinAltitudeByJumpNumber(jumpNumber: Int): Int?

    /**
     * Insert a new position.
     *
     * @param position The position to insert.
     */
    @Insert
    fun insertPosition(position: Position)

    /**
     * Delete all position records.
     */
    @Query("DELETE FROM Position")
    fun deleteAll()

    /**
     * Gets a list of positions ordered by ascending timestamp for a user for a jump.
     *
     * @param uuid       The user's UUID.
     * @param jumpNumber The jump number.
     * @return A list of positions ordered by timestamp.
     */
    @Query("SELECT * FROM position WHERE user_id = :uuid AND jump_id = :jumpNumber ORDER BY time ASC")
    fun getOrderedLocationsByUserByJumpNumber(uuid: UUID, jumpNumber: Int): MutableList<Position>

    /**
     * Gets a list of positions ordered by ascending timestamp for a user for a jump.
     *
     * @param fallType   The fall type to get locations of.
     * @param uuid       The user's UUID.
     * @param jumpNumber The jump number.
     * @return A list of positions ordered by timestamp.
     */
    @Query("SELECT * FROM position WHERE fall_type = :fallType AND user_id = :uuid AND jump_id = :jumpNumber ORDER BY time ASC")
    fun getOrderedTypeLocationsByUserByJumpNumber(fallType: FallType, uuid: UUID, jumpNumber: Int): MutableList<Position>

    @Query("SELECT MIN(time) FROM position WHERE user_id = :uuid AND jump_id = :jumpNumber")
    fun getFirstDateForUser(uuid: UUID, jumpNumber: Int): Date

    @Query("SELECT MAX(time) FROM position WHERE user_id = :uuid AND jump_id = :jumpNumber")
    fun getLastDateForUser(uuid: UUID, jumpNumber: Int): Date

    @Query("SELECT MIN(time) FROM position WHERE fall_type = :fallType AND user_id = :uuid AND jump_id = :jumpNumber")
    fun getFirstDateForUser(fallType: FallType, uuid: UUID, jumpNumber: Int): Date

    @Query("SELECT MAX(time) FROM position WHERE fall_type = :fallType AND user_id = :uuid AND jump_id = :jumpNumber")
    fun getLastDateForUser(fallType: FallType, uuid: UUID, jumpNumber: Int): Date

    @Query("SELECT MAX(vertical_speed) FROM position WHERE fall_type = :fallType AND user_id = :uuid AND jump_id = :jumpNumber ")
    fun getMaxVSpeed(fallType: FallType, uuid: UUID, jumpNumber: Int): Double?

    @Query("SELECT MAX(horizontal_speed) FROM position WHERE fall_type = :fallType AND user_id = :uuid AND jump_id = :jumpNumber")
    fun getMaxHSpeed(fallType: FallType, uuid: UUID, jumpNumber: Int): Float?
}
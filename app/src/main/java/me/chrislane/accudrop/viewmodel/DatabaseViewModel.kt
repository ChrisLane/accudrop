package me.chrislane.accudrop.viewmodel

import android.app.Application
import android.location.Location
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import me.chrislane.accudrop.db.*
import java.util.*

class DatabaseViewModel(application: Application) : AndroidViewModel(application) {
    private val db: AccuDropDb = AccuDropDb.getInstance(application)

    /**
     * Get the last jump ID.
     *
     * @return The last jump ID.
     */
    val lastJumpId: Int?
        get() = db.jumpModel().lastJumpId

    /**
     * Get the first jump ID.
     *
     * @return The first jump ID.
     */
    val firstJumpId: Int?
        get() = db.jumpModel().firstJumpId

    /**
     * Find the first jump ID.
     *
     * @return A `LiveData` object containing the first jump ID.
     */
    fun findFirstJumpId(): LiveData<Int> {
        return db.jumpModel().findFirstJumpId()
    }

    /**
     * Find the last jump ID.
     *
     * @return A `LiveData` object containing the last jump ID.
     */
    fun findLastJumpId(): LiveData<Int> {
        return db.jumpModel().findLastJumpId()
    }

    /**
     * Add a jump to the database.
     *
     * @param jump The jump to add to the database.
     */
    fun addJump(jump: Jump) {
        db.jumpModel().insertJump(jump)
    }

    fun addJump() {
        val lastJumpId = db.jumpModel().lastJumpId

        val jump = Jump(
            id = if (lastJumpId != null) lastJumpId + 1 else 1,
            time = Date())

        db.jumpModel().insertJump(jump)
    }

    /**
     * Get positions for a jump.
     *
     * @param jumpId The jump ID.
     * @return A list of positions for the jump ID.
     */
    fun getPositionsForJump(jumpId: Int): MutableList<Position> {
        return db.locationModel().getLocationsByJumpNumber(jumpId)
    }

    /**
     * Get the maximum latitude for a jump.
     *
     * @param jumpId The jump ID.
     * @return The maximum latitude for a jump.
     */
    fun getMaxLatitudeForJump(jumpId: Int): Double? {
        return db.locationModel().getMaxLatitudeByJumpNumber(jumpId)
    }

    /**
     * Get the minimum latitude for a jump.
     *
     * @param jumpId The jump ID.
     * @return The minimum latitude for a jump.
     */
    fun getMinLatitudeForJump(jumpId: Int): Double? {
        return db.locationModel().getMinLatitudeByJumpNumber(jumpId)
    }

    /**
     * Get the maximum longitude for a jump.
     *
     * @param jumpId The jump ID.
     * @return The maximum longitude for a jump.
     */
    fun getMaxLongitudeForJump(jumpId: Int): Double? {
        return db.locationModel().getMaxLongitudeByJumpNumber(jumpId)
    }

    /**
     * Get the minimum longitude for a jump.
     *
     * @param jumpId The jump ID.
     * @return The maximum longitude for a jump.
     */
    fun getMinLongitudeForJump(jumpId: Int): Double? {
        return db.locationModel().getMinLongitudeByJumpNumber(jumpId)
    }

    /**
     * Get the maximum altitude for a jump.
     *
     * @param jumpId The jump ID.
     * @return The maximum altitude for a jump.
     */
    fun getMaxAltitudeForJump(jumpId: Int): Int? {
        return db.locationModel().getMaxAltitudeByJumpNumber(jumpId)
    }

    /**
     * Get the minimum altitude for a jump.
     *
     * @param jumpId The jump ID.
     * @return The minimum altitude for a jump.
     */
    fun getMinAltitudeForJump(jumpId: Int): Int? {
        return db.locationModel().getMinAltitudeByJumpNumber(jumpId)
    }

    /**
     * Get all users with data for a jump.
     *
     * @param jumpId The jump ID.
     * @return A list of users.
     */
    fun getUsersForJump(jumpId: Int): MutableList<UUID> {
        val dbResult = db.locationModel().getUsersForJump(jumpId)
        val uuids = mutableListOf<UUID>()
        for (uuidString in dbResult) {
            uuids.add(UUID.fromString(uuidString))
        }

        return uuids
    }

    /**
     * Get the positions of a user during a jump.
     *
     * @param uuid   The user ID.
     * @param jumpId The jump ID.
     * @return A list of positions for a user during a jump.
     */
    fun getPositionsForUserForJump(uuid: UUID, jumpId: Int): MutableList<Position> {
        return db.locationModel().getOrderedLocationsByUserByJumpNumber(uuid, jumpId)
    }

    /**
     * Get the positions of a user during a jump.
     *
     * @param fallType The fall type to get locations of.
     * @param uuid     The user ID.
     * @param jumpId   The jump ID.
     * @return A list of positions for a user during a jump.
     */
    fun getTypePositionsForUserForJump(fallType: FallType, uuid: UUID, jumpId: Int): MutableList<Position> {
        return db.locationModel().getOrderedTypeLocationsByUserByJumpNumber(fallType, uuid, jumpId)
    }

    /**
     * Get a list of users and their positions for a jump.
     *
     * @param jumpId The jump ID.
     * @return A list of users and their positions for a jump.
     */
    fun getUsersAndPositionsForJump(jumpId: Int): MutableList<Pair<UUID, MutableList<Location>>>? {
        val result = mutableListOf<Pair<UUID, MutableList<Location>>>()

        // Get users in a jump
        val users = getUsersForJump(jumpId)

        // Get positions for each user and add to return value
        for (user in users) {
            val positions = getPositionsForUserForJump(user, jumpId)
            val locations = mutableListOf<Location>()
            for (position in positions) {
                if (position.latitude != null && position.longitude != null &&
                        position.altitude != null) {
                    val location = Location("")
                    location.latitude = position.latitude!!
                    location.longitude = position.longitude!!
                    location.altitude = position.altitude!!.toDouble()
                    location.time = position.time!!.time
                    locations.add(location)
                }
            }
            result.add(Pair(user, locations))
        }

        return result
    }

    /**
     * Get a list of users and their positions for a jump.
     *
     * @param jumpId The jump ID.
     * @return A list of users and their positions for a jump.
     */
    fun getUsersAndTypePositionsForJump(fallType: FallType,
                                        jumpId: Int): MutableList<Pair<UUID, MutableList<Location>>>? {
        val result = mutableListOf<Pair<UUID, MutableList<Location>>>()

        // Get users in a jump
        val users = getUsersForJump(jumpId)

        // Get positions for each user and add to return value
        for (user in users) {
            val positions = getTypePositionsForUserForJump(fallType, user, jumpId)
            val locations = mutableListOf<Location>()
            for (position in positions) {
                if (position.latitude != null && position.longitude != null &&
                        position.altitude != null) {
                    val location = Location("")
                    location.latitude = position.latitude!!
                    location.longitude = position.longitude!!
                    location.altitude = position.altitude!!.toDouble()
                    location.time = position.time!!.time
                    locations.add(location)
                }
            }
            result.add(Pair(user, locations))
        }

        return result
    }

    /**
     * Get whether a jump with a given ID exists in the database.
     *
     * @param jumpId The jump ID to check for.
     * @return Whether a jump with the given ID exists in the database.
     */
    fun jumpExists(jumpId: Int): Boolean? {
        return db.jumpModel().jumpExists(jumpId)
    }

    /**
     * Add a jump position to the database.
     *
     * @param position The position to add to the database.
     */
    fun addPosition(position: Position) {
        db.locationModel().insertPosition(position)
    }

    fun getFirstDate(uuid: UUID, jumpId: Int): Date? {
        return db.locationModel().getFirstDateForUser(uuid, jumpId)
    }

    fun getLastDate(uuid: UUID, jumpId: Int): Date? {
        return db.locationModel().getLastDateForUser(uuid, jumpId)
    }

    fun getFirstDateOfFallType(fallType: FallType, uuid: UUID, jumpId: Int): Date? {
        return db.locationModel().getFirstDateForUser(fallType, uuid, jumpId)
    }

    fun getLastDateOfFallType(fallType: FallType, uuid: UUID, jumpId: Int): Date? {
        return db.locationModel().getLastDateForUser(fallType, uuid, jumpId)
    }

    fun getMaxVSpeedOfFallType(fallType: FallType, uuid: UUID, jumpId: Int): Double? {
        return db.locationModel().getMaxVSpeed(fallType, uuid, jumpId)
    }

    fun getMaxHSpeedOfFallType(fallType: FallType, uuid: UUID, jumpId: Int): Float? {
        return db.locationModel().getMaxHSpeed(fallType, uuid, jumpId)
    }

    fun deleteJump(jumpId: Int) {
        db.jumpModel().deleteJump(jumpId)
    }

    fun addUser(user: User) {
        db.userModel().insertUser(user)
    }

    fun getUserById(uuid: UUID): User {
        return db.userModel().getUserById(uuid)
    }

    companion object {
        private val TAG = DatabaseViewModel::class.java.simpleName
    }
}

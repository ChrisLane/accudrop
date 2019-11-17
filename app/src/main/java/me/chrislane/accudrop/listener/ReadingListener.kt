package me.chrislane.accudrop.listener

import android.app.Application
import android.content.Context
import android.location.Location
import android.os.AsyncTask
import android.util.Log
import androidx.preference.PreferenceManager
import me.chrislane.accudrop.BuildConfig
import me.chrislane.accudrop.db.FallType
import me.chrislane.accudrop.db.Position
import me.chrislane.accudrop.network.CoordSender
import me.chrislane.accudrop.viewmodel.DatabaseViewModel
import me.chrislane.accudrop.viewmodel.GnssViewModel
import me.chrislane.accudrop.viewmodel.PressureViewModel
import java.util.*

class ReadingListener(private val gnssViewModel: GnssViewModel, private val pressureViewModel: PressureViewModel,
                      private val databaseViewModel: DatabaseViewModel) {
    private val isGuidanceEnabled: Boolean
    private var logging = false
    private var coordSender: CoordSender? = null
    private var jumpId: Int? = null
    private var prevAlt: Float? = null
    private var prevTime: Long? = null
    private var vSpeed: Double? = null
    private var hasFreefallen = false
    private var isUnderCanopy = false
    private val fallToggle = 20
    private val canopyToggle = 15

    init {

        val preferences = PreferenceManager
                .getDefaultSharedPreferences(databaseViewModel.getApplication())
        isGuidanceEnabled = preferences.getBoolean("guidance_enabled", false)

        subscribeToJumpId()
        subscribeToLocation()
        subscribeToAltitude()
    }

    /**
     * Subscribe to the latest jump ID and store it in this object.
     */
    private fun subscribeToJumpId() {
        val jumpIdObserver = { jumpId: Int? ->
            if (jumpId != null) {
                this.jumpId = jumpId
            }
        }

        databaseViewModel.findLastJumpId().observeForever(jumpIdObserver)
    }

    /**
     *
     * Subscribe to altitude changes.
     *
     * This handles checks for whether logging should be enabled/disabled and adding
     * position entries to the database.
     */
    private fun subscribeToAltitude() {
        pressureViewModel.getLastAltitude().observeForever { this.checkAltitude(it) }
    }

    /**
     * Checks and actions to be performed on new altitude data.
     *
     * @param altitude The new altitude data.
     */
    private fun checkAltitude(altitude: Float?) {
        // Exit if no altitude is given
        if (altitude == null) {
            return
        }

        // Should we start logging?
        // Check we have an altitude and aren't already logging.
        if (!logging) {
            //if (hasReachedSpeed(altitude, 20)) {
            if (altitude >= 2000) {
                enableLogging()
            }
        } else {
            // Add entry to the db.
            val location = gnssViewModel.getLastLocation().value
            vSpeed = getFallRate(altitude)

            // Decide the fall type
            val vSpeed = vSpeed
            if (vSpeed != null) {
                if (vSpeed > fallToggle && !hasFreefallen) {
                    // TODO: Set this boolean value when enabling logging
                    hasFreefallen = true
                } else if (vSpeed < canopyToggle && hasFreefallen) {
                    isUnderCanopy = true
                }
            }

            // Add the position to the database
            if (jumpId != null) {
                addPositionToDb(jumpId, location, altitude, vSpeed)
            }

            if (isGuidanceEnabled) {
                if (coordSender != null && location != null) {
                    // TODO: Only send new location after a set distance moved
                    val send = String.format(Locale.ENGLISH, "%f %f %f", location.latitude,
                            location.longitude, altitude)
                    coordSender!!.write(send.toByteArray())
                }
            }

            // Should we stop logging?
            if (altitude < 3) {
                disableLogging()
            }
        }
    }

    /**
     *
     * Get the fall rate of the user.
     *
     * This depends on the method being called twice to get time periods between altitudes.
     *
     * @param altitude The current altitude of the user.
     * @return The fall rate of the user.
     */
    @Synchronized
    private fun getFallRate(altitude: Float): Double? {
        val now = Date().time
        var speed: Double? = null

        // Check if this is our first run
        if (prevAlt != null && prevTime != null) {
            val period = (now - prevTime!!) * 0.001 // Period in seconds
            val distance = prevAlt!! - altitude // Distance in metres
            speed = distance / period // Speed in m/s
        }

        prevTime = now
        prevAlt = altitude

        if (BuildConfig.DEBUG) {
            Log.v(TAG, "Fall Rate: " + speed + "m/s")
        }
        return speed
    }

    /**
     *
     * Check if the user has reached at least a certain vSpeed.
     *
     * This check depends on the vSpeed of the user having been previously checked.
     *
     * @param altitude The current altitude.
     * @param minSpeed The minimum vSpeed.
     * @return If the user has reached at least the minimum vSpeed.
     */
    private fun hasReachedSpeed(altitude: Float?, minSpeed: Double): Boolean {
        val speed = getFallRate(altitude!!)
        return speed != null && speed >= minSpeed
    }

    /**
     * Subscribe to location changes and add positions to the database.
     */
    private fun subscribeToLocation() {
        gnssViewModel.getLastLocation().observeForever { this.checkLocation(it) }
    }

    /**
     * Checks and actions performed on new location data.
     *
     * @param location The new location data.
     */
    private fun checkLocation(location: Location?) {
        // Add entry to db
        if (location != null && logging) {
            val altitude = pressureViewModel.getLastAltitude().value
            if (jumpId != null) {
                addPositionToDb(jumpId, location, altitude, vSpeed)
            }

            if (isGuidanceEnabled) {
                if (coordSender != null && altitude != null) {
                    // TODO: Only send new location after a set distance moved
                    val send = String.format(Locale.ENGLISH, "%f %f %f", location.latitude,
                            location.longitude, altitude)
                    coordSender!!.write(send.toByteArray())
                }
            }
        }
    }

    /**
     * Add a new position to the database.
     *
     * @param jumpId   The jump id for the position.
     * @param location The location of the position.
     * @param altitude The altitude of the position.
     */
    private fun addPositionToDb(jumpId: Int?, location: Location?, altitude: Float?, vSpeed: Double?) {
        val settings = databaseViewModel.getApplication<Application>()
                .getSharedPreferences("userInfo", Context.MODE_PRIVATE)
        val uuid = settings.getString("userUUID", "")

        val pos = Position()
        pos.latitude = location?.latitude
        pos.longitude = location?.longitude
        pos.hspeed = location?.speed
        pos.vspeed = vSpeed
        pos.altitude = altitude?.toInt()
        pos.time = Date()
        pos.jumpId = jumpId!!
        pos.useruuid = uuid

        if (isUnderCanopy) {
            pos.fallType = FallType.CANOPY
        } else {
            pos.fallType = FallType.FREEFALL
        }

        val msg = String.format(Locale.ENGLISH, "Inserting position:%n" +
                "\tUser UUID: %s%n" +
                "\tJump ID: %d%n" +
                "\t(Lat, Long): (%f,%f)%n" +
                "\tAltitude: %d%n" +
                "\tTime: %s%n" +
                "\tHorizontal Speed: %f%n" +
                "\tVertical Speed: %f",
                pos.useruuid, pos.jumpId, pos.latitude, pos.longitude, pos.altitude, pos.time,
                pos.hspeed, pos.vspeed)
        Log.v(TAG, msg)

        AsyncTask.execute { databaseViewModel.addPosition(pos) }
    }

    /**
     * Enable position logging.
     */
    fun enableLogging() {
        logging = true
        Log.i(TAG, "Logging enabled.")
    }

    /**
     * Disable position logging.
     */
    fun disableLogging() {
        logging = false
        Log.i(TAG, "Logging disabled.")
    }

    fun setCoordSender(coordSender: CoordSender) {
        this.coordSender = coordSender
    }

    companion object {
        private val TAG = ReadingListener::class.java.simpleName
    }
}

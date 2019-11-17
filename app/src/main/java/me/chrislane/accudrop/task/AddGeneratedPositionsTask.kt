package me.chrislane.accudrop.task

import android.location.Location
import android.os.AsyncTask
import android.util.Log
import me.chrislane.accudrop.BuildConfig
import me.chrislane.accudrop.db.FallType
import me.chrislane.accudrop.db.Position
import me.chrislane.accudrop.viewmodel.DatabaseViewModel
import java.util.*

class AddGeneratedPositionsTask(private val jumpId: Int, private val uuid: UUID, private val route: MutableList<Location>,
                                private val databaseViewModel: DatabaseViewModel) : AsyncTask<Void, Void, Void>() {

    /**
     * Add positions in a route to a jump.
     */
    private fun addPositions() {
        // Set time to be equal for all jumpers
        val date = Date()
        date.time = 0L

        // Add "Freefall" data (shift landing pattern up in altitude)
        for (i in route.indices) {
            val location = route[i]
            val pos = Position(
                jumpId = jumpId,
                userUuid = uuid,
                altitude = location.altitude.toInt() + 1000,
                vSpeed = 54.0,
                hSpeed = 4f,
                latitude = location.latitude,
                longitude = location.longitude,
                time = date.clone() as Date,
                fallType = FallType.FREEFALL)

            databaseViewModel.addPosition(pos)

            // Increment time by a second for next position
            date.time = date.time + 450L
        }

        // Add canopy data
        for (i in route.indices) {
            val location = route[i]
            val pos = Position(
                jumpId = jumpId,
                userUuid = uuid,
                altitude = location.altitude.toInt(),
                vSpeed = 15.4 / 2.5,
                hSpeed = 15.4f + 2f,
                latitude = location.latitude,
                longitude = location.longitude,
                time = date.clone() as Date,
                fallType = FallType.CANOPY)

            databaseViewModel.addPosition(pos)

            // Increment time by a second for next position
            date.time = date.time + 1000L
        }
    }

    override fun doInBackground(vararg voids: Void): Void? {
        addPositions()
        return null
    }

    companion object {

        private val TAG = AddGeneratedPositionsTask::class.java.simpleName

        private fun getFallRate(newAlti: Float, newTime: Long?, prevAlt: Double?, prevTime: Long?): Double? {
            var speed: Double? = null

            // Check if this is our first run
            if (prevAlt != null && prevTime != null) {
                val period = (newTime!! - prevTime) * 0.001 // Period in seconds
                val distance = prevAlt - newAlti // Distance in metres
                speed = distance / period // Speed in m/s
            }

            if (BuildConfig.DEBUG) {
                Log.v(TAG, "Fall Rate: " + speed + "m/s")
            }
            return speed
        }
    }
}

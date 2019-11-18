package me.chrislane.accudrop.generator

import android.content.SharedPreferences
import android.content.res.Resources
import android.location.Location
import android.util.Log
import android.util.Pair
import androidx.lifecycle.ViewModelProviders
import androidx.preference.PreferenceManager
import com.google.android.gms.maps.model.LatLng
import me.chrislane.accudrop.BuildConfig
import me.chrislane.accudrop.MainActivity
import me.chrislane.accudrop.db.FallType
import me.chrislane.accudrop.db.Position
import me.chrislane.accudrop.util.UserUtil
import me.chrislane.accudrop.viewmodel.DatabaseViewModel
import me.chrislane.accudrop.viewmodel.GnssViewModel
import java.util.*
import java.util.concurrent.ThreadLocalRandom

class JumpGenerator(private val main: MainActivity) {
    private val databaseViewModel: DatabaseViewModel = ViewModelProviders.of(main).get(DatabaseViewModel::class.java)

    suspend fun generateJump(target: LatLng, noOfGuests: Int) {
        val userUuid = UserUtil.getCurrentUserUuid(main)
        val sharedPrefs = PreferenceManager.getDefaultSharedPreferences(main)
        val resources = main.resources

        // Add a new jump to the database
        databaseViewModel.addJump()

        val jumpId = databaseViewModel.getLastJumpId()
        if (jumpId == null) {
            Log.e(TAG, "Could not get last jump ID.")
            throw IllegalStateException("Could not get last jump ID")
        }

        if (BuildConfig.DEBUG) {
            Log.d(TAG, "Generating jump $jumpId")
        }

        addPositions(jumpId, userUuid, generateJumpRoute(sharedPrefs, resources, target), databaseViewModel)

        for (i in 0 until noOfGuests) {
          addPositions(jumpId, UUID.randomUUID(), generateJumpRoute(sharedPrefs, resources, target), databaseViewModel)
        }
    }



    companion object {

        private val TAG: String = JumpGenerator::class.java.simpleName

        private fun generateJumpRoute(sharedPrefs: SharedPreferences, resources: Resources, target: LatLng): MutableList<Location> {
            // Generate random wind stats
            val randSpeed = ThreadLocalRandom.current().nextInt(0, 10).toDouble()
            val randDir = ThreadLocalRandom.current().nextInt(0, 360).toDouble()

            // Generate a route for the subject
            val routeCalculator = RouteCalculator(sharedPrefs, resources, Pair(randSpeed, randDir), target)
            val route = routeCalculator.calcRoute()
            return addIntermediaryPoints(route)
        }

        /**
         * Add positions in a route to a jump.
         */
        suspend fun addPositions(jumpId: Int, uuid: UUID, route: MutableList<Location>, dbViewModel: DatabaseViewModel) {
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

                dbViewModel.addPosition(pos)

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

                dbViewModel.addPosition(pos)

                // Increment time by a second for next position
                date.time = date.time + 1000L
            }
        }

        /**
         * Add intermediary points to a route with random tweaks to the bearing.
         *
         * @param route The route to add to.
         * @return The route containing additional points.
         */
        private fun addIntermediaryPoints(route: MutableList<Location>): MutableList<Location> {
            val result = mutableListOf<Location>()

            for (i in 0 until route.size - 1) {
                val loc1 = route[i]
                val loc2 = route[i + 1]
                val totalDistance = loc1.distanceTo(loc2).toDouble()
                var altitude = loc1.altitude
                val split = (totalDistance / 5).toInt()
                val altitudeDec = (altitude - loc2.altitude) / split

                result.add(loc1)
                var prevPos = GnssViewModel.getLatLng(loc1)
                var prevLoc = loc1
                for (j in 0 until split - 1) {
                    val bearing = prevLoc.bearingTo(loc2).toDouble()

                    val randBear = ThreadLocalRandom.current().nextInt(-15, 15)
                    prevLoc = Location("")
                    prevPos = RouteCalculator.getPosAfterMove(prevPos, 5.0, bearing + randBear)
                    prevLoc.latitude = prevPos.latitude
                    prevLoc.longitude = prevPos.longitude
                    altitude -= altitudeDec
                    prevLoc.altitude = altitude

                    result.add(prevLoc)
                }
            }
            result.add(route[route.size - 1])

            return result
        }
    }
}

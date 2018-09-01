package me.chrislane.accudrop.generator

import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.location.Location
import android.os.AsyncTask
import com.google.android.gms.maps.model.LatLng
import me.chrislane.accudrop.MainActivity
import me.chrislane.accudrop.task.GenerateJumpTask
import me.chrislane.accudrop.viewmodel.DatabaseViewModel
import me.chrislane.accudrop.viewmodel.GnssViewModel
import java.util.*
import java.util.concurrent.ThreadLocalRandom

class JumpGenerator(private val main: MainActivity) {
    private val databaseViewModel: DatabaseViewModel

    init {
        databaseViewModel = ViewModelProviders.of(main).get(DatabaseViewModel::class.java)
    }

    fun generateJump(target: LatLng, noOfGuests: Int) {
        val settings = main.getSharedPreferences("userInfo", Context.MODE_PRIVATE)
        val stringUuid = settings.getString("userUUID", "")
        val uuid = UUID.fromString(stringUuid)

        GenerateJumpTask(uuid, target, databaseViewModel)
                .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, noOfGuests)
    }

    companion object {

        private val TAG = JumpGenerator::class.java.simpleName

        /**
         * Add intermediary points to a route with random tweaks to the bearing.
         *
         * @param route The route to add to.
         * @return The route containing additional points.
         */
        fun addIntermediaryPoints(route: MutableList<Location>): MutableList<Location> {
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
                    prevPos = RouteCalculator.getPosAfterMove(prevPos!!, 5.0, bearing + randBear)
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

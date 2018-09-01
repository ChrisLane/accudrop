package me.chrislane.accudrop.task

import android.location.Location
import android.os.AsyncTask
import android.util.Log
import me.chrislane.accudrop.viewmodel.DatabaseViewModel
import java.util.*

/**
 * Get the latest jump.
 */
class FetchJumpTask(private val listener: FetchJumpListener, private val databaseViewModel: DatabaseViewModel) : AsyncTask<Int, Void, MutableList<Location>>() {

    override fun doInBackground(vararg integers: Int?): MutableList<Location> {
        val jumpNumber: Int?
        if (integers.isNotEmpty()) {
            jumpNumber = integers[0]
            Log.d(TAG, "Fetching jump $jumpNumber")
        } else {
            jumpNumber = databaseViewModel.lastJumpId
            Log.d(TAG, "Fetching last jump ($jumpNumber)")
        }

        val locations = mutableListOf<Location>()
        if (jumpNumber != null) {
            val positions = databaseViewModel.getPositionsForJump(jumpNumber)
            for (position in positions) {
                if (position.latitude != null && position.longitude != null &&
                        position.altitude != null) {
                    val location = Location("")
                    location.latitude = position.latitude!!
                    location.longitude = position.longitude!!
                    location.altitude = position.altitude!!.toDouble()
                    locations.add(location)

                    Log.v(TAG, location.toString())
                }
            }
        } else {
            Log.e(TAG, "No last jump id found.")
        }

        return locations
    }

    override fun onPostExecute(locations: MutableList<Location>) {
        super.onPostExecute(locations)

        Log.d(TAG, "Finished getting jump data.")
        listener.onFinished(locations)
    }

    interface FetchJumpListener {
        fun onFinished(locations: MutableList<Location>?)
    }

    companion object {

        private val TAG = FetchJumpTask::class.java!!.getSimpleName()
    }
}

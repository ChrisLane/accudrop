package me.chrislane.accudrop.task

import android.os.AsyncTask
import android.util.Pair

import me.chrislane.accudrop.viewmodel.DatabaseViewModel

/**
 * Get the minimum and maximum latitude or longitude for the latest jump.
 */
class MinMaxLatLngTask(private val listener: (Double, Double) -> Unit, private val databaseViewModel: DatabaseViewModel, private val getLatitude: Boolean) : AsyncTask<Void, Void, Pair<Double, Double>>() {

    override fun doInBackground(vararg aVoid: Void): Pair<Double, Double>? {
        val jumpId = databaseViewModel.lastJumpId
        if (jumpId != null) {
            val min: Double?
            val max: Double?
            if (getLatitude) {
                min = databaseViewModel.getMinLatitudeForJump(jumpId)
                max = databaseViewModel.getMaxLatitudeForJump(jumpId)
            } else {
                min = databaseViewModel.getMinLongitudeForJump(jumpId)
                max = databaseViewModel.getMaxLongitudeForJump(jumpId)
            }

            if (min != null && max != null) {
                return Pair(min, max)
            }
        }

        return null
    }

    override fun onPostExecute(result: Pair<Double, Double>?) {
        super.onPostExecute(result)

        if (result != null) {
            listener(result.first, result.second)
        }
    }
}

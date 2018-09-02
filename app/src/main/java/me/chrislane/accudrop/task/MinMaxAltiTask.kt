package me.chrislane.accudrop.task

import android.os.AsyncTask
import android.util.Pair

import me.chrislane.accudrop.viewmodel.DatabaseViewModel

/**
 * Get the minimum and maximum altitude for a jump.
 */
class MinMaxAltiTask(private val listener: (Int, Int) -> Unit, private val databaseViewModel: DatabaseViewModel) : AsyncTask<Int, Void, Pair<Int, Int>>() {

    override fun doInBackground(vararg integers: Int?): Pair<Int, Int>? {
        val jumpId = if (integers.isNotEmpty()) {
            integers[0]
        } else {
            databaseViewModel.lastJumpId
        }

        if (jumpId != null) {
            val min = databaseViewModel.getMinAltitudeForJump(jumpId)
            val max = databaseViewModel.getMaxAltitudeForJump(jumpId)

            if (min != null && max != null) {
                return Pair(min, max)
            }
        }

        return null
    }

    override fun onPostExecute(result: Pair<Int, Int>?) {
        super.onPostExecute(result)

        if (result != null) {
            listener(result.first, result.second)
        }
    }
}

package me.chrislane.accudrop.task

import android.graphics.Point
import android.graphics.PointF
import android.os.AsyncTask
import android.util.Log
import me.chrislane.accudrop.util.DistanceAndSpeedUtil
import me.chrislane.accudrop.viewmodel.ReplayViewModel

class ProduceSideViewTask(private val width: Int, private val height: Int, private val margin: Int,
                          private val mapPointList: MutableList<MutableList<Point>>, private val model: ReplayViewModel, private val listener: (MutableList<MutableList<PointF>>) -> Unit) : AsyncTask<Void, Void, MutableList<MutableList<PointF>>>() {

    /**
     * Convert map coordinates into side view coordinates.
     *
     * @return The screen coordinates converted for the side view.
     */
    private fun produceViewPositions(): MutableList<MutableList<PointF>> {
        val screenPosList = mutableListOf<MutableList<PointF>>()

        val usersAndLocs = model.getUsersAndLocs().value
        if (usersAndLocs == null || usersAndLocs.isEmpty()) {
            Log.e(TAG, "Users and locations list is null or empty.")
            return screenPosList
        }

        var i = 0
        while (i < mapPointList.size && i < usersAndLocs.size) {
            val mapPoints = mapPointList[i]
            val locations = usersAndLocs[i].second
            val screenPos = mutableListOf<PointF>()

            // Return an empty list if the route is empty.
            if (mapPoints.isEmpty()) {
                Log.d(TAG, "No points in the route.")
                return mutableListOf()
            }

            var min = 0
            var max = if (width > height) height else width
            val diff = Math.abs(width - height)
            max -= margin
            min += margin

            // Set the minimum and maximum x coordinate
            var minX = mapPoints[0].x
            var maxX = minX
            for (point in mapPoints) {
                val x = point.x

                if (x < minX) {
                    minX = x
                } else if (x > maxX) {
                    maxX = x
                }
            }

            if (locations != null) {
                val minAltitude = locations[0].altitude
                val maxAltitude = locations[locations.size - 1].altitude

                // Generate screen points
                var j = 0
                while (j < mapPoints.size && j < locations.size) {
                    var x = DistanceAndSpeedUtil
                        .getScaledValue(mapPoints[j].x.toDouble(), minX.toDouble(), maxX.toDouble(), min.toDouble(), max.toDouble())
                    val y = DistanceAndSpeedUtil
                        .getScaledValue(locations[j].altitude, minAltitude, maxAltitude, min.toDouble(), max.toDouble())
                    x += (diff / 2f).toDouble()
                    screenPos.add(PointF(x.toFloat(), (height - y).toFloat()))
                    j++
                }

                Log.v(TAG, "Generated screen positions: $screenPos")
            }

            screenPosList.add(screenPos)
            i++

        }

        return screenPosList
    }

    override fun doInBackground(vararg voids: Void): MutableList<MutableList<PointF>> {
        return produceViewPositions()
    }

    override fun onPostExecute(screenPoints: MutableList<MutableList<PointF>>) {
        super.onPostExecute(screenPoints)
        listener(screenPoints)
    }

    companion object {
        private val TAG = ProduceSideViewTask::class.java.simpleName
    }
}

package me.chrislane.accudrop.presenter

import androidx.lifecycle.ViewModelProvider
import android.graphics.Point
import android.graphics.PointF
import android.os.AsyncTask

import me.chrislane.accudrop.fragment.ReplayFragment
import me.chrislane.accudrop.fragment.ReplaySideViewFragment
import me.chrislane.accudrop.task.ProduceSideViewTask
import me.chrislane.accudrop.viewmodel.GnssViewModel
import me.chrislane.accudrop.viewmodel.ReplayViewModel

class ReplaySideViewPresenter(private val fragment: ReplaySideViewFragment) {
    private val parentFragment: ReplayFragment? = fragment.parentFragment as ReplayFragment?
    private var replayViewModel: ReplayViewModel

    private val mapPoints: MutableList<MutableList<Point>>
        get() {
            val map = parentFragment!!.replayMap.map
            val mapPoints = mutableListOf<MutableList<Point>>()

            val usersAndLocs = replayViewModel.getUsersAndLocs().value

            if (usersAndLocs != null) {
                for (userAndLocs in usersAndLocs) {
                    val locations = userAndLocs.second
                    if (locations != null) {
                        val points = mutableListOf<Point>()
                        for (location in locations) {
                            points.add(map.projection
                                    .toScreenLocation(GnssViewModel.getLatLng(location)))
                        }
                        mapPoints.add(points)
                    }
                }
            }

            return mapPoints
        }

    init {

        if (parentFragment == null) {
            throw Exception("Missing parent fragment (Replay)")
        }
        replayViewModel = ViewModelProvider(parentFragment).get(ReplayViewModel::class.java)
    }

    fun produceViewPositions(width: Int, height: Int, margin: Int) {
        val mapPointsList = mapPoints
        val listener = { pointList: MutableList<MutableList<PointF>> ->
            fragment.setScreenPointsList(pointList)

        }
        ProduceSideViewTask(width, height, margin, mapPointsList, replayViewModel, listener)
                .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
    }

    companion object {
        private val TAG = ReplaySideViewPresenter::class.java.simpleName
    }
}

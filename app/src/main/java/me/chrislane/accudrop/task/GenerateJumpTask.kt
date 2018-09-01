package me.chrislane.accudrop.task

import android.app.Application
import android.location.Location
import android.os.AsyncTask
import android.preference.PreferenceManager
import android.util.Log
import android.util.Pair
import com.google.android.gms.maps.model.LatLng
import me.chrislane.accudrop.BuildConfig
import me.chrislane.accudrop.generator.JumpGenerator
import me.chrislane.accudrop.viewmodel.DatabaseViewModel
import java.util.*
import java.util.concurrent.ThreadLocalRandom

class GenerateJumpTask(private val subjectUUID: UUID, private val target: LatLng, private val databaseViewModel: DatabaseViewModel) : AsyncTask<Int, Void, Void>() {

    override fun doInBackground(vararg guestCounts: Int?): Void? {
        val guestCount = guestCounts[0] ?: throw IllegalArgumentException("Expected guest count")


        // Add a new jump to the database
        databaseViewModel.addJump()

        val jumpId = databaseViewModel.lastJumpId
        if (jumpId == null) {
            Log.e(TAG, "Could not get last jump ID.")
            return null
        }

        if (BuildConfig.DEBUG) {
            Log.d(TAG, "Generating jump $jumpId")
        }

        // Generate a route for the subject
        val routeListener = { route: MutableList<Location> ->
            // Add intermediary points to route
            val subjectRoute = JumpGenerator.addIntermediaryPoints(route)
            // Add subject route to database
            AddGeneratedPositionsTask(jumpId, subjectUUID, subjectRoute, databaseViewModel)
                    .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
        }


        // Generate random wind stats
        val randSpeed = ThreadLocalRandom.current().nextInt(0, 10).toDouble()
        val randDir = ThreadLocalRandom.current().nextInt(0, 360).toDouble()
        val context = databaseViewModel.getApplication<Application>().applicationContext
        val sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context)
        val resources = context.resources
        // Execute subject task
        RouteTask(routeListener, sharedPrefs, resources, Pair(randSpeed, randDir))
                .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, target)

        for (i in 0 until guestCount) {
            // Generate a route for the subject
            val guestRouteListener = { route: MutableList<Location> ->
                // Add intermediary points to route
                val guestRoute = JumpGenerator.addIntermediaryPoints(route)
                // Add subject route to database
                AddGeneratedPositionsTask(jumpId, UUID.randomUUID(), guestRoute, databaseViewModel)
                        .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
            }

            // Generate random wind stats
            val guestRandSpeed = ThreadLocalRandom.current().nextInt(0, 10).toDouble()
            val guestRandDir = ThreadLocalRandom.current().nextInt(0, 360).toDouble()
            // Execute subject task
            RouteTask(guestRouteListener, sharedPrefs, resources, Pair(guestRandSpeed, guestRandDir))
                    .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, target)
        }
        return null
    }

    companion object {
        private val TAG = GenerateJumpTask::class.java.simpleName
    }
}

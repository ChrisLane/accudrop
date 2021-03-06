package me.chrislane.accudrop.task

import android.location.Location
import android.os.AsyncTask
import android.util.Log
import me.chrislane.accudrop.viewmodel.DatabaseViewModel
import java.util.*

class FetchUsersAndPositionsTask(private val listener: (MutableList<Pair<UUID, MutableList<Location>>>) -> Unit,
                                 private val databaseViewModel: DatabaseViewModel) : AsyncTask<Int, Void, MutableList<Pair<UUID, MutableList<Location>>>>() {

    override fun doInBackground(vararg integers: Int?): MutableList<Pair<UUID, MutableList<Location>>>? {
        var result: MutableList<Pair<UUID, MutableList<Location>>>? = mutableListOf()
        val jumpNumber: Int?
        if (integers.isNotEmpty()) {
            jumpNumber = integers[0]
            Log.d(TAG, "Fetching jump $jumpNumber")
        } else {
            jumpNumber = databaseViewModel.lastJumpId
            Log.d(TAG, "Fetching last jump ($jumpNumber)")
        }

        if (jumpNumber != null) {
            result = databaseViewModel.getUsersAndPositionsForJump(jumpNumber)
        } else {
            Log.e(TAG, "No last jump id found.")
        }

        return result
    }

    override fun onPostExecute(result: MutableList<Pair<UUID, MutableList<Location>>>) {
        super.onPostExecute(result)

        Log.d(TAG, "Finished getting jump data.")
        listener(result)
    }

    companion object {
        private val TAG = FetchUsersAndPositionsTask::class.java.simpleName
    }
}

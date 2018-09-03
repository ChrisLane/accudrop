package me.chrislane.accudrop.task

import android.os.AsyncTask
import android.util.Log
import me.chrislane.accudrop.db.Jump
import me.chrislane.accudrop.viewmodel.DatabaseViewModel
import java.util.*

/**
 * Create and insert a new jump into the database.
 */
class CreateAndInsertJumpTask(private val databaseViewModel: DatabaseViewModel, private val createListener: (Int) -> Unit,
                              private val insertListener: () -> Unit) : AsyncTask<Void, Void, Int>() {

    override fun doInBackground(vararg params: Void): Int {
        var jumpId = databaseViewModel.lastJumpId

        jumpId = if (jumpId != null) {
            Log.d(TAG, "Previous jump id: $jumpId")
            jumpId + 1
        } else {
            Log.d(TAG, "No previous jump id.")
            1
        }
        return jumpId
    }

    override fun onPostExecute(result: Int) {
        val jump = Jump()
        jump.id = result
        jump.time = Date()

        createListener(result)

        InsertJumpTask(databaseViewModel, insertListener).execute(jump)
    }

    companion object {
        private val TAG = CreateAndInsertJumpTask::class.java.simpleName
    }
}
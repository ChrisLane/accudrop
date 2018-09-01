package me.chrislane.accudrop.task

import android.os.AsyncTask

import me.chrislane.accudrop.db.Jump
import me.chrislane.accudrop.viewmodel.DatabaseViewModel

/**
 * Insert a new jump into the database.
 */
class InsertJumpTask internal constructor(private val databaseViewModel: DatabaseViewModel, private val listener: Listener) : AsyncTask<Jump, Void, Void>() {

    override fun doInBackground(vararg jumps: Jump): Void? {
        databaseViewModel.addJump(jumps[0])
        return null
    }

    override fun onPostExecute(aVoid: Void) {
        super.onPostExecute(aVoid)

        listener.onFinished()
    }

    interface Listener {
        fun onFinished()
    }
}

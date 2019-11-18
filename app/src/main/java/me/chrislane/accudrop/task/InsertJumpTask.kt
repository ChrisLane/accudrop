package me.chrislane.accudrop.task

import android.os.AsyncTask

import me.chrislane.accudrop.db.Jump
import me.chrislane.accudrop.viewmodel.DatabaseViewModel

/**
 * Insert a new jump into the database.
 */
class InsertJumpTask internal constructor(private val databaseViewModel: DatabaseViewModel, private val listener: () -> Unit) : AsyncTask<Jump, Void, Boolean>() {

    override fun doInBackground(vararg jumps: Jump): Boolean {
        // TODO: Make this handle the suspend function (or replace class completely)
        //databaseViewModel.addJump(jumps[0])
        return true
    }

    override fun onPostExecute(bool: Boolean) {
        super.onPostExecute(bool)

        listener
    }
}

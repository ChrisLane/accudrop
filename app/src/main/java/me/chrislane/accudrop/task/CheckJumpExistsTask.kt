package me.chrislane.accudrop.task

import android.os.AsyncTask

import me.chrislane.accudrop.viewmodel.DatabaseViewModel

class CheckJumpExistsTask(private val listener: Listener, private val databaseViewModel: DatabaseViewModel) : AsyncTask<Int, Void, Boolean>() {

    override fun doInBackground(vararg integers: Int?): Boolean? {
        integers[0]?.let { return databaseViewModel.jumpExists(it) }
        throw IllegalArgumentException("Expected jump number")

    }

    override fun onPostExecute(jumpExists: Boolean?) {
        super.onPostExecute(jumpExists)

        if (jumpExists == null) {
            listener.onFinished(false)
        } else {
            listener.onFinished(jumpExists)
        }
    }

    interface Listener {
        fun onFinished(jumpExists: Boolean?)
    }
}

package me.chrislane.accudrop.task

import android.os.AsyncTask

import me.chrislane.accudrop.viewmodel.DatabaseViewModel


class FetchFirstJumpIdTask(private val listener: FetchFirstJumpIdTask.Listener, private val databaseViewModel: DatabaseViewModel) : AsyncTask<Void, Void, Int>() {

    override fun doInBackground(vararg voids: Void): Int? {
        return databaseViewModel.firstJumpId
    }

    override fun onPostExecute(jumpId: Int?) {
        super.onPostExecute(jumpId)
        listener.onFinished(jumpId)
    }

    interface Listener {
        fun onFinished(jumpId: Int?)
    }
}

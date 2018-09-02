package me.chrislane.accudrop.task

import android.os.AsyncTask

import me.chrislane.accudrop.viewmodel.DatabaseViewModel

class FetchLastJumpIdTask(private val listener: (Int?) -> Unit, private val databaseViewModel: DatabaseViewModel) : AsyncTask<Void, Void, Int>() {

    override fun doInBackground(vararg voids: Void): Int? {
        return databaseViewModel.lastJumpId
    }

    override fun onPostExecute(jumpId: Int?) {
        super.onPostExecute(jumpId)
        listener(jumpId)
    }
}

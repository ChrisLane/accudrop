package me.chrislane.accudrop.task

import android.os.AsyncTask
import me.chrislane.accudrop.viewmodel.DatabaseViewModel
import java.util.*


class FetchTotalDuration(private val listener: (Long?) -> Unit, private val uuid: UUID, private val viewModel: DatabaseViewModel) : AsyncTask<Int, Void, Long>() {

    override fun doInBackground(vararg integers: Int?): Long? {
        val jumpId = integers[0] ?: return null

        val first = viewModel.getFirstDate(uuid, jumpId)
        val last = viewModel.getLastDate(uuid, jumpId)

        if (first == null || last == null) {
            return null
        }
        return last.time - first.time
    }

    override fun onPostExecute(millis: Long?) {
        super.onPostExecute(millis)

        listener(millis)
    }
}

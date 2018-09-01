package me.chrislane.accudrop.task

import android.os.AsyncTask
import me.chrislane.accudrop.db.FallType
import me.chrislane.accudrop.viewmodel.DatabaseViewModel
import java.util.*


class FetchFallTypeDuration(private val listener: (Long?) -> Unit, private val fallType: FallType,
                            private val uuid: UUID, private val viewModel: DatabaseViewModel) : AsyncTask<Int, Void, Long>() {

    override fun doInBackground(vararg integers: Int?): Long? {
        val jumpId = integers[0] ?: return null

        val first = viewModel.getFirstDateOfFallType(fallType, uuid, jumpId)
        val last = viewModel.getLastDateOfFallType(fallType, uuid, jumpId)

        return last.time - first.time

    }

    override fun onPostExecute(millis: Long?) {
        super.onPostExecute(millis)

        listener(millis)
    }

    interface Listener {
        fun onFinished(millis: Long?)
    }
}

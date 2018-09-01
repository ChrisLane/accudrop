package me.chrislane.accudrop.task

import android.os.AsyncTask
import me.chrislane.accudrop.db.FallType
import me.chrislane.accudrop.viewmodel.DatabaseViewModel
import java.util.*


class FetchFallTypeMaxHSpeed(private val listener: (Float?) -> Unit, private val fallType: FallType,
                             private val uuid: UUID, private val viewModel: DatabaseViewModel) : AsyncTask<Int, Void, Float>() {

    override fun doInBackground(vararg integers: Int?): Float? {
        val jumpId = integers[0] ?: return null

        return viewModel.getMaxHSpeedOfFallType(fallType, uuid, jumpId)
    }

    override fun onPostExecute(hSpeed: Float?) {
        super.onPostExecute(hSpeed)

        listener(hSpeed)
    }

    interface Listener {
        fun onFinished(hSpeed: Float?)
    }
}

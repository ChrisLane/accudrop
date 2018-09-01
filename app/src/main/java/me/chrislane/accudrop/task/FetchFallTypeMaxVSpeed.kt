package me.chrislane.accudrop.task

import android.os.AsyncTask
import me.chrislane.accudrop.db.FallType
import me.chrislane.accudrop.viewmodel.DatabaseViewModel
import java.util.*


class FetchFallTypeMaxVSpeed(private val listener: (Double?) -> Unit, private val fallType: FallType,
                             private val uuid: UUID, private val viewModel: DatabaseViewModel) : AsyncTask<Int, Void, Double>() {

    override fun doInBackground(vararg integers: Int?): Double? {
        val jumpId = integers[0] ?: return null

        return viewModel.getMaxVSpeedOfFallType(fallType, uuid, jumpId)
    }

    override fun onPostExecute(vSpeed: Double?) {
        super.onPostExecute(vSpeed)

        listener(vSpeed)
    }

    interface Listener {
        fun onFinished(vSpeed: Double?)
    }
}

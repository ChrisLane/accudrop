package me.chrislane.accudrop.task

import android.location.Location
import android.os.AsyncTask
import android.util.Log
import android.util.Pair
import com.google.android.gms.maps.model.LatLng
import me.chrislane.accudrop.BuildConfig
import me.chrislane.accudrop.presenter.PlanPresenter
import org.json.JSONException
import org.json.JSONObject
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.StandardCharsets

/**
 *
 * AsyncTask to fetch the current weather from the OpenWeatherMap API.
 *
 * Code is adapted from a [previous project.](https://github.com/ChrisLane/weather/)
 */
class WindTask(private val listener: (Pair<Double, Double>) -> AsyncTask<LatLng, Void, MutableList<Location>>, private val planPresenter: PlanPresenter, private val apiKey: String) : AsyncTask<LatLng, Void, JSONObject>() {

    override fun onPreExecute() {
        super.onPreExecute()
        Log.d(TAG, "Task pre execute")

        planPresenter.setTaskRunning(true)
    }

    override fun doInBackground(vararg params: LatLng): JSONObject {
        Log.d(TAG, "Task background process")

        val latLng = params[0]

        val api = "http://api.openweathermap.org/data/2.5/"
        val url = api +
                "weather?" +
                "lat=" + latLng.latitude +
                "&lon=" + latLng.longitude +
                "&units=metric" +
                "&appid=" + apiKey

        if (BuildConfig.DEBUG) {
            Log.d(TAG, "Request: $url")
        }

        return fetchJson(url)
    }

    override fun onPostExecute(json: JSONObject) {
        super.onPostExecute(json)
        Log.d(TAG, "Task post execute")

        try {
            // Get wind properties
            val wind = json.getJSONObject("wind")

            val windSpeed = wind.getDouble("speed")
            val windDirection = wind.getDouble("deg")

            if (BuildConfig.DEBUG) {
                Log.d(TAG, "Wind: Speed = $windSpeed Direction = $windDirection")
            }

            // Run code that the caller wants to do on the result
            listener(Pair(windSpeed, windDirection))
        } catch (e: JSONException) {
            e.printStackTrace()
        }

        // Task is no longer running
        planPresenter.setTaskRunning(false)
    }

    interface WeatherTaskListener {
        fun onFinished(windTuple: Pair<Double, Double>)
    }

    companion object {
        private val TAG = WindTask::class.java.simpleName

        private fun fetchJson(param: String): JSONObject {
            var connection: HttpURLConnection? = null
            var reader: BufferedReader? = null

            try {
                val url = URL(param)
                connection = url.openConnection() as HttpURLConnection
                connection.connect()


                val stream = connection.inputStream

                reader = BufferedReader(InputStreamReader(stream, StandardCharsets.UTF_8))

                val buffer = StringBuilder()
                val line = reader.readLine()
                while (line != null) {
                    buffer.append(line).append("\n")
                    if (BuildConfig.DEBUG) {
                        Log.d("JsonTask Response", "> $line")
                    }
                }

                try {
                    return JSONObject(buffer.toString())
                } catch (e: JSONException) {
                    e.printStackTrace()
                }


            } catch (e: IOException) {
                e.printStackTrace()
            } finally {
                connection?.disconnect()
                try {
                    reader?.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }

            }
            return JSONObject()
        }
    }
}

package me.chrislane.accudrop.task;

import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import me.chrislane.accudrop.presenter.RoutePlanPresenter;

/**
 * <p>AsyncTask to fetch the current weather from the OpenWeatherMap API.</p>
 * <p>Code is adapted from a <a href="https://github.com/ChrisLane/weather/">previous project.</a></p>
 */
public class WindTask extends AsyncTask<LatLng, Void, JSONObject> {
    private static final String TAG = WindTask.class.getSimpleName();
    private final RoutePlanPresenter routePlanPresenter;
    private final String apiKey;
    private final WeatherTaskListener listener;

    public WindTask(WeatherTaskListener listener, RoutePlanPresenter routePlanPresenter, String apiKey) {
        this.listener = listener;
        this.routePlanPresenter = routePlanPresenter;
        this.apiKey = apiKey;
    }

    private static JSONObject fetchJson(String param) {
        HttpURLConnection connection = null;
        BufferedReader reader = null;

        try {
            URL url = new URL(param);
            connection = (HttpURLConnection) url.openConnection();
            connection.connect();


            InputStream stream = connection.getInputStream();

            reader = new BufferedReader(new InputStreamReader(stream));

            StringBuilder buffer = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                buffer.append(line).append("\n");
                Log.d("JsonTask Response", "> " + line);
            }

            try {
                return new JSONObject(buffer.toString());
            } catch (JSONException e) {
                e.printStackTrace();
            }


        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return new JSONObject();
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        Log.d("WindTask", "Task pre execute");

        routePlanPresenter.setTaskRunning(true);
    }

    @Override
    protected JSONObject doInBackground(LatLng... params) {
        Log.d(TAG, "Task background process");

        LatLng latLng = params[0];

        String api = "http://api.openweathermap.org/data/2.5/";
        String url = api +
                "weather?" +
                "lat=" + latLng.latitude +
                "&lon=" + latLng.longitude +
                "&units=metric" +
                "&appid=" + apiKey;

        Log.d(TAG, "Request: " + url);

        return fetchJson(url);
    }

    @Override
    protected void onPostExecute(JSONObject json) {
        super.onPostExecute(json);
        Log.d(TAG, "Task post execute");

        try {
            // Get wind properties
            JSONObject wind = json.getJSONObject("wind");

            double windSpeed = wind.getDouble("speed");
            double windDirection = wind.getDouble("deg");
            WindTuple windTuple = new WindTuple(windSpeed, windDirection);

            Log.d(TAG, windTuple.toString());

            // Run code that the caller wants to do on the result
            listener.onFinished(windTuple);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        // Task is no longer running
        routePlanPresenter.setTaskRunning(false);
    }

    public interface WeatherTaskListener {
        void onFinished(WindTuple windTuple);
    }

    public class WindTuple {
        public double windSpeed;
        public double windDirection;

        public WindTuple(double windSpeed, double windDirection) {
            this.windSpeed = windSpeed;
            this.windDirection = windDirection;
        }

        @Override
        public String toString() {
            return "WindTuple{" +
                    "windSpeed=" + windSpeed +
                    ", windDirection=" + windDirection +
                    '}';
        }
    }
}
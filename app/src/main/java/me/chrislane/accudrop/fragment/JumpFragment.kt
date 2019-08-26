package me.chrislane.accudrop.fragment


import android.app.ActivityManager
import androidx.lifecycle.DefaultLifecycleObserver
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.preference.PreferenceManager
import androidx.fragment.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CompoundButton
import android.widget.TextView
import android.widget.ToggleButton

import me.chrislane.accudrop.BuildConfig
import me.chrislane.accudrop.MainActivity
import me.chrislane.accudrop.R
import me.chrislane.accudrop.Util
import me.chrislane.accudrop.presenter.JumpPresenter
import me.chrislane.accudrop.service.LocationService

class JumpFragment : Fragment(), DefaultLifecycleObserver {
    private lateinit var jumpView: View
    private lateinit var jumpPresenter: JumpPresenter
    private lateinit var preferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super<Fragment>.onCreate(savedInstanceState)
        retainInstance = true

        jumpPresenter = JumpPresenter(this)

        preferences = PreferenceManager.getDefaultSharedPreferences(context)

        val main = requireActivity() as MainActivity
        main.lifecycle.addObserver(this)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment.
        jumpView = inflater.inflate(R.layout.fragment_jump, container, false)

        // Add click listener for fragment jumpView.
        val calibrateButton = jumpView.findViewById<Button>(R.id.calibrate_button)
        calibrateButton.setOnClickListener { this.onClickCalibrate() }

        // Set the jump button toggle state
        val jumpButton = jumpView.findViewById<ToggleButton>(R.id.jump_button)
        jumpButton.setOnCheckedChangeListener(null)

        if (BuildConfig.DEBUG) {
            Log.d(TAG, "LocationService running: " + isServiceRunning(LocationService::class.java))
        }

        jumpButton.isChecked = isServiceRunning(LocationService::class.java)
        jumpButton.setOnCheckedChangeListener(onClickJump())

        return jumpView
    }

    /**
     * Zeros the altitude.
     */
    private fun onClickCalibrate() {
        Log.d(TAG, "Calibrating.")
        jumpPresenter.calibrate()
    }

    /**
     * Create a button listener to start a jump.
     *
     * @return The button listener.
     */
    private fun onClickJump(): CompoundButton.OnCheckedChangeListener {
        return CompoundButton.OnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                jumpPresenter.startJump()
            } else {
                jumpPresenter.stopJump()
            }
        }
    }

    /**
     * Update the altitude text.
     *
     * @param altitude The altitude to set.
     */
    fun updatePressureAltitude(altitude: Float) {
        Log.v(TAG, "Updating pressure altitude text.")

        val unitString = preferences.getString("general_unit", "")
        val unit = Util.getUnit(unitString)

        val text = jumpView.findViewById<TextView>(R.id.pressure_altitude)
        text.text = Util.getAltitudeText(java.lang.Double.valueOf(altitude.toDouble()), unit)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        outState.putBoolean("jumpButton", jumpPresenter.isJumping)
    }

    override fun onResume() {
        super<Fragment>.onResume()
        jumpPresenter.resume()
    }

    override fun onPause() {
        super<Fragment>.onPause()
        jumpPresenter.pause()
    }

    /**
     *
     * Check if a service is running.
     *
     * Code taken from [a StackOverflow answer.](https://stackoverflow.com/a/5921190)
     *
     * @param serviceClass The service class to check for an instance of.
     * @return Whether the service is running.
     */
    private fun isServiceRunning(serviceClass: Class<*>): Boolean {
        val activity = requireActivity()
        val manager = activity.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager?

        if (manager != null) {
            for (service in manager.getRunningServices(Integer.MAX_VALUE)) {
                if (serviceClass.name == service.service.className) {
                    return true
                }
            }
        }
        return false
    }

    companion object {
        private val TAG: String = JumpFragment::class.java.simpleName
    }
}

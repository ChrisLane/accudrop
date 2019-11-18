package me.chrislane.accudrop.fragment


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import me.chrislane.accudrop.R
import me.chrislane.accudrop.UnitType
import me.chrislane.accudrop.util.DistanceAndSpeedUtil
import me.chrislane.accudrop.presenter.JumpStatsPresenter
import java.util.*
import java.util.concurrent.TimeUnit

class JumpStatsFragment : Fragment() {

    private lateinit var presenter: JumpStatsPresenter
    private lateinit var statsView: View
    private var prevButton: ImageButton? = null
    private var nextButton: ImageButton? = null
    private lateinit var unit: UnitType

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        presenter = JumpStatsPresenter(this)

        // Get unit preference
        val sharedPref = PreferenceManager.getDefaultSharedPreferences(context)
        val unitString = sharedPref.getString("general_unit", "")!!
        unit = UnitType.valueOf(unitString.toUpperCase())
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        statsView = inflater.inflate(R.layout.fragment_jump_stats, container, false)

        // Set button listeners
        prevButton = statsView.findViewById(R.id.stats_prev_button)
        prevButton?.setOnClickListener { presenter.prevJump() }
        nextButton = statsView.findViewById(R.id.stats_next_button)
        nextButton?.setOnClickListener { presenter.nextJump() }

        return statsView
    }

    fun updateJumpId(jumpId: Int) {
        val number: TextView? = statsView.findViewById(R.id.jump_number)
        number?.text = String.format(Locale.ENGLISH, "%d", jumpId)
    }

    fun updateExitAltitude(altitude: Int) {
        val formatted = DistanceAndSpeedUtil
            .getAltitudeText(DistanceAndSpeedUtil.getAltitudeInUnit(altitude.toDouble(), unit), unit)
        val value: TextView? = statsView.findViewById(R.id.exit_altitude_value)
        value?.text = formatted
    }

    fun updateTotalDuration(millis: Long) {
        val minutes = TimeUnit.MILLISECONDS.toMinutes(millis)
        val seconds = millis / 1000 % 60

        val time: TextView? = statsView.findViewById(R.id.total_duration_value)
        time?.text = String.format(Locale.ENGLISH, "%d:%02d", minutes, seconds)
    }

    fun updateCanopyDuration(millis: Long) {
        val minutes = TimeUnit.MILLISECONDS.toMinutes(millis)
        val seconds = millis / 1000 % 60

        val time: TextView? = statsView.findViewById(R.id.canopy_duration_value)
        time?.text = String.format(Locale.ENGLISH, "%d:%02d", minutes, seconds)
    }

    fun updateFreefallDuration(millis: Long) {
        val minutes = TimeUnit.MILLISECONDS.toMinutes(millis)
        val seconds = millis / 1000 % 60

        val time: TextView? = statsView.findViewById(R.id.freefall_duration_value)
        time?.text = String.format(Locale.ENGLISH, "%d:%02d", minutes, seconds)
    }

    fun updateCanopyVSpeed(vSpeed: Double) {
        val formatted = DistanceAndSpeedUtil
            .getSpeedText(DistanceAndSpeedUtil.getSpeedInUnit(vSpeed, unit), unit)
        val textView: TextView? = statsView.findViewById(R.id.canopy_max_vspeed_value)
        textView?.text = formatted
    }

    fun updateCanopyHSpeed(hSpeed: Float) {
        val formatted = DistanceAndSpeedUtil
            .getSpeedText(DistanceAndSpeedUtil.getSpeedInUnit(hSpeed.toDouble(), unit), unit)
        val textView: TextView? = statsView.findViewById(R.id.canopy_max_hspeed_value)
        textView?.text = formatted
    }

    fun updateFreefallVSpeed(vSpeed: Double) {
        val formatted = DistanceAndSpeedUtil
            .getSpeedText(DistanceAndSpeedUtil.getSpeedInUnit(vSpeed, unit), unit)
        val textView: TextView? = statsView.findViewById(R.id.freefall_max_vspeed_value)
        textView?.text = formatted
    }

    fun updateFreefallHSpeed(hSpeed: Float) {
        val formatted = DistanceAndSpeedUtil
            .getSpeedText(DistanceAndSpeedUtil.getSpeedInUnit(hSpeed.toDouble(), unit), unit)
        val textView: TextView? = statsView.findViewById(R.id.freefall_max_hspeed_value)
        textView?.text = formatted
    }

    fun updateButtons(jumpId: Int, firstJumpId: Int, lastJumpId: Int) {
        val disabled = ContextCompat.getDrawable(requireContext(), R.drawable.ic_button_disabled)
        val leftArrow = ContextCompat.getDrawable(requireContext(), R.drawable.ic_button_left_arrow)
        val rightArrow = ContextCompat.getDrawable(requireContext(), R.drawable.ic_button_right_arrow)

        // Check limits for previous button
        if (jumpId <= firstJumpId) {
            prevButton?.setImageDrawable(disabled)
            prevButton?.isEnabled = false
        } else {
            prevButton?.setImageDrawable(leftArrow)
            prevButton?.isEnabled = true
        }

        // Check limits for next button
        if (jumpId >= lastJumpId) {
            nextButton?.setImageDrawable(disabled)
            nextButton?.isEnabled = false
        } else {
            nextButton?.setImageDrawable(rightArrow)
            nextButton?.isEnabled = true
        }
    }

    companion object {
        private val TAG: String = JumpStatsFragment::class.java.simpleName
    }
}

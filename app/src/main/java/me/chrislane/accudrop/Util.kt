package me.chrislane.accudrop

import java.util.*

object Util {

    private val TAG = Util::class.java.simpleName

    /**
     * Convert metres to feet.
     *
     * @param metres Metres to be converted.
     * @return The number of feet in the metres value.
     */
    fun metresToFeet(metres: Double): Double {
        return 3.281f * metres
    }

    /**
     * Convert metres per second to miles per hour.
     *
     * @param ms Metres per second.
     * @return Miles per hour
     */
    fun msToMph(ms: Double): Double {
        return 2.2369362920544 * ms
    }

    /**
     * Generate altitude strings with the correct unit letters.
     *
     * @param altitude The altitude.
     * @param unit     The unit of measurement.
     * @return A formatted string containing the altitude.
     */
    fun getAltitudeText(altitude: Double?, unit: Unit): String {
        val unitSymbol: String = when (unit) {
            Util.Unit.METRIC -> "m"
            Util.Unit.IMPERIAL -> "ft"
        }

        return String.format(Locale.ENGLISH, "%.0f %s", altitude, unitSymbol)
    }

    /**
     * Generate speed strings with the correct unit suffix.
     *
     * @param speed The speed.
     * @param unit  The unit of measurement.
     * @return A formatted string containing the speed.
     */
    fun getSpeedText(speed: Double?, unit: Unit): String {
        val unitSymbol: String = when (unit) {
            Util.Unit.METRIC -> "m/s"
            Util.Unit.IMPERIAL -> "mph"
        }

        return String.format(Locale.ENGLISH, "%.0f %s", speed, unitSymbol)
    }

    /**
     * Get the correct unit enum from a string of the unit name.
     *
     * @param unitString The unit name string.
     * @return The unit enum for the input string.
     */
    fun getUnit(unitString: String): Unit {
        return when (unitString) {
            "metric" -> Unit.METRIC
            "imperial" -> Unit.IMPERIAL
            else -> throw IllegalArgumentException("Unit string did not match: $unitString")
        }
    }

    fun getSpeedInUnit(speed: Double, unit: Unit): Double {
        return if (unit == Unit.IMPERIAL) {
            msToMph(speed)
        } else speed

    }

    fun getAltitudeInUnit(altitude: Double, unit: Unit): Double {
        return if (unit == Unit.IMPERIAL) {
            metresToFeet(altitude)
        } else altitude

    }

    /**
     * Convert metres to kilometres.
     *
     * @param metres The metres value to convert.
     * @return The number of kilometres for the input.
     */
    fun metresToKilometres(metres: Double): Double {
        return 0.001f * metres
    }

    /**
     * Returns a value scaled to be between 0 and 100 for an input value and an input's minimum
     * and maximum possible value.
     *
     * @param input      The value to be scaled.
     * @param min        An input's minimum possible value.
     * @param max        An input's maximum possible value.
     * @param allowedMin The output's minimum possible value.
     * @param allowedMax The output's maximum possible value.
     * @return The input value scaled between allowedMin and allowedMax.
     */
    fun getScaledValue(input: Double, min: Double, max: Double, allowedMin: Double, allowedMax: Double): Double {
        return (allowedMax - allowedMin) * (input - min) / (max - min) + allowedMin
    }

    /**
     * Types of unit of measurement.
     */
    enum class Unit {
        METRIC, IMPERIAL
    }
}
package me.chrislane.accudrop;

import java.util.Locale;

public class Util {

    private static final String TAG = Util.class.getSimpleName();

    /**
     * Convert metres to feet.
     *
     * @param metres Metres to be converted.
     * @return The number of feet in the metres value.
     */
    public static double metresToFeet(double metres) {
        return 3.281f * metres;
    }

    /**
     * Convert metres per second to miles per hour.
     *
     * @param ms Metres per second.
     * @return Miles per hour
     */
    public static double msToMph(double ms) {
        return 2.2369362920544 * ms;
    }

    /**
     * Generate altitude strings with the correct unit letters.
     *
     * @param altitude The altitude.
     * @param unit     The unit of measurement.
     * @return A formatted string containing the altitude.
     */
    public static String getAltitudeText(Double altitude, Unit unit) {
        String unitSymbol = "";
        switch (unit) {
            case METRIC:
                unitSymbol = "m";
                break;
            case IMPERIAL:
                unitSymbol = "ft";
                break;
        }

        return String.format(Locale.ENGLISH, "%.0f %s", altitude, unitSymbol);
    }

    /**
     * Generate speed strings with the correct unit suffix.
     *
     * @param speed The speed.
     * @param unit  The unit of measurement.
     * @return A formatted string containing the speed.
     */
    public static String getSpeedText(Double speed, Unit unit) {
        String unitSymbol = "";
        switch (unit) {
            case METRIC:
                unitSymbol = "m/s";
                break;
            case IMPERIAL:
                unitSymbol = "mph";
                break;
        }

        return String.format(Locale.ENGLISH, "%.0f %s", speed, unitSymbol);
    }

    /**
     * Get the correct unit enum from a string of the unit name.
     *
     * @param unitString The unit name string.
     * @return The unit enum for the input string.
     */
    public static Unit getUnit(String unitString) {
        switch (unitString) {
            case "metric":
                return Unit.METRIC;
            case "imperial":
                return Unit.IMPERIAL;
            default:
                return null;
        }
    }

    public static double getAltitudeInUnit(double altitude, Unit unit) {
        if (unit == Unit.IMPERIAL) {
            return metresToFeet(altitude);
        }

        return altitude;
    }

    /**
     * Convert metres to kilometres.
     *
     * @param metres The metres value to convert.
     * @return The number of kilometres for the input.
     */
    public static double metresToKilometres(double metres) {
        return 0.001f * metres;
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
    public static double getScaledValue(double input, double min, double max, double allowedMin, double allowedMax) {
        return ((allowedMax - allowedMin) * (input - min) / (max - min)) + allowedMin;
    }

    /**
     * Types of unit of measurement.
     */
    public enum Unit {
        METRIC, IMPERIAL
    }
}
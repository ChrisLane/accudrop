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
     * Types of unit of measurement.
     */
    public enum Unit {
        METRIC, IMPERIAL
    }
}
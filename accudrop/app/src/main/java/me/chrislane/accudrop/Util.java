package me.chrislane.accudrop;

import java.util.Locale;

public class Util {
    private static final String TAG = Util.class.getSimpleName();

    public static double metresToFeet(double metres) {
        return 3.281f * metres;
    }

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

    public static double metresToKilometres(double metres) {
        return 0.001f * metres;
    }

    public enum Unit {METRIC, IMPERIAL}
}
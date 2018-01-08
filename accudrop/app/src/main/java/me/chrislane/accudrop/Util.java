package me.chrislane.accudrop;

import java.util.Locale;

public class Util {
    private static final String TAG = Util.class.getSimpleName();

    public static float metresToFeet(float metres) {
        return 3.281f * metres;
    }

    public static String getAltitudeText(Float altitude, Unit unit) {
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

    public enum Unit {METRIC, IMPERIAL}
}
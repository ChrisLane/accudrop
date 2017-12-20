package me.chrislane.accudrop;

import java.util.Locale;

public class Util {
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

    public enum Unit {METRIC, IMPERIAL}
}
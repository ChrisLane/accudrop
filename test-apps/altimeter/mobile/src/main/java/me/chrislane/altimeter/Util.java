package me.chrislane.altimeter;

public class Util {
    public static float metresToFeet(float metres) {
        return 3.281f * metres;
    }

    public enum Unit {METRIC, IMPERIAL}
}

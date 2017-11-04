package me.chrislane.altimeter;

public class Util {
    public enum Unit {METRIC, IMPERIAL}

    public static float metresToFeet(float metres) {
        return 3.281f * metres;
    }
}

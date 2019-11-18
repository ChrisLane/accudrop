package me.chrislane.accudrop

/**
 * Types of unit of measurement.
 */
enum class UnitType(val altitudeSymbol: String, val speedSymbol: String) {
    METRIC("m", "m/s"),
    IMPERIAL("ft", "mph");
}
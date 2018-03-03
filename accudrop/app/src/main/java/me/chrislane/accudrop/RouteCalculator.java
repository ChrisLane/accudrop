package me.chrislane.accudrop;

import android.location.Location;
import android.util.Log;
import android.util.Pair;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;

import me.chrislane.accudrop.viewmodel.GnssViewModel;

public class RouteCalculator {

    private static final String TAG = RouteCalculator.class.getSimpleName();
    private final List<Location> route = new ArrayList<>();
    private final double airspeed = 15.4; // Metres per second
    private final double descentRate = 6.16; // Metres per second
    private final double windDirection;
    private final double windSpeed;
    private final double p3Altitude = 91.44; // 300ft
    private final double p2Altitude = 182.88; // 600ft
    private final double p1Altitude = 304.8; // 1000ft
    private LatLng target;
    private Location p3;
    private Location p2;
    private Location p1;

    public RouteCalculator(Pair<Double, Double> wind, LatLng target) {
        this.windSpeed = wind.first;
        this.windDirection = wind.second;
        this.target = target;
    }

    /**
     * <p>Get coordinates after a move of a certain distance in a bearing from an initial location.</p>
     * <p>Adapted from a <a href="https://stackoverflow.com/a/7835325">StackOverflow answer</a></p>
     *
     * @param initialPosition The initial position before a move.
     * @param distance        The distance to be travelled from the initial position.
     * @param bearing         The bearing to travel in from the initial position.
     * @return The coordinates after the move.
     */
    public static LatLng getPosAfterMove(LatLng initialPosition, double distance, double bearing) {
        double R = 6378.1; // Radius of Earth
        double b = Math.toRadians(bearing); // Bearing is converted to radians.
        double d = Util.metresToKilometres(distance); // Distance in km

        double lat1 = Math.toRadians(initialPosition.latitude); // Initial latitude converted to radians
        double lon1 = Math.toRadians(initialPosition.longitude); // Initial longitude converted to radians

        double lat2 = Math.asin(Math.sin(lat1) * Math.cos(d / R) +
                Math.cos(lat1) * Math.sin(d / R) * Math.cos(b));

        double lon2 = lon1 + Math.atan2(Math.sin(b) * Math.sin(d / R) * Math.cos(lat1),
                Math.cos(d / R) - Math.sin(lat1) * Math.sin(lat2));

        lat2 = Math.toDegrees(lat2);
        lon2 = Math.toDegrees(lon2);

        return new LatLng(lat2, lon2);
    }

    /**
     * Get the current landing target.
     *
     * @return The current landing target.
     */
    public LatLng getTarget() {
        return target;
    }

    /**
     * Set the target location for route calculations.
     *
     * @param target The target location.
     */
    public void setTarget(LatLng target) {
        this.target = target;
    }

    /**
     * Calculate the route.
     *
     * @return The route calculated.
     */
    public List<Location> calcRoute() {
        route.clear();

        calcP3();
        calcP2();
        calcP1();

        route.add(p1);
        route.add(p2);
        route.add(p3);

        Location ground = new Location("");
        ground.setLatitude(target.latitude);
        ground.setLongitude(target.longitude);
        ground.setAltitude(0);
        route.add(ground);

        Log.d(TAG, "Route calculated: " + route);

        return route;
    }

    /**
     * Calculate the position of the start location of the route.
     */
    private void calcP1() {
        double altitudeChange = p1Altitude - p2Altitude;
        double distance = distanceFromHeight(airspeed + windSpeed, altitudeChange);

        LatLng p2LatLng = GnssViewModel.getLatLng(p2);
        LatLng loc = getPosAfterMove(p2LatLng, distance, windDirection);
        Location location = new Location("");
        location.setLatitude(loc.latitude);
        location.setLongitude(loc.longitude);
        location.setAltitude(p1Altitude);
        p1 = location;
    }

    /**
     * Calculate the position of the second turn in the route.
     */
    private void calcP2() {
        double altitudeChange = p2Altitude - p3Altitude;
        double distance = distanceFromHeight(8.9408, altitudeChange);

        LatLng p3LatLng = GnssViewModel.getLatLng(p3);
        LatLng loc = getPosAfterMove(p3LatLng, distance, get270Bearing(windDirection));
        Location location = new Location("");
        location.setLatitude(loc.latitude);
        location.setLongitude(loc.longitude);
        location.setAltitude(p2Altitude);
        p2 = location;
    }

    /**
     * Calculate the position of the final turn in the route.
     */
    private void calcP3() {
        double distance = distanceFromHeight(airspeed - windSpeed, p3Altitude);

        // Subtract distance along upwind direction from coordinates
        LatLng loc = getPosAfterMove(target, distance, getOppositeBearing(windDirection));
        Location location = new Location("");
        location.setLatitude(loc.latitude);
        location.setLongitude(loc.longitude);
        location.setAltitude(p3Altitude);
        p3 = location;
    }

    /**
     * Calculate the distance that a canopy can travel horizontally from an altitude to the ground.
     *
     * @param groundSpeed The ground speed of the canopy in metres per second.
     * @param altitude    The altitude of the canopy in metres.
     * @return The distance in metres that can be travelled.
     */
    private double distanceFromHeight(double groundSpeed, double altitude) {
        // We know descent rate, calculate time to ground
        double seconds = altitude / descentRate;

        // Calculate distance travelled at ground speed after time
        return groundSpeed * seconds;
    }

    /**
     * Get the opposite to a bearing.
     *
     * @param bearing The initial bearing.
     * @return The opposite bearing to the input bearing.
     */
    private double getOppositeBearing(double bearing) {
        double opposite = bearing - 180;
        if (opposite < 0) {
            opposite += 360;
        }

        return opposite;
    }

    /**
     * Get the bearing 90 degrees from the input bearing.
     *
     * @param bearing The initial bearing.
     * @return The bearing 90 degrees from the input.
     */
    public double get90Bearing(double bearing) {
        double result = bearing + 90;
        if (result > 360) {
            result = 360 - result;
        }
        return result;
    }

    /**
     * Get the bearing 270 degrees from the input bearing.
     *
     * @param bearing The initial bearing.
     * @return The bearing 270 degrees from the input.
     */
    private double get270Bearing(double bearing) {
        double result = bearing - 90;
        if (result < 0) {
            result = 360 + result;
        }
        return result;
    }

    public double getSinkSpeed(double airspeed, double glideRatio) {
        return airspeed / glideRatio;
    }
}

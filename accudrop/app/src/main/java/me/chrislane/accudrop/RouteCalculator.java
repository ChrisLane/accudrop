package me.chrislane.accudrop;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;

public class RouteCalculator {
    private List<Point3D> route = new ArrayList<>();
    private LatLng target;
    private double airspeed = 8.9408; // Metres per second
    private double descentRate = 8.9408; // Metres per second
    private double windDirection = 90;
    private double p3Altitude = 91.44;
    private double p2Altitude = 182.88;
    private double p1Altitude = 304.8;
    private Point3D p3;
    private Point3D p2;
    private Point3D p1;

    public RouteCalculator(LatLng target) {
        this.target = target;
    }

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
     * @return The route.
     */
    public List<Point3D> calcRoute() {
        route.clear();

        calcP3();
        calcP2();
        calcP1();

        route.add(p1);
        route.add(p2);
        route.add(p3);
        route.add(new Point3D(target, 0));

        return route;
    }

    /**
     * Calculate the position of the start location of the route.
     */
    public void calcP1() {
        double altitudeChange = p1Altitude - p2Altitude;
        double distance = distanceFromHeight(8.9408, altitudeChange);

        LatLng loc = getPosAfterMove(p2.getLatLng(), distance, windDirection);
        p1 = new Point3D(loc, p1Altitude);

    }

    /**
     * Calculate the position of the second turn in the route.
     */
    public void calcP2() {
        double altitudeChange = p2Altitude - p3Altitude;
        double distance = distanceFromHeight(8.9408, altitudeChange);

        LatLng loc = getPosAfterMove(p3.getLatLng(), distance, get270Bearing(windDirection));
        p2 = new Point3D(loc, p2Altitude);
    }

    /**
     * Calculate the position of the final turn in the route.
     */
    public void calcP3() {
        double distance = distanceFromHeight(8.9408, p3Altitude);

        // Subtract distance along upwind direction from coordinates
        LatLng loc = getPosAfterMove(target, distance, getOppositeBearing(windDirection));
        p3 = new Point3D(loc, p3Altitude);
    }

    /**
     * Calculate the distance that a canopy can travel horizontally from an altitude to the ground.
     *
     * @param groundSpeed The ground speed of the canopy in metres per second.
     * @param altitude    The altitude of the canopy in metres.
     * @return The distance in metres that can be travelled.
     */
    public double distanceFromHeight(double groundSpeed, double altitude) {
        // We know descent rate, calculate time to ground
        double seconds = altitude / descentRate;

        // Calculate distance travelled at ground speed after time
        return groundSpeed * seconds;
    }

    /**
     * <p>Get coordinates after a move of a certain distance in a bearing from an initial location.</p>
     * <p>Adapted from a <a href=https://stackoverflow.com/a/7835325>StackOverflow answer</a></p>
     *
     * @param initialPosition The initial position before a move.
     * @param distance        The distance to be travelled from the initial position.
     * @param bearing         The bearing to travel in from the initial position.
     * @return The coordinates after the move.
     */
    public LatLng getPosAfterMove(LatLng initialPosition, double distance, double bearing) {
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
     * Get the opposite to a bearing.
     *
     * @param bearing The initial bearing.
     * @return The opposite bearing to the input bearing.
     */
    public double getOppositeBearing(double bearing) {
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
    public double get270Bearing(double bearing) {
        double result = bearing - 90;
        if (result < 0) {
            result = 360 + result;
        }
        return result;
    }
}

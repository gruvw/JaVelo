package ch.epfl.javelo.projection;

import ch.epfl.javelo.Math2;

/**
 * Coordinates converter between the WGS84 system and the Web Mercator projection.
 * <p>
 * Non-instantiable. Arguments are not checked.
 *
 * @author Lucas Jung (324724)
 * @author Florian Kolly (328313)
 */
public final class WebMercator {

    private WebMercator() {}

    /**
     * Converts from WGS84 longitude to Web Mercator projection (x coordinate).
     *
     * @param lon longitude in radians
     * @return the x coordinate (Web Mercator) from the given longitude (WGS84)
     */
    public static double x(double lon) {
        return lon / (2 * Math.PI) + 0.5;
    }

    /**
     * Converts from WGS84 latitude to Web Mercator projection (y coordinate).
     *
     * @param lat latitude in radians
     * @return the y coordinate (Web Mercator) from the given latitude (WGS84)
     */
    public static double y(double lat) {
        return 0.5 - Math2.asinh(Math.tan(lat)) / (2 * Math.PI);
    }

    /**
     * Converts from Web Mercator projection (x coordinate) to WGS84 (longitude).
     *
     * @param x coordinate in Web Mercator projection
     * @return the longitude (WGS84), in radians, corresponding to the given {@code x} coordinate
     */
    public static double lon(double x) {
        return 2 * Math.PI * x - Math.PI;
    }

    /**
     * Converts from Web Mercator projection (y coordinate) to WGS84 (latitude).
     *
     * @param y coordinate in Web Mercator projection
     * @return the latitude (WGS84), in radians, corresponding to the given {@code y} coordinate
     */
    public static double lat(double y) {
        return Math.atan(Math.sinh(Math.PI - 2 * Math.PI * y));
    }

}

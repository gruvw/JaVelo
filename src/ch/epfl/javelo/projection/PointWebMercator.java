package ch.epfl.javelo.projection;

import ch.epfl.javelo.Preconditions;

/**
 * A point in the Web Mercator projection. (record)
 *
 * @param x x coordinate
 * @param y y coordinate
 *
 * @author Lucas Jung (324724)
 * @author Florian Kolly (328313)
 */
public record PointWebMercator(double x, double y) {

    private static final int BASE_ZOOM_EXPONENT = 8;

    /**
     * Constructor of a Web Mercator point.
     *
     * @throws IllegalArgumentException if given coordinates are less than 0 or larger than 1
     *                                  (strictly)
     */
    public PointWebMercator {
        Preconditions.checkArgument(0 <= x && x <= 1);
        Preconditions.checkArgument(0 <= y && y <= 1);
    }

    /**
     * Generates the point with coordinates x and y at the specified zoom level.
     *
     * @param zoomLevel map zoom level
     * @param x         x coordinate at {@code zoomLevel} zoom level
     * @param y         y coordinate at {@code zoomLevel} zoom level
     * @return the point corresponding to the {@code x} and {@code y} coordinates at
     *         {@code zoomLevel} map zoom level
     */
    public static PointWebMercator of(int zoomLevel, double x, double y) {
        return new PointWebMercator(Math.scalb(x, -BASE_ZOOM_EXPONENT - zoomLevel),
                                    Math.scalb(y, -BASE_ZOOM_EXPONENT - zoomLevel));
    }

    /**
     * Generates the point with the given latitude and longitude (in degrees) in the WGS84 system.
     * <p>
     * Utility method used to treat human input GPS longitude and latitude.
     *
     * @param lat latitude (in degrees)
     * @param lon longitude (in degrees)
     * @return the point converted in the Web Mercator projection
     */
    public static PointWebMercator of(double lat, double lon) {
        return new PointWebMercator(WebMercator.x(Math.toRadians(lon)),
                                    WebMercator.y(Math.toRadians(lat)));
    }

    /**
     * Generates the Web Mercator point corresponding to the given Swiss coordinates point.
     *
     * @param pointCh point in the Swiss coordinates system
     * @return the point in Web Mercator projection corresponding to the given point in Switzerland
     */
    public static PointWebMercator ofPointCh(PointCh pointCh) {
        return new PointWebMercator(WebMercator.x(pointCh.lon()), WebMercator.y(pointCh.lat()));
    }

    /**
     * Computes the x coordinate at the specified zoom level.
     *
     * @param zoomLevel map zoom level
     * @return the x coordinate at the given zoom level
     */
    public double xAtZoomLevel(int zoomLevel) {
        return Math.scalb(x, BASE_ZOOM_EXPONENT + zoomLevel);
    }

    /**
     * Computes the y coordinate at the specified zoom level.
     *
     * @param zoomLevel map zoom level
     * @return the y coordinate at the given zoom level
     */
    public double yAtZoomLevel(int zoomLevel) {
        return Math.scalb(y, BASE_ZOOM_EXPONENT + zoomLevel);
    }

    /**
     * Computes the longitude of the x coordinate in WGS84.
     *
     * @return the longitude of the point (WGS84), in radians
     * @see WebMercator#lon(double)
     */
    public double lon() {
        return WebMercator.lon(x);
    }

    /**
     * Computes the latitude of the y coordinate in WGS84.
     *
     * @return the latitude of the point (WGS84), in radians
     * @see WebMercator#lat(double)
     */
    public double lat() {
        return WebMercator.lat(y);
    }

    /**
     * Computes the equivalent Swiss coordinates point.
     *
     * @return the point in the Swiss coordinates system corresponding to this point if the point is
     *         inside Switzerland's limits, {@code null} otherwise
     */
    public PointCh toPointCh() {
        double lon = lon();
        double lat = lat();
        double e = Ch1903.e(lon, lat);
        double n = Ch1903.n(lon, lat);
        return SwissBounds.containsEN(e, n) ? new PointCh(e, n) : null;
    }

}

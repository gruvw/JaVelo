package ch.epfl.javelo.projection;

import ch.epfl.javelo.Preconditions;

/**
 * A point in the Web Mercator projection (record).
 *
 * @author Lucas Jung (324724)
 */
public record PointWebMercator(double x, double y) {

    /**
     * Validates coordinates.
     *
     * @param x x coordinate
     * @param y y coordinate
     * @throws IllegalArgumentException if given coordinates are lower than 0 or larger than 1
     */
    public PointWebMercator {
        Preconditions.checkArgument(0 <= x && x <= 1);
        Preconditions.checkArgument(0 <= y && y <= 1);
    }

    /**
     * @param zoomLevel map zoom level
     * @param x x coordinate at <code>zoomLevel</code> zoom level
     * @param y y coordinate at <code>zoomLevel</code> zoom level
     * @return point corresponding to the <code>x</code> and <code>y</code> coordinates at
     *         <code>zoomLevel</code> map zoom level
     */
    public static PointWebMercator of(int zoomLevel, double x, double y) {
        return new PointWebMercator(Math.scalb(x, -8 - zoomLevel), Math.scalb(y, -8 - zoomLevel));
    }

    /**
     * @param pointCh in Swiss Coordinates system
     * @return the point in Web Mercator projection corresponding to the given point in Swiss
     *         coordinates system
     */
    public static PointWebMercator ofPointCh(PointCh pointCh) {
        return new PointWebMercator(WebMercator.x(pointCh.lon()), WebMercator.y(pointCh.lat()));
    }

    /**
     * @param zoomLevel map zoom level
     * @return x coordinate at given zoom level
     */
    public double xAtZoomLevel(int zoomLevel) {
        return Math.scalb(x, 8 + zoomLevel);
    }

    /**
     * @param zoomLevel map zoom level
     * @return y coordinate at given zoom level
     */
    public double yAtZoomLevel(int zoomLevel) {
        return Math.scalb(y, 8 + zoomLevel);
    }

    /**
     * @return longitude of the point (WGS84), in radians
     * @see WebMercator#lon(double)
     */
    public double lon() {
        return WebMercator.lon(x);
    }

    /**
     * @return latitude of the point (WGS84), in radians
     * @see WebMercator#lat(double)
     */
    public double lat() {
        return WebMercator.lat(y);
    }

    /**
     * @return the point in the Swiss coordinates system corresponding to this point if the point is
     *         inside Switzerland's limits, <code>null</code> otherwise
     */
    public PointCh toPointCh() {
        double lon = lon(), lat = lat();
        double e = Ch1903.e(lon, lat), n = Ch1903.n(lon, lat);
        return SwissBounds.containsEN(e, n) ? new PointCh(e, n) : null;
    }

}

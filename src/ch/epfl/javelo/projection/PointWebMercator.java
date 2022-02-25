package ch.epfl.javelo.projection;

import ch.epfl.javelo.Math2;
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
        Preconditions.checkArgument(x <= 1 || x >= 0);
        Preconditions.checkArgument(y <= 1 || y >= 0);
    }

    /**
     * @param zoomLevel map zoom level
     * @param x x coordinate at <code>zoomLevel</code> zoom level
     * @param y y coordinate at <code>zoomLevel</code> zoom level
     * @return point corresponding to the <code>x</code> and <code>y</code> coordinates at
     *         <code>zoomLevel</code> map zoom level
     */
    public static PointWebMercator of(int zoomLevel, double x, double y) {
        return new PointWebMercator(Math.scalb(x, 8 - zoomLevel), Math.scalb(y, 8 - zoomLevel));
    }

    /**
     * @param pointCh in Swiss Coordinates system
     * @return the point in Web Mercator projection corresponding to the given point in Swiss
     *         coordinates system
     */
    public static PointWebMercator ofPointCh(PointCh pointCh) {
        double x = pointCh.lon() / (2 * Math.PI) + 0.5;
        double y = 0.5 - Math2.asinh(Math.tan(pointCh.lat())) / (2 * Math.PI);
        return new PointWebMercator(x, y);
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
     */
    public double lon() {
        return Math.fma(2 * Math.PI, x, -Math.PI);
    }

    /**
     * @return latitude of the point (WGS84), in radians
     */
    public double lat() {
        return Math.atan(Math.sinh(Math.fma(2 * Math.PI, -y, Math.PI)));
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

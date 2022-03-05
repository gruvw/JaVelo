package ch.epfl.javelo.projection;

import ch.epfl.javelo.Preconditions;

/**
 * A point in Switzerland (record).
 *
 * @author Lucas Jung (324724)
 * @author Florian Kolly (328313)
 */
public record PointCh(double e, double n) {

    /**
     * Validates that the coordinates are inside the Swiss borders.
     *
     * @param e east coordinate
     * @param n north coordinate
     * @throws IllegalArgumentException if the point is outside of Switzerland's limits
     */
    public PointCh {
        Preconditions.checkArgument(SwissBounds.containsEN(e, n));
    }

    /**
     * @param that other point
     * @return squared distance between this point and the given one (<code>that</code>)
     */
    public double squaredDistanceTo(PointCh that) {
        return Math.pow(that.e - this.e, 2) + Math.pow(that.n - this.n, 2);
    }

    /**
     * @param that other point
     * @return distance between this point and the given one (<code>that</code>)
     */
    public double distanceTo(PointCh that) {
        return Math.sqrt(squaredDistanceTo(that));
    }

    /**
     * @return longitude of the point in the WGS84 system, in radians
     */
    public double lon() {
        return Ch1903.lon(e, n);
    }

    /**
     * @return latitude of the point in the WGS84 system, in radians
     */
    public double lat() {
        return Ch1903.lat(e, n);
    }

}

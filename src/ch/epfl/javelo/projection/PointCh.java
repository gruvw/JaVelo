// TODO: read Florian
package ch.epfl.javelo.projection;

import ch.epfl.javelo.Preconditions;

/**
 * A point in Switzerland. (record)
 *
 * @param e east coordinate
 * @param n north coordinate
 *
 * @author Lucas Jung (324724)
 * @author Florian Kolly (328313)
 */
public record PointCh(double e, double n) {

    /**
     * Constructor of a point in Switzerland.
     *
     * @throws IllegalArgumentException if the point is outside of Switzerland's limits
     */
    public PointCh {
        Preconditions.checkArgument(SwissBounds.containsEN(e, n));
    }

    /**
     * Computes the squared distance to another point.
     *
     * @param that other point
     * @return the squared distance between this point and the given one ({@code that})
     */
    public double squaredDistanceTo(PointCh that) {
        return Math.pow(that.e - this.e, 2) + Math.pow(that.n - this.n, 2);
    }

    /**
     * Computes the distance to another point.
     *
     * @param that other point
     * @return the distance between this point and the given one ({@code that})
     */
    public double distanceTo(PointCh that) {
        return Math.sqrt(squaredDistanceTo(that));
    }

    /**
     * Computes the longitude in the WGS84 system.
     *
     * @return the longitude of the point in the WGS84 system, in radians
     */
    public double lon() {
        return Ch1903.lon(e, n);
    }

    /**
     * Computes the latitude in the WGS84 system.
     *
     * @return the latitude of the point in the WGS84 system, in radians
     */
    public double lat() {
        return Ch1903.lat(e, n);
    }

}

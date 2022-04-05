// TODO: read Lucas
package ch.epfl.javelo.routing;

import ch.epfl.javelo.projection.PointCh;

/**
 * A point on a route that is the closest to a point of reference (in Switzerland). (record)
 *
 * @param point               point on the route
 * @param position            position of the point on the route, in meters
 * @param distanceToReference distance between the point on the route and the point of reference, in
 *                            meters
 *
 * @author Lucas Jung (324724)
 * @author Florian Kolly (328313)
 */
public record RoutePoint(PointCh point, double position, double distanceToReference) {

    /**
     * Represents a point that does not exist.
     */
    public static final RoutePoint NONE = new RoutePoint(null, Double.NaN,
            Double.POSITIVE_INFINITY);

    /**
     * Generates an identical point to the current instance where the position is shifted by
     * {@code positionDifference}.
     *
     * @param positionDifference shifting value (can be positive, negative or 0)
     * @return a shifted copy of the current instance
     */
    public RoutePoint withPositionShiftedBy(double positionDifference) {
        return new RoutePoint(point, position + positionDifference, distanceToReference);
    }

    /**
     * Returns the point on the route that is the closest to a point of reference between the
     * current instance and {@code that}.
     *
     * @param that other instance to compare the distance to the point of reference against
     * @return {@code that} if it is strictly closer to the point of reference, {@code this}
     *         otherwise
     */
    public RoutePoint min(RoutePoint that) {
        return this.distanceToReference > that.distanceToReference ? that : this;
    }

    /**
     * Returns the point on the route that is the closest to a point of reference between the
     * current instance and a point at {@code thatDistanceToReference} meters away from the point of
     * reference.
     *
     * @param thatPoint               a point at {@code thatDistanceToReference} away from the point
     *                                of reference
     * @param thatPosition            the position of the point
     * @param thatDistanceToReference distance from the point to the point of reference
     * @return a new instance of {@code RoutePoint} corresponding to {@code thatPoint} if
     *         {@code thatDistanceToReference} is strictly smaller than the distance from
     *         {@code this} to the reference, {@code this} otherwise
     */
    public RoutePoint min(PointCh thatPoint, double thatPosition, double thatDistanceToReference) {
        return this.distanceToReference <= thatDistanceToReference ? this
                : new RoutePoint(thatPoint, thatPosition, thatDistanceToReference);
    }

}

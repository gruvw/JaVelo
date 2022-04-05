// TODO: read Lucas
package ch.epfl.javelo.routing;

import java.util.List;
import ch.epfl.javelo.projection.PointCh;

/**
 * Represents a route. (interface)
 *
 * @author Lucas Jung (324724)
 * @author Florian Kolly (328313)
 */
public interface Route {

    /**
     * Retrieves the index of the segment at a given position.
     *
     * @param position position on the route, in meters
     * @return the index of the segment at the position {@code position}
     */
    public abstract int indexOfSegmentAt(double position);

    /**
     * Computes the length of the route.
     *
     * @return the length of the route, in meters
     */
    public abstract double length();

    /**
     * Retrieves all edges belonging to the route.
     *
     * @return a list of every edge constituting the route
     */
    public abstract List<Edge> edges();

    /**
     * Retrieves all points located on the extremities of every edge belonging to the route.
     *
     * @return a list of every point located on the extremities of every edge belonging to the route
     */
    public abstract List<PointCh> points();

    /**
     * Retrieves the point at a given position on the route.
     *
     * @param position a position on the route, in meters
     * @return the point at the given position on the route
     */
    public abstract PointCh pointAt(double position);

    /**
     * Retrieves the altitude at a given position on the route.
     *
     * @param position a position on the route, in meters
     * @return the elevation at the given position
     */
    public abstract double elevationAt(double position);

    /**
     * Retrieves the id of the closest node (belonging to the route) to a given position.
     *
     * @param position a position on the route
     * @return the id (index) of the closest node (on the route) to the given position
     */
    public abstract int nodeClosestTo(double position);

    /**
     * Retrieves the point of the route which is the closest to a given point in Switzerland.
     *
     * @param point a point in Switzerland
     * @return the point of the route which is the closest to the given point
     */
    public abstract RoutePoint pointClosestTo(PointCh point);

}

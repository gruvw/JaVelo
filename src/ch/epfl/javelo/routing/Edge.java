package ch.epfl.javelo.routing;

import java.util.function.DoubleUnaryOperator;
import ch.epfl.javelo.Math2;
import ch.epfl.javelo.data.Graph;
import ch.epfl.javelo.projection.PointCh;

/**
 * An edge of a route. (record)
 *
 * @param fromNodeId index (id) of the edge's starting node
 * @param toNodeId   index (id) of the edge's destination node
 * @param fromPoint  edge's starting position
 * @param toPoint    edge's destination position
 * @param length     edge's length, in meters
 * @param profile    edge's profile
 *
 * @author Lucas Jung (324724)
 * @author Florian Kolly (328313)
 */
public record Edge(int fromNodeId, int toNodeId, PointCh fromPoint, PointCh toPoint, double length,
                   DoubleUnaryOperator profile) {

    /**
     * Generates the edge with the specified {@code fromNodeId} and {@code toNodeId} (the other
     * attributes are found in the graph).
     *
     * @param graph      graph containing the edge
     * @param edgeId     id (index) of the edge
     * @param fromNodeId index (id) of the edge's starting node
     * @param toNodeId   index (id) of the edge's destination node
     * @return the edge, corresponding to the given id, initialized through the parameters and the
     *         graph
     */
    public static Edge of(Graph graph, int edgeId, int fromNodeId, int toNodeId) {
        // FIXME from the edge ??
        PointCh fromPoint = graph.nodePoint(fromNodeId);
        PointCh toPoint = graph.nodePoint(toNodeId);
        double length = graph.edgeLength(edgeId);
        DoubleUnaryOperator profile = graph.edgeProfile(edgeId);
        return new Edge(fromNodeId, toNodeId, fromPoint, toPoint, length, profile);
    }

    /**
     * Computes the position, in meters, of the closest point to a given point, along this edge (or
     * the line extending this edge).
     *
     * @param point point for which the nearest position is sought
     * @return the position along this edge (or the line extending this edge), in meters
     */
    public double positionClosestTo(PointCh point) {
        return Math2.projectionLength(fromPoint.e(), fromPoint.n(), toPoint.e(), toPoint.n(),
                point.e(), point.n());
    }

    // TODO: try with interpolate
    /**
     * Returns a point at a given position along the edge.
     *
     * @param position position along this edge, in meters
     * @return the point at the given position
     */
    public PointCh pointAt(double position) {
        double eastDiff = toPoint.e() - fromPoint.e();
        double northDiff = toPoint.n() - fromPoint.n();
        double ratio = position / length;
        double n = northDiff * ratio + fromPoint.n();
        double e = eastDiff * ratio + fromPoint.e();
        return new PointCh(e, n);
    }

    /**
     * Retrieves the altitude, in meters, at a given position along the edge.
     *
     * @param position position along this edge, in meters
     * @return the elevation at the given position
     */
    public double elevationAt(double position) {
        return profile.applyAsDouble(position);
    }

}

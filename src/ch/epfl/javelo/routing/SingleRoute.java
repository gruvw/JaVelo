package ch.epfl.javelo.routing;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import ch.epfl.javelo.Math2;
import ch.epfl.javelo.Preconditions;
import ch.epfl.javelo.projection.PointCh;

/**
 * Represents a basic route (without intermediate waypoints).
 * <p>
 * Immutable.
 *
 * @author Lucas Jung (324724)
 * @author Florian Kolly (328313)
 */
public final class SingleRoute implements Route {

    private final List<Edge> edges;
    private final List<PointCh> points;
    private final double[] runningLengths;

    /**
     * SingleRoute's constructor.
     *
     * @param edges edges composing the route
     * @throws IllegalArgumentException if the edges' list is empty
     */
    public SingleRoute(List<Edge> edges) {
        Preconditions.checkArgument(!edges.isEmpty());
        this.edges = List.copyOf(edges);
        runningLengths = new double[this.edges.size() + 1];
        List<PointCh> points = new ArrayList<>();
        runningLengths[0] = 0;
        points.add(this.edges.get(0).fromPoint());
        for (int i = 0; i < this.edges.size(); i++) {
            Edge edge = this.edges.get(i);
            runningLengths[i + 1] = runningLengths[i] + edge.length();
            points.add(edge.toPoint());
        }
        this.points = List.copyOf(points);
    }

    @Override
    public int indexOfSegmentAt(double position) {
        return 0;
    }

    @Override
    public double length() {
        return runningLengths[runningLengths.length - 1];
    }

    @Override
    public List<Edge> edges() {
        return edges;
    }

    @Override
    public List<PointCh> points() {
        return points;
    }

    @Override
    public PointCh pointAt(double position) {
        double clampedPosition = Math2.clamp(0, position, length());
        int edgeIndex = indexAt(clampedPosition);
        return edges.get(edgeIndex).pointAt(clampedPosition - runningLengths[edgeIndex]);
    }

    @Override
    public double elevationAt(double position) {
        double clampedPosition = Math2.clamp(0, position, length());
        int edgeIndex = indexAt(clampedPosition);
        return edges.get(edgeIndex).elevationAt(clampedPosition - runningLengths[edgeIndex]);
    }

    @Override
    public int nodeClosestTo(double position) {
        double clampedPosition = Math2.clamp(0, position, length());
        Edge edge = edges.get(indexAt(clampedPosition));
        PointCh point = pointAt(clampedPosition);
        double distFrom = point.distanceTo(edge.fromPoint());
        double distTo = point.distanceTo(edge.toPoint());
        // Returns destination node id when the position is in the middle of the edge
        return distFrom < distTo ? edge.fromNodeId() : edge.toNodeId();
    }

    @Override
    public RoutePoint pointClosestTo(PointCh point) {
        RoutePoint closest = RoutePoint.NONE;
        for (int i = 0; i < edges.size(); i++) {
            Edge edge = edges.get(i);
            double proj = edge.positionClosestTo(point);
            // proj < 0: edge's starting point, proj > edge's length: edge's destination point
            PointCh closestEdgePoint = edge.pointAt(Math2.clamp(0, proj, edge.length()));
            double distanceToPoint = closestEdgePoint.distanceTo(point);
            double positionOnRoute = runningLengths[i]
                    + edge.fromPoint().distanceTo(closestEdgePoint);
            closest = closest.min(closestEdgePoint, positionOnRoute, distanceToPoint);
        }
        return closest;
    }

    private int indexAt(double position) {
        int index = Arrays.binarySearch(runningLengths, position);
        // binarySearch starts at -1, goes up to length (included)
        return Math2.clamp(0, index >= 0 ? index : (-index - 2), edges.size() - 1);
    }

}

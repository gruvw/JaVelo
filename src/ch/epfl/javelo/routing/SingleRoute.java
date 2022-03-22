package ch.epfl.javelo.routing;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import ch.epfl.javelo.Math2;
import ch.epfl.javelo.Preconditions;
import ch.epfl.javelo.projection.PointCh;

/**
 * Represents a simple route (without intermediate waypoints).
 * <p>
 * Immutable.
 *
 * @author Lucas Jung (324724)
 * @author Florian Kolly (328313)
 */
public final class SingleRoute implements Route {

    private final List<Edge> edges;
    private final double[] runningLength;

    /**
     * SingleRoute's constructor.
     *
     * @param edges edges composing the route
     * @throws IllegalArgumentException if the edges' list is empty
     */
    public SingleRoute(List<Edge> edges) {
        Preconditions.checkArgument(!edges.isEmpty());
        this.edges = List.copyOf(edges);
        runningLength = new double[edges.size() + 1];
        runningLength[0] = 0;
        for (int i = 0; i < edges.size(); i++)
            runningLength[i + 1] = runningLength[i] + edges.get(i).length();
    }

    @Override
    public int indexOfSegmentAt(double position) {
        return 0;
    }

    @Override
    public double length() {
        return runningLength[edges.size()];
    }

    @Override
    public List<Edge> edges() {
        // FIXME: copy or not copy (class is immutable!)
        return List.copyOf(edges);
    }

    @Override
    public List<PointCh> points() {
        // TODO: do it in constructor ?
        List<PointCh> points = new ArrayList<>();
        // TODO: first point (firstEdge.fromPoint)
        for (Edge edge : edges) {
            points.add(edge.toPoint());
        }
        // FIXME: is the copy necessary ?
        return List.copyOf(points);
    }

    private int edgeIndex(double position) {
        position = Math2.clamp(0, position, length());
        int index = Arrays.binarySearch(runningLength, position);
        // binarySearch starts at -1
        return Math2.clamp(0, index >= 0 ? index : (-index - 2), edges.size() - 1);
    }

    @Override
    public PointCh pointAt(double position) {
        int edgeIndex = edgeIndex(position);
        return edges.get(edgeIndex).pointAt(position - runningLength[edgeIndex]);
    }

    @Override
    public double elevationAt(double position) {
        int edgeIndex = edgeIndex(position);
        return edges.get(edgeIndex).elevationAt(position - runningLength[edgeIndex]);
    }

    @Override
    public int nodeClosestTo(double position) {
        Edge edge = edges.get(edgeIndex(position));
        PointCh point = pointAt(position);
        double distFrom = point.distanceTo(edge.fromPoint());
        double distTo = point.distanceTo(edge.toPoint());
        return distFrom < distTo ? edge.fromNodeId() : edge.toNodeId();
    }

    @Override
    public RoutePoint pointClosestTo(PointCh point) {
        RoutePoint closest = RoutePoint.NONE;
        for (int i = 0; i < edges.size(); i++) {
            Edge edge = edges.get(i);
            double proj = Math2.projectionLength(edge.fromPoint().e(), edge.fromPoint().n(),
                    edge.toPoint().e(), edge.toPoint().n(), point.e(), point.n());
            closest = closest.min(point, runningLength[i] + proj, proj);
        }
        return closest;
    }

}

package ch.epfl.javelo.routing;

import java.util.ArrayList;
import java.util.List;

import ch.epfl.javelo.Preconditions;
import ch.epfl.javelo.projection.PointCh;

/**
 * Represents a route without intermediate waypoints.
 * <p>
 * Immutable.
 *
 * @author Lucas Jung (324724)
 * @author Florian Kolly (328313)
 */
public final class SingleRoute implements Route {
    private final List<Edge> edges;

    /**
     * SingleRoute's constructor.
     * 
     * @param edges edges composing the route
     * @throws IllegalArgumentException if the edges' list is empty
     */
    public SingleRoute(List<Edge> edges) {
        Preconditions.checkArgument(!edges.isEmpty());
        this.edges = List.copyOf(edges);
    }

    @Override
    public int indexOfSegmentAt(double position) {
        return 0;
    }

    @Override
    public double length() {
        double length = 0;
        for (Edge edge : edges) {
            length += edge.length();
        }
        return length;
    }

    @Override
    public List<Edge> edges() {
        // TODO: copy or not copy, that is the question (class is immutable!)
        return List.copyOf(edges);
    }

    @Override
    public List<PointCh> points() {
        List<PointCh> points = new ArrayList<>();
        for (Edge edge : edges) {
            points.add(edge.toPoint());
        }
        // TODO: seems like a safe thing to do
        return List.copyOf(points);
    }

    @Override
    public PointCh pointAt(double position) {
        // TODO
    }

    @Override
    public double elevationAt(double position) {
        // TODO
    }

    @Override
    public int nodeClosestTo(double position) {
        // TODO
    }

    @Override
    public RoutePoint pointClosestTo(PointCh point) {
        // TODO
    }

}

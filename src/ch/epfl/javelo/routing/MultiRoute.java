// TODO: read Lucas
// TODO: read Florian
package ch.epfl.javelo.routing;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import ch.epfl.javelo.Math2;
import ch.epfl.javelo.Preconditions;
import ch.epfl.javelo.projection.PointCh;

/**
 * Represents a route composed of multiple segments.
 * <p>
 * Immutable.
 *
 * @author Lucas Jung (324724)
 * @author Florian Kolly (328313)
 */
public final class MultiRoute implements Route {

    private final List<Route> segments;
    private final List<Edge> edges;
    private final List<PointCh> points;
    private final double[] runningLengths;

    /**
     * MultiRoute's constructor.
     *
     * @param segments segments composing the route
     * @throws IllegalArgumentException if the list of segments is empty
     */
    public MultiRoute(List<Route> segments) {
        Preconditions.checkArgument(!segments.isEmpty());
        this.segments = List.copyOf(segments);
        List<Edge> edges = new ArrayList<>();
        List<PointCh> points = new ArrayList<>();
        runningLengths = new double[this.segments.size() + 1];
        runningLengths[0] = 0;
        for (int i = 0; i < segments.size(); i++) {
            Route segment = this.segments.get(i);
            edges.addAll(segment.edges());
            points.addAll(segment.points());
            if (i != segments.size() - 1) // remove duplicates (don't remove last)
                points.remove(points.size() - 1);
            runningLengths[i + 1] = runningLengths[i] + segment.length();
        }
        this.edges = List.copyOf(edges);
        this.points = List.copyOf(points);
    }

    private int indexAt(double position) {
        int index = Arrays.binarySearch(runningLengths, position);
        // binarySearch starts at -1, goes up to length (included)
        return Math2.clamp(0, index >= 0 ? index : (-index - 2), segments.size() - 1);
    }

    @Override
    public int indexOfSegmentAt(double position) {
        int index = indexAt(position);
        int indexCount = 0;
        for (int i = 0; i < index; i++) {
            Route segment = segments.get(i);
            indexCount += segment.indexOfSegmentAt(segment.length()) + 1;
        }
        indexCount += segments.get(index).indexOfSegmentAt(position - runningLengths[index]);
        return indexCount;
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
        int segmentIndex = indexAt(position);
        return segments.get(segmentIndex).pointAt(position - runningLengths[segmentIndex]);
    }

    @Override
    public double elevationAt(double position) {
        int segmentIndex = indexAt(position);
        return segments.get(segmentIndex).elevationAt(position - runningLengths[segmentIndex]);
    }

    @Override
    public int nodeClosestTo(double position) {
        int segmentIndex = indexAt(position);
        return segments.get(segmentIndex).nodeClosestTo(position - runningLengths[segmentIndex]);
    }

    @Override
    public RoutePoint pointClosestTo(PointCh point) {
        RoutePoint closest = RoutePoint.NONE;
        for (int i = 0; i < segments.size(); i++)
            closest = closest.min(
                    segments.get(i).pointClosestTo(point).withPositionShiftedBy(runningLengths[i]));
        return closest;
    }

}

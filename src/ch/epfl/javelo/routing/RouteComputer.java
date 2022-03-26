package ch.epfl.javelo.routing;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.PriorityQueue;
import ch.epfl.javelo.Preconditions;
import ch.epfl.javelo.data.Graph;
import ch.epfl.javelo.projection.PointCh;

/**
 * Represents a route planning.
 * <p>
 * Immutable.
 *
 * @author Lucas Jung (324724)
 * @author Florian Kolly (328313)
 */
public final class RouteComputer {

    private final Graph graph;
    private final CostFunction costFunction;

    /**
     * RouteComputer's constructor.
     *
     * @param graph        JaVelo graph
     * @param costFunction cost function to consider for the computation of the route
     */
    public RouteComputer(Graph graph, CostFunction costFunction) {
        this.graph = graph;
        this.costFunction = costFunction;
    }

    /**
     * Computes the route with the minimal total cost between two nodes.
     *
     * @param startNodeId id (index) of the route's starting node
     * @param endNodeId   id (index) of the route's destination node
     * @return the route with the minimal total cost between the node with id {@code startNodeId}
     *         and the node with id {@code endNodeId}, or {@code null} if no route exists between
     *         those nodes
     * @throws IllegalArgumentException if the starting node and the destination node are the same
     */
    public Route bestRouteBetween(int startNodeId, int endNodeId) {
        // FIXME: why WeightedNode inside method and not inside class ?
        /**
         * Represents a weighted node (record). Used to determine the best node to visit next.
         *
         * @param nodeId   id (index) of the node
         * @param distance smallest known weighted distance from the starting node to this node
         * @param score    value used to compare two nodes (which one to visit next)
         */
        record WeightedNode(int nodeId, float distance, float score)
                implements Comparable<WeightedNode> {

            @Override
            public int compareTo(WeightedNode that) {
                return Float.compare(this.score, that.score);
            }

        }

        // FIXME: allowed to create a Tuple and use it in the hashMap ? nodeId -> (
        // fromNodeId, edgeId, distance)
        record PreviousNode(int fromNodeId, int edgeId, float distance) {
        }

        Preconditions.checkArgument(startNodeId != endNodeId);
        PriorityQueue<WeightedNode> toVisit = new PriorityQueue<WeightedNode>();
        // FIXME: I don't init distances to inf and previous to 0 so that I don't loop here
        // toNodeId -> (fromNodeId, edgeId, distance)
        HashMap<Integer, PreviousNode> previous = new HashMap<Integer, PreviousNode>();
        // FIXME: using done instead of Float NegativeInf
        HashSet<Integer> done = new HashSet<Integer>();
        // Score is not 0 but it is the only element in toVisit so it does not matter
        toVisit.add(new WeightedNode(startNodeId, 0, 0));
        PointCh endPoint = graph.nodePoint(endNodeId);

        while (!toVisit.isEmpty()) {
            WeightedNode current = toVisit.poll();
            if (done.contains(current.nodeId)) // faster evaluation
                continue;
            if (current.nodeId == endNodeId) {
                List<Edge> edges = new ArrayList<Edge>();
                int toNodeId = endNodeId;
                do {
                    PreviousNode fromNode = previous.get(toNodeId);
                    Edge edge = Edge.of(graph, fromNode.edgeId, fromNode.fromNodeId, toNodeId);
                    edges.add(edge);
                    toNodeId = fromNode.fromNodeId;
                } while (toNodeId != startNodeId);
                return new SingleRoute(edges);
            }
            int outDegree = graph.nodeOutDegree(current.nodeId);
            PointCh fromPoint = graph.nodePoint(current.nodeId);
            for (int edgeIndex = 0; edgeIndex < outDegree; edgeIndex++) {
                int edgeId = graph.nodeOutEdgeId(current.nodeId, edgeIndex);
                int toNodeId = graph.edgeTargetNodeId(edgeId);
                PointCh toPoint = graph.nodePoint(toNodeId);
                double cost = costFunction.costFactor(current.nodeId, edgeId);
                // FIXME: why cast ? why weightedNode distance is not double ?
                float distance = current.distance + (float) (cost * toPoint.distanceTo(fromPoint));
                // Using euclidean distance to destination as heuristic
                float score = distance + (float) toPoint.distanceTo(endPoint);
                if (!previous.containsKey(toNodeId) || distance < previous.get(toNodeId).distance) {
                    previous.put(toNodeId, new PreviousNode(current.nodeId, edgeId, distance));
                    toVisit.add(new WeightedNode(toNodeId, distance, score));
                }
            }
            done.add(current.nodeId);
        }
        return null;
    }

}

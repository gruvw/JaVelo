package ch.epfl.javelo.routing;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.PriorityQueue;
import ch.epfl.javelo.Preconditions;
import ch.epfl.javelo.data.Graph;
import ch.epfl.javelo.data.GraphNodes;
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
        // TODO: Explain what happens in our algo if multiple routes have the same cost
        // FIXME: why WeightedNode inside method and not inside class ?
        /**
         * Represents a weighted node (record).
         *
         * @param nodeId   id (index) of the node
         * @param distance smallest known distance to the node from the starting node
         */
        record WeightedNode(int nodeId, float distance) implements Comparable<WeightedNode> {

            @Override
            public int compareTo(WeightedNode that) {
                return Float.compare(this.distance, that.distance);
            }

        }

        // FIXME: allowed to create a Tuple and use it in the hashMap ? nodeId -> (
        // fromNodeId, edgeId)
        record FromNode(int fromNodeId, int edgeId) {
        }

        Preconditions.checkArgument(startNodeId != endNodeId);
        PriorityQueue<WeightedNode> toVisit = new PriorityQueue<WeightedNode>();
        // FIXME: I don't init distances to inf and previous to 0 so that I don't loop here
        HashMap<Integer, Float> visited = new HashMap<Integer, Float>(); // nodeId -> distance
        HashMap<Integer, FromNode> previous = new HashMap<Integer, FromNode>(); // toNodeId ->
                                                                                // FromNode
        toVisit.add(new WeightedNode(startNodeId, 0));
        // PointCh endPoint = graph.nodePoint(endNodeId);
        int i = 0;
        while (!toVisit.isEmpty()) {
            i++;
            WeightedNode current = toVisit.poll();
            if (current.nodeId == endNodeId) {
                List<Edge> edges = new ArrayList<Edge>();
                int toNodeId = endNodeId;
                do {
                    FromNode fromNode = previous.get(toNodeId);
                    Edge edge = Edge.of(graph, fromNode.edgeId, fromNode.fromNodeId, toNodeId);
                    edges.add(edge);
                    toNodeId = fromNode.fromNodeId;
                } while (toNodeId != startNodeId);
                System.out.println(i);
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
                if (!visited.containsKey(toNodeId) || distance < visited.get(toNodeId)) {
                    visited.put(toNodeId, distance);
                    previous.put(toNodeId, new FromNode(current.nodeId, edgeId));
                    toVisit.add(new WeightedNode(toNodeId, distance));
                }
            }
        }
        return null;
    }

}

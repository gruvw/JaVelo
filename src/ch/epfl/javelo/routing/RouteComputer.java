package ch.epfl.javelo.routing;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.PriorityQueue;
import ch.epfl.javelo.Preconditions;
import ch.epfl.javelo.data.Graph;
import ch.epfl.javelo.data.GraphNodes;

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

        Preconditions.checkArgument(startNodeId != endNodeId);
        PriorityQueue<WeightedNode> toVisit = new PriorityQueue<WeightedNode>();
        // FIXME: I don't init distances to inf and previous to 0 so that I don't loop here
        // FIXME: allowed to create a Tuple and use it in the hashMap ? nodeId -> (distance, Edge)
        HashMap<Integer, Float> visited = new HashMap<Integer, Float>(); // nodeId -> distance
        HashMap<Integer, Edge> previous = new HashMap<Integer, Edge>(); // toNodeId -> Edge
        toVisit.add(new WeightedNode(startNodeId, 0));
        int i = 0;
        while (!toVisit.isEmpty()) {
            i++;
            WeightedNode current = toVisit.poll();
            if (current.nodeId == endNodeId) {
                List<Edge> edges = new ArrayList<Edge>();
                int nodeId = current.nodeId;
                do {
                    Edge edge = previous.get(nodeId);
                    edges.add(edge);
                    nodeId = edge.fromNodeId();
                } while (nodeId != startNodeId);
                System.out.println(i);
                return new SingleRoute(edges);
            }
            int outDegree = graph.nodeOutDegree(current.nodeId);
            for (int edgeIndex = 0; edgeIndex < outDegree; edgeIndex++) {
                int edgeId = graph.nodeOutEdgeId(current.nodeId, edgeIndex);
                int toNodeId = graph.edgeTargetNodeId(edgeId);
                Edge edge = Edge.of(graph, edgeId, current.nodeId, toNodeId);
                double cost = costFunction.costFactor(current.nodeId, edgeId);
                // FIXME: why cast ? why weightedNode distance is not double ?
                float distance = current.distance + (float) (cost * edge.length());
                if (!visited.containsKey(toNodeId) || distance < visited.get(toNodeId)) {
                    visited.put(toNodeId, distance);
                    previous.put(toNodeId, edge);
                    toVisit.add(new WeightedNode(toNodeId, distance));
                }
            }
        }
        return null;
    }

}

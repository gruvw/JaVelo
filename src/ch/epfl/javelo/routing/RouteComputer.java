package ch.epfl.javelo.routing;

import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;
import ch.epfl.javelo.Preconditions;
import ch.epfl.javelo.data.Graph;
import ch.epfl.javelo.projection.PointCh;

/**
 * Represents a route planner. Used to compute the best route.
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
     * Generates the route/path ending at {@code currentNodeId}.
     *
     * @param previous      map linking a node id to the id of the previous node
     * @param previousEdge  map linking a node id to the id of the edge reaching it
     * @param currentNodeId last node of the route we want to reconstruct
     * @return the route ending at {@code currentNodeId}
     */
    private Route reconstructRoute(int[] previous, int[] previousEdge, int currentNodeId) {
        List<Edge> edges = new LinkedList<Edge>();
        int toNodeId = currentNodeId;
        while (previous[toNodeId] != 0) {
            Edge edge = Edge.of(graph, previousEdge[toNodeId], previous[toNodeId], toNodeId);
            edges.add(0, edge);
            toNodeId = previous[toNodeId];
        }
        return new SingleRoute(edges);
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

        /**
         * Represents a weighted node (record). Used to determine the best node to visit next.
         *
         * @param nodeId id (index) of the node
         * @param score  value used to compare two nodes, the one with the smallest score will be
         *               visited first
         */
        record WeightedNode(int nodeId, float score) implements Comparable<WeightedNode> {

            @Override
            public int compareTo(WeightedNode that) {
                return Float.compare(this.score, that.score);
            }

        }

        Preconditions.checkArgument(startNodeId != endNodeId);
        PriorityQueue<WeightedNode> toVisit = new PriorityQueue<WeightedNode>();
        int nodeCount = graph.nodeCount();
        float[] distances = new float[nodeCount];
        int[] previous = new int[nodeCount];
        // FIXME: Using list of previousEdge ?
        int[] previousEdge = new int[nodeCount];
        // FIXME: set distance to inf for every node ?
        for (int i = 0; i < distances.length; i++)
            distances[i] = Float.POSITIVE_INFINITY;

        // Score is not 0 but it is the only element in toVisit so it does not matter
        distances[startNodeId] = 0;
        toVisit.add(new WeightedNode(startNodeId, 0));
        PointCh endPoint = graph.nodePoint(endNodeId);

        // FIXME: split in multi methods ?
        while (!toVisit.isEmpty()) {
            WeightedNode current = toVisit.poll();
            if (distances[current.nodeId] == Float.NEGATIVE_INFINITY)
                continue;
            if (current.nodeId == endNodeId) // path found
                return reconstructRoute(previous, previousEdge, current.nodeId);

            int outDegree = graph.nodeOutDegree(current.nodeId);
            for (int edgeIndex = 0; edgeIndex < outDegree; edgeIndex++) {
                int edgeId = graph.nodeOutEdgeId(current.nodeId, edgeIndex);
                int toNodeId = graph.edgeTargetNodeId(edgeId);
                if (distances[toNodeId] == Float.NEGATIVE_INFINITY)
                    continue;
                double cost = costFunction.costFactor(current.nodeId, edgeId);
                float distance = (float) (distances[current.nodeId]
                        + cost * graph.edgeLength(edgeId));
                if (distance < distances[toNodeId]) {
                    // Using euclidean distance to destination as heuristic
                    PointCh toPoint = graph.nodePoint(toNodeId);
                    float score = distance + (float) toPoint.distanceTo(endPoint);
                    distances[toNodeId] = distance;
                    previous[toNodeId] = current.nodeId;
                    previousEdge[toNodeId] = edgeId;
                    toVisit.add(new WeightedNode(toNodeId, score));
                }
            }
            distances[current.nodeId] = Float.NEGATIVE_INFINITY;
        }
        return null; // path does not exist
    }

}

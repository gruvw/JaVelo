package ch.epfl.javelo.routing;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;
import ch.epfl.javelo.Bits;
import ch.epfl.javelo.Preconditions;
import ch.epfl.javelo.data.Graph;
import ch.epfl.javelo.projection.PointCh;

/**
 * Represents a route planner. Used to compute the best route between two nodes.
 * <p>
 * Immutable.
 *
 * @author Lucas Jung (324724)
 * @author Florian Kolly (328313)
 */
public final class RouteComputer {

    private static final float DEFAULT_DISTANCE = Float.POSITIVE_INFINITY;
    private static final float SEEN_DISTANCE = Float.NEGATIVE_INFINITY;

    /**
     * Number of bits taken by the previous node id in the packed previous node id and outgoing edge
     * index.
     */
    private static final byte NODE_ID_LENGTH = 28;

    /**
     * Number of bits taken by the outgoing edge index in the packed previous node id and outgoing
     * edge index.
     */
    private static final byte EDGE_INDEX_LENGTH = 4;

    private final Graph graph;
    private final CostFunction costFunction;

    /**
     * RouteComputer's constructor.
     * <p>
     * WARNING: Creating a route computer with a modifiable {@code costFunction} violates
     * immutability.
     *
     * @param graph        JaVelo graph
     * @param costFunction cost function to consider for the route computation
     */
    public RouteComputer(Graph graph, CostFunction costFunction) {
        this.graph = graph;
        this.costFunction = costFunction;
    }

    /**
     * Computes the route minimizing the total cost between two nodes.
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
         * Represents a weighted node. (record)
         * <p>
         * Used to determine the best node to visit next.
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
        Queue<WeightedNode> toVisit = new PriorityQueue<>();
        int nodeCount = graph.nodeCount();
        float[] distances = new float[nodeCount];
        // Packed outgoing edge index with previous node id (U4 U28)
        int[] previous = new int[nodeCount];
        Arrays.fill(distances, DEFAULT_DISTANCE);
        distances[startNodeId] = 0;
        previous[startNodeId] = -1;
        // Score is not 0 but it is the only element in toVisit so it does not matter
        toVisit.add(new WeightedNode(startNodeId, 0));
        PointCh endPoint = graph.nodePoint(endNodeId);

        while (!toVisit.isEmpty()) {
            WeightedNode current = toVisit.poll();
            // In case node was added twice in toVisit
            if (distances[current.nodeId] == SEEN_DISTANCE)
                continue;
            if (current.nodeId == endNodeId) // path found
                return reconstructRoute(previous, current.nodeId);

            int outDegree = graph.nodeOutDegree(current.nodeId);
            for (int edgeIndex = 0; edgeIndex < outDegree; edgeIndex++) {
                int edgeId = graph.nodeOutEdgeId(current.nodeId, edgeIndex);
                int toNodeId = graph.edgeTargetNodeId(edgeId);
                // Don't evaluate cost function if node has already been visited
                if (distances[toNodeId] == SEEN_DISTANCE)
                    continue;
                double cost = costFunction.costFactor(current.nodeId, edgeId);
                float distance = (float) (distances[current.nodeId]
                        + cost * graph.edgeLength(edgeId));
                if (distance < distances[toNodeId]) {
                    // Using euclidean distance to destination as heuristic
                    PointCh toPoint = graph.nodePoint(toNodeId);
                    float score = (float) (distance + toPoint.distanceTo(endPoint));
                    distances[toNodeId] = distance;
                    previous[toNodeId] = (edgeIndex << NODE_ID_LENGTH) | current.nodeId;
                    toVisit.add(new WeightedNode(toNodeId, score));
                }
            }
            distances[current.nodeId] = SEEN_DISTANCE;
        }
        return null; // path does not exist
    }

    /**
     * Generates the route/path ending at {@code currentNodeId}.
     *
     * @param previous      map linking a node id to the id of the previous node packed with the
     *                      outgoing edge index to follow (int - U4 -> edge index, U28 -> node id)
     * @param currentNodeId last node id (index) of the route to reconstruct
     * @return the route ending at {@code currentNodeId}
     */
    private Route reconstructRoute(int[] previous, int currentNodeId) {
        List<Edge> edges = new LinkedList<>();
        int toNodeId = currentNodeId;
        while (previous[toNodeId] != -1) {
            int previousNodeId = Bits.extractUnsigned(previous[toNodeId], 0, NODE_ID_LENGTH);
            int outGoingEdgeIndex = Bits.extractUnsigned(previous[toNodeId], NODE_ID_LENGTH,
                    EDGE_INDEX_LENGTH);
            int edgeId = graph.nodeOutEdgeId(previousNodeId, outGoingEdgeIndex);
            Edge edge = Edge.of(graph, edgeId, previousNodeId, toNodeId);
            edges.add(0, edge); // prepend
            toNodeId = previousNodeId;
        }
        return new SingleRoute(edges);
    }

}

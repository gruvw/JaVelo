package ch.epfl.javelo.routing;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
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
        // FIXME: costFunction is not immutable ??
        this.costFunction = costFunction;
    }

    /**
     * Generates the route/path ending at {@code currentNodeId}.
     *
     * @param cameFrom      map linking a nodeId to the edge reaching it
     * @param currentNodeId last node of the route we want to reconstruct
     * @return the route ending at {@code currentNodeId}
     */
    private Route reconstructRoute(HashMap<Integer, EdgeTo> cameFrom, int currentNodeId) {
        // FIXME: I use linkdexList to prepend in O(1)
        List<Edge> edges = new LinkedList<Edge>();
        int toNodeId = currentNodeId;
        while (cameFrom.containsKey(toNodeId)) {
            EdgeTo fromNode = cameFrom.get(toNodeId);
            Edge edge = Edge.of(graph, fromNode.edgeId, fromNode.fromNodeId, toNodeId);
            edges.add(0, edge);
            toNodeId = fromNode.fromNodeId;
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
         * @param nodeId   id (index) of the node
         * @param distance smallest known (weighted) distance from the starting node to this node
         * @param score    value used to compare two nodes, the one with the smallest score will be
         *                 visited first
         */
        record WeightedNode(int nodeId, float distance, float score)
                implements Comparable<WeightedNode> {

            @Override
            public int compareTo(WeightedNode that) {
                return Float.compare(this.score, that.score);
            }

        }

        Preconditions.checkArgument(startNodeId != endNodeId);
        PriorityQueue<WeightedNode> toVisit = new PriorityQueue<WeightedNode>();
        // toNodeId -> (fromNodeId, edgeId, distance)
        HashMap<Integer, EdgeTo> cameFrom = new HashMap<Integer, EdgeTo>();
        // FIXME: I don't init distances to inf and previous to 0 so that I don't loop here
        // FIXME: using visited instead of distance Float NegativeInf
        HashSet<Integer> visited = new HashSet<Integer>();
        // Score is not 0 but it is the only element in toVisit so it does not matter
        toVisit.add(new WeightedNode(startNodeId, 0, 0));
        PointCh endPoint = graph.nodePoint(endNodeId);

        while (!toVisit.isEmpty()) {
            WeightedNode current = toVisit.poll();
            // FIXME: check here as we don't want to check node again if we already found a faster
            // way
            if (visited.contains(current.nodeId)) // faster evaluation (not necessary)
                continue;
            if (current.nodeId == endNodeId) // path found
                return reconstructRoute(cameFrom, current.nodeId);
            PointCh fromPoint = graph.nodePoint(current.nodeId);
            int outDegree = graph.nodeOutDegree(current.nodeId);
            for (int edgeIndex = 0; edgeIndex < outDegree; edgeIndex++) {
                int edgeId = graph.nodeOutEdgeId(current.nodeId, edgeIndex);
                int toNodeId = graph.edgeTargetNodeId(edgeId);
                // FIXME: check here too as we don't want to check back where we already walked (ex:
                // 1->2->1), don't add to cameFrom, don't recompute score
                if (visited.contains(toNodeId)) // necessary: do not add start to cameFrom
                    continue;
                PointCh toPoint = graph.nodePoint(toNodeId);
                double cost = costFunction.costFactor(current.nodeId, edgeId);
                // FIXME: why cast ? why weightedNode distance is not double ?
                float distance = current.distance + (float) (cost * toPoint.distanceTo(fromPoint));
                // Using euclidean distance to destination as heuristic
                if (!cameFrom.containsKey(toNodeId) || distance < cameFrom.get(toNodeId).distance) {
                    float score = distance + (float) toPoint.distanceTo(endPoint);
                    cameFrom.put(toNodeId, new EdgeTo(edgeId, current.nodeId, distance));
                    toVisit.add(new WeightedNode(toNodeId, distance, score));
                }
            }
            visited.add(current.nodeId);
        }
        return null; // path does not exist
    }

    // FIXME: allowed to create a Tuple and use it in the hashMap ? nodeId -> (
    // fromNodeId, edgeId, distance)
    /**
     * Represents an edge going from {@code fromNodeId} to a particular node. Used to keep track of
     * the best edge going to a particular node.
     *
     * @param edgeId     id (index) of this edge in the graph
     * @param fromNodeId starting node of this edge
     * @param distance   (weighted) distance from {@code startNodeId} to the end of the edge
     */
    private record EdgeTo(int edgeId, int fromNodeId, float distance) {
    }

}

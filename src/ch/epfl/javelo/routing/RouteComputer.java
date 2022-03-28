package ch.epfl.javelo.routing;

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

    private Route reconstruct(int[] predecessor, int[] edgeIds, int endNodeId) {
        List<Edge> edges = new LinkedList<>();
        int index = endNodeId;
        while (predecessor[index] != 0) {
            Edge e = Edge.of(graph, edgeIds[index], predecessor[index], index);
            edges.add(0, e);
            index = e.fromNodeId();
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
        PriorityQueue<WeightedNode> exploring = new PriorityQueue<>();
        float[] distance = new float[graph.nodeCount()];
        int[] predecessor = new int[graph.nodeCount()];
        int[] edgeIds = new int[graph.nodeCount()];
        PointCh endPoint = graph.nodePoint(endNodeId);
        for (int i = 0; i < graph.nodeCount(); i++) {
            distance[i] = Float.POSITIVE_INFINITY;
            predecessor[i] = 0;
            edgeIds[i] = -1;
        }
        distance[startNodeId] = 0;
        exploring.add(new WeightedNode(startNodeId, 0,
                (float) graph.nodePoint(startNodeId).distanceTo(endPoint)));
        while (!exploring.isEmpty()) {
            WeightedNode curNode = exploring.remove();
            int curNodeId = curNode.nodeId;
            if (distance[curNodeId] == Float.NEGATIVE_INFINITY) {
                continue;
            }
            if (curNodeId == endNodeId) {
                return reconstruct(predecessor, edgeIds, endNodeId);
            }
            for (int outEdgeIndex = 0; outEdgeIndex < graph.nodeOutDegree(curNodeId);
                 outEdgeIndex++) {
                int edgeId = graph.nodeOutEdgeId(curNodeId, outEdgeIndex);
                int toNodeId = graph.edgeTargetNodeId(edgeId);
                float distToStart = curNode.distance + (float) (graph.edgeLength(edgeId)
                        * costFunction.costFactor(curNodeId, edgeId));
                float distToEnd = (float) graph.nodePoint(toNodeId).distanceTo(endPoint);
                if (distToStart < distance[toNodeId]) {
                    distance[toNodeId] = distToStart;
                    predecessor[toNodeId] = curNodeId;
                    edgeIds[toNodeId] = edgeId;
                    exploring.add(new WeightedNode(toNodeId, distToStart, distToStart + distToEnd));
                }
            }
            distance[curNodeId] = Float.NEGATIVE_INFINITY;
        }
        return null;
    }

}

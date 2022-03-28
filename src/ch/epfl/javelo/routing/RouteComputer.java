package ch.epfl.javelo.routing;

import java.util.ArrayList;
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

    private Route reconstruct(int[] predecessor, int endNodeId) {
        List<Edge> edges = new LinkedList<>();
        int index = endNodeId;
        while (predecessor[index] != 0) {
            int edgeId = 0;
            for (int curEdgeId = 0; curEdgeId < graph.nodeOutDegree(index); curEdgeId++) {
                if (graph.nodeOutEdgeId(index, curEdgeId) == predecessor[index]) {
                    edgeId = curEdgeId;
                    break;
                }
            }
            Edge e = Edge.of(graph, edgeId, predecessor[index], index);
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
        int iter = 0;
        PriorityQueue<WeightedNode> exploring = new PriorityQueue<>();
        float[] distance = new float[graph.nodeCount()];
        int[] predecessor = new int[graph.nodeCount()];
        for (int i = 0; i < graph.nodeCount(); i++) {
            distance[i] = Float.POSITIVE_INFINITY;
            predecessor[i] = 0;
        }
        distance[startNodeId] = 0;
        exploring.add(new WeightedNode(startNodeId, 0,
                (float) graph.nodePoint(startNodeId).distanceTo(graph.nodePoint(endNodeId))));
        while (!exploring.isEmpty()) {
            WeightedNode curNode = exploring.remove();
            int curNodeId = curNode.nodeId;
            if (distance[curNodeId] == Float.NEGATIVE_INFINITY) {
                continue;
            }
            PointCh endPoint = graph.nodePoint(endNodeId);
            if (curNodeId == endNodeId) {
                System.out.println(iter + " iterations");
                return reconstruct(predecessor, endNodeId);
            }
            for (int curEdgeId = 0; curEdgeId < graph.nodeOutDegree(curNodeId); curEdgeId++) {
                int edgeId = graph.nodeOutEdgeId(curNodeId, curEdgeId);
                int arrivalNodeId = graph.edgeTargetNodeId(edgeId);
                float distToStart = curNode.distance + (float) (graph.edgeLength(edgeId)
                        * costFunction.costFactor(curNodeId, edgeId));
                float distToEnd = (float) graph.nodePoint(arrivalNodeId).distanceTo(endPoint);
                if (distToStart < distance[arrivalNodeId]) {
                    distance[arrivalNodeId] = distToStart;
                    predecessor[arrivalNodeId] = curNodeId;
                    exploring.add(
                            new WeightedNode(arrivalNodeId, distToStart, distToStart + distToEnd));

                }
            }
            iter++;
            distance[curNodeId] = Float.NEGATIVE_INFINITY;
        }
        return null;
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

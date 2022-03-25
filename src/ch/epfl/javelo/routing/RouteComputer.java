package ch.epfl.javelo.routing;

import ch.epfl.javelo.data.Graph;

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
     * Computes the route with the minimal cost between two nodes.
     *
     * @param startNodeId id (index) of the route's starting node
     * @param endNodeId   id (index) of the route's destination node
     * @return the route with minimal cost between the node with id {@code startNodeId} and the node
     *         with id {@code endNodeId}, or {@code null} if no route exists between those nodes
     * @throws IllegalArgumentException if the starting node and the destination node are the same
     */
    public Route bestRouteBetween(int startNodeId, int endNodeId) {
        // TODO: Explain what happens in our algo if multiple routes have the same cost
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
    }

}

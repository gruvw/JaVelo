package ch.epfl.javelo.routing;

/**
 * Represents a cost function. (interface)
 * <p>
 * Used to calculate the travel cost of an edge.
 *
 * @author Lucas Jung (324724)
 * @author Florian Kolly (328313)
 */
public interface CostFunction {

    /**
     * Retrieves the multiplicative factor for an edge with id {@code edgeId} (in the graph) and
     * first node id {@code nodeId}.
     *
     * @param nodeId id (index) of the edge's starting node
     * @param edgeId id (index) of the edge in the graph
     * @return the factor (greater than or equal to 1) by which the length of the specified edge
     *         must be multiplied with. This factor can be {@code Double.POSITIVE_INFINITY}, meaning
     *         the edge does not exist
     */
    double costFactor(int nodeId, int edgeId);

}

package ch.epfl.javelo.data;

import java.nio.IntBuffer;
import ch.epfl.javelo.Bits;
import ch.epfl.javelo.Q28_4;

/**
 * All the nodes in a buffer of nodes. (record)
 * <p>
 * Arguments are not checked.
 * <p>
 * Node attributes: (int - Q28.4) east coordinate, (int - Q28.4) north coordinate, (int - U4 U28)
 * number of outgoing edges and id of the first one.
 *
 * @param buffer data buffer containing the value of each attribute for every node in the graph
 *
 * @author Lucas Jung (324724)
 * @author Florian Kolly (328313)
 */
public record GraphNodes(IntBuffer buffer) {

    /**
     * Position of the east coordinate within a buffer range corresponding to a node.
     */
    private static final int OFFSET_E = 0;

    /**
     * Position of the north coordinate within a buffer range corresponding to a node.
     */
    private static final int OFFSET_N = OFFSET_E + 1;

    /**
     * Position of the number of outgoing edges within a buffer range corresponding to a node.
     */
    private static final int OFFSET_OUT_EDGES = OFFSET_N + 1;

    /**
     * Number of bits taken by the edge id inside the packed edge id and out degree.
     */
    private static final int EDGE_ID_LENGTH = 28;

    /**
     * Number of bits taken by the out degree inside the packed edge id and out degree.
     */
    private static final int OUT_DEGREE_LENGTH = 4;

    /**
     * Number of integers contained inside a buffer range corresponding to a node.
     */
    private static final int NODE_INTS = OFFSET_OUT_EDGES + 1;


    /**
     * Computes the number of nodes in the buffer.
     *
     * @return the total number of nodes
     */
    public int count() {
        return buffer.capacity() / NODE_INTS;
    }

    /**
     * Retrieves the east coordinate of a node.
     *
     * @param nodeId id (index) of the node
     * @return the east coordinate of the node corresponding to the given id
     */
    public double nodeE(int nodeId) {
        return Q28_4.asDouble(buffer.get(nodeId * NODE_INTS + OFFSET_E));
    }

    /**
     * Retrieves the north coordinate of a node.
     *
     * @param nodeId id (index) of the node
     * @return the north coordinate of the node corresponding to the given id
     */
    public double nodeN(int nodeId) {
        return Q28_4.asDouble(buffer.get(nodeId * NODE_INTS + OFFSET_N));
    }

    /**
     * Retrieves the number of outgoing edges of a node.
     *
     * @param nodeId id (index) of the node
     * @return the number of outgoing edges of the node corresponding to the given id
     */
    public int outDegree(int nodeId) {
        return Bits.extractUnsigned(buffer.get(nodeId * NODE_INTS + OFFSET_OUT_EDGES),
                EDGE_ID_LENGTH, OUT_DEGREE_LENGTH); // U4
    }

    /**
     * Retrieves the id of the edge number {@code edgeIndex} going out of a node.
     *
     * @param nodeId    id (index) of the node
     * @param edgeIndex index of the edge going out of the given node, between 0 (included) and the
     *                  total number of outgoing edges of the given node (excluded), supposed valid
     * @return the id of the {@code edgeIndex}-th edge going out of the node corresponding to the
     *         given id {@code nodeId}
     */
    public int edgeId(int nodeId, int edgeIndex) {
        return Bits.extractUnsigned(buffer.get(nodeId * NODE_INTS + OFFSET_OUT_EDGES), 0,
                EDGE_ID_LENGTH) + edgeIndex; // U28
    }

}

package ch.epfl.javelo.data;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.nio.ShortBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.DoubleUnaryOperator;
import ch.epfl.javelo.Functions;
import ch.epfl.javelo.projection.PointCh;

/**
 * Represents the JaVelo graph.
 * <p>
 * Arguments are not checked.
 * <p>
 *
 * @author Lucas Jung (324724)
 * @author Florian Kolly (328313)
 */
public final class Graph {

    private final GraphNodes nodes;
    private final GraphSectors sectors;
    private final GraphEdges edges;
    private final List<AttributeSet> attributeSets;

    /**
     * Loads and creates the graph.
     *
     * @param nodes         graph's nodes
     * @param sectors       graph's sectors
     * @param edges         graph's edges
     * @param attributeSets graph's set of OSM attributes
     */
    public Graph(GraphNodes nodes, GraphSectors sectors, GraphEdges edges,
                 List<AttributeSet> attributeSets) {
        this.nodes = nodes;
        this.sectors = sectors;
        this.edges = edges;
        this.attributeSets = List.copyOf(attributeSets);
    }

    private static ByteBuffer mapFileToBuffer(Path basePath, String fileName) throws IOException {
        Path filePath = basePath.resolve(fileName);
        try (FileChannel channel = FileChannel.open(filePath)) {
            return channel.map(FileChannel.MapMode.READ_ONLY, 0, channel.size());
        }
    }

    /**
     * Retrieves the graph from the files in the directory indicated by {@code basePath}.
     *
     * @param basePath directory of the files
     * @return the graph with the nodes, the sectors, the edges and the attribute set
     * @throws IOException if some input/output error is thrown during file related operations
     */
    public static Graph loadFrom(Path basePath) throws IOException {
        IntBuffer nodesBuffer = mapFileToBuffer(basePath, "nodes.bin").asIntBuffer();
        ByteBuffer sectorsBuffer = mapFileToBuffer(basePath, "sectors.bin");
        ByteBuffer edgesBuffer = mapFileToBuffer(basePath, "edges.bin");
        IntBuffer profileIds = mapFileToBuffer(basePath, "profile_ids.bin").asIntBuffer();
        ShortBuffer elevations = mapFileToBuffer(basePath, "elevations.bin").asShortBuffer();

        LongBuffer attributesBuffer = mapFileToBuffer(basePath, "attributes.bin").asLongBuffer();
        ArrayList<AttributeSet> attributeSets = new ArrayList<AttributeSet>();
        for (long bits : attributesBuffer.array()) {
            attributeSets.add(new AttributeSet(bits));
        }

        return new Graph(new GraphNodes(nodesBuffer), new GraphSectors(sectorsBuffer),
                new GraphEdges(edgesBuffer, profileIds, elevations), attributeSets);
    }

    /**
     * Retrieves the total number of nodes in the graph.
     *
     * @return the total number of nodes
     */
    public int nodeCount() {
        return nodes.count();
    }

    /**
     * Retrieves the position of the given node.
     *
     * @param nodeId id (index) of the node
     * @return the position of the node corresponding to the given id
     */
    public PointCh nodePoint(int nodeId) {
        return new PointCh(nodes.nodeE(nodeId), nodes.nodeN(nodeId));
    }

    /**
     * Retrieves the number of outgoing edges from the given node.
     *
     * @param nodeId id (index) of the node
     * @return the number of outgoing edges
     */
    public int nodeOutDegree(int nodeId) {
        return nodes.outDegree(nodeId);
    }

    /**
     * Retrieves the index of the {@code edgeIndex}-th edge going out of the node.
     *
     * @param nodeId    id (index) of the node
     * @param edgeIndex id (index) of the edge
     * @return the id (index) of the outgoing {@code edgeIndex}-th edge of the node corresponding to
     *         the given id
     */
    public int nodeOutEdgeId(int nodeId, int edgeIndex) {
        return nodes.edgeId(nodeId, edgeIndex);
    }

    /**
     * Retrieves the index of the node closest to the given point within a maximum distance (in
     * meters) of {@code searchDistance}.
     *
     * @param point          center around which the search is done
     * @param searchDistance maximum distance around the point
     * @return the closest node's index, -1 if no node respects the conditions
     */
    public int nodeClosestTo(PointCh point, double searchDistance) {
        // TODO
    }

    /**
     * Retrieves the index of an edge's destination node.
     *
     * @param edgeId id (index) of the edge
     * @return the index of the destination node
     */
    public int edgeTargetNodeId(int edgeId) {
        return edges.targetNodeId(edgeId);
    }

    /**
     * Checks if an edge is inverted compared to the OSM way it represents.
     *
     * @param edgeId id (index) of the edge
     * @return true if the edge corresponding to the given id is inverted, false otherwise
     */
    public boolean edgeIsInverted(int edgeId) {
        return edges.isInverted(edgeId);
    }

    /**
     * Retrieves the OSM attributes' set of an edge.
     *
     * @param edgeId id (index) of the edge
     * @return the OSM attributes' set of the edge corresponding to the given id
     */
    public AttributeSet edgeAttributes(int edgeId) {
        return attributeSets.get(edges.attributesIndex(edgeId));
    }

    /**
     * Retrieves the length of the edge in meters.
     *
     * @param edgeId id (index) of the edge
     * @return the length of the edge corresponding to the given id
     */
    public double edgeLength(int edgeId) {
        return edges.length(edgeId);
    }

    /**
     * Retrieves the elevation gain of the edge, in meters.
     *
     * @param edgeId id (index) of the edge
     * @return the elevation gain of the edge corresponding to the given id
     */
    public double edgeElevationGain(int edgeId) {
        return edges.elevationGain(edgeId);
    }

    /**
     * Retrieves the profile of an edge, as a function. If the edge does not have a profile, the
     * returned function will always return {@code Double.NaN}.
     *
     * @param edgeId id (index) of the edge
     * @return the profile of the edge corresponding to the given id, as a function or a function
     *         always returning {@code Double.NaN} if the edge does not have a profile
     */
    public DoubleUnaryOperator edgeProfile(int edgeId) {
        if (!edges.hasProfile(edgeId))
            return value -> Double.NaN;
        return Functions.sampled(edges.profileSamples(edgeId), edgeLength(edgeId));
    }

}

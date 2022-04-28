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
import ch.epfl.javelo.data.GraphSectors.Sector;
import ch.epfl.javelo.projection.PointCh;

/**
 * Represents the JaVelo graph.
 * <p>
 * Immutable. Arguments are not checked.
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

    // FIXME RENDU: <p> n'est pas superflu
    /**
     * Loads and creates a graph.
     * <p>
     * WARNING: Creating a graph with {@code GraphNodes}, {@code GraphSectors} or {@code GraphEdges}
     * generated with modifiable buffers violates immutability.
     *
     * @param nodes         graph's nodes
     * @param sectors       graph's sectors
     * @param edges         graph's edges
     * @param attributeSets graph's set of OSM attributes
     */
    public Graph(GraphNodes nodes,
                 GraphSectors sectors,
                 GraphEdges edges,
                 List<AttributeSet> attributeSets) {
        this.nodes = nodes;
        this.sectors = sectors;
        this.edges = edges;
        this.attributeSets = List.copyOf(attributeSets);
    }

    /**
     * Generates a graph from the files in the directory indicated by {@code basePath}.
     * <p>
     * The following files are required: {@code nodes.bin}, {@code sectors.bin}, {@code edges.bin},
     * {@code profile_ids.bin}, {@code elevations.bin}, {@code attributes.bin}.
     *
     * @param basePath directory containing the files
     * @return the generated graph
     * @throws IOException if any input/output error is thrown during file related operations
     */
    public static Graph loadFrom(Path basePath) throws IOException {
        IntBuffer nodesBuffer = mapFileToBuffer(basePath, "nodes.bin").asIntBuffer();
        ByteBuffer sectorsBuffer = mapFileToBuffer(basePath, "sectors.bin");
        ByteBuffer edgesBuffer = mapFileToBuffer(basePath, "edges.bin");
        IntBuffer profileIds = mapFileToBuffer(basePath, "profile_ids.bin").asIntBuffer();
        ShortBuffer elevations = mapFileToBuffer(basePath, "elevations.bin").asShortBuffer();

        LongBuffer attributesBuffer = mapFileToBuffer(basePath, "attributes.bin").asLongBuffer();
        ArrayList<AttributeSet> attributeSets = new ArrayList<>();
        while (attributesBuffer.hasRemaining())
            attributeSets.add(new AttributeSet(attributesBuffer.get()));

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
     * Retrieves the position of a given node.
     *
     * @param nodeId id (index) of the node
     * @return the position of the node corresponding to the given id
     */
    public PointCh nodePoint(int nodeId) {
        return new PointCh(nodes.nodeE(nodeId), nodes.nodeN(nodeId));
    }

    /**
     * Retrieves the number of outgoing edges from a given node.
     *
     * @param nodeId id (index) of the node
     * @return the number of edges going out of the node
     */
    public int nodeOutDegree(int nodeId) {
        return nodes.outDegree(nodeId);
    }

    /**
     * Retrieves the id of the {@code edgeIndex}-th outgoing edge of a node.
     *
     * @param nodeId    id (index) of the node
     * @param edgeIndex id (index) of the edge
     * @return the id (index) of the {@code edgeIndex}-th edge going out of the node corresponding
     *         to the given id
     */
    public int nodeOutEdgeId(int nodeId, int edgeIndex) {
        return nodes.edgeId(nodeId, edgeIndex);
    }

    /**
     * Retrieves the index of the closest node to a given point, within a maximum distance of
     * {@code searchDistance} (in meters).
     *
     * @param point          center point around which the search is performed
     * @param searchDistance maximum search distance around the point (supposed positive or 0)
     * @return the closest node's id (index), -1 if there is no node within the given distance
     */
    public int nodeClosestTo(PointCh point, double searchDistance) {
        List<Sector> closeSectors = sectors.sectorsInArea(point, searchDistance);
        int closestNodeId = -1;
        // FIXME RENDU: don't understand mistake ??
        double smallestSquaredDistance = Double.MAX_VALUE;
        for (Sector sector : closeSectors)
            // Won't iterate if sector is empty
            for (int nodeId = sector.startNodeId(); nodeId < sector.endNodeId(); nodeId++) {
                PointCh candidate = nodePoint(nodeId);
                double squaredDistanceToPoint = candidate.squaredDistanceTo(point);
                if (squaredDistanceToPoint < smallestSquaredDistance) {
                    smallestSquaredDistance = squaredDistanceToPoint;
                    closestNodeId = nodeId;
                }
            }
        return closestNodeId;
    }

    /**
     * Retrieves the index of an edge's destination node.
     *
     * @param edgeId id (index) of the edge
     * @return the id (index) of the destination node
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
     * Retrieves the OSM attributes of an edge.
     *
     * @param edgeId id (index) of the edge
     * @return the attribute set containing the OSM attributes of the edge corresponding to the
     *         given id
     */
    public AttributeSet edgeAttributes(int edgeId) {
        return attributeSets.get(edges.attributesIndex(edgeId));
    }

    /**
     * Retrieves the length of an edge.
     *
     * @param edgeId id (index) of the edge
     * @return the length of the edge corresponding to the given id, in meters
     */
    public double edgeLength(int edgeId) {
        return edges.length(edgeId);
    }

    /**
     * Retrieves the elevation gain of an edge.
     *
     * @param edgeId id (index) of the edge
     * @return the elevation gain of the edge corresponding to the given id, in meters
     */
    public double edgeElevationGain(int edgeId) {
        return edges.elevationGain(edgeId);
    }

    /**
     * Retrieves the profile of an edge, as a function.
     *
     * @param edgeId id (index) of the edge
     * @return the profile of the edge corresponding to the given id as a function, or a function
     *         always returning {@code Double.NaN} if the edge does not have a profile
     */
    public DoubleUnaryOperator edgeProfile(int edgeId) {
        if (!edges.hasProfile(edgeId))
            return Functions.constant(Double.NaN);
        return Functions.sampled(edges.profileSamples(edgeId), edgeLength(edgeId));
    }

    private static ByteBuffer mapFileToBuffer(Path basePath, String fileName) throws IOException {
        Path filePath = basePath.resolve(fileName);
        try (FileChannel channel = FileChannel.open(filePath)) {
            return channel.map(FileChannel.MapMode.READ_ONLY, 0, channel.size());
        }
    }

}

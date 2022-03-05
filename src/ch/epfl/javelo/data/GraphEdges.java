package ch.epfl.javelo.data;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;

/**
 * All the edges of the JaVelo graph (record).
 * <p>
 * Arguments are not checked.
 * <p>
 * Edge attributes: (int) edge's direction and destination node's id, (short) edge's length, (short)
 * positive elevation gain in meters, (short) id of the OSM attributes set.
 * <p>
 * Profile attributes: (int) profile type and id/index of the first sample.
 * <p>
 * Elevation attributes: samples XXXXXXXXXXXXXXXXXXXXX
 *
 * @param edgesBuffer buffer memory containing the value of each attributes (fundamental and
 *        derived) for all edges of the graph (used for route computation)
 * @param profileIds buffer memory containing the value of each profile attribute for all edges
 * @param elevations buffer memory containing every samples for each profile (compressed or not)
 *
 * @author Lucas Jung (324724)
 * @author Florian Kolly (328313)
 */
public record GraphEdges(ByteBuffer edgesBuffer, IntBuffer profileIds, ShortBuffer elevations) {

    /**
     * Indicates if an edge is going the opposite direction to how OSM represents it.
     *
     * @param edgeId id (index) of the edge
     * @return true if the given edge goes the opposite way to OSM, false otherwise
     */
    public boolean isInverted(int edgeId) {

    }

    /**
     * Retrieves the target node's id of an edge.
     *
     * @param edgeId id (index) of the edge
     * @return the target node's id of the edge corresponding to the given id
     */
    public int targetNodeId(int edgeId) {

    }

    /**
     * Retrieves the length of an edge.
     *
     * @param edgeId id (index) of the edge
     * @return the length, in meters, of the edge corresponding to the given id
     */
    public double length(int edgeId) {

    }

    /**
     * Retrieves the positive elevation of an edge.
     *
     * @param edgeId id (index) of the edge
     * @return the positive elevation, in meters, of the edge corresponding to the given id
     */
    public double elevationGain(int edgeId) {

    }

    /**
     * Indicates if an edge has a profile stored in the {@code elevations} buffer.
     *
     * @param edgeId id (index) of the edge
     * @return true if the edge has a profile, false otherwise
     */
    public boolean hasProfile(int edgeId) {

    }

    /**
     * Retrieves (and decompresses if needed) the samples of an edge's profile (empty if the edge
     * does not have a profile).
     *
     * @param edgeId id (index) of the edge
     * @return an array of the samples contained in the profile of the edge corresponding to the
     *         given id
     */
    public float[] profileSamples(int edgeId) {

    }

    /**
     * Retrieves the id of the attribute set attached to an edge.
     *
     * @param edgeId id (index) of the edge
     * @return the attribute set's id of to the edge corresponding to the given id
     */
    public int attributesIndex(int edgeId) {

    }

}

package ch.epfl.javelo.data;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import ch.epfl.javelo.Bits;
import ch.epfl.javelo.Math2;
import ch.epfl.javelo.Q28_4;

/**
 * All the edges of the JaVelo graph (record).
 * <p>
 * Arguments are not checked.
 * <p>
 * Edge attributes: (int - U1 U31) edge's direction and target node's id, (short - UQ12.4) edge's
 * length in meters, (short - UQ12.4) positive elevation gain in meters, (short - U16) id of the OSM
 * attributes set.
 * <p>
 * Profile attributes: (int - U2 U30) profile type and id/index of the first sample.
 * <p>
 * Elevations: samples value only -> first altitude (short - UQ12.4), type 1 (short - UQ12.4) | type
 * 2 (short - Q4.4 Q4.4) | type 3 (short Q0.4 Q0.4 Q0.4 Q0.4).
 *
 * @param edgesBuffer buffer memory containing the value of each attributes (fundamental and
 *                    derived) for all edges of the graph (used for route computation)
 * @param profileIds  buffer memory containing the value of each profile attribute for all edges
 * @param elevations  buffer memory containing every samples for each profile (compressed or not)
 *
 * @author Lucas Jung (324724)
 * @author Florian Kolly (328313)
 */
public record GraphEdges(ByteBuffer edgesBuffer, IntBuffer profileIds, ShortBuffer elevations) {

    // == EDGES BUFFER ==

    /**
     * Position of the direction and target node's id within a buffer range corresponding to an
     * edge.
     */
    private final static byte OFFSET_DIRECTION_TARGET = 0;

    /**
     * Position of the length within a buffer range corresponding to an edge.
     */
    private final static byte OFFSET_LENGTH = OFFSET_DIRECTION_TARGET + Integer.BYTES;

    /**
     * Position of the gain in elevation within a buffer range corresponding to an edge.
     */
    private final static byte OFFSET_ELEVATION = OFFSET_LENGTH + Short.BYTES;

    /**
     * Position of the set of attributes within a buffer range corresponding to an edge.
     */
    private final static byte OFFSET_ATTRIBUTES = OFFSET_ELEVATION + Short.BYTES;

    /**
     * Size of an edge range within the buffer (in bytes).
     */
    private final static byte EDGE_SIZE = OFFSET_ATTRIBUTES + Short.BYTES;

    // == PROFILES BUFFER ==

    /**
     * Position of the profile's type and id of the first elevation sample within a buffer range
     * corresponding to a profile.
     */
    private final static int OFFSET_PROFILE_TYPE_ID = 0;

    /**
     * Number of integers contained inside a buffer range corresponding to a profile.
     */
    private final static int PROFILE_INTS = OFFSET_PROFILE_TYPE_ID + 1;

    /**
     * Reverses a given array (in-place reversal).
     *
     * @param array the array to reverse
     */
    private final static void reverseArray(float[] array) {
        for (int i = 0; i < array.length / 2; i++) {
            float temp = array[i];
            array[i] = array[array.length - i - 1];
            array[array.length - i - 1] = temp;
        }
    }

    /**
     * Indicates if an edge is going the opposite direction to how OSM represents it.
     *
     * @param edgeId id (index) of the edge
     * @return true if the given edge goes the opposite way to OSM, false otherwise
     */
    public boolean isInverted(int edgeId) {
        return edgesBuffer.getInt(edgeId * EDGE_SIZE + OFFSET_DIRECTION_TARGET) < 0;
    }

    /**
     * Retrieves the target node's id of an edge.
     *
     * @param edgeId id (index) of the edge
     * @return the target node's id of the edge corresponding to the given id
     */
    public int targetNodeId(int edgeId) {
        int targetNode = edgesBuffer.getInt(edgeId * EDGE_SIZE + OFFSET_DIRECTION_TARGET);
        return isInverted(edgeId) ? ~targetNode : targetNode;
    }

    /**
     * Retrieves the length of an edge.
     *
     * @param edgeId id (index) of the edge
     * @return the length, in meters, of the edge corresponding to the given id
     */
    public double length(int edgeId) {
        return Q28_4.asDouble(
                Short.toUnsignedInt(edgesBuffer.getShort(edgeId * EDGE_SIZE + OFFSET_LENGTH)));
    }

    /**
     * Retrieves the positive elevation of an edge.
     *
     * @param edgeId id (index) of the edge
     * @return the positive elevation, in meters, of the edge corresponding to the given id
     */
    public double elevationGain(int edgeId) {
        return Q28_4.asDouble(
                Short.toUnsignedInt(edgesBuffer.getShort(edgeId * EDGE_SIZE + OFFSET_ELEVATION)));
    }

    /**
     * Indicates if an edge has a profile stored in the {@code elevations} buffer.
     *
     * @param edgeId id (index) of the edge
     * @return true if the edge has a profile, false otherwise
     */
    public boolean hasProfile(int edgeId) {
        int profileIndex = edgeId * PROFILE_INTS + OFFSET_PROFILE_TYPE_ID;
        return Bits.extractUnsigned(profileIds.get(profileIndex), 30, 2) != 0;
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
        if (!hasProfile(edgeId))
            return new float[0];

        int length = Short.toUnsignedInt(edgesBuffer.getShort(edgeId * EDGE_SIZE + OFFSET_LENGTH)); // UQ12.4
        int nbSamples = 1 + Math2.ceilDiv(length, Q28_4.ofInt(2)); // at least one sample
        float[] profiles = new float[nbSamples];
        int profileIndex = edgeId * PROFILE_INTS + OFFSET_PROFILE_TYPE_ID;
        int profileType = Bits.extractUnsigned(profileIds.get(profileIndex), 30, 2); // U2
        int firstSampleId = Bits.extractUnsigned(profileIds.get(profileIndex), 0, 30); // U30

        // Starting altitude (first sample, full short)
        profiles[0] = Q28_4.asFloat(Short.toUnsignedInt(elevations.get(firstSampleId))); // UQ12.4
        if (profileType == 1) {
            for (int i = 1; i < nbSamples; i++)
                profiles[i] = Q28_4.asFloat(Short.toUnsignedInt(elevations.get(firstSampleId + i)));
        } else {
            // Number of samples per short: type 2 -> 2, type 3 -> 4
            final int SAMPLES_PER_SHORT = (profileType - 1) * 2;
            // Size of a sample in bits: type 2 -> 8, type 3 -> 4
            final int SAMPLE_SIZE = Short.SIZE / SAMPLES_PER_SHORT;

            for (int offset = 1; offset <= Math2.ceilDiv(nbSamples - 1,
                    SAMPLES_PER_SHORT); offset++) {
                short compressedSamples = elevations.get(firstSampleId + offset);
                // Index i: type 2 -> 0 / 1, 2 / 3, 4 / ..., nbSamples - 2 / nbSamples - 1
                // Index i: type 3 -> 0 / 1, 2, 3, 4 / 5, 6, 7, 8 / ..., nbSamples - 4,
                // nbSamples - 3 / nbSamples - 2, nbSamples - 1
                for (int i = 1 + SAMPLES_PER_SHORT * (offset - 1); i <= offset * SAMPLES_PER_SHORT
                        && i < nbSamples; i++) {
                    // (-i mod m = -i & ~-m) when m is a power of 2
                    int start = (-i & ~-SAMPLES_PER_SHORT) * SAMPLE_SIZE;
                    float elevationDelta = Q28_4.asFloat(
                            Bits.extractSigned(compressedSamples, start, SAMPLE_SIZE));
                    profiles[i] = profiles[i - 1] + elevationDelta;
                }
            }
        }

        if (isInverted(edgeId))
            reverseArray(profiles);
        return profiles;
    }

    /**
     * Retrieves the id of the attribute set attached to an edge.
     *
     * @param edgeId id (index) of the edge
     * @return the attribute set's id of to the edge corresponding to the given id
     */
    public int attributesIndex(int edgeId) {
        return Short.toUnsignedInt(edgesBuffer.getShort(edgeId * EDGE_SIZE + OFFSET_ATTRIBUTES));
    }

}

package ch.epfl.javelo.data;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.DoubleUnaryOperator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import ch.epfl.javelo.Bits;
import ch.epfl.javelo.Q28_4;
import ch.epfl.javelo.projection.PointCh;
import ch.epfl.javelo.projection.SwissBounds;
import ch.epfl.test.TestUtils;

public class GraphTest {

    private static Graph graph;

    private static double SECTOR_WIDTH = 2726.5625;
    private static double SECTOR_HEIGHT = 1726.5625;

    private static final double DELTA = 0.0625;

    // PointCh[e=2485100.0, n=1075100.0]
    private static final PointCh node0 = new PointCh(SwissBounds.MIN_E + 100,
            SwissBounds.MIN_N + 100);
    // PointCh[e=2487826.5625, n=1078553.125]
    private static final PointCh node1 = new PointCh(SwissBounds.MIN_E + SECTOR_WIDTH + 100,
            SwissBounds.MIN_N + SECTOR_HEIGHT * 2 + 100);
    // PointCh[e=2490553.125, n=1078353.125]
    private static final PointCh node2 = new PointCh(SwissBounds.MIN_E + SECTOR_WIDTH * 2 + 100,
            SwissBounds.MIN_N + SECTOR_HEIGHT * 2 - 100);
    // PointCh[e=2495806.25, n=1076826.5625]
    private static final PointCh node3 = new PointCh(SwissBounds.MIN_E + SECTOR_WIDTH * 4 - 100,
            SwissBounds.MIN_N + SECTOR_HEIGHT + 100);

    private static void addEmptySectors(ByteBuffer buffer, int nb) {
        for (int i = 0; i < nb; i++) {
            buffer.putInt(0);
            buffer.putShort((short) 0);
        }
    }

    /**
     * Type 2 compressed samples of two elevation deltas.
     *
     * @param e1 first elevation delta (integer of 4 bits max)
     * @param e2 second elevation delta (integer of 4 bits max)
     * @return compressed deltas as short
     */
    private static short compressedElevations(int e1, int e2) {
        return (short) ((Q28_4.ofInt(e1) << 8) | Bits.extractUnsigned(Q28_4.ofInt(e2), 0, 8));
    }

    @BeforeAll
    static void initGlobalVars() {
        IntBuffer nodesBuffer = IntBuffer.allocate(4 * 3);
        // Positions comes from nodeI converted to Q28_4
        // Node 0
        nodesBuffer.put(0b1001011110101101101100 << 4);
        nodesBuffer.put(0b100000110011110011100 << 4);
        nodesBuffer.put(0x10000000); // 1 outgoing edges, first edge is id 0
        // Node 1
        nodesBuffer.put(0b1001011111011000010010_1001);
        nodesBuffer.put(0b100000111010100011001_0010);
        nodesBuffer.put(0x20000001); // 2 outgoing edges, first edge is id 1
        // Node 2
        nodesBuffer.put(0b1001100000000010111001_0010);
        nodesBuffer.put(0b100000111010001010001_0010);
        nodesBuffer.put(0x00000000); // 0 outgoing edges, no first edge
        // Node 3
        nodesBuffer.put(0b1001100001010100111110_0100);
        nodesBuffer.put(0b100000110111001011010_1001);
        nodesBuffer.put(0x00000000); // 0 outgoing edges, no first edge

        ByteBuffer sectorsBuffer = ByteBuffer.allocate(128 * 128 * (Integer.BYTES + Short.BYTES));
        // Sector (0, 0)
        sectorsBuffer.putInt(0);
        sectorsBuffer.putShort((short) 1);
        addEmptySectors(sectorsBuffer, 127); // End of first line
        addEmptySectors(sectorsBuffer, 2);
        // Sector (2, 1)
        sectorsBuffer.putInt(2);
        sectorsBuffer.putShort((short) 1);
        // Sector (3, 1)
        sectorsBuffer.putInt(3);
        sectorsBuffer.putShort((short) 1);
        addEmptySectors(sectorsBuffer, 124); // End of second line
        addEmptySectors(sectorsBuffer, 1);
        // Sector (1, 2)
        sectorsBuffer.putInt(1);
        sectorsBuffer.putShort((short) 1);
        addEmptySectors(sectorsBuffer, 126); // End of third line
        addEmptySectors(sectorsBuffer, (128 - 3) * 128);

        ByteBuffer edgesBuffer = ByteBuffer.allocate(3 * (Integer.BYTES + 3 * Short.BYTES));
        // Edge 0
        edgesBuffer.putInt(0x00000002);
        // Spoofed length because points are too far appart
        edgesBuffer.putShort(Short.MAX_VALUE);
        edgesBuffer.putShort((short) 0);
        edgesBuffer.putShort((short) 0);
        // Edge 1
        edgesBuffer.putInt(~0); // Inverted and target node id is 0 (inverted)
        // Spoofed length because points are too far appart
        edgesBuffer.putShort((short) 10);
        edgesBuffer.putShort((short) 0);
        edgesBuffer.putShort((short) 0);
        // Edge 2
        edgesBuffer.putInt(0x00000002);
        edgesBuffer.putShort((short) 0b1010101011011110); // real length
        edgesBuffer.putShort((short) Q28_4.ofInt(8));
        edgesBuffer.putShort((short) 1);

        IntBuffer profileIds = IntBuffer.allocate(3);
        // Profile of Edge 0
        profileIds.put(0);
        // Profile of Edge 1
        profileIds.put(0);
        // Profile of Edge 2
        profileIds.put(0x80000000);

        // 1368 samples for a total length of 2733.875
        ShortBuffer elevations = ShortBuffer.allocate(1 + 684);
        elevations.put((short) Q28_4.ofInt(600));
        elevations.put(compressedElevations(-5, -4));
        elevations.put(compressedElevations(3, 0));
        elevations.put(compressedElevations(5, 0));
        // The remaining 1368-7 samples are filled with 0s
        for (int i = 0; i < Math.ceil((1368 - 7) / 2); i++) {
            elevations.put((short) 0);
        }
        List<AttributeSet> attributeSets = new ArrayList<AttributeSet>();
        attributeSets.add(new AttributeSet(0L));
        attributeSets.add(new AttributeSet(0b100111L));
        graph = new Graph(new GraphNodes(nodesBuffer), new GraphSectors(sectorsBuffer),
                new GraphEdges(edgesBuffer, profileIds, elevations), attributeSets);
    }

    @Test
    void loadFromThrows() {
        assertThrows(IOException.class, () -> Graph.loadFrom(Path.of("idontexist")));
    }

    @Test
    void nodeCountTest() {
        assertEquals(4, graph.nodeCount());
    }

    @Test
    void nodePointTest() {
        TestUtils.assertEqualsPointCh(node0, graph.nodePoint(0), 1e-6);
        TestUtils.assertEqualsPointCh(node1, graph.nodePoint(1), 1e-6);
        TestUtils.assertEqualsPointCh(node2, graph.nodePoint(2), 1e-6);
        TestUtils.assertEqualsPointCh(node3, graph.nodePoint(3), 1e-6);
    }

    @Test
    void nodeOutDegreeTest() {
        assertEquals(1, graph.nodeOutDegree(0));
        assertEquals(2, graph.nodeOutDegree(1));
        assertEquals(0, graph.nodeOutDegree(2));
        assertEquals(0, graph.nodeOutDegree(3));
    }

    @Test
    void nodeOutEdgeIdTest() {
        assertEquals(0, graph.nodeOutEdgeId(0, 0));
        assertEquals(1, graph.nodeOutEdgeId(1, 0));
        assertEquals(2, graph.nodeOutEdgeId(1, 1));
    }

    @Test
    void nodeClosestToTest() {
        assertEquals(2, graph.nodeClosestTo(node2, 1.5 * SECTOR_WIDTH));
        assertEquals(2, graph.nodeClosestTo(node2, 0));
        assertEquals(-1, graph.nodeClosestTo(new PointCh(SwissBounds.MIN_E + 1.5 * SECTOR_WIDTH,
                SwissBounds.MIN_N + 0.5 * SECTOR_HEIGHT), 200));
        assertEquals(0, graph.nodeClosestTo(new PointCh(SwissBounds.MIN_E + 1.1 * SECTOR_WIDTH,
                SwissBounds.MIN_N + 0.3 * SECTOR_HEIGHT), 2 * SECTOR_WIDTH));
        assertEquals(3, graph.nodeClosestTo(new PointCh(SwissBounds.MIN_E + 3 * SECTOR_WIDTH + 100,
                SwissBounds.MIN_N + SECTOR_HEIGHT), 2 * SECTOR_WIDTH));
    }

    @Test
    void edgeTargetNodeIdTest() {
        assertEquals(2, graph.edgeTargetNodeId(0));
        assertEquals(0, graph.edgeTargetNodeId(1));
        assertEquals(2, graph.edgeTargetNodeId(2));
    }

    @Test
    void edgeIsInvertedTest() {
        assertFalse(graph.edgeIsInverted(0));
        assertTrue(graph.edgeIsInverted(1));
        assertFalse(graph.edgeIsInverted(2));
    }

    @Test
    void edgeAttributesTest() {
        assertEquals(new AttributeSet(0), graph.edgeAttributes(0));
        assertEquals(new AttributeSet(0), graph.edgeAttributes(1));
        assertEquals(new AttributeSet(0b100111L), graph.edgeAttributes(2));
    }

    @Test
    void edgeLengthTest() {
        assertEquals(Q28_4.asDouble(0b111111111111111), graph.edgeLength(0));
        assertEquals(Q28_4.asDouble(0b1010), graph.edgeLength(1));
        assertEquals(2733.8879, graph.edgeLength(2), DELTA);
    }

    @Test
    void elevationGainTest() {
        assertEquals(0, graph.edgeElevationGain(0));
        assertEquals(0, graph.edgeElevationGain(1));
        assertEquals(8, graph.edgeElevationGain(2));
    }

    @Test
    void edgeProfileTest() {
        double samplesGap = 1.998446637426900584;
        DoubleUnaryOperator nullProfile = value -> Double.NaN;
        TestUtils.assertEqualsProfile(nullProfile, graph.edgeProfile(0));
        TestUtils.assertEqualsProfile(nullProfile, graph.edgeProfile(1));
        DoubleUnaryOperator edgeProfile2 = graph.edgeProfile(2);
        assertEquals(600, edgeProfile2.applyAsDouble(-100));
        assertEquals(600, edgeProfile2.applyAsDouble(0));
        assertEquals(595, edgeProfile2.applyAsDouble(samplesGap), DELTA);
        assertEquals(591, edgeProfile2.applyAsDouble(samplesGap * 2), DELTA);
        assertEquals(594, edgeProfile2.applyAsDouble(samplesGap * 3), DELTA);
        assertEquals(594, edgeProfile2.applyAsDouble(samplesGap * 4), DELTA);
        assertEquals(599, edgeProfile2.applyAsDouble(samplesGap * 5), DELTA);
        // The remaining samples are filled with 0s
        assertEquals(599, edgeProfile2.applyAsDouble(samplesGap * 6), DELTA);
        assertEquals(599, edgeProfile2.applyAsDouble(2000));
        assertEquals(599, edgeProfile2.applyAsDouble(3000));
        for (double i = 3 * samplesGap; i < 4 * samplesGap; i++) {
            assertEquals(594, graph.edgeProfile(2).applyAsDouble(i), DELTA);
        }
        assertEquals(598.054879268, graph.edgeProfile(2).applyAsDouble(0.7774439944), DELTA);
        assertEquals(592.1077363273, graph.edgeProfile(2).applyAsDouble(3.4434552774), DELTA);
        assertEquals(595.3242327484, graph.edgeProfile(2).applyAsDouble(8.5230682147), DELTA);
    }

}

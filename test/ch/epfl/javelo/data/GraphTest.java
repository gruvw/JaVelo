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
import java.util.Collections;
import java.util.List;
import java.util.function.DoubleUnaryOperator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import ch.epfl.javelo.Bits;
import ch.epfl.javelo.Q28_4;
import ch.epfl.javelo.projection.PointCh;
import ch.epfl.javelo.projection.SwissBounds;
import ch.epfl.test.TestUtils;

import static ch.epfl.test.TestRandomizer.RANDOM_ITERATIONS;
import static ch.epfl.test.TestRandomizer.newRandom;

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
        List<AttributeSet> attributeSets = new ArrayList<>();
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
        assertEquals(2, graph.nodeClosestTo(node2, -1));
        assertEquals(-1,
                graph.nodeClosestTo(new PointCh(SwissBounds.MIN_E + 1.5 * SECTOR_WIDTH,
                                                SwissBounds.MIN_N + 0.5 * SECTOR_HEIGHT),
                        200));
        assertEquals(0,
                graph.nodeClosestTo(new PointCh(SwissBounds.MIN_E + 1.1 * SECTOR_WIDTH,
                                                SwissBounds.MIN_N + 0.3 * SECTOR_HEIGHT),
                        2 * SECTOR_WIDTH));
        assertEquals(3,
                graph.nodeClosestTo(new PointCh(SwissBounds.MIN_E + 3 * SECTOR_WIDTH + 100,
                                                SwissBounds.MIN_N + SECTOR_HEIGHT),
                        2 * SECTOR_WIDTH));
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

    // == GIVEN TESTS ==

    private static final int SUBDIVISIONS_PER_SIDE = 128;
    private static final int SECTORS_COUNT = SUBDIVISIONS_PER_SIDE * SUBDIVISIONS_PER_SIDE;

    private static final ByteBuffer SECTORS_BUFFER = createSectorsBuffer();

    private static ByteBuffer createSectorsBuffer() {
        ByteBuffer sectorsBuffer = ByteBuffer.allocate(
                SECTORS_COUNT * (Integer.BYTES + Short.BYTES));
        for (int i = 0; i < SECTORS_COUNT; i += 1) {
            sectorsBuffer.putInt(i);
            sectorsBuffer.putShort((short) 1);
        }
        assert !sectorsBuffer.hasRemaining();
        return sectorsBuffer.rewind().asReadOnlyBuffer();
    }

    @Test
    void graphLoadFromWorksOnLausanneData() throws IOException {
        var graph = Graph.loadFrom(Path.of(".javelo/lausanne"));

        // Check that nodes.bin was properly loaded
        var actual1 = graph.nodeCount();
        var expected1 = 212679;
        assertEquals(expected1, actual1);

        var actual2 = graph.nodeOutEdgeId(2022, 0);
        var expected2 = 4095;
        assertEquals(expected2, actual2);

        // Check that edges.bin was properly loaded
        var actual3 = graph.edgeLength(2022);
        var expected3 = 17.875;
        assertEquals(expected3, actual3);

        // Check that profile_ids.bin and elevations.bin was properly loaded
        var actual4 = graph.edgeProfile(2022).applyAsDouble(0);
        var expected4 = 625.5625;
        assertEquals(expected4, actual4);

        // Check that attributes.bin and elevations.bin was properly loaded
        var actual5 = graph.edgeAttributes(2022).bits();
        var expected5 = 16;
        assertEquals(expected5, actual5);
    }

    @Test
    void graphNodeCountWorksFrom0To99() {
        var edgesCount = 10;
        var edgesBuffer = ByteBuffer.allocate(10 * edgesCount);
        var profileIds = IntBuffer.allocate(edgesCount);
        var elevations = ShortBuffer.allocate(10);
        var graphSectors = new GraphSectors(SECTORS_BUFFER);
        var graphEdges = new GraphEdges(edgesBuffer, profileIds, elevations);
        var attributeSets = List.<AttributeSet>of();

        for (int count = 0; count < 100; count += 1) {
            var buffer = IntBuffer.allocate(3 * count);
            var graphNodes = new GraphNodes(buffer);

            var graph = new Graph(graphNodes, graphSectors, graphEdges, attributeSets);
            assertEquals(count, graph.nodeCount());
        }
    }

    @Test
    void graphNodePointWorksOnRandomValues() {
        var edgesCount = 10;
        var edgesBuffer = ByteBuffer.allocate(10 * edgesCount);
        var profileIds = IntBuffer.allocate(edgesCount);
        var elevations = ShortBuffer.allocate(10);
        var graphSectors = new GraphSectors(SECTORS_BUFFER);
        var graphEdges = new GraphEdges(edgesBuffer, profileIds, elevations);
        var attributeSets = List.<AttributeSet>of();

        var nodesCount = 10_000;
        var buffer = IntBuffer.allocate(3 * nodesCount);
        var rng = newRandom();
        for (int i = 0; i < RANDOM_ITERATIONS; i += 1) {
            var e = 2_600_000 + 50_000 * rng.nextDouble();
            var n = 1_200_000 + 50_000 * rng.nextDouble();
            var e_q28_4 = (int) Math.scalb(e, 4);
            var n_q28_4 = (int) Math.scalb(n, 4);
            e = Math.scalb((double) e_q28_4, -4);
            n = Math.scalb((double) n_q28_4, -4);
            var nodeId = rng.nextInt(nodesCount);
            buffer.put(3 * nodeId, e_q28_4);
            buffer.put(3 * nodeId + 1, n_q28_4);
            var graphNodes = new GraphNodes(buffer);

            var graph = new Graph(graphNodes, graphSectors, graphEdges, attributeSets);
            assertEquals(new PointCh(e, n), graph.nodePoint(nodeId));
        }
    }

    @Test
    void graphNodeOutDegreeWorksOnRandomValues() {
        var edgesCount = 10;
        var edgesBuffer = ByteBuffer.allocate(10 * edgesCount);
        var profileIds = IntBuffer.allocate(edgesCount);
        var elevations = ShortBuffer.allocate(10);
        var graphSectors = new GraphSectors(SECTORS_BUFFER);
        var graphEdges = new GraphEdges(edgesBuffer, profileIds, elevations);
        var attributeSets = List.<AttributeSet>of();

        var nodesCount = 10_000;
        var buffer = IntBuffer.allocate(3 * nodesCount);
        var rng = newRandom();
        for (int outDegree = 0; outDegree < 16; outDegree += 1) {
            var firstEdgeId = rng.nextInt(1 << 28);
            var nodeId = rng.nextInt(nodesCount);
            buffer.put(3 * nodeId + 2, (outDegree << 28) | firstEdgeId);
            var graphNodes = new GraphNodes(buffer);
            var graph = new Graph(graphNodes, graphSectors, graphEdges, attributeSets);
            assertEquals(outDegree, graph.nodeOutDegree(nodeId));
        }
    }

    @Test
    void graphNodeOutEdgeIdWorksOnRandomValues() {
        var edgesCount = 10;
        var edgesBuffer = ByteBuffer.allocate(10 * edgesCount);
        var profileIds = IntBuffer.allocate(edgesCount);
        var elevations = ShortBuffer.allocate(10);
        var graphSectors = new GraphSectors(SECTORS_BUFFER);
        var graphEdges = new GraphEdges(edgesBuffer, profileIds, elevations);
        var attributeSets = List.<AttributeSet>of();

        var nodesCount = 10_000;
        var buffer = IntBuffer.allocate(3 * nodesCount);
        var rng = newRandom();
        for (int outDegree = 0; outDegree < 16; outDegree += 1) {
            var firstEdgeId = rng.nextInt(1 << 28);
            var nodeId = rng.nextInt(nodesCount);
            buffer.put(3 * nodeId + 2, (outDegree << 28) | firstEdgeId);
            var graphNodes = new GraphNodes(buffer);
            var graph = new Graph(graphNodes, graphSectors, graphEdges, attributeSets);

            for (int i = 0; i < outDegree; i += 1)
                assertEquals(firstEdgeId + i, graph.nodeOutEdgeId(nodeId, i));
        }
    }

    @Test
    void graphNodeClosestToWorksOnLausanneData() throws IOException {
        var graph = Graph.loadFrom(Path.of(".javelo/lausanne"));

        var actual1 = graph.nodeClosestTo(new PointCh(2_532_734.8, 1_152_348.0), 100);
        var expected1 = 159049;
        assertEquals(expected1, actual1);

        var actual2 = graph.nodeClosestTo(new PointCh(2_538_619.9, 1_154_088.0), 100);
        var expected2 = 117402;
        assertEquals(expected2, actual2);

        var actual3 = graph.nodeClosestTo(new PointCh(2_600_000, 1_200_000), 100);
        var expected3 = -1;
        assertEquals(expected3, actual3);
    }

    @Test
    void graphEdgeTargetNodeIdWorksOnRandomValues() {
        var nodesCount = 10;
        var nodesBuffer = IntBuffer.allocate(3 * nodesCount);
        var graphNodes = new GraphNodes(nodesBuffer);
        var graphSectors = new GraphSectors(SECTORS_BUFFER);
        var attributeSets = List.<AttributeSet>of();

        var edgesCount = 10_000;
        var edgesBuffer = ByteBuffer.allocate(10 * edgesCount);
        var profileIds = IntBuffer.allocate(edgesCount);
        var elevations = ShortBuffer.allocate(10);
        var rng = newRandom();
        for (int i = 0; i < RANDOM_ITERATIONS; i += 1) {
            var targetNodeId = rng.nextInt();
            var edgeId = rng.nextInt(edgesCount);
            edgesBuffer.putInt(10 * edgeId, targetNodeId);
            var graphEdges = new GraphEdges(edgesBuffer, profileIds, elevations);
            var graph = new Graph(graphNodes, graphSectors, graphEdges, attributeSets);
            var expectedTargetNodeId = targetNodeId < 0 ? ~targetNodeId : targetNodeId;
            assertEquals(expectedTargetNodeId, graph.edgeTargetNodeId(edgeId));
        }
    }

    @Test
    void graphEdgeIsInvertedWorksForPlusMinus100() {
        var nodesCount = 10;
        var nodesBuffer = IntBuffer.allocate(3 * nodesCount);
        var graphNodes = new GraphNodes(nodesBuffer);
        var graphSectors = new GraphSectors(SECTORS_BUFFER);
        var attributeSets = List.<AttributeSet>of();

        var edgesCount = 10_000;
        var edgesBuffer = ByteBuffer.allocate(10 * edgesCount);
        var profileIds = IntBuffer.allocate(edgesCount);
        var elevations = ShortBuffer.allocate(10);
        var rng = newRandom();
        for (int targetNodeId = -100; targetNodeId < 100; targetNodeId += 1) {
            var edgeId = rng.nextInt(edgesCount);
            edgesBuffer.putInt(10 * edgeId, targetNodeId);
            var graphEdges = new GraphEdges(edgesBuffer, profileIds, elevations);
            var graph = new Graph(graphNodes, graphSectors, graphEdges, attributeSets);
            assertEquals(targetNodeId < 0, graph.edgeIsInverted(edgeId));
        }
    }

    @Test
    void graphEdgeAttributesWorksOnRandomValues() {
        var nodesCount = 10;
        var nodesBuffer = IntBuffer.allocate(3 * nodesCount);
        var graphNodes = new GraphNodes(nodesBuffer);
        var graphSectors = new GraphSectors(SECTORS_BUFFER);

        var attributeSetsCount = 3 * RANDOM_ITERATIONS;
        var rng = newRandom();
        var attributeSets = new ArrayList<AttributeSet>(attributeSetsCount);
        for (int i = 0; i < attributeSetsCount; i += 1) {
            var attributeSetBits = rng.nextLong(1L << 62);
            attributeSets.add(new AttributeSet(attributeSetBits));
        }
        var unmodifiableAttributeSets = Collections.unmodifiableList(attributeSets);

        var edgesCount = 10_000;
        var edgesBuffer = ByteBuffer.allocate(10 * edgesCount);
        var profileIds = IntBuffer.allocate(edgesCount);
        var elevations = ShortBuffer.allocate(10);
        for (int i = 0; i < RANDOM_ITERATIONS; i += 1) {
            var edgeId = rng.nextInt(edgesCount);
            var attributeSetIndex = (short) rng.nextInt(attributeSetsCount);
            edgesBuffer.putShort(10 * edgeId + 8, attributeSetIndex);
            var graphEdges = new GraphEdges(edgesBuffer, profileIds, elevations);
            var graph = new Graph(graphNodes, graphSectors, graphEdges, unmodifiableAttributeSets);
            assertEquals(unmodifiableAttributeSets.get(attributeSetIndex),
                    graph.edgeAttributes(edgeId));
        }
    }

    @Test
    void graphConstructorCopiesAttributesListToEnsureImmutability() {
        var nodesCount = 10;
        var nodesBuffer = IntBuffer.allocate(3 * nodesCount);
        var graphNodes = new GraphNodes(nodesBuffer);
        var graphSectors = new GraphSectors(SECTORS_BUFFER);

        var attributeSet = new AttributeSet(0b1111L);
        var attributeSets = new ArrayList<>(List.of(attributeSet));
        var unmodifiableAttributeSets = Collections.unmodifiableList(attributeSets);

        var edgesCount = 1;
        var edgesBuffer = ByteBuffer.allocate(10 * edgesCount);
        var profileIds = IntBuffer.allocate(edgesCount);
        var elevations = ShortBuffer.allocate(10);
        edgesBuffer.putShort(8, (short) 0);
        var graphEdges = new GraphEdges(edgesBuffer, profileIds, elevations);
        var graph = new Graph(graphNodes, graphSectors, graphEdges, unmodifiableAttributeSets);
        attributeSets.set(0, new AttributeSet(0L));
        assertEquals(attributeSet, graph.edgeAttributes(0));
    }

    @Test
    void graphEdgeLengthWorksOnRandomValues() {
        var nodesCount = 10;
        var nodesBuffer = IntBuffer.allocate(3 * nodesCount);
        var graphNodes = new GraphNodes(nodesBuffer);
        var graphSectors = new GraphSectors(SECTORS_BUFFER);

        var edgesCount = 10_000;
        var edgesBuffer = ByteBuffer.allocate(10 * edgesCount);
        var profileIds = IntBuffer.allocate(edgesCount);
        var elevations = ShortBuffer.allocate(10);
        var rng = newRandom();
        for (int i = 0; i < RANDOM_ITERATIONS; i += 1) {
            var edgeId = rng.nextInt(edgesCount);
            var length = rng.nextDouble(1 << 12);
            var length_q12_4 = (int) Math.scalb(length, 4);
            length = Math.scalb((double) length_q12_4, -4);
            edgesBuffer.putShort(10 * edgeId + 4, (short) length_q12_4);
            var graphEdges = new GraphEdges(edgesBuffer, profileIds, elevations);
            var graph = new Graph(graphNodes, graphSectors, graphEdges, List.of());

            assertEquals(length, graph.edgeLength(edgeId));
        }
    }

    @Test
    void graphEdgeElevationGainWorksOnRandomValues() {
        var nodesCount = 10;
        var nodesBuffer = IntBuffer.allocate(3 * nodesCount);
        var graphNodes = new GraphNodes(nodesBuffer);
        var graphSectors = new GraphSectors(SECTORS_BUFFER);

        var edgesCount = 10_000;
        var edgesBuffer = ByteBuffer.allocate(10 * edgesCount);
        var profileIds = IntBuffer.allocate(edgesCount);
        var elevations = ShortBuffer.allocate(10);
        var rng = newRandom();
        for (int i = 0; i < RANDOM_ITERATIONS; i += 1) {
            var edgeId = rng.nextInt(edgesCount);
            var elevationGain = rng.nextDouble(1 << 12);
            var elevationGain_q12_4 = (int) Math.scalb(elevationGain, 4);
            elevationGain = Math.scalb((double) elevationGain_q12_4, -4);
            edgesBuffer.putShort(10 * edgeId + 6, (short) elevationGain_q12_4);
            var graphEdges = new GraphEdges(edgesBuffer, profileIds, elevations);
            var graph = new Graph(graphNodes, graphSectors, graphEdges, List.of());

            assertEquals(elevationGain, graph.edgeElevationGain(edgeId));
        }
    }

    @Test
    void graphEdgeProfileWorksForType0() {
        var nodesCount = 10;
        var nodesBuffer = IntBuffer.allocate(3 * nodesCount);
        var graphNodes = new GraphNodes(nodesBuffer);
        var graphSectors = new GraphSectors(SECTORS_BUFFER);

        var edgesCount = 10_000;
        var elevationsCount = 25_000;
        var edgesBuffer = ByteBuffer.allocate(10 * edgesCount);
        var profileIds = IntBuffer.allocate(edgesCount);
        var elevations = ShortBuffer.allocate(elevationsCount);
        var rng = newRandom();
        for (int i = 0; i < RANDOM_ITERATIONS; i += 1) {
            var edgeId = rng.nextInt(edgesCount);
            var firstSampleIndex = rng.nextInt(elevationsCount);
            profileIds.put(edgeId, firstSampleIndex);
            var graphEdges = new GraphEdges(edgesBuffer, profileIds, elevations);
            var graph = new Graph(graphNodes, graphSectors, graphEdges, List.of());
            var edgeProfile = graph.edgeProfile(edgeId);
            assertTrue(Double.isNaN(edgeProfile.applyAsDouble(-1)));
            assertTrue(Double.isNaN(edgeProfile.applyAsDouble(0)));
            assertTrue(Double.isNaN(edgeProfile.applyAsDouble(1000)));
        }
    }

    @Test
    void graphEdgeProfileWorksForType1() {
        var nodesCount = 10;
        var nodesBuffer = IntBuffer.allocate(3 * nodesCount);
        var graphNodes = new GraphNodes(nodesBuffer);
        var graphSectors = new GraphSectors(SECTORS_BUFFER);

        var elevationsCount = 500;
        var edgesBuffer = ByteBuffer.allocate(10);
        var profileIds = IntBuffer.allocate(1);
        var elevations = ShortBuffer.allocate(elevationsCount);
        var rng = newRandom();
        for (int i = 0; i < elevationsCount; i += 1)
            elevations.put(i, (short) rng.nextInt(1 << 16));
        for (int i = 0; i < RANDOM_ITERATIONS; i += 1) {
            var inverted = rng.nextBoolean();
            var sampleCount = rng.nextInt(2, 100);
            var firstSampleIndex = rng.nextInt(elevationsCount - sampleCount);
            var edgeLength_q28_4 = (2 * (sampleCount - 1)) << 4;
            edgesBuffer.putInt(0, inverted ? ~0 : 0);
            edgesBuffer.putShort(4, (short) edgeLength_q28_4);
            profileIds.put(0, (1 << 30) | firstSampleIndex);
            var graphEdges = new GraphEdges(edgesBuffer.asReadOnlyBuffer(),
                                            profileIds.asReadOnlyBuffer(),
                                            elevations.asReadOnlyBuffer());
            var graph = new Graph(graphNodes, graphSectors, graphEdges, List.of());
            var edgeProfile = graph.edgeProfile(0);

            for (int j = 0; j < sampleCount; j += 1) {
                var elevation = Math.scalb(
                        Short.toUnsignedInt(elevations.get(firstSampleIndex + j)), -4);
                if (inverted) {
                    var x = (sampleCount - 1 - j) * Math.scalb((double) edgeLength_q28_4, -4)
                            / (sampleCount - 1);
                    assertEquals(elevation, edgeProfile.applyAsDouble(x), 1e-7);
                } else {
                    var x = j * Math.scalb((double) edgeLength_q28_4, -4) / (sampleCount - 1);
                    assertEquals(elevation, edgeProfile.applyAsDouble(x), 1e-7);
                }
            }
        }
    }

}

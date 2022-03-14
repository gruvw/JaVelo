package ch.epfl.javelo.data;

import java.nio.IntBuffer;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import static ch.epfl.test.TestRandomizer.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

class GraphNodesTest {

    private static final double DELTA = 1e-11;

    private static GraphNodes nodesB1;
    private static GraphNodes nodesB2;

    @BeforeAll
    static void initGlobalVars() {
        IntBuffer b1 = IntBuffer.wrap(new int[] {2_600_000 << 4, 1_200_000 << 4, 0x2_0001234});
        IntBuffer b2 = IntBuffer.wrap(
                new int[] {3_800_000 << 4, 2_300_000 << 4, 0x0_0000001, Integer.MAX_VALUE >> 26,
                           Integer.MAX_VALUE >> 26, 0x5_0001000});
        nodesB1 = new GraphNodes(b1.asReadOnlyBuffer());
        nodesB2 = new GraphNodes(b2.asReadOnlyBuffer());
    }

    static Stream<Arguments> countTest() {
        return Stream.of(Arguments.of(nodesB1, 1), Arguments.of(nodesB2, 2),
                Arguments.of(new GraphNodes(IntBuffer.wrap(new int[0])), 0));
    }

    @ParameterizedTest
    @MethodSource
    void countTest(GraphNodes nodes, int expected) {
        assertEquals(expected, nodes.count());
    }

    static Stream<Arguments> nodeETest() {
        return Stream.of(Arguments.of(nodesB1, 0, 2_600_000), Arguments.of(nodesB2, 0, 3_800_000),
                Arguments.of(nodesB2, 1, 1.9375));
    }

    @ParameterizedTest
    @MethodSource
    void nodeETest(GraphNodes nodes, int nodeId, double expected) {
        double actual = nodes.nodeE(nodeId);
        assertEquals(expected, actual, DELTA);
    }

    static Stream<Arguments> nodeNTest() {
        return Stream.of(Arguments.of(nodesB1, 0, 1_200_000), Arguments.of(nodesB2, 0, 2_300_000),
                Arguments.of(nodesB2, 1, 1.9375));
    }

    @ParameterizedTest
    @MethodSource
    void nodeNTest(GraphNodes nodes, int nodeId, double expected) {
        double actual = nodes.nodeN(nodeId);
        assertEquals(expected, actual, DELTA);
    }

    static Stream<Arguments> outDegreeTest() {
        return Stream.of(Arguments.of(nodesB1, 0, 2), Arguments.of(nodesB2, 0, 0),
                Arguments.of(nodesB2, 1, 5),
                Arguments.of(new GraphNodes(
                        IntBuffer.wrap(new int[] {2_600_000 << 4, 1_200_000 << 4, 0xF_0001234})), 0,
                        15));
    }

    @ParameterizedTest
    @MethodSource
    void outDegreeTest(GraphNodes nodes, int nodeId, int expected) {
        int actual = nodes.outDegree(nodeId);
        assertEquals(expected, actual);
    }

    static Stream<Arguments> edgeIdTest() {
        GraphNodes gn = new GraphNodes(
                IntBuffer.wrap(new int[] {2_600_000 << 4, 1_200_000 << 4, 0x3_0000000}));
        return Stream.of(Arguments.of(nodesB1, 0, 0, 0x1234), Arguments.of(nodesB1, 0, 1, 0x1235),
                Arguments.of(nodesB2, 1, 0, 0x1000), Arguments.of(nodesB2, 1, 1, 0x1001),
                Arguments.of(nodesB2, 1, 2, 0x1002), Arguments.of(nodesB2, 1, 3, 0x1003),
                Arguments.of(nodesB2, 1, 4, 0x1004), Arguments.of(nodesB2, 1, 5, 0x1005),
                Arguments.of(gn, 0, 0, 0), Arguments.of(gn, 0, 1, 1), Arguments.of(gn, 0, 2, 2));
    }

    @ParameterizedTest
    @MethodSource
    void edgeIdTest(GraphNodes nodes, int nodeId, int edgeIndex, int expected) {
        double actual = nodes.edgeId(nodeId, edgeIndex);
        assertEquals(expected, actual, DELTA);
    }

    // == Given Tests ==
    @Test
    void graphNodesWorksOnGivenExample() {
        IntBuffer b = IntBuffer.wrap(new int[] {2_600_000 << 4, 1_200_000 << 4, 0x2_000_1234});
        GraphNodes ns = new GraphNodes(b);
        assertEquals(1, ns.count());
        assertEquals(2_600_000, ns.nodeE(0));
        assertEquals(1_200_000, ns.nodeN(0));
        assertEquals(2, ns.outDegree(0));
        assertEquals(0x1234, ns.edgeId(0, 0));
        assertEquals(0x1235, ns.edgeId(0, 1));
    }

    @Test
    void graphNodesCountWorksFrom0To99() {
        for (int count = 0; count < 100; count += 1) {
            var buffer = IntBuffer.allocate(3 * count);
            var graphNodes = new GraphNodes(buffer);
            assertEquals(count, graphNodes.count());
        }
    }

    @Test
    void graphNodesENWorkOnRandomCoordinates() {
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
            assertEquals(e, graphNodes.nodeE(nodeId));
            assertEquals(n, graphNodes.nodeN(nodeId));
        }
    }

    @Test
    void graphNodesOutDegreeWorks() {
        var nodesCount = 10_000;
        var buffer = IntBuffer.allocate(3 * nodesCount);
        var rng = newRandom();
        for (int outDegree = 0; outDegree < 16; outDegree += 1) {
            var firstEdgeId = rng.nextInt(1 << 28);
            var nodeId = rng.nextInt(nodesCount);
            buffer.put(3 * nodeId + 2, (outDegree << 28) | firstEdgeId);
            var graphNodes = new GraphNodes(buffer);
            assertEquals(outDegree, graphNodes.outDegree(nodeId));
        }
    }

    @Test
    void graphNodesEdgeIdWorksOnRandomValues() {
        var nodesCount = 10_000;
        var buffer = IntBuffer.allocate(3 * nodesCount);
        var rng = newRandom();
        for (int outDegree = 0; outDegree < 16; outDegree += 1) {
            var firstEdgeId = rng.nextInt(1 << 28);
            var nodeId = rng.nextInt(nodesCount);
            buffer.put(3 * nodeId + 2, (outDegree << 28) | firstEdgeId);
            var graphNodes = new GraphNodes(buffer);
            for (int i = 0; i < outDegree; i += 1)
                assertEquals(firstEdgeId + i, graphNodes.edgeId(nodeId, i));
        }
    }

}

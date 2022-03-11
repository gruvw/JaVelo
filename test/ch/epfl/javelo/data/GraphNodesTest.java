package ch.epfl.javelo.data;

import java.nio.IntBuffer;
import java.util.stream.Stream;
import static org.junit.Assert.assertEquals;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

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

}

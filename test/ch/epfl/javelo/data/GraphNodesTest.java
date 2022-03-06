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

    private static GraphNodes nodes;

    @BeforeAll
    static void initGlobalVars() {
        IntBuffer b = IntBuffer.wrap(new int[] {2_600_000 << 4, 1_200_000 << 4, 0x2_000_1234});
        nodes = new GraphNodes(b.asReadOnlyBuffer());
    }

    static Stream<Arguments> countTest() {
        return Stream.of(Arguments.of(nodes, 1));
    }

    @ParameterizedTest
    @MethodSource
    void countTest(GraphNodes nodes, int expected) {
        assertEquals(expected, nodes.count());
    }

    static Stream<Arguments> nodeETest() {
        return Stream.of(Arguments.of(nodes, 0, 2_600_000));
    }

    @ParameterizedTest
    @MethodSource
    void nodeETest(GraphNodes nodes, int nodeId, int expected) {
        double actual = nodes.nodeE(nodeId);
        assertEquals(expected, actual, DELTA);
    }

    static Stream<Arguments> nodeNTest() {
        return Stream.of(Arguments.of(nodes, 0, 1_200_000));
    }

    @ParameterizedTest
    @MethodSource
    void nodeNTest(GraphNodes nodes, int nodeId, int expected) {
        double actual = nodes.nodeN(nodeId);
        assertEquals(expected, actual, DELTA);
    }

    static Stream<Arguments> outDegreeTest() {
        return Stream.of(Arguments.of(nodes, 0, 2));
    }

    @ParameterizedTest
    @MethodSource
    void outDegreeTest(GraphNodes nodes, int nodeId, int expected) {
        int actual = nodes.outDegree(nodeId);
        assertEquals(expected, actual);
    }

    static Stream<Arguments> edgeIdTest() {
        return Stream.of(Arguments.of(nodes, 0, 0, 0x1234), Arguments.of(nodes, 0, 1, 0x1235));
    }

    @ParameterizedTest
    @MethodSource
    void edgeIdTest(GraphNodes nodes, int nodeId, int edgeIndex, int expected) {
        double actual = nodes.edgeId(nodeId, edgeIndex);
        assertEquals(expected, actual, DELTA);
    }

}

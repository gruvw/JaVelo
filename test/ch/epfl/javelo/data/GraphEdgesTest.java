package ch.epfl.javelo.data;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import java.util.stream.Stream;
import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class GraphEdgesTest {

    private static final double DELTA = 1e-10;

    private static GraphEdges edges;

    @BeforeAll
    static void initGlobalVars() {
        ByteBuffer edgesBuffer = ByteBuffer.allocate(10);
        edgesBuffer.putInt(0, ~12);
        edgesBuffer.putShort(4, (short) 0x10B);
        edgesBuffer.putShort(6, (short) 0x100);
        edgesBuffer.putShort(8, (short) 2022);
        IntBuffer profileIds = IntBuffer.wrap(new int[] {(3 << 30) | 1});
        ShortBuffer elevations = ShortBuffer.wrap(
                new short[] {(short) 0, (short) 0x180C, (short) 0xFEFF, (short) 0xFFFE,
                             (short) 0xF000});

        edges = new GraphEdges(edgesBuffer.asReadOnlyBuffer(), profileIds.asReadOnlyBuffer(),
                elevations.asReadOnlyBuffer());
    }

    static Stream<Arguments> isInvertedTest() {
        return Stream.of(Arguments.of(edges, 0, true));
    }

    @ParameterizedTest
    @MethodSource
    void isInvertedTest(GraphEdges edges, int edgeId, boolean expected) {
        assertEquals(expected, edges.isInverted(edgeId));
    }

    static Stream<Arguments> targetNodeIdTest() {
        return Stream.of(Arguments.of(edges, 0, 12));
    }

    @ParameterizedTest
    @MethodSource
    void targetNodeIdTest(GraphEdges edges, int edgeId, int expected) {
        int actual = edges.targetNodeId(0);
        assertEquals(expected, actual);
    }

    static Stream<Arguments> lengthTest() {
        return Stream.of(Arguments.of(edges, 0, 16.6875));
    }

    @ParameterizedTest
    @MethodSource
    void lengthTest(GraphEdges edges, int edgeId, double expected) {
        double actual = edges.length(edgeId);
        assertEquals(expected, actual, DELTA);
    }

    static Stream<Arguments> elevationGainTest() {
        return Stream.of(Arguments.of(edges, 0, 16));
    }

    @ParameterizedTest
    @MethodSource
    void elevationGainTest(GraphEdges edges, int edgeId, double expected) {
        double actual = edges.elevationGain(edgeId);
        assertEquals(expected, actual, DELTA);
    }

    static Stream<Arguments> attributesIndexTest() {
        return Stream.of(Arguments.of(edges, 0, 2022));
    }

    @ParameterizedTest
    @MethodSource
    void attributesIndexTest(GraphEdges edges, int edgeId, int expected) {
        int actual = edges.attributesIndex(edgeId);
        assertEquals(expected, actual);
    }

    static Stream<Arguments> profileSamplesTest() {
        return Stream.of(Arguments.of(edges, 0,
                new float[] {384.0625F, 384.125F, 384.25F, 384.3125F, 384.375F, 384.4375F, 384.5F,
                             384.5625F, 384.6875F, 384.75F}));
    }

    @ParameterizedTest
    @MethodSource
    void profileSamplesTest(GraphEdges edges, int edgeId, float[] expected) {
        float[] actual = edges.profileSamples(edgeId);
        assertArrayEquals(expected, actual);
    }

}

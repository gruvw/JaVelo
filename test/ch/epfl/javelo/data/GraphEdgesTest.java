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
        ByteBuffer edgesBuffer = ByteBuffer.allocate(4 * 10);
        IntBuffer profilesBuffer = IntBuffer.allocate(4); // one int per edge
        ShortBuffer elevationsBuffer = ShortBuffer.allocate(12);

        // Edge first attribute, don't care about the destination node id, only care about its sign:
        // if negative edge is inverted

        // Type 0
        edgesBuffer.putInt(0); // Not inverted
        edgesBuffer.putShort((short) 0b101000); // 2.5 meters
        edgesBuffer.putShort((short) 0); // No elevation gain as type 0
        edgesBuffer.putShort((short) 0); // Attributes ID
        profilesBuffer.put(0); // type 0, id 0 but nothing in elevation

        // Type 1
        edgesBuffer.putInt(~1); // Inverted
        edgesBuffer.putShort((short) 0b1010000); // 5 meters
        edgesBuffer.putShort((short) 0b100000); // 2 meters
        edgesBuffer.putShort((short) 31); // Attributes ID
        profilesBuffer.put((1 << 30) | 0); // type 1, id 0

        elevationsBuffer.put((short) (2 << 4)); // 2 meters in UQ12.4
        elevationsBuffer.put((short) (1 << 4));
        elevationsBuffer.put((short) (3 << 4));
        elevationsBuffer.put((short) (0 << 4));

        // Type 2
        edgesBuffer.putInt(49); // Not inverted
        edgesBuffer.putShort((short) 0b1000100); // 4.25 meters
        edgesBuffer.putShort((short) 0b11000); // 1.5 meters
        edgesBuffer.putShort((short) 2021); // Attributes ID
        profilesBuffer.put((2 << 30) | 4); // type 2, id 4

        elevationsBuffer.put((short) 0xFFFF); // 4095.9375 meters in UQ12.4
        elevationsBuffer.put((short) (((-4 << 4) << 8) | 0b10100)); // -4, +1.25 meters in Q4.4
        elevationsBuffer.put((short) (0b100 << 8)); // + 0.25 meters in Q4.4

        // Type 3 (given example)
        edgesBuffer.putInt(~12); // Inverted
        edgesBuffer.putShort((short) 0x10B); // 16.6875 meters
        edgesBuffer.putShort((short) 0x100); // 16 meters
        edgesBuffer.putShort((short) 2022); // Attributes ID
        profilesBuffer.put((3 << 30) | 7); // type 3, id 7

        elevationsBuffer.put((short) 0x180C); // starting altitude
        elevationsBuffer.put((short) 0xFEFF); // meters Q0.4
        elevationsBuffer.put((short) 0xFFFE);
        elevationsBuffer.put((short) 0xF000);


        // Graph
        edges = new GraphEdges(edgesBuffer.asReadOnlyBuffer(), profilesBuffer.asReadOnlyBuffer(),
                elevationsBuffer.asReadOnlyBuffer());
    }

    static Stream<Arguments> isInvertedTest() {
        return Stream.of(Arguments.of(edges, 0, false), Arguments.of(edges, 1, true),
                Arguments.of(edges, 2, false), Arguments.of(edges, 3, true));
    }

    @ParameterizedTest
    @MethodSource
    void isInvertedTest(GraphEdges edges, int edgeId, boolean expected) {
        assertEquals(expected, edges.isInverted(edgeId));
    }

    static Stream<Arguments> targetNodeIdTest() {
        return Stream.of(Arguments.of(edges, 0, 0), Arguments.of(edges, 1, 1),
                Arguments.of(edges, 2, 49), Arguments.of(edges, 3, 12));
    }

    @ParameterizedTest
    @MethodSource
    void targetNodeIdTest(GraphEdges edges, int edgeId, int expected) {
        int actual = edges.targetNodeId(edgeId);
        assertEquals(expected, actual);
    }

    static Stream<Arguments> lengthTest() {
        return Stream.of(Arguments.of(edges, 0, 2.5), Arguments.of(edges, 1, 5),
                Arguments.of(edges, 2, 4.25), Arguments.of(edges, 3, 16.6875));
    }

    @ParameterizedTest
    @MethodSource
    void lengthTest(GraphEdges edges, int edgeId, double expected) {
        double actual = edges.length(edgeId);
        assertEquals(expected, actual, DELTA);
    }

    static Stream<Arguments> elevationGainTest() {
        return Stream.of(Arguments.of(edges, 0, 0), Arguments.of(edges, 1, 2),
                Arguments.of(edges, 2, 1.5), Arguments.of(edges, 3, 16));
    }

    @ParameterizedTest
    @MethodSource
    void elevationGainTest(GraphEdges edges, int edgeId, double expected) {
        double actual = edges.elevationGain(edgeId);
        assertEquals(expected, actual, DELTA);
    }

    static Stream<Arguments> attributesIndexTest() {
        return Stream.of(Arguments.of(edges, 0, 0), Arguments.of(edges, 1, 31),
                Arguments.of(edges, 2, 2021), Arguments.of(edges, 3, 2022));
    }

    @ParameterizedTest
    @MethodSource
    void attributesIndexTest(GraphEdges edges, int edgeId, int expected) {
        int actual = edges.attributesIndex(edgeId);
        assertEquals(expected, actual);
    }

    static Stream<Arguments> profileSamplesTest() {
        return Stream.of(Arguments.of(edges, 0, new float[0]),
                Arguments.of(edges, 1, new float[] {0, 3, 1, 2}),
                Arguments.of(edges, 2,
                        new float[] {4095.9375F, 4091.9375F, 4093.1875F, 4093.4375F}),
                Arguments.of(edges, 3,
                        new float[] {384.0625F, 384.125F, 384.25F, 384.3125F, 384.375F, 384.4375F,
                                     384.5F, 384.5625F, 384.6875F, 384.75F}));
    }

    @ParameterizedTest
    @MethodSource
    void profileSamplesTest(GraphEdges edges, int edgeId, float[] expected) {
        float[] actual = edges.profileSamples(edgeId);
        assertArrayEquals(expected, actual);
    }

}

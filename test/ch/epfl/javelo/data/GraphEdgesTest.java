package ch.epfl.javelo.data;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import static ch.epfl.test.TestRandomizer.*;

class GraphEdgesTest {

    private static final double DELTA = 1e-10;

    private static GraphEdges edges;

    @BeforeAll
    static void initGlobalVars() {
        ByteBuffer edgesBuffer = ByteBuffer.allocate(4 * 10); // 4 edges, 10 bytes each
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

    // == Given Tests ==

    @Test
    void graphEdgesWorksOnGivenExample() {
        ByteBuffer edgesBuffer = ByteBuffer.allocate(10);
        // Sens : inversé. Nœud destination : 12.
        edgesBuffer.putInt(0, ~12);
        // Longueur : 0x10.b m (= 16.6875 m)
        edgesBuffer.putShort(4, (short) 0x10_b);
        // Dénivelé : 0x10.0 m (= 16.0 m)
        edgesBuffer.putShort(6, (short) 0x10_0);
        // Identité de l'ensemble d'attributs OSM : 1
        edgesBuffer.putShort(8, (short) 2022);

        IntBuffer profileIds = IntBuffer.wrap(new int[] {
                                                         // Type : 3. Index du premier échantillon :
                                                         // 1.
                                                         (3 << 30) | 1});

        ShortBuffer elevations = ShortBuffer.wrap(
                new short[] {(short) 0, (short) 0x180C, (short) 0xFEFF, (short) 0xFFFE,
                             (short) 0xF000});

        GraphEdges edges = new GraphEdges(edgesBuffer, profileIds, elevations);

        assertTrue(edges.isInverted(0));
        assertEquals(12, edges.targetNodeId(0));
        assertEquals(16.6875, edges.length(0));
        assertEquals(16.0, edges.elevationGain(0));
        assertTrue(edges.hasProfile(0));
        assertEquals(2022, edges.attributesIndex(0));
        float[] expectedSamples = new float[] {384.0625f, 384.125f, 384.25f, 384.3125f, 384.375f,
                                               384.4375f, 384.5f, 384.5625f, 384.6875f, 384.75f};
        assertArrayEquals(expectedSamples, edges.profileSamples(0));
    }

    @Test
    void graphEdgesIsInvertedWorksForPlusMinus100() {
        var edgesCount = 10_000;
        var edgesBuffer = ByteBuffer.allocate(10 * edgesCount);
        var profileIds = IntBuffer.allocate(edgesCount);
        var elevations = ShortBuffer.allocate(10);
        var rng = newRandom();
        for (int targetNodeId = -100; targetNodeId < 100; targetNodeId += 1) {
            var edgeId = rng.nextInt(edgesCount);
            edgesBuffer.putInt(10 * edgeId, targetNodeId);
            var graphEdges = new GraphEdges(edgesBuffer, profileIds, elevations);
            assertEquals(targetNodeId < 0, graphEdges.isInverted(edgeId));
        }
    }

    @Test
    void graphEdgesTargetNodeIdWorksForPlusMinus100() {
        var edgesCount = 10_000;
        var edgesBuffer = ByteBuffer.allocate(10 * edgesCount);
        var profileIds = IntBuffer.allocate(edgesCount);
        var elevations = ShortBuffer.allocate(10);
        var rng = newRandom();
        for (int targetNodeId = -100; targetNodeId < 100; targetNodeId += 1) {
            var edgeId = rng.nextInt(edgesCount);
            edgesBuffer.putInt(10 * edgeId, targetNodeId);
            var graphEdges = new GraphEdges(edgesBuffer, profileIds, elevations);
            var expectedTargetNodeId = targetNodeId < 0 ? ~targetNodeId : targetNodeId;
            assertEquals(expectedTargetNodeId, graphEdges.targetNodeId(edgeId));
        }
    }

    @Test
    void graphEdgesLengthWorksOnRandomValues() {
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
            assertEquals(length, graphEdges.length(edgeId));
        }
    }

    @Test
    void graphEdgesElevationGainWorksOnRandomValues() {
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
            assertEquals(elevationGain, graphEdges.elevationGain(edgeId));
        }
    }

    @Test
    void graphEdgesHasProfileWorks() {
        var edgesCount = 10_000;
        var elevationsCount = 25_000;
        var edgesBuffer = ByteBuffer.allocate(10 * edgesCount);
        var profileIds = IntBuffer.allocate(edgesCount);
        var elevations = ShortBuffer.allocate(elevationsCount);
        var rng = newRandom();
        for (int i = 0; i < RANDOM_ITERATIONS; i += 1) {
            for (int profileType = 0; profileType < 4; profileType += 1) {
                var edgeId = rng.nextInt(edgesCount);
                var firstSampleIndex = rng.nextInt(elevationsCount);
                profileIds.put(edgeId, (profileType << 30) | firstSampleIndex);
                var graphEdges = new GraphEdges(edgesBuffer, profileIds, elevations);
                assertEquals(profileType != 0, graphEdges.hasProfile(edgeId));
            }
        }
    }

    @Test
    void graphEdgesProfileSamplesWorksForType0() {
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
            assertArrayEquals(new float[0], graphEdges.profileSamples(edgeId));
        }
    }

    @Test
    void graphEdgesProfileSamplesWorksForType1() {
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
            var expectedSamples = new float[sampleCount];
            for (int j = 0; j < sampleCount; j += 1) {
                var elevation = Math.scalb(
                        Short.toUnsignedInt(elevations.get(firstSampleIndex + j)), -4);
                if (inverted)
                    expectedSamples[sampleCount - 1 - j] = elevation;
                else
                    expectedSamples[j] = elevation;
            }
            var graphEdges = new GraphEdges(edgesBuffer.asReadOnlyBuffer(),
                    profileIds.asReadOnlyBuffer(), elevations.asReadOnlyBuffer());
            assertArrayEquals(expectedSamples, graphEdges.profileSamples(0));
        }
    }

    @Test
    void graphEdgesProfileSamplesWorksForType2() {
        List<TestCase> samples = List.of(
                new TestCase(new short[] {0x2a2d, 0x0201},
                        new float[] {674.812500f, 674.937500f, 675.000000f}),
                new TestCase(new short[] {0x2036, 0x01e0, (short) 0xd200},
                        new float[] {515.375000f, 515.437500f, 513.437500f, 510.562500f}),
                new TestCase(new short[] {0x2022, 0x0103, 0x090c},
                        new float[] {514.125000f, 514.187500f, 514.375000f, 514.937500f,
                                     515.687500f}),
                new TestCase(new short[] {0x204d, (short) 0xf2f9, 0x0209, (short) 0xfa00},
                        new float[] {516.812500f, 515.937500f, 515.500000f, 515.625000f,
                                     516.187500f, 515.812500f}),
                new TestCase(new short[] {0x19c8, (short) 0xfefe, (short) 0xfeff, (short) 0xff13},
                        new float[] {412.500000f, 412.375000f, 412.250000f, 412.125000f,
                                     412.062500f, 412.000000f, 413.187500f}),
                new TestCase(new short[] {0x1776, 0x0100, (short) 0xfff3, (short) 0xe800, 0x0100},
                        new float[] {375.375000f, 375.437500f, 375.437500f, 375.375000f,
                                     374.562500f, 373.062500f, 373.062500f, 373.125000f}));

        var edgesBuffer = ByteBuffer.allocate(10);
        var profileIds = IntBuffer.wrap(new int[] {2 << 30}).asReadOnlyBuffer();
        var elevations = ShortBuffer.allocate(20);
        for (TestCase testCase : samples) {
            var sampleCount = testCase.uncompressed().length;
            var edgeLength_q28_4 = (2 * (sampleCount - 1)) << 4;
            elevations.put(0, testCase.compressed());
            edgesBuffer.putShort(4, (short) edgeLength_q28_4);
            var graphEdges = new GraphEdges(edgesBuffer.asReadOnlyBuffer(), profileIds,
                    elevations.asReadOnlyBuffer());

            // Straight
            edgesBuffer.putInt(0, 0);
            assertArrayEquals(testCase.uncompressed(), graphEdges.profileSamples(0));

            // Inverted
            edgesBuffer.putInt(0, ~0);
            assertArrayEquals(testCase.uncompressedInverted(), graphEdges.profileSamples(0));
        }
    }

    @Test
    void graphEdgesProfileSamplesWorksForType3() {
        List<TestCase> samples = List.of(
                new TestCase(new short[] {0x2a0f, (short) 0xeff0},
                        new float[] {672.937500f, 672.812500f, 672.750000f, 672.687500f}),
                new TestCase(new short[] {0x2a3e, (short) 0xefef},
                        new float[] {675.875000f, 675.750000f, 675.687500f, 675.562500f,
                                     675.500000f}),
                new TestCase(new short[] {0x2a13, 0x1121, 0x1000},
                        new float[] {673.187500f, 673.250000f, 673.312500f, 673.437500f,
                                     673.500000f, 673.562500f}),
                new TestCase(new short[] {0x2a8b, 0x2121, 0x2200},
                        new float[] {680.687500f, 680.812500f, 680.875000f, 681.000000f,
                                     681.062500f, 681.187500f, 681.312500f}),
                new TestCase(new short[] {0x2a49, (short) 0xefef, (short) 0xeef0},
                        new float[] {676.562500f, 676.437500f, 676.375000f, 676.250000f,
                                     676.187500f, 676.062500f, 675.937500f, 675.875000f}));

        var edgesBuffer = ByteBuffer.allocate(10);
        var profileIds = IntBuffer.wrap(new int[] {3 << 30}).asReadOnlyBuffer();
        var elevations = ShortBuffer.allocate(20);
        for (TestCase testCase : samples) {
            var sampleCount = testCase.uncompressed().length;
            var edgeLength_q28_4 = (2 * (sampleCount - 1)) << 4;
            elevations.put(0, testCase.compressed());
            edgesBuffer.putShort(4, (short) edgeLength_q28_4);
            var graphEdges = new GraphEdges(edgesBuffer.asReadOnlyBuffer(), profileIds,
                    elevations.asReadOnlyBuffer());

            // Straight
            edgesBuffer.putInt(0, 0);
            assertArrayEquals(testCase.uncompressed(), graphEdges.profileSamples(0));

            // Inverted
            edgesBuffer.putInt(0, ~0);
            assertArrayEquals(testCase.uncompressedInverted(), graphEdges.profileSamples(0));
        }
    }

    private record TestCase(short[] compressed, float[] uncompressed) {

        public float[] uncompressedInverted() {
            float[] array = uncompressed();
            var inverted = Arrays.copyOf(array, array.length);
            for (int i = 0, j = inverted.length - 1; i < j; i += 1, j -= 1) {
                var t = inverted[i];
                inverted[i] = inverted[j];
                inverted[j] = t;
            }
            return inverted;
        }

    }

    @Test
    void graphEdgesAttributesIndexWorksOnRandomValues() {
        var edgesCount = 10_000;
        var edgesBuffer = ByteBuffer.allocate(10 * edgesCount);
        var profileIds = IntBuffer.allocate(edgesCount);
        var elevations = ShortBuffer.allocate(10);
        var rng = newRandom();
        for (int i = 0; i < RANDOM_ITERATIONS; i += 1) {
            var edgeId = rng.nextInt(edgesCount);
            var attributesIndex = rng.nextInt(1 << 16);
            edgesBuffer.putShort(10 * edgeId + 8, (short) attributesIndex);
            var graphEdges = new GraphEdges(edgesBuffer, profileIds, elevations);
            assertEquals(attributesIndex, graphEdges.attributesIndex(edgeId));
        }
    }


}

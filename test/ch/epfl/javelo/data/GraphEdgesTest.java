package ch.epfl.javelo.data;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import org.junit.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

public class GraphEdgesTest {

    private static final double DELTA = 1e-10;
    ByteBuffer edgesBuffer;
    IntBuffer profileIds;
    ShortBuffer elevations;
    GraphEdges edges;

    @BeforeEach
    public void init() {
        edgesBuffer = ByteBuffer.allocate(10);
        edgesBuffer.putInt(0, -12);
        edgesBuffer.putShort(4, (short) 0x10_b);
        edgesBuffer.putShort(6, (short) 0x10_0);
        edgesBuffer.putShort(8, (short) 2022);

        profileIds = IntBuffer.wrap(new int[] {(3 << 30) | 1});

        elevations = ShortBuffer.wrap(new short[] {(short) 0, (short) 0x180C, (short) 0xFEFF,
                                                   (short) 0xFFFE, (short) 0xF000});

        edges = new GraphEdges(edgesBuffer, profileIds, elevations);
    }

    @Test
    public void isInvertedTest() {
        assertTrue(edges.isInverted(0));
    }

    @ParameterizedTest
    @CsvSource({"0,12"})
    public void targetNodeIdTest(int input, int expected) {
        int actual = edges.targetNodeId(0);
        assertEquals(expected, actual);
    }

    @ParameterizedTest
    @CsvSource({"0,16.6875"})
    public void lengthTest(int input, double expected) {
        double actual = edges.length(0);
        assertEquals(expected, actual, DELTA);
    }

    @ParameterizedTest
    @CsvSource({"0,16.0"})
    public void elevationGainTest(int input, double expected) {
        double actual = edges.elevationGain(0);
        assertEquals(expected, actual, DELTA);
    }

    @ParameterizedTest
    @CsvSource({"0,2022"})
    public void attributesIndexTest(int input, int expected) {
        int actual = edges.attributesIndex(0);
        assertEquals(expected, actual);
    }

    @Test
    public void profileSamplesTest() {
        init();
        float[] expectedSamples = new float[] {384.0625f, 384.125f, 384.25f, 384.3125f, 384.375f,
                                               384.4375f, 384.5f, 384.5625f, 384.6875f, 384.75f};
        assertArrayEquals(expectedSamples, edges.profileSamples(0));
    }

}

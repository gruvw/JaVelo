package ch.epfl.javelo.data;

import java.nio.IntBuffer;
import java.util.stream.Stream;
import static org.junit.Assert.assertEquals;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class GraphNodesTest {

    private static final double DELTA = 1e-6;

    private static Stream<Arguments> countTest() {
        IntBuffer b = IntBuffer.wrap(new int[] {2_600_000 << 4, 1_200_000 << 4, 0x2_000_1234});
        return Stream.of(Arguments.of(new GraphNodes(b), 1));
    }

    @ParameterizedTest
    @MethodSource
    public void countTest(GraphNodes gn, int expected) {
        assertEquals(expected, gn.count());
    }

    private static Stream<Arguments> nodeETest() {
        IntBuffer b = IntBuffer.wrap(new int[] {2_600_000 << 4, 1_200_000 << 4, 0x2_000_1234});
        return Stream.of(Arguments.of(new GraphNodes(b), 0, 2_600_000));
    }

    @ParameterizedTest
    @MethodSource
    public void nodeETest(GraphNodes gn, int input, int expected) {
        double actual = gn.nodeE(input);
        assertEquals(expected, actual, DELTA);
    }

    // @ParameterizedTest
    // @MethodSource("provideBuffersForNodeN")
    // public void nodeNTest(int input, int expected) {
    // double actual = ns1.nodeN(input);
    // assertEquals(expected, actual, DELTA);
    // }

    // @ParameterizedTest
    // @CsvSource({"0,2"})
    // public void outDegreeTest(int input, int expected) {
    // double actual = ns1.outDegree(0);
    // assertEquals(expected, actual, DELTA);
    // }

    // @ParameterizedTest
    // @CsvSource({"0,0,0x1234", "0,1,0x1235"})
    // public void edgeIdTest(int input1, int input2, int expected) {
    // double actual = ns1.edgeId(input1, input2);
    // assertEquals(expected, actual, DELTA);
    // }

}

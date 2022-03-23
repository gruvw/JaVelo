package ch.epfl.javelo.routing;

import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.DoubleUnaryOperator;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import ch.epfl.javelo.Functions;
import ch.epfl.javelo.projection.PointCh;
import ch.epfl.javelo.projection.SwissBounds;

public class SingleRouteTest {

    private static final PointCh point1 = new PointCh(SwissBounds.MIN_E, SwissBounds.MIN_N);
    private static final PointCh point2 = new PointCh(SwissBounds.MIN_E + 10,
            SwissBounds.MIN_N + 10);
    private static final PointCh point3 = new PointCh(SwissBounds.MIN_E + 20,
            SwissBounds.MIN_N + 20);
    private static final PointCh point4 = new PointCh(SwissBounds.MIN_E + 30,
            SwissBounds.MIN_N + 30);
    private final static Edge edge1 = new Edge(0, 1, point1, point2, 10, Functions.constant(1));
    private final static Edge edge2 = new Edge(2, 3, point2, point3, 20, Functions.constant(2));
    private final static Edge edge3 = new Edge(4, 5, point3, point4, 0, Functions.constant(3));
    private final static List<Edge> edges12 = Arrays.asList(new Edge[] {edge1, edge2});
    private final static List<Edge> edges123 = Arrays.asList(new Edge[] {edge1, edge2, edge3});
    private final static SingleRoute srTwoEdges = new SingleRoute(edges12);
    private final static SingleRoute srThreeEdges = new SingleRoute(edges123);

    @Test
    void singleRouteThrowsOnEmptyList() {
        assertThrows(IllegalArgumentException.class, () -> new SingleRoute(new ArrayList<>()));
    }

    static Stream<Arguments> indexOfSegmentAtTest() {
        return Stream.of(Arguments.of(srTwoEdges, -10, 0), Arguments.of(srTwoEdges, 0, 0),
                Arguments.of(srTwoEdges, 10, 0));
    }

    @ParameterizedTest
    @MethodSource
    void indexOfSegmentAtTest(SingleRoute sr, double position, int expected) {
        assertEquals(expected, sr.indexOfSegmentAt(position));
    }

    static Stream<Arguments> lengthTest() {
        return Stream.of(Arguments.of(srTwoEdges, 30), Arguments.of(srThreeEdges, 30));
    }

    @ParameterizedTest
    @MethodSource
    void lengthTest(SingleRoute sr, double expected) {
        assertEquals(expected, sr.length());
    }

    @Test
    void edgesImmutabilityTest() {
        List<Edge> mutableList = new ArrayList<>();
        mutableList.add(edge1);
        mutableList.add(edge2);
        SingleRoute sr = new SingleRoute(mutableList);
        List<Edge> expected = List.copyOf(mutableList);
        mutableList.add(edge3); // should not change the list in the SingleRoute!
        assertEquals(expected, sr.edges());
    }

    static Stream<Arguments> edgesTest() {
        return Stream.of(Arguments.of(srTwoEdges, edges12), Arguments.of(srThreeEdges, edges123));
    }

    @ParameterizedTest
    @MethodSource
    void edgesTest(SingleRoute sr, List<Edge> expected) {
        assertEquals(expected, sr.edges());
    }

    static Stream<Arguments> pointsTest() {
        return Stream.of(
                Arguments.of(srTwoEdges,
                        Arrays.asList(edge1.fromPoint(), edge1.toPoint(), edge2.toPoint())),
                Arguments.of(srThreeEdges, Arrays.asList(edge1.fromPoint(), edge1.toPoint(),
                        edge2.toPoint(), edge3.toPoint())));
    }

    @ParameterizedTest
    @MethodSource
    void pointsTest(SingleRoute sr, List<PointCh> expected) {
        assertEquals(expected, sr.points());
    }

    static Stream<Arguments> pointAtTest() {
        return Stream.of(Arguments.of(srTwoEdges, -10, point1), Arguments.of(srTwoEdges, 0, point1),
                Arguments.of(srTwoEdges, 10, point2), Arguments.of(srTwoEdges, 30, point3));
    }

    @ParameterizedTest
    @MethodSource
    void pointAtTest(SingleRoute sr, double position, PointCh expected) {
        assertEquals(expected, sr.pointAt(position));
    }

    private static final Edge edgeOf(double length, DoubleUnaryOperator profile) {
        return new Edge(0, 1, point1, point2, length, profile);
    }

    @Test
    void elevationAtFullTest() {
        DoubleUnaryOperator nanProfile = Functions.constant(Double.NaN);
        SingleRoute sr = new SingleRoute(Arrays.asList(edgeOf(0.5, nanProfile),
                edgeOf(1.5, Functions.sampled(new float[] {2, 5}, 1.5)),
                edgeOf(0.4, Functions.sampled(new float[] {5, 4}, 0.4)), edgeOf(1.1, nanProfile),
                edgeOf(0.7, nanProfile),
                edgeOf(1.6, Functions.sampled(new float[] {8.2F, 7, 10}, 1.6)),
                edgeOf(1.5, nanProfile), edgeOf(0.7, Functions.sampled(new float[] {4, 6.1F}, 0.7)),
                edgeOf(1, nanProfile)));
        double[] expected = new double[] {Double.NaN, 2.5, 4.0, 4.375, Double.NaN, Double.NaN, 7.75,
                                          7.9375, Double.NaN, Double.NaN, 4.6, Double.NaN,
                                          Double.NaN};
        for (int i = 0; i < 1; i++) {
            assertEquals(expected[i], sr.elevationAt(i * 0.75));
        }
    }

    static Stream<Arguments> elevationAtTest() {
        return Stream.of(Arguments.of(srTwoEdges, -10, 1), Arguments.of(srTwoEdges, 35, 2),
                Arguments.of(srTwoEdges, 5, 1), Arguments.of(srTwoEdges, 10 + 0.1, 2),
                Arguments.of(srTwoEdges, 10 - 0.1, 1), Arguments.of(srTwoEdges, 15, 2),
                Arguments.of(srTwoEdges, 20, 2), Arguments.of(srTwoEdges, 25, 2),
                Arguments.of(srTwoEdges, 30, 2));
    }

    @ParameterizedTest
    @MethodSource
    void elevationAtTest(SingleRoute sr, double position, double expected) {
        assertEquals(expected, sr.elevationAt(position));
    }

    static Stream<Arguments> nodeClosestToTest() {
        return Stream.of(Arguments.of(srTwoEdges, -10, 0), Arguments.of(srTwoEdges, 0, 0),
                Arguments.of(srTwoEdges, 5, 1), Arguments.of(srTwoEdges, 10 - 0.1, 1),
                Arguments.of(srTwoEdges, 10 + 0.1, 2), Arguments.of(srTwoEdges, 15, 2),
                Arguments.of(srTwoEdges, 20, 3), Arguments.of(srTwoEdges, 30 - 0.1, 3),
                Arguments.of(srTwoEdges, 30 + 0.1, 3));
    }

    @ParameterizedTest
    @MethodSource
    void nodeClosestToTest(SingleRoute sr, double position, int expected) {
        assertEquals(expected, sr.nodeClosestTo(position));
    }

    static Stream<Arguments> pointClosestToTest() {
        return Stream.of();
    }

    @ParameterizedTest
    @MethodSource
    void pointClosestToTest(SingleRoute sr, PointCh point, RoutePoint expected) {
        assertEquals(expected, sr.pointClosestTo(point));
    }

}

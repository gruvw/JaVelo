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
import org.junit.jupiter.params.provider.ValueSource;
import ch.epfl.javelo.Functions;
import ch.epfl.javelo.projection.PointCh;
import ch.epfl.javelo.projection.SwissBounds;

import static ch.epfl.test.TestUtils.assertEqualsRoutePoint;

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
    private final static DoubleUnaryOperator nanProfile = Functions.constant(Double.NaN);

    @Test
    void singleRouteThrowsOnEmptyList() {
        assertThrows(IllegalArgumentException.class, () -> new SingleRoute(new ArrayList<>()));
    }

    @ParameterizedTest
    @ValueSource(doubles = {-10, 10, 0})
    void indexOfSegmentAtTest(double position) {
        assertEquals(0, srTwoEdges.indexOfSegmentAt(position));
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
        for (int i = 0; i < expected.length; i++) {
            assertEquals(expected[i], sr.elevationAt(i * 0.75), 1e-6);
        }
    }

    static Stream<Arguments> elevationAtTest() {
        return Stream.of(Arguments.of(-10, 1), Arguments.of(35, 2), Arguments.of(5, 1),
                Arguments.of(10 + 0.1, 2), Arguments.of(10 - 0.1, 1), Arguments.of(15, 2),
                Arguments.of(20, 2), Arguments.of(25, 2), Arguments.of(30, 2));
    }

    @ParameterizedTest
    @MethodSource
    void elevationAtTest(double position, double expected) {
        assertEquals(expected, srTwoEdges.elevationAt(position));
    }

    static Stream<Arguments> nodeClosestToTest() {
        return Stream.of(Arguments.of(-10, 0), Arguments.of(0, 0), Arguments.of(5, 1),
                Arguments.of(10 - 0.1, 1), Arguments.of(10 + 0.1, 2), Arguments.of(15, 2),
                Arguments.of(20, 3), Arguments.of(30 - 0.1, 3), Arguments.of(30 + 0.1, 3),
                Arguments.of(50, 3), Arguments.of(0.8, 0), Arguments.of(Math.PI, 0));
    }

    @ParameterizedTest
    @MethodSource
    void nodeClosestToTest(double position, int expected) {
        assertEquals(expected, srTwoEdges.nodeClosestTo(position));
    }

    @Test
    void pointClosestToTest() {
        final double DELTA = 1e-8;
        // Points
        PointCh pointA = new PointCh(SwissBounds.MIN_E + 5.6, SwissBounds.MIN_N + 3.7);
        PointCh pointB = new PointCh(SwissBounds.MIN_E + 17, SwissBounds.MIN_N + 10);
        PointCh pointC = new PointCh(SwissBounds.MIN_E + 11, SwissBounds.MIN_N + 15);
        PointCh pointE = new PointCh(SwissBounds.MIN_E + 10, SwissBounds.MIN_N + 11.5);
        PointCh pointH = new PointCh(SwissBounds.MIN_E + 11, SwissBounds.MIN_N + 19);
        PointCh pointD = new PointCh(SwissBounds.MIN_E + 2.2099318052,
                SwissBounds.MIN_N + 1.8265412608);
        PointCh pointI = new PointCh(SwissBounds.MIN_E + 15.0651487827,
                SwissBounds.MIN_N + 11.6123760144);
        PointCh pointF = new PointCh(SwissBounds.MIN_E + 16.4542380523,
                SwissBounds.MIN_N + 21.5450856628);
        PointCh pointJ = new PointCh(SwissBounds.MIN_E + 12.6003123407,
                SwissBounds.MIN_N + 10.4189590297);
        PointCh pointK = new PointCh(SwissBounds.MIN_E + 21.0173503028,
                SwissBounds.MIN_N + 9.6174489384);
        PointCh pointG = new PointCh(SwissBounds.MIN_E + 2.7, SwissBounds.MIN_N + 4.3);
        // Edges
        Edge edgeAB = new Edge(0, 1, pointA, pointB, 13.0249760077, nanProfile);
        Edge edgeBC = new Edge(1, 2, pointB, pointC, 7.8102496759, nanProfile);
        // SingleRoute
        SingleRoute sr = new SingleRoute(Arrays.asList(new Edge[] {edgeAB, edgeBC}));
        // Point A (on fromPoint of the first edge)
        RoutePoint expected = new RoutePoint(pointA, 0, 0);
        assertEqualsRoutePoint(expected, sr.pointClosestTo(pointA), DELTA);
        // Point B (on toPoint of the first edge)
        expected = new RoutePoint(pointB, edgeAB.length(), 0);
        assertEqualsRoutePoint(expected, sr.pointClosestTo(pointB), DELTA);
        // Point D
        expected = new RoutePoint(pointA, 0, 3.873294465);
        assertEqualsRoutePoint(expected, sr.pointClosestTo(pointD), DELTA);
        // Point G
        expected = new RoutePoint(pointA, 0, 2.961418579);
        assertEqualsRoutePoint(expected, sr.pointClosestTo(pointG), DELTA);
        // Point H
        expected = new RoutePoint(pointC, sr.length(), 4);
        assertEqualsRoutePoint(expected, sr.pointClosestTo(pointH), DELTA);
        // Point F
        expected = new RoutePoint(pointC, sr.length(), 8.5197921961);
        assertEqualsRoutePoint(expected, sr.pointClosestTo(pointF), DELTA);
        // Point K
        expected = new RoutePoint(pointB, edgeAB.length(), 4.0355233577);
        assertEqualsRoutePoint(expected, sr.pointClosestTo(pointK), DELTA);
        // Point I
        expected = new RoutePoint(pointI, 15.5435878565, 0);
        assertEqualsRoutePoint(expected, sr.pointClosestTo(pointI), DELTA);
        // Point E
        PointCh pointL = new PointCh(SwissBounds.MIN_E + 12.131147541,
                SwissBounds.MIN_N + 14.0573770492);
        expected = new RoutePoint(pointL, 19.3628015643, 3.3289588783);
        assertEqualsRoutePoint(expected, sr.pointClosestTo(pointE), DELTA);
        // Point J
        PointCh pointM = new PointCh(SwissBounds.MIN_E + 13.8069922152,
                SwissBounds.MIN_N + 8.2354430663);
        expected = new RoutePoint(pointM, 9.3768312893, 2.494758161);
        assertEqualsRoutePoint(expected, sr.pointClosestTo(pointJ), DELTA);
    }

}

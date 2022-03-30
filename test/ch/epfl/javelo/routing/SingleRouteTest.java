package ch.epfl.javelo.routing;

import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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
import static ch.epfl.test.TestRandomizer.RANDOM_ITERATIONS;
import static ch.epfl.test.TestRandomizer.newRandom;

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

    // == GIVEN TESTS ==

    private static final int ORIGIN_N = 1_200_000;
    private static final int ORIGIN_E = 2_600_000;
    private static final double EDGE_LENGTH = 100.25;

    // Sides of triangle used for "sawtooth" edges (shape: /\/\/\…)
    private static final double TOOTH_EW = 1023;
    private static final double TOOTH_NS = 64;
    private static final double TOOTH_LENGTH = 1025;
    private static final double TOOTH_ELEVATION_GAIN = 100d;
    private static final double TOOTH_SLOPE = TOOTH_ELEVATION_GAIN / TOOTH_LENGTH;

    @Test
    void singleRouteConstructorThrowsOnEmptyEdgeList() {
        assertThrows(IllegalArgumentException.class, () -> {
            new SingleRoute(List.of());
        });
    }

    @Test
    void singleRouteIndexOfSegmentAtAlwaysReturns0() {
        var route = new SingleRoute(verticalEdges(10));
        var rng = newRandom();
        for (int i = 0; i < RANDOM_ITERATIONS; i += 1) {
            var p = rng.nextDouble(-100, route.length() + 100);
            assertEquals(0, route.indexOfSegmentAt(p));
        }
    }

    @Test
    void singleRouteLengthReturnsTotalLength() {
        for (int i = 1; i < 10; i += 1) {
            var route = new SingleRoute(verticalEdges(i));
            assertEquals(i * EDGE_LENGTH, route.length());
        }
    }

    @Test
    void singleRouteEdgesAreCopiedToEnsureImmutability() {
        var immutableEdges = verticalEdges(10);
        var mutableEdges = new ArrayList<>(immutableEdges);
        var route = new SingleRoute(mutableEdges);
        mutableEdges.clear();
        assertEquals(immutableEdges, route.edges());
    }

    @Test
    void singleRoutePointsAreNotModifiableFromOutside() {
        var edgesCount = 5;
        var route = new SingleRoute(verticalEdges(edgesCount));
        try {
            route.points().clear();
        } catch (UnsupportedOperationException e) {
            // Nothing to do (the list of points is not modifiable, which is fine).
        }
        assertEquals(edgesCount + 1, route.points().size());
    }

    @Test
    void singleRoutePointsAreCorrect() {
        for (int edgesCount = 1; edgesCount < 10; edgesCount += 1) {
            var edges = verticalEdges(edgesCount);
            var route = new SingleRoute(edges);
            var points = route.points();
            assertEquals(edgesCount + 1, points.size());
            assertEquals(edges.get(0).fromPoint(), points.get(0));
            for (int i = 1; i < points.size(); i += 1)
                assertEquals(edges.get(i - 1).toPoint(), points.get(i));
        }
    }

    @Test
    void singleRoutePointAtWorks() {
        var edgesCount = 4;
        var route = new SingleRoute(sawToothEdges(edgesCount));

        // Outside the range of the route
        assertEquals(sawToothPoint(0), route.pointAt(-1e6));
        assertEquals(sawToothPoint(edgesCount), route.pointAt(+1e6));

        // Edge endpoints
        for (int i = 0; i < edgesCount + 1; i += 1)
            assertEquals(sawToothPoint(i), route.pointAt(i * TOOTH_LENGTH));

        // Points at 1/4, 2/4 and 3/4 of the edges
        for (int i = 0; i < edgesCount; i += 1) {
            for (double p = 0.25; p <= 0.75; p += 0.25) {
                var expectedE = ORIGIN_E + (i + p) * TOOTH_EW;
                var expectedN = (i & 1) == 0 ? ORIGIN_N + TOOTH_NS * p
                        : ORIGIN_N + TOOTH_NS * (1 - p);
                assertEquals(new PointCh(expectedE, expectedN),
                        route.pointAt((i + p) * TOOTH_LENGTH));
            }
        }
    }

    @Test
    void singleRouteElevationAtWorks() {
        var edgesCount = 4;
        var route = new SingleRoute(sawToothEdges(edgesCount));
        for (int i = 0; i < edgesCount; i += 1) {
            for (double p = 0; p < 1; p += 0.125) {
                var pos = (i + p) * TOOTH_LENGTH;
                var expectedElevation = (i + p) * TOOTH_ELEVATION_GAIN;
                assertEquals(expectedElevation, route.elevationAt(pos));
            }
        }
        assertEquals(0, route.elevationAt(-1e6));
        assertEquals(edgesCount * TOOTH_ELEVATION_GAIN, route.elevationAt(+1e6));
    }

    @Test
    void singleRouteNodeClosestToWorks() {
        var edgesCount = 4;
        var route = new SingleRoute(sawToothEdges(edgesCount));
        for (int i = 0; i <= edgesCount; i += 1) {
            for (double p = -0.25; p <= 0.25; p += 0.25) {
                var pos = (i + p) * TOOTH_LENGTH;
                assertEquals(i, route.nodeClosestTo(pos));
            }
        }
    }

    @Test
    void singleRoutePointClosestToWorksWithFarAwayPoints() {
        var rng = newRandom();
        var route = new SingleRoute(verticalEdges(1));

        // Points below the route
        var origin = new PointCh(ORIGIN_E, ORIGIN_N);
        for (int i = 0; i < RANDOM_ITERATIONS; i += 1) {
            var dN = rng.nextDouble(-10_000, -1);
            var dE = rng.nextDouble(-1000, 1000);
            var p = new PointCh(ORIGIN_E + dE, ORIGIN_N + dN);
            var pct = route.pointClosestTo(p);
            assertEquals(origin, pct.point());
            assertEquals(0, pct.position());
            assertEquals(Math.hypot(dE, dN), pct.distanceToReference(), 1e-4);
        }

        // Points above the route
        var end = new PointCh(ORIGIN_E, ORIGIN_N + EDGE_LENGTH);
        for (int i = 0; i < RANDOM_ITERATIONS; i += 1) {
            var dN = rng.nextDouble(1, 10_000);
            var dE = rng.nextDouble(-1000, 1000);
            var p = new PointCh(ORIGIN_E + dE, ORIGIN_N + EDGE_LENGTH + dN);
            var pct = route.pointClosestTo(p);
            assertEquals(end, pct.point());
            assertEquals(EDGE_LENGTH, pct.position());
            assertEquals(Math.hypot(dE, dN), pct.distanceToReference(), 1e-4);
        }
    }

    @Test
    void singleRoutePointClosestToWorksWithPointsOnRoute() {
        var rng = newRandom();
        var route = new SingleRoute(verticalEdges(20));
        for (int i = 0; i < RANDOM_ITERATIONS; i += 1) {
            var pos = rng.nextDouble(0, route.length());
            var pt = route.pointAt(pos);
            var pct = route.pointClosestTo(pt);
            assertEquals(pt.e(), pct.point().e(), 1e-4);
            assertEquals(pt.n(), pct.point().n(), 1e-4);
            assertEquals(pos, pct.position(), 1e-4);
            assertEquals(0, pct.distanceToReference(), 1e-4);
        }
    }

    @Test
    void singleRoutePointClosestToWorksWithSawtoothPoints() {
        var edgesCount = 4;
        var edges = sawToothEdges(edgesCount);
        var route = new SingleRoute(edges);

        // Points above the sawtooth
        for (int i = 1; i <= edgesCount; i += 2) {
            var p = sawToothPoint(i);
            var dN = i * 500;
            var pAbove = new PointCh(p.e(), p.n() + dN);
            var pct = route.pointClosestTo(pAbove);
            assertEquals(p, pct.point());
            assertEquals(i * TOOTH_LENGTH, pct.position());
            assertEquals(dN, pct.distanceToReference());
        }

        // Points below the sawtooth
        for (int i = 0; i <= edgesCount; i += 2) {
            var p = sawToothPoint(i);
            var dN = i * 500;
            var pBelow = new PointCh(p.e(), p.n() - dN);
            var pct = route.pointClosestTo(pBelow);
            assertEquals(p, pct.point());
            assertEquals(i * TOOTH_LENGTH, pct.position());
            assertEquals(dN, pct.distanceToReference());
        }

        // Points close to the n/8
        var dE = TOOTH_NS / 16d;
        var dN = TOOTH_EW / 16d;
        for (int i = 0; i < edgesCount; i += 1) {
            var upwardEdge = (i & 1) == 0;
            for (double p = 0.125; p <= 0.875; p += 0.125) {
                var pointE = ORIGIN_E + (i + p) * TOOTH_EW;
                var pointN = ORIGIN_N + TOOTH_NS * (upwardEdge ? p : (1 - p));
                var point = new PointCh(pointE, pointN);
                var position = (i + p) * TOOTH_LENGTH;
                var reference = new PointCh(pointE + dE, pointN + (upwardEdge ? -dN : dN));
                var pct = route.pointClosestTo(reference);
                assertEquals(point, pct.point());
                assertEquals(position, pct.position());
                assertEquals(Math.hypot(dE, dN), pct.distanceToReference());
            }
        }
    }

    private static List<Edge> verticalEdges(int edgesCount) {
        var edges = new ArrayList<Edge>(edgesCount);
        for (int i = 0; i < edgesCount; i += 1) {
            var p1 = new PointCh(ORIGIN_E, ORIGIN_N + i * EDGE_LENGTH);
            var p2 = new PointCh(ORIGIN_E, ORIGIN_N + (i + 1) * EDGE_LENGTH);
            edges.add(new Edge(i, i + 1, p1, p2, EDGE_LENGTH, x -> Double.NaN));
        }
        return Collections.unmodifiableList(edges);
    }

    private static List<Edge> sawToothEdges(int edgesCount) {
        var edges = new ArrayList<Edge>(edgesCount);
        for (int i = 0; i < edgesCount; i += 1) {
            var p1 = sawToothPoint(i);
            var p2 = sawToothPoint(i + 1);
            var startingElevation = i * TOOTH_ELEVATION_GAIN;
            edges.add(new Edge(i, i + 1, p1, p2, TOOTH_LENGTH,
                    x -> startingElevation + x * TOOTH_SLOPE));
        }
        return Collections.unmodifiableList(edges);
    }

    private static PointCh sawToothPoint(int i) {
        return new PointCh(ORIGIN_E + TOOTH_EW * i, ORIGIN_N + ((i & 1) == 0 ? 0 : TOOTH_NS));
    }

}

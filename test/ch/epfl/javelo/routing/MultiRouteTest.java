package ch.epfl.javelo.routing;

import static org.junit.Assert.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import ch.epfl.javelo.Functions;
import ch.epfl.javelo.projection.PointCh;
import ch.epfl.javelo.projection.SwissBounds;
import ch.epfl.test.TestUtils;


import static ch.epfl.test.TestRandomizer.RANDOM_ITERATIONS;
import static ch.epfl.test.TestRandomizer.newRandom;

public class MultiRouteTest {

    private static final PointCh point1 = new PointCh(SwissBounds.MIN_E, SwissBounds.MIN_N);
    private static final PointCh point2 = new PointCh(SwissBounds.MIN_E + 10,
                                                      SwissBounds.MIN_N + 10);
    private static final PointCh point3 = new PointCh(SwissBounds.MIN_E + 20,
                                                      SwissBounds.MIN_N + 20);
    private static final PointCh point4 = new PointCh(SwissBounds.MIN_E + 30,
                                                      SwissBounds.MIN_N + 30);
    private static final PointCh point5 = new PointCh(SwissBounds.MIN_E + 40,
                                                      SwissBounds.MIN_N + 40);
    private static final PointCh point6 = new PointCh(SwissBounds.MIN_E + 50,
                                                      SwissBounds.MIN_N + 50);
    private static final PointCh point7 = new PointCh(SwissBounds.MIN_E + 60,
                                                      SwissBounds.MIN_N + 60);
    private static final PointCh point8 = new PointCh(SwissBounds.MIN_E + 70,
                                                      SwissBounds.MIN_N + 70);
    private static final PointCh point9 = new PointCh(SwissBounds.MIN_E + 80,
                                                      SwissBounds.MIN_N + 80);
    private static final PointCh point10 = new PointCh(SwissBounds.MIN_E + 90,
                                                       SwissBounds.MIN_N + 90);
    private static final PointCh point11 = new PointCh(SwissBounds.MIN_E + 100,
                                                       SwissBounds.MIN_N + 100);
    private static final PointCh point12 = new PointCh(SwissBounds.MIN_E + 110,
                                                       SwissBounds.MIN_N + 100);
    private static final PointCh point13 = new PointCh(SwissBounds.MIN_E + 120,
                                                       SwissBounds.MIN_N + 120);
    private static final PointCh pointA = new PointCh(SwissBounds.MIN_E, SwissBounds.MIN_N);
    private static final PointCh pointB = new PointCh(SwissBounds.MIN_E + 1, SwissBounds.MIN_N + 3);
    private static final PointCh pointC = new PointCh(SwissBounds.MIN_E + 3, SwissBounds.MIN_N + 1);
    private static final PointCh pointD = new PointCh(SwissBounds.MIN_E + 4, SwissBounds.MIN_N + 4);
    private static final PointCh pointE = new PointCh(SwissBounds.MIN_E + 4, SwissBounds.MIN_N + 6);
    private static final PointCh pointF = new PointCh(SwissBounds.MIN_E + 1.2,
                                                      SwissBounds.MIN_N + 1.4);
    private static final PointCh pointQ = new PointCh(SwissBounds.MIN_E + 1.5146938281,
                                                      SwissBounds.MIN_N + 2.4853061719);
    private static final PointCh pointR = new PointCh(SwissBounds.MIN_E + 3.25,
                                                      SwissBounds.MIN_N + 1.75);
    private static final PointCh pointS = new PointCh(SwissBounds.MIN_E + 4,
                                                      SwissBounds.MIN_N + 4.7);

    private static final Edge edge1 = new Edge(1, 2, point1, point2, 400, Functions.constant(1));
    private static final Edge edge2 = new Edge(2, 3, point2, point3, 600, Functions.constant(2));
    private static final Edge edge3 = new Edge(3, 4, point3, point4, 600, Functions.constant(3));
    private static final Edge edge4 = new Edge(4, 5, point4, point5, 400, Functions.constant(4));
    private static final Edge edge5 = new Edge(5, 6, point5, point6, 600, Functions.constant(5));
    private static final Edge edge6 = new Edge(6, 7, point6, point7, 400, Functions.constant(6));
    private static final Edge edge7 = new Edge(7, 8, point7, point8, 400, Functions.constant(7));
    private static final Edge edge8 = new Edge(8, 9, point8, point9, 600, Functions.constant(8));
    private static final Edge edge9 = new Edge(9, 10, point9, point10, 600, Functions.constant(9));
    private static final Edge edge10 = new Edge(10, 11, point10, point11, 400,
                                                Functions.constant(10));
    private static final Edge edge11 = new Edge(11, 12, point11, point12, 600,
                                                Functions.constant(11));
    private static final Edge edge12 = new Edge(12, 13, point12, point13, 400,
                                                Functions.constant(12));
    private static final Edge edgeBC = new Edge(2, 3, pointB, pointC, 2.8284271247,
                                                Functions.constant(0));
    private static final Edge edgeCD = new Edge(3, 4, pointC, pointD, 3.1622776602,
                                                Functions.constant(0));
    private static final Edge edgeDE = new Edge(4, 5, pointD, pointE, 2, Functions.constant(0));
    private static final Edge edgeAF = new Edge(1, 6, pointA, pointF, 1.8439088915,
                                                Functions.constant(0));
    private static final Edge edgeFB = new Edge(6, 2, pointF, pointB, 1.6124515497,
                                                Functions.constant(0));

    // two edges, length = 400 + 600
    private static final SingleRoute segment1_1 = new SingleRoute(Arrays.asList(edge1, edge2));
    // two edges, length = 600 + 400
    private static final SingleRoute segment1_2 = new SingleRoute(Arrays.asList(edge3, edge4));
    // two edges, length = 400 + 600
    private static final SingleRoute segment1_3 = new SingleRoute(Arrays.asList(edge5, edge6));

    private static final MultiRoute mr1 = new MultiRoute(Arrays.asList(segment1_1, segment1_2,
            segment1_3));

    // two edges, length = 400 + 600
    private static final SingleRoute segment2_1 = new SingleRoute(Arrays.asList(edge7, edge8));
    // two edges, length = 600 + 400
    private static final SingleRoute segment2_2 = new SingleRoute(Arrays.asList(edge9, edge10));
    // two edges, length = 600 + 400
    private static final SingleRoute segment2_3 = new SingleRoute(Arrays.asList(edge11, edge12));

    private static final MultiRoute mr2 = new MultiRoute(Arrays.asList(segment2_1, segment2_2,
            segment2_3));

    private static final MultiRoute route = new MultiRoute(Arrays.asList(mr1, mr2));

    // One edge, length = 252.252
    private static final Edge oneEdge = new Edge(1, 2, point1, point2, 252.252,
                                                 Functions.constant(100));
    private static final SingleRoute srOneSegment = new SingleRoute(Arrays.asList(oneEdge));
    private static final MultiRoute mrOneSegment = new MultiRoute(Arrays.asList(srOneSegment));

    // Route making a loop
    private static final Edge loopEdge1 = new Edge(1, 2, point1, point2, 300,
                                                   Functions.constant(0));
    private static final Edge loopEdge2 = new Edge(2, 1, point2, point1, 300,
                                                   Functions.constant(0));
    private static final SingleRoute srLoop = new SingleRoute(Arrays.asList(loopEdge1, loopEdge2));
    private static final MultiRoute mrLoop = new MultiRoute(Arrays.asList(srLoop));

    // MultiRoute containing a MultiRoute and a SingleRoute
    private static final MultiRoute mrSingleAndMulti = new MultiRoute(Arrays.asList(mr1,
            segment2_1));

    // Geogebra routes
    private static final SingleRoute srAB = new SingleRoute(Arrays.asList(edgeAF, edgeFB));
    private static final SingleRoute srBC = new SingleRoute(Arrays.asList(edgeBC));
    private static final SingleRoute srCD = new SingleRoute(Arrays.asList(edgeCD));
    private static final SingleRoute srDE = new SingleRoute(Arrays.asList(edgeDE));

    private static final MultiRoute mrAD = new MultiRoute(Arrays.asList(srAB, srBC, srCD));
    private static final MultiRoute mrGGB = new MultiRoute(Arrays.asList(mrAD, srDE));

    @Test
    void multiRouteThrowsOnEmptyList() {
        assertThrows(IllegalArgumentException.class, () -> new MultiRoute(new ArrayList<>()));
    }

    @Test
    void segmentsImmutabilityTest() {
        List<Route> mutableList = new ArrayList<>();
        mutableList.add(segment1_1);
        mutableList.add(segment1_2);
        MultiRoute mr = new MultiRoute(mutableList);
        PointCh fixedPoint = mr.pointAt(2500);
        mutableList.add(segment1_3); // should not change the list in the MultiRoute!
        TestUtils.assertEqualsPointCh(fixedPoint, mr.pointAt(2500), 0);
    }

    static Stream<Arguments> indexOfSegmentAtTest() {
        return Stream.of(Arguments.of(route, -100, 0), Arguments.of(route, 0, 0),
                Arguments.of(route, 100, 0), Arguments.of(route, 1100, 1),
                Arguments.of(route, 2999.99, 2), Arguments.of(route, 3000.01, 3),
                Arguments.of(route, 4050, 4), Arguments.of(route, 5500, 5),
                Arguments.of(route, 6600, 5), Arguments.of(mr1, -100, 0), Arguments.of(mr1, 0, 0),
                Arguments.of(mr1, 100, 0), Arguments.of(mr1, 401, 0), Arguments.of(mr1, 601, 0),
                Arguments.of(mr1, 1001, 1), Arguments.of(mr1, 1500, 1), Arguments.of(mr1, 1990, 1),
                Arguments.of(mr1, 2000, 2), Arguments.of(mr1, 2010, 2), Arguments.of(mr1, 2500, 2),
                Arguments.of(mr1, 2990, 2), Arguments.of(mr1, 3000, 2), Arguments.of(mr2, -100, 0),
                Arguments.of(mr2, 0, 0), Arguments.of(mr2, 100.5543, 0),
                Arguments.of(mr2, 400.001, 0), Arguments.of(mr2, 599.999, 0),
                Arguments.of(mr2, 1000.001, 1), Arguments.of(mr2, 1500.24124, 1),
                Arguments.of(mr2, 1999.999, 1), Arguments.of(mr2, 2000.001, 2),
                Arguments.of(mr2, 3000, 2), Arguments.of(mrOneSegment, -100, 0),
                Arguments.of(mrOneSegment, 0, 0), Arguments.of(mrOneSegment, 100, 0),
                Arguments.of(mrOneSegment, 252.252, 0), Arguments.of(mrOneSegment, 300, 0),
                Arguments.of(mrSingleAndMulti, -100, 0), Arguments.of(mrSingleAndMulti, 0, 0),
                Arguments.of(mrSingleAndMulti, 1100, 1), Arguments.of(mrSingleAndMulti, 2100, 2),
                Arguments.of(mrSingleAndMulti, 3100, 3));
    }

    @ParameterizedTest
    @MethodSource
    void indexOfSegmentAtTest(MultiRoute mr, double position, int expected) {
        assertEquals(expected, mr.indexOfSegmentAt(position));
    }

    static Stream<Arguments> lengthTest() {
        return Stream.of(Arguments.of(mr1, 3000), Arguments.of(mr2, 3000),
                Arguments.of(route, 6000), Arguments.of(mrOneSegment, 252.252),
                Arguments.of(mrSingleAndMulti, 4000));
    }

    @ParameterizedTest
    @MethodSource
    void lengthTest(MultiRoute mr, double expected) {
        assertEquals(expected, mr.length());
    }

    @Test
    void edgesImmutabilityTest() {
        List<Edge> mutableList = new ArrayList<>();
        mutableList.add(edge1);
        mutableList.add(edge2);
        SingleRoute sr = new SingleRoute(mutableList);
        MultiRoute mr = new MultiRoute(Arrays.asList(sr));
        List<Edge> expected = List.copyOf(mutableList);
        mutableList.add(edge3); // should not change the list in the MultiRoute!
        assertEquals(expected, mr.edges());
    }

    static Stream<Arguments> edgesTest() {
        return Stream.of(Arguments.of(mr1, Arrays.asList(edge1, edge2, edge3, edge4, edge5, edge6)),
                Arguments.of(mr2, Arrays.asList(edge7, edge8, edge9, edge10, edge11, edge12)),
                Arguments.of(route,
                        Arrays.asList(edge1, edge2, edge3, edge4, edge5, edge6, edge7, edge8, edge9,
                                edge10, edge11, edge12)),
                Arguments.of(mrSingleAndMulti,
                        Arrays.asList(edge1, edge2, edge3, edge4, edge5, edge6, edge7, edge8)));
    }

    @ParameterizedTest
    @MethodSource
    void edgesTest(MultiRoute mr, List<Edge> expected) {
        assertEquals(expected, mr.edges());
    }

    static Stream<Arguments> pointsTest() {
        return Stream.of(
                Arguments.of(mr1,
                        Arrays.asList(point1, point2, point3, point4, point5, point6, point7)),
                Arguments.of(mr2,
                        Arrays.asList(point7, point8, point9, point10, point11, point12, point13)),
                Arguments.of(route,
                        Arrays.asList(point1, point2, point3, point4, point5, point6, point7,
                                point8, point9, point10, point11, point12, point13)),
                Arguments.of(mrOneSegment, Arrays.asList(point1, point2)),
                Arguments.of(mrLoop, Arrays.asList(point1, point2, point1)),
                Arguments.of(mrSingleAndMulti, Arrays.asList(point1, point2, point3, point4, point5,
                        point6, point7, point8, point9)));
    }

    @ParameterizedTest
    @MethodSource
    void pointsTest(MultiRoute mr, List<PointCh> expected) {
        assertEquals(expected, mr.points());
    }

    @Test
    void pointsImmutabilityTest() {
        List<PointCh> mutableList = new ArrayList<>();
        mutableList.add(point1);
        mutableList.add(point2);
        Edge e = new Edge(0, 1, point1, point2, 100, Functions.constant(0));
        SingleRoute sr = new SingleRoute(Arrays.asList(e));
        MultiRoute mr = new MultiRoute(Arrays.asList(sr));
        List<PointCh> expected = List.copyOf(mutableList);
        mutableList.add(point3); // should not change the list in the MultiRoute!
        assertEquals(expected, mr.points());
    }

    static Stream<Arguments> pointAtTest() {
        return Stream.of(Arguments.of(route, -100, point1), Arguments.of(route, 0, point1),
                Arguments.of(route, 400, point2), Arguments.of(route, 1000, point3),
                Arguments.of(route, 1600, point4), Arguments.of(route, 2000, point5),
                Arguments.of(route, 2600, point6), Arguments.of(route, 3000, point7),
                Arguments.of(route, 3400, point8), Arguments.of(route, 4000, point9),
                Arguments.of(route, 4600, point10), Arguments.of(route, 5000, point11),
                Arguments.of(route, 5600, point12), Arguments.of(route, 6000, point13),
                Arguments.of(route, 8000, point13), Arguments.of(mr2, -0.65345, point7),
                Arguments.of(mrGGB, 1.1933124755,
                        new PointCh(SwissBounds.MIN_E + 0.7765974649,
                                    SwissBounds.MIN_N + 0.9060303757)),
                Arguments.of(mrGGB, 2.2973600701,
                        new PointCh(SwissBounds.MIN_E + 1.1437563034,
                                    SwissBounds.MIN_N + 1.8499495727)),
                Arguments.of(mrGGB, 4.1842474334, pointQ),
                Arguments.of(mrGGB, 7.0753569809, pointR),
                Arguments.of(mrGGB, 10.147065226, pointS));
    }

    @ParameterizedTest
    @MethodSource
    void pointAtTest(MultiRoute mr, double position, PointCh expected) {
        double DELTA = 0;
        TestUtils.assertEqualsPointCh(expected, mr.pointAt(position), DELTA);
    }

    static Stream<Arguments> elevationAtTest() {
        return Stream.of(Arguments.of(route, -100, 1), Arguments.of(route, 0, 1),
                Arguments.of(route, 200, 1), Arguments.of(route, 800, 2),
                Arguments.of(route, 1200, 3), Arguments.of(route, 1800, 4),
                Arguments.of(route, 2200, 5), Arguments.of(route, 2800, 6),
                Arguments.of(route, 3200, 7), Arguments.of(route, 3800, 8),
                Arguments.of(route, 4200, 9), Arguments.of(route, 4800, 10),
                Arguments.of(route, 5200, 11), Arguments.of(route, 5800, 12),
                Arguments.of(route, 8000, 12), Arguments.of(mrOneSegment, -100, 100),
                Arguments.of(mrOneSegment, 0, 100), Arguments.of(mrOneSegment, 100, 100),
                Arguments.of(mrOneSegment, 252.252, 100), Arguments.of(mrOneSegment, 300, 100));
    }

    @ParameterizedTest
    @MethodSource
    void elevationAtTest(MultiRoute mr, double position, double expected) {
        assertEquals(expected, mr.elevationAt(position));
    }

    static Stream<Arguments> nodeClosestToTest() {
        return Stream.of(Arguments.of(-10, 1), Arguments.of(0, 1), Arguments.of(10, 1),
                Arguments.of(200 - 0.1, 1), Arguments.of(200 + 0.1, 2), Arguments.of(400, 2),
                Arguments.of(1000 - 0.1, 3), Arguments.of(1000 + 0.1, 3), Arguments.of(2500, 6),
                Arguments.of(8000, 13));
    }

    @ParameterizedTest
    @MethodSource
    void nodeClosestToTest(double position, int expected) {
        assertEquals(expected, route.nodeClosestTo(position));
    }

    @Test
    void pointClosestToTest() {
        double DELTA = 1e-7;
        RoutePoint expected = new RoutePoint(pointQ, 4.1842474334, 0.4909128458);
        TestUtils.assertEqualsRoutePoint(
                expected, mrGGB.pointClosestTo(new PointCh(SwissBounds.MIN_E + 1.8618216304,
                                                           SwissBounds.MIN_N + 2.8324339741)),
                DELTA);
        expected = new RoutePoint(pointR, 7.0753569809, 0.790569415);
        TestUtils.assertEqualsRoutePoint(expected,
                mrGGB.pointClosestTo(new PointCh(SwissBounds.MIN_E + 4, SwissBounds.MIN_N + 1.5)),
                DELTA);
        expected = new RoutePoint(pointS, 10.147065226, 1.5);
        TestUtils.assertEqualsRoutePoint(expected,
                mrGGB.pointClosestTo(new PointCh(SwissBounds.MIN_E + 5.5, SwissBounds.MIN_N + 4.7)),
                DELTA);
        expected = new RoutePoint(pointE, mrGGB.length(), 1.5652475842);
        TestUtils.assertEqualsRoutePoint(expected,
                mrGGB.pointClosestTo(new PointCh(SwissBounds.MIN_E + 3.3, SwissBounds.MIN_N + 7.4)),
                DELTA);
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

    private static Edge horizontalEdge1K(int i) {
        var j = i + 1;
        var pI = new PointCh(2_600_000 + 1000 * i, 1_200_000);
        var pJ = new PointCh(2_600_000 + 1000 * j, 1_200_000);
        return new Edge(i, j, pI, pJ, 1000, x -> 500);
    }

    @Test
    void multiRouteConstructorThrowsOnEmptyEdgeList() {
        assertThrows(IllegalArgumentException.class, () -> {
            new MultiRoute(List.of());
        });
    }

    @Test
    void multiRouteIndexOfSegmentAtWorksWithShallowRoutes() {
        var m = new MultiRoute(List.of(new SingleRoute(List.of(horizontalEdge1K(0))),
                new SingleRoute(List.of(horizontalEdge1K(1))),
                new SingleRoute(List.of(horizontalEdge1K(2))),
                new SingleRoute(List.of(horizontalEdge1K(3))),
                new SingleRoute(List.of(horizontalEdge1K(4))),
                new SingleRoute(List.of(horizontalEdge1K(5)))));
        for (int i = 0; i < 6; i += 1)
            assertEquals(i, m.indexOfSegmentAt((i + 0.5) * 1000));
        assertEquals(5, m.indexOfSegmentAt(10000));
    }

    @Test
    void multiRouteIndexOfSegmentAtWorksWithDeepRoutes() {
        var m1 = new MultiRoute(List.of(new SingleRoute(List.of(horizontalEdge1K(0))),
                new SingleRoute(List.of(horizontalEdge1K(1))),
                new SingleRoute(List.of(horizontalEdge1K(2)))));
        var m2 = new MultiRoute(List.of(new SingleRoute(List.of(horizontalEdge1K(3))),
                new MultiRoute(List.of(new SingleRoute(List.of(horizontalEdge1K(4))),
                        new SingleRoute(List.of(horizontalEdge1K(5)))))));
        var m = new MultiRoute(List.of(m1, m2));
        for (int i = 0; i < 6; i += 1)
            assertEquals(i, m.indexOfSegmentAt((i + 0.5) * 1000));
        assertEquals(5, m.indexOfSegmentAt(10000));
    }

    @Test
    void multiRouteLengthReturnsTotalLength() {
        for (int i = 1; i < 10; i += 1) {
            var routes = new ArrayList<Route>();
            for (var edge : verticalEdges(i))
                routes.add(new SingleRoute(List.of(edge)));
            var route = new MultiRoute(routes);
            assertEquals(i * EDGE_LENGTH, route.length());
        }
    }

    @Test
    void multiRouteRoutesAreCopiedToEnsureImmutability() {
        var immutableRoutes = List.<Route>of(new SingleRoute(verticalEdges(10)));
        var mutableRoutes = new ArrayList<>(immutableRoutes);
        var route = new MultiRoute(mutableRoutes);
        mutableRoutes.clear();
        assertNotEquals(0, route.length());
    }

    @Test
    void multiRouteEdgesAreNotModifiableFromOutside() {
        var edgesCount = 5;
        var route = new MultiRoute(List.of(new SingleRoute(verticalEdges(edgesCount))));
        try {
            route.edges().clear();
        } catch (UnsupportedOperationException e) {
            // Nothing to do (the list of points is not modifiable, which is fine).
        }
        assertEquals(edgesCount, route.edges().size());
    }

    @Test
    void multiRoutePointsAreNotModifiableFromOutside() {
        var edgesCount = 5;
        var route = new MultiRoute(List.of(new SingleRoute(verticalEdges(edgesCount))));
        try {
            route.points().clear();
        } catch (UnsupportedOperationException e) {
            // Nothing to do (the list of points is not modifiable, which is fine).
        }
        assertEquals(edgesCount + 1, route.points().size());
    }

    @Test
    void multiRoutePointsAreCorrect() {
        for (int edgesCount = 1; edgesCount < 10; edgesCount += 1) {
            var edges = verticalEdges(edgesCount);
            var routes = new ArrayList<Route>();
            for (var edge : edges)
                routes.add(new SingleRoute(List.of(edge)));
            var route = new MultiRoute(routes);
            var points = route.points();
            assertEquals(edgesCount + 1, points.size());
            assertEquals(edges.get(0).fromPoint(), points.get(0));
            for (int i = 1; i < points.size(); i += 1)
                assertEquals(edges.get(i - 1).toPoint(), points.get(i));
        }
    }

    @Test
    void multiRoutePointAtWorks() {
        var edgesCount = 12;
        var edges = sawToothEdges(edgesCount);
        var route = new MultiRoute(List.of(new SingleRoute(edges.subList(0, 4)),
                new SingleRoute(edges.subList(4, 8)), new SingleRoute(edges.subList(8, 12))));

        // Outside the range of the route
        assertEquals(sawToothPoint(0), route.pointAt(Math.nextDown(0)));
        assertEquals(sawToothPoint(edgesCount),
                route.pointAt(Math.nextUp(edgesCount * TOOTH_LENGTH)));

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
    void multiRouteElevationAtWorks() {
        var edgesCount = 12;
        var edges = sawToothEdges(edgesCount);
        var route = new MultiRoute(List.of(new SingleRoute(edges.subList(0, 4)),
                new SingleRoute(edges.subList(4, 8)), new SingleRoute(edges.subList(8, 12))));

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
    void multiRouteNodeClosestToWorks() {
        var edgesCount = 12;
        var edges = sawToothEdges(edgesCount);
        var route = new MultiRoute(List.of(new SingleRoute(edges.subList(0, 4)),
                new SingleRoute(edges.subList(4, 8)), new SingleRoute(edges.subList(8, 12))));

        for (int i = 0; i <= edgesCount; i += 1) {
            for (double p = -0.25; p <= 0.25; p += 0.25) {
                var pos = (i + p) * TOOTH_LENGTH;
                assertEquals(i, route.nodeClosestTo(pos));
            }
        }
    }

    @Test
    void multiRoutePointClosestToWorksWithFarAwayPoints() {
        var rng = newRandom();

        var edgesCount = 12;
        var edges = verticalEdges(edgesCount);
        var route = new MultiRoute(List.of(new SingleRoute(edges.subList(0, 4)),
                new SingleRoute(edges.subList(4, 8)), new SingleRoute(edges.subList(8, 12))));

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
        var end = new PointCh(ORIGIN_E, ORIGIN_N + edgesCount * EDGE_LENGTH);
        for (int i = 0; i < RANDOM_ITERATIONS; i += 1) {
            var dN = rng.nextDouble(1, 10_000);
            var dE = rng.nextDouble(-1000, 1000);
            var p = new PointCh(ORIGIN_E + dE, ORIGIN_N + edgesCount * EDGE_LENGTH + dN);
            var pct = route.pointClosestTo(p);
            assertEquals(end, pct.point());
            assertEquals(edgesCount * EDGE_LENGTH, pct.position());
            assertEquals(Math.hypot(dE, dN), pct.distanceToReference(), 1e-4);
        }
    }

    @Test
    void multiRoutePointClosestToWorksWithPointsOnRoute() {
        var rng = newRandom();

        var edgesCount = 12;
        var edges = verticalEdges(edgesCount);
        var route = new MultiRoute(List.of(new SingleRoute(edges.subList(0, 4)),
                new SingleRoute(edges.subList(4, 8)), new SingleRoute(edges.subList(8, 12))));

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
    void multiRoutePointClosestToWorksWithSawtoothPoints() {
        var edgesCount = 12;
        var edges = sawToothEdges(edgesCount);
        var route = new MultiRoute(List.of(new SingleRoute(edges.subList(0, 4)),
                new SingleRoute(edges.subList(4, 8)), new SingleRoute(edges.subList(8, 12))));

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

package ch.epfl.javelo.routing;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import java.util.ArrayList;
import java.util.Arrays;
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
    private final static PointCh pointF = new PointCh(SwissBounds.MIN_E + 1.2,
            SwissBounds.MIN_N + 1.4);
    private static final PointCh pointQ = new PointCh(SwissBounds.MIN_E + 1.5146938281,
            SwissBounds.MIN_N + 2.4853061719);
    private static final PointCh pointR = new PointCh(SwissBounds.MIN_E + 3.25,
            SwissBounds.MIN_N + 1.75);
    private static final PointCh pointS = new PointCh(SwissBounds.MIN_E + 4,
            SwissBounds.MIN_N + 4.7);

    private final static Edge edge1 = new Edge(1, 2, point1, point2, 400, Functions.constant(1));
    private final static Edge edge2 = new Edge(2, 3, point2, point3, 600, Functions.constant(2));
    private final static Edge edge3 = new Edge(3, 4, point3, point4, 600, Functions.constant(3));
    private final static Edge edge4 = new Edge(4, 5, point4, point5, 400, Functions.constant(4));
    private final static Edge edge5 = new Edge(5, 6, point5, point6, 600, Functions.constant(5));
    private final static Edge edge6 = new Edge(6, 7, point6, point7, 400, Functions.constant(6));
    private final static Edge edge7 = new Edge(7, 8, point7, point8, 400, Functions.constant(7));
    private final static Edge edge8 = new Edge(8, 9, point8, point9, 600, Functions.constant(8));
    private final static Edge edge9 = new Edge(9, 10, point9, point10, 600, Functions.constant(9));
    private final static Edge edge10 = new Edge(10, 11, point10, point11, 400,
            Functions.constant(10));
    private final static Edge edge11 = new Edge(11, 12, point11, point12, 600,
            Functions.constant(11));
    private final static Edge edge12 = new Edge(12, 13, point12, point13, 400,
            Functions.constant(12));
    private final static Edge edgeBC = new Edge(2, 3, pointB, pointC, 2.8284271247,
            Functions.constant(0));
    private final static Edge edgeCD = new Edge(3, 4, pointC, pointD, 3.1622776602,
            Functions.constant(0));
    private final static Edge edgeDE = new Edge(4, 5, pointD, pointE, 2, Functions.constant(0));
    private final static Edge edgeAF = new Edge(1, 6, pointA, pointF, 1.8439088915,
            Functions.constant(0));
    private final static Edge edgeFB = new Edge(6, 2, pointF, pointB, 1.6124515497,
            Functions.constant(0));

    // two edges, length = 400 + 600
    private final static SingleRoute segment1_1 = new SingleRoute(Arrays.asList(edge1, edge2));
    // two edges, length = 600 + 400
    private final static SingleRoute segment1_2 = new SingleRoute(Arrays.asList(edge3, edge4));
    // two edges, length = 400 + 600
    private final static SingleRoute segment1_3 = new SingleRoute(Arrays.asList(edge5, edge6));

    private final static MultiRoute mr1 = new MultiRoute(
            Arrays.asList(segment1_1, segment1_2, segment1_3));

    // two edges, length = 400 + 600
    private final static SingleRoute segment2_1 = new SingleRoute(Arrays.asList(edge7, edge8));
    // two edges, length = 600 + 400
    private final static SingleRoute segment2_2 = new SingleRoute(Arrays.asList(edge9, edge10));
    // two edges, length = 600 + 400
    private final static SingleRoute segment2_3 = new SingleRoute(Arrays.asList(edge11, edge12));

    private final static MultiRoute mr2 = new MultiRoute(
            Arrays.asList(segment2_1, segment2_2, segment2_3));

    private final static MultiRoute route = new MultiRoute(Arrays.asList(mr1, mr2));

    // One edge, length = 252.252
    private final static Edge oneEdge = new Edge(1, 2, point1, point2, 252.252,
            Functions.constant(100));
    private final static SingleRoute srOneSegment = new SingleRoute(Arrays.asList(oneEdge));
    private final static MultiRoute mrOneSegment = new MultiRoute(Arrays.asList(srOneSegment));

    // Route making a loop
    private final static Edge loopEdge1 = new Edge(1, 2, point1, point2, 300,
            Functions.constant(0));
    private final static Edge loopEdge2 = new Edge(2, 1, point2, point1, 300,
            Functions.constant(0));
    private final static SingleRoute srLoop = new SingleRoute(Arrays.asList(loopEdge1, loopEdge2));
    private final static MultiRoute mrLoop = new MultiRoute(Arrays.asList(srLoop));

    // MultiRoute containing a MultiRoute and a SingleRoute
    private final static MultiRoute mrSingleAndMulti = new MultiRoute(
            Arrays.asList(mr1, segment2_1));

    // Geogebra routes
    private final static SingleRoute srAB = new SingleRoute(Arrays.asList(edgeAF, edgeFB));
    private final static SingleRoute srBC = new SingleRoute(Arrays.asList(edgeBC));
    private final static SingleRoute srCD = new SingleRoute(Arrays.asList(edgeCD));
    private final static SingleRoute srDE = new SingleRoute(Arrays.asList(edgeDE));

    private final static MultiRoute mrAD = new MultiRoute(Arrays.asList(srAB, srBC, srCD));
    private final static MultiRoute mrGGB = new MultiRoute(Arrays.asList(mrAD, srDE));

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
                Arguments.of(mrLoop, Arrays.asList(point1, point2, point1)), // FIXME
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
        TestUtils.assertEqualsRoutePoint(expected, mrGGB.pointClosestTo(
                new PointCh(SwissBounds.MIN_E + 1.8618216304, SwissBounds.MIN_N + 2.8324339741)),
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

}

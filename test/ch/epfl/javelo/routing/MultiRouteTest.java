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
import org.junit.jupiter.params.provider.ValueSource;
import ch.epfl.javelo.Functions;
import ch.epfl.javelo.projection.PointCh;
import ch.epfl.javelo.projection.SwissBounds;

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
    private final static Edge edge1 = new Edge(1, 2, point1, point2, 400, Functions.constant(1));
    private final static Edge edge2 = new Edge(2, 3, point2, point3, 600, Functions.constant(2));
    private final static Edge edge3 = new Edge(3, 4, point3, point4, 400, Functions.constant(3));
    private final static Edge edge4 = new Edge(4, 5, point4, point5, 600, Functions.constant(4));
    private final static Edge edge5 = new Edge(5, 6, point5, point6, 400, Functions.constant(5));
    private final static Edge edge6 = new Edge(6, 7, point6, point7, 600, Functions.constant(6));
    private final static Edge edge7 = new Edge(7, 8, point7, point8, 400, Functions.constant(7));
    private final static Edge edge8 = new Edge(8, 9, point8, point9, 600, Functions.constant(8));
    private final static Edge edge9 = new Edge(9, 10, point9, point10, 400, Functions.constant(9));
    private final static Edge edge10 = new Edge(10, 11, point10, point11, 600,
            Functions.constant(10));
    private final static Edge edge11 = new Edge(11, 12, point11, point12, 400,
            Functions.constant(11));
    private final static Edge edge12 = new Edge(12, 13, point12, point13, 600,
            Functions.constant(12));

    // two edges, length = 600 + 400
    private final static SingleRoute segment1_1 = new SingleRoute(Arrays.asList(edge1, edge2));
    // two edges, length = 600 + 400
    private final static SingleRoute segment1_2 = new SingleRoute(Arrays.asList(edge3, edge4));
    // two edges, length = 600 + 400
    private final static SingleRoute segment1_3 = new SingleRoute(Arrays.asList(edge5, edge6));

    private final static MultiRoute mr1 = new MultiRoute(
            Arrays.asList(segment1_1, segment1_2, segment1_3));

    // two edges, length = 600 + 400
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

    @Test
    void multiRouteThrowsOnEmptyList() {
        assertThrows(IllegalArgumentException.class, () -> new MultiRoute(new ArrayList<>()));
    }

    static Stream<Arguments> indexOfSegmentAtTest() {
        return Stream.of(Arguments.of(-100, 0), Arguments.of(0, 0), Arguments.of(100, 0),
                Arguments.of(1100, 1), Arguments.of(2999.99, 2), Arguments.of(3000.01, 3),
                Arguments.of(4050, 4), Arguments.of(5500, 5), Arguments.of(6600, 5));
    }

    @ParameterizedTest
    @MethodSource
    void indexOfSegmentAtTest(double position, int expected) {
        assertEquals(expected, route.indexOfSegmentAt(position));
    }

    @ParameterizedTest
    @ValueSource(doubles = {-100, 0, 1, 100, 252.252, 300})
    void indexOfSegmentAtOneSegmentTest(double position) {
        assertEquals(0, mrOneSegment.indexOfSegmentAt(position));
    }

    static Stream<Arguments> lengthTest() {
        return Stream.of(Arguments.of(mr1, 3000), Arguments.of(mr2, 3000),
                Arguments.of(route, 6000), Arguments.of(mrOneSegment, 252.252));
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
                Arguments.of(route, Arrays.asList(edge1, edge2, edge3, edge4, edge5, edge6, edge7,
                        edge8, edge9, edge10, edge11, edge12)));
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
                Arguments.of(mrOneSegment, Arrays.asList(point1, point2)));
    }

    @ParameterizedTest
    @MethodSource
    void pointsTest(MultiRoute mr, List<PointCh> expected) {
        assertEquals(expected, mr.points());
    }

    static Stream<Arguments> pointAtTest() {
        return Stream.of(Arguments.of(route, -100, point1), Arguments.of(route, 0, point1),
                Arguments.of(route, 400, point2), Arguments.of(route, 1000, point3),
                Arguments.of(route, 1400, point4), Arguments.of(route, 2000, point5),
                Arguments.of(route, 2400, point6), Arguments.of(route, 3000, point7),
                Arguments.of(route, 3400, point8), Arguments.of(route, 4000, point9),
                Arguments.of(route, 4400, point10), Arguments.of(route, 5000, point11),
                Arguments.of(route, 5400, point12), Arguments.of(route, 6000, point13),
                Arguments.of(route, 8000, point13));
    }

    @ParameterizedTest
    @MethodSource
    void pointAtTest(MultiRoute mr, double position, PointCh expected) {
        // TODO: test with points anywhere on the route
        assertEquals(expected, mr.pointAt(position));
    }

    static Stream<Arguments> elevationAtTest() {
        return Stream.of(Arguments.of(-100, 1), Arguments.of(0, 1), Arguments.of(200, 1),
                Arguments.of(800, 2), Arguments.of(1200, 3), Arguments.of(1800, 4),
                Arguments.of(2200, 5), Arguments.of(2800, 6), Arguments.of(3200, 7),
                Arguments.of(3800, 8), Arguments.of(4200, 9), Arguments.of(4800, 10),
                Arguments.of(5200, 11), Arguments.of(5800, 12), Arguments.of(8000, 12));
    }

    @ParameterizedTest
    @MethodSource
    void elevationAtTest(double position, double expected) {
        assertEquals(expected, route.elevationAt(position));
    }

    @ParameterizedTest
    @ValueSource(doubles = {-100, 0, 1, 100, 252.252, 300})
    void elevationAtOneSegmentTest(double position) {
        assertEquals(100, mrOneSegment.elevationAt(position));
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

    }

}

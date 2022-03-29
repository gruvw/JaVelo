package ch.epfl.javelo.routing;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;
import org.junit.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
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
    private final static Edge edge1 = new Edge(0, 1, point1, point2, 400, Functions.constant(1));
    private final static Edge edge2 = new Edge(2, 3, point2, point3, 600, Functions.constant(2));
    private final static Edge edge3 = new Edge(4, 5, point3, point4, 400, Functions.constant(3));
    private final static Edge edge4 = new Edge(6, 7, point4, point5, 600, Functions.constant(4));
    private final static Edge edge5 = new Edge(8, 9, point5, point6, 400, Functions.constant(5));
    private final static Edge edge6 = new Edge(10, 11, point6, point7, 600, Functions.constant(6));
    private final static Edge edge7 = new Edge(12, 13, point7, point8, 400, Functions.constant(7));

    private final static List<Edge> edges12 = Arrays.asList(new Edge[] {edge1, edge2});
    private final static List<Edge> edges23 = Arrays.asList(new Edge[] {edge2, edge3});
    private final static List<Edge> edges34 = Arrays.asList(new Edge[] {edge3, edge4});
    private final static List<Edge> edges45 = Arrays.asList(new Edge[] {edge4, edge5});
    private final static List<Edge> edges56 = Arrays.asList(new Edge[] {edge5, edge6});
    private final static List<Edge> edges67 = Arrays.asList(new Edge[] {edge6, edge7});

    SingleRoute segment1_1 = new SingleRoute(edges12); // two edges, length = 400 + 600
    SingleRoute segment1_2 = new SingleRoute(edges23); // two edges, length = 600 + 400
    SingleRoute segment1_3 = new SingleRoute(edges34); // two edges, length = 400 + 600
    MultiRoute mr1 = new MultiRoute(Arrays.asList(segment1_1, segment1_2, segment1_3));

    SingleRoute segment2_1 = new SingleRoute(edges45); // two edges, length = 600 + 400
    SingleRoute segment2_2 = new SingleRoute(edges56); // two edges, length = 400 + 600
    SingleRoute segment2_3 = new SingleRoute(edges67); // two edges, length = 600 + 400
    MultiRoute mr2 = new MultiRoute(Arrays.asList(segment2_1, segment2_2, segment2_3));

    MultiRoute route = new MultiRoute(Arrays.asList(mr1, mr2));

    @Test
    void multiRouteThrowsOnEmptyList() {
        assertThrows(IllegalArgumentException.class, () -> new MultiRoute(new ArrayList<>()));
    }

    static Stream<Arguments> indexOfSegmentAtTest() {
        return Stream.of(Arguments.of(-100, 0), Arguments.of(0, 0), Arguments.of(100, 0),
                Arguments.of(1100, 1), Arguments.of(5500, 5));
    }

    @ParameterizedTest
    @MethodSource
    void indexOfSegmentAtTest(double position, int expected) {
        assertEquals(expected, route.indexOfSegmentAt(position));
    }

    static Stream<Arguments> lengthTest() {
        return Stream.of();
    }

    @ParameterizedTest
    @MethodSource
    void lengthTest(MultiRoute mr, double expected) {}

    @Test
    void edgesImmutabilityTest() {

    }

    static Stream<Arguments> edgesTest() {
        return Stream.of();
    }

    @ParameterizedTest
    @MethodSource
    void edgesTest(MultiRoute sr, List<Edge> expected) {}

    static Stream<Arguments> pointsTest() {
        return Stream.of();
    }

    @ParameterizedTest
    @MethodSource
    void pointsTest(MultiRoute sr, List<PointCh> expected) {}

    static Stream<Arguments> pointAtTest() {
        return Stream.of();
    }

    @ParameterizedTest
    @MethodSource
    void pointAtTest(MultiRoute sr, double position, PointCh expected) {}

    @Test
    void elevationAtFullTest() {

    }

    static Stream<Arguments> elevationAtTest() {
        return Stream.of();
    }

    @ParameterizedTest
    @MethodSource
    void elevationAtTest(double position, double expected) {

    }

    static Stream<Arguments> nodeClosestToTest() {
        return Stream.of();
    }

    @ParameterizedTest
    @MethodSource
    void nodeClosestToTest(double position, int expected) {

    }

    @Test
    void pointClosestToTest() {

    }

}

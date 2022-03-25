package ch.epfl.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import java.util.function.DoubleUnaryOperator;
import ch.epfl.javelo.projection.PointCh;
import ch.epfl.javelo.routing.Edge;
import ch.epfl.javelo.routing.ElevationProfile;
import ch.epfl.javelo.routing.RoutePoint;

public final class TestUtils {

    public static void assertEqualsPointCh(PointCh expected, PointCh actual, double delta) {
        assertEquals(expected.e(), actual.e(), delta);
        assertEquals(expected.n(), actual.n(), delta);
    }

    public static void assertEqualsEdge(Edge expected, Edge actual, double delta) {
        assertEquals(expected.fromNodeId(), actual.fromNodeId());
        assertEquals(expected.toNodeId(), actual.toNodeId());
        assertEqualsPointCh(expected.fromPoint(), actual.fromPoint(), delta);
        assertEqualsPointCh(expected.toPoint(), actual.toPoint(), delta);
        assertEquals(expected.length(), actual.length());
        assertEqualsProfile(expected.profile(), actual.profile());
    }

    public static void assertEqualsProfile(DoubleUnaryOperator expected,
                                           DoubleUnaryOperator actual) {
        for (double i = -5000; i < 5000; i += 0.3) {
            assertEquals(expected.applyAsDouble(i), actual.applyAsDouble(i), "Position: " + i);
        }
    }

    public static void assertEqualsElevationProfile(ElevationProfile expected,
                                                    ElevationProfile actual) {
        assertEquals(expected.length(), actual.length());
        for (double i = -5000; i < 5000; i += 0.3) {
            assertEquals(expected.elevationAt(i), actual.elevationAt(i), "Position: " + i);
        }
    }

    public static void assertEqualsRoutePoint(RoutePoint expected, RoutePoint actual,
                                              double delta) {
        assertEqualsPointCh(expected.point(), actual.point(), delta);
        assertEquals(expected.position(), actual.position(), delta);
        assertEquals(expected.distanceToReference(), actual.distanceToReference(), delta);
    }

}

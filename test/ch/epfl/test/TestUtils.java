package ch.epfl.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import java.util.function.DoubleUnaryOperator;
import ch.epfl.javelo.projection.PointCh;
import ch.epfl.javelo.routing.Edge;

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
        // FIXME: how to compare two profiles?
        for (int i = -5000; i < 5000; i++) {
            assertEquals(expected.applyAsDouble(i), actual.applyAsDouble(i));
        }
    }

}

package ch.epfl.javelo.routing;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.util.random.RandomGenerator;
import org.junit.jupiter.api.Test;
import ch.epfl.javelo.projection.PointCh;
import ch.epfl.javelo.projection.SwissBounds;
import ch.epfl.test.TestRandomizer;
import static ch.epfl.test.TestRandomizer.RANDOM_ITERATIONS;

public class RoutePointTest {

    @Test
    void NoneRoutePointTest() {
        assertEquals(new RoutePoint(null, Double.NaN, Double.POSITIVE_INFINITY), RoutePoint.NONE);
    }

    @Test
    void withPositionShiftedByTest() {
        PointCh point = new PointCh(SwissBounds.MIN_E + 100, SwissBounds.MIN_N + 100);
        RoutePoint rp = new RoutePoint(point, 100, 1000);
        assertEquals(new RoutePoint(point, 120, 1000), rp.withPositionShiftedBy(20));
        assertEquals(rp, rp.withPositionShiftedBy(0));
        assertEquals(new RoutePoint(point, 80, 1000), rp.withPositionShiftedBy(-20));
    }

    @Test
    void minWithRoutePointTest() {
        PointCh point = new PointCh(SwissBounds.MIN_E + 100, SwissBounds.MIN_N + 100);
        RoutePoint rpClose = new RoutePoint(point, 100, 1000);
        RoutePoint rpFar = new RoutePoint(point, 100, 1200);
        RoutePoint rpEqual = new RoutePoint(point, 100, 1000);
        assertEquals(rpClose, rpClose.min(rpFar));
        assertEquals(rpClose, rpFar.min(rpClose));
        assertEquals(rpClose, rpClose.min(rpEqual));
        assertEquals(rpEqual, rpEqual.min(rpClose));
    }

    @Test
    void minWithPointChTest() {
        PointCh point = new PointCh(SwissBounds.MIN_E + 100, SwissBounds.MIN_N + 100);
        PointCh thatPoint = new PointCh(SwissBounds.MIN_E + 200, SwissBounds.MIN_N + 200);
        RoutePoint rpClose = new RoutePoint(point, 100, 1000);
        assertEquals(rpClose, rpClose.min(thatPoint, 100, 1020));
        assertEquals(new RoutePoint(thatPoint, 100, 800), rpClose.min(thatPoint, 100, 800));
        assertEquals(rpClose, rpClose.min(thatPoint, 100, 1000));
    }

    // == GIVEN TESTS ==
    @Test
    void routePointNoneIsDefinedCorrectly() {
        assertNull(RoutePoint.NONE.point());
        assertTrue(Double.isNaN(RoutePoint.NONE.position()));
        assertEquals(Double.POSITIVE_INFINITY, RoutePoint.NONE.distanceToReference());
    }

    @Test
    void routePointWithPositionShiftedShiftsPositionAndNothingElse() {
        var rng = TestRandomizer.newRandom();
        for (int i = 0; i < RANDOM_ITERATIONS; i += 1) {
            var e = 2_600_000 + rng.nextDouble(-50_000, 50_000);
            var n = 1_200_000 + rng.nextDouble(-50_000, 50_000);
            var pointCh = new PointCh(e, n);
            var position = rng.nextDouble(0, 200_000);
            var distanceToReference = rng.nextDouble(0, 1_000);
            var routePoint = new RoutePoint(pointCh, position, distanceToReference);
            var positionShift = rng.nextDouble(-position, 200_000);
            var routePointShifted = routePoint.withPositionShiftedBy(positionShift);
            assertEquals(pointCh, routePointShifted.point());
            assertEquals(distanceToReference, routePointShifted.distanceToReference());
            assertEquals(position + positionShift, routePointShifted.position());
        }
    }

    @Test
    void routePointMin1Works() {
        var rng = TestRandomizer.newRandom();
        for (int i = 0; i < RANDOM_ITERATIONS; i += 1) {
            var point1 = randomRoutePoint(rng);
            var point2 = randomRoutePoint(rng);
            if (point1.distanceToReference() < point2.distanceToReference()) {
                assertEquals(point1, point1.min(point2));
                assertEquals(point1, point2.min(point1));
            } else if (point2.distanceToReference() < point1.distanceToReference()) {
                assertEquals(point2, point1.min(point2));
                assertEquals(point2, point2.min(point1));
            }
        }
    }

    @Test
    void routePointMin2Works() {
        var rng = TestRandomizer.newRandom();
        for (int i = 0; i < RANDOM_ITERATIONS; i += 1) {
            var point1 = randomRoutePoint(rng);
            var point2 = randomRoutePoint(rng);
            if (point1.distanceToReference() < point2.distanceToReference()) {
                assertEquals(point1, point1.min(point2.point(), point2.position(),
                        point2.distanceToReference()));
                assertNotEquals(point2, point2.min(point1.point(), point1.position(),
                        point1.distanceToReference()));
            } else if (point2.distanceToReference() < point1.distanceToReference()) {
                assertEquals(point2, point1.min(point2.point(), point2.position(),
                        point2.distanceToReference()));
                assertNotEquals(point1, point2.min(point1.point(), point1.position(),
                        point2.distanceToReference()));
            }
        }
    }

    private RoutePoint randomRoutePoint(RandomGenerator rng) {
        var e = 2_600_000 + rng.nextDouble(-50_000, 50_000);
        var n = 1_200_000 + rng.nextDouble(-50_000, 50_000);
        var pointCh = new PointCh(e, n);
        var position = rng.nextDouble(0, 200_000);
        var distanceToReference = rng.nextDouble(0, 1_000);
        return new RoutePoint(pointCh, position, distanceToReference);
    }

}

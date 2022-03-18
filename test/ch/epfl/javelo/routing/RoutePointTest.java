package ch.epfl.javelo.routing;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;
import ch.epfl.javelo.projection.PointCh;
import ch.epfl.javelo.projection.SwissBounds;

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

}

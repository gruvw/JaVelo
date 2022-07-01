package ch.epfl.javelo.routing;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import ch.epfl.javelo.data.Graph;
import ch.epfl.javelo.projection.PointCh;

public class RouteComputerTest {

    @Test
    void bestRouteThrowsTest() throws IOException {
        Graph gLausanne = Graph.loadFrom(Path.of(".javelo/lausanne"));
        CostFunction cfLausanne = new CityBikeCF(gLausanne);
        RouteComputer rcLausanne = new RouteComputer(gLausanne, cfLausanne);
        assertThrows(IllegalArgumentException.class,
                () -> rcLausanne.bestRouteBetween(159049, 159049));
    }

    @Test
    void bestRouteBetweenLausanneTest() throws IOException {
        Graph gLausanne = Graph.loadFrom(Path.of(".javelo/lausanne"));
        CostFunction cfLausanne = new CityBikeCF(gLausanne);
        RouteComputer rcLausanne = new RouteComputer(gLausanne, cfLausanne);
        Route r = rcLausanne.bestRouteBetween(159049, 117669);
        assertEquals(9588.5625, r.length());
        assertEquals(new PointCh(2532691.010670732, 1152252.1768292682), r.pointAt(100));
        assertEquals(new PointCh(2533448.8003640776, 1152191.2233009709), r.pointAt(1000));
        assertEquals(new PointCh(2538383.744573955, 1154088.4417202573), r.pointAt(9000));
        assertEquals(new PointCh(2536630.838708212, 1151761.614871003), r.pointAt(4750));
        assertEquals(new PointCh(2536040.3336431226, 1151826.1785548327), r.pointAt(4000));
        assertEquals(new PointCh(2536660.6114569316, 1151958.6052321668), r.pointAt(5000));
        assertEquals(new PointCh(2535742.7392907306, 1152108.738676264), r.pointAt(3500));
    }

    @Test
    void bestRouteBetweenChWestTest() throws IOException {
        Graph g_west = Graph.loadFrom(Path.of(".javelo/ch_west"));
        CostFunction cf_west = new CityBikeCF(g_west);
        RouteComputer rc_west = new RouteComputer(g_west, cf_west);
        Route r = rc_west.bestRouteBetween(2046055, 2694240);
        assertEquals(168_378.875, r.length());
        assertEquals(new PointCh(2537812.9302884615, 1152134.2367788462), r.pointAt(100));
        assertEquals(new PointCh(2537531.2835365855, 1152754.4420731708), r.pointAt(1000));
        assertEquals(new PointCh(2536345.7342741936, 1159954.4012096773), r.pointAt(10000));
        assertEquals(new PointCh(2562083.7156791906, 1222320.3082851637), r.pointAt(100000));
        assertEquals(new PointCh(2576785.442122496, 1250048.1664098613), r.pointAt(150000));
        assertEquals(new PointCh(2556152.3921755725, 1212822.4666030535), r.pointAt(84000));
        assertEquals(new PointCh(2556623.0351688103, 1211030.8110932475), r.pointAt(80000));
        assertEquals(new PointCh(2556457.848880597, 1216673.6389925373), r.pointAt(90000));
        assertEquals(new PointCh(2555899.0558873722, 1207753.799274744), r.pointAt(75000));
        assertEquals(new PointCh(2559366.699598735, 1219108.3736624513), r.pointAt(95000));
    }

    // == GIVEN TESTS ==

    private Graph graph;

    private RouteComputer newLausanneRouteComputer() {
        if (graph == null) {
            try {
                graph = Graph.loadFrom(Path.of(".javelo/lausanne"));
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
        var cf = new CityBikeCF(graph);
        return new RouteComputer(graph, cf);
    }

    @Test
    void routeComputerThrowsOnIdenticalStartAndEndNodes() {
        assertThrows(IllegalArgumentException.class, () -> {
            var rc = newLausanneRouteComputer();
            rc.bestRouteBetween(2022, 2022);
        });
    }

    @Test
    void routeComputerReturnsNullForUnreachableNodes() {
        var rc = newLausanneRouteComputer();
        assertNull(rc.bestRouteBetween(149195, 153181));
    }

    @Test
    void routeComputerComputesCorrectRouteForGivenExample() {
        var rc = newLausanneRouteComputer();
        var route = rc.bestRouteBetween(159049, 117669);
        assertNotNull(route);

        var actualLength = route.length();
        var expectedLength = 9588.5625;
        assertEquals(expectedLength, actualLength, 1);

        var actualPointsCount = route.points().size();
        var expectedPointsCount = 576;
        assertEquals(expectedPointsCount, actualPointsCount);

        var actualEdgesCount = route.edges().size();
        var expectedEdgesCount = 575;
        assertEquals(expectedEdgesCount, actualEdgesCount);

        var actualPointAt8k = route.pointAt(8000);
        var actualPointAt8kE = actualPointAt8k.e();
        var actualPointAt8kN = actualPointAt8k.n();
        var expected8kE = 2538281.263888889;
        var expected8kN = 1153278.236111111;
        assertEquals(expected8kE, actualPointAt8kE, 1);
        assertEquals(expected8kN, actualPointAt8kN, 1);
    }

    @Test
    void routeComputerComputesCorrectRouteForOtherExample() {
        var rc = newLausanneRouteComputer();
        var route = rc.bestRouteBetween(210641, 43713);
        assertNotNull(route);

        var actualLength = route.length();
        var expectedLength = 38612.75;
        assertEquals(expectedLength, actualLength, 1);

        var actualPointsCount = route.points().size();
        var expectedPointsCount = 1590;
        assertEquals(expectedPointsCount, actualPointsCount);

        var actualEdgesCount = route.edges().size();
        var expectedEdgesCount = 1589;
        assertEquals(expectedEdgesCount, actualEdgesCount);

        var actualPointAt20k = route.pointAt(20000);
        var actualPointAt20kE = actualPointAt20k.e();
        var actualPointAt20kN = actualPointAt20k.n();
        var expected20kE = 2533955.1600274723;
        var expected20kN = 1153195.4842032967;
        assertEquals(expected20kE, actualPointAt20kE, 1);
        assertEquals(expected20kN, actualPointAt20kN, 1);
    }

}

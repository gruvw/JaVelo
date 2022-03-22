package ch.epfl.javelo.routing;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.io.IOException;
import java.nio.file.Path;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import ch.epfl.javelo.data.Graph;
import ch.epfl.javelo.projection.Ch1903;
import ch.epfl.javelo.projection.PointCh;
import ch.epfl.test.TestUtils;

import static ch.epfl.test.TestRandomizer.RANDOM_ITERATIONS;
import static ch.epfl.test.TestRandomizer.newRandom;

public class EdgeTest {

    private static Graph graph;

    private static PointCh pointA = new PointCh(2535259.4802113194, 1154575.2654684375);
    private static PointCh pointB = new PointCh(2546875.820309356, 1183248.8220051485);

    private static Edge edgeAB = new Edge(2022, 2023, pointA, pointB, pointA.distanceTo(pointB),
            value -> Math.pow(value, 2));;

    @BeforeAll
    static void initGlobalVars() throws IOException {
        graph = Graph.loadFrom(Path.of("lausanne"));
    }

    @Test
    void ofTest() {
        // Testing using edge id (index) 5413 -> OSM edge id 32319962[-3:-1]
        // From node OSM 7780441076 (id/index 2666) to node OSM 7780441077 (id/index 2664)
        PointCh origin = new PointCh(
                Ch1903.e(Math.toRadians(6.6214028), Math.toRadians(46.6303228)),
                Ch1903.n(Math.toRadians(6.6214028), Math.toRadians(46.6303228)));
        PointCh target = new PointCh(
                Ch1903.e(Math.toRadians(6.6236244), Math.toRadians(46.6295067)),
                Ch1903.n(Math.toRadians(6.6236244), Math.toRadians(46.6295067)));
        Edge actualEdge = Edge.of(graph, 5413, 2666, 2664);
        Edge expectedEdge = new Edge(2666, 2664, origin, target, origin.distanceTo(target),
                value -> Math.pow(value, 2));
        assertEquals(expectedEdge.fromNodeId(), actualEdge.fromNodeId());
        assertEquals(expectedEdge.toNodeId(), actualEdge.toNodeId());
        TestUtils.assertEqualsPointCh(expectedEdge.fromPoint(), actualEdge.fromPoint(), 0.0625);
        TestUtils.assertEqualsPointCh(expectedEdge.toPoint(), actualEdge.toPoint(), 0.0625);
        assertEquals(expectedEdge.length(), actualEdge.length(), 0.07);
    }

    @Test
    void positionClosestTo() {
        PointCh pointK = new PointCh(2570518.33, 1165805.41);
        PointCh pointE = new PointCh(2548088.39918, 1120969.9258);
        PointCh pointF = new PointCh(2536323.21548, 1205539.46409);
        assertEquals(0, edgeAB.positionClosestTo(pointA));
        assertEquals(edgeAB.length(), edgeAB.positionClosestTo(pointB), 1e-7);
        assertEquals(23647.47, edgeAB.positionClosestTo(pointK), 1e-2);
        assertEquals(-26329.43, edgeAB.positionClosestTo(pointE), 1e-2);
        assertEquals(47634.57, edgeAB.positionClosestTo(pointF), 1e-2);
    }

    @Test
    void pointAtTest() {
        PointCh pointI = new PointCh(2635259.48, 1154575.27);
        Edge edgeFlat = new Edge(2022, 2023, pointA, pointI, pointA.distanceTo(pointI),
                graph.edgeProfile(0));
        PointCh pointC = new PointCh(2566842.8, 1154575.27);
        TestUtils.assertEqualsPointCh(pointC, edgeFlat.pointAt(31583.32), 1e-2);
        PointCh pointD = new PointCh(2544138.65, 1176492.45);
        PointCh pointH = new PointCh(2525373.28, 1130172.36);
        PointCh pointG = new PointCh(2553145.35, 1198724.42);
        TestUtils.assertEqualsPointCh(pointD, edgeAB.pointAt(23647.47), 1e-2);
        TestUtils.assertEqualsPointCh(pointH, edgeAB.pointAt(-26329.43), 1e-2);
        TestUtils.assertEqualsPointCh(pointG, edgeAB.pointAt(47634.57), 1e-2);
    }

    @Test
    void elevationAtTest() {
        for (int i = -1000; i < 1000; i++) {
            assertEquals(Math.pow(i, 2), edgeAB.elevationAt(i));
        }
    }

    // == GIVEN TESTS ==

    private static final double DELTA = 1e-7;

    @Test
    void edgeOfWorksOnLausanneData() throws IOException {
        var graph = Graph.loadFrom(Path.of("lausanne"));

        var edge = Edge.of(graph, 4095, 2022, 2021);

        var actual1 = edge.fromPoint().e();
        var expected1 = 2535880.25;
        assertEquals(expected1, actual1);

        var actual2 = edge.fromPoint().n();
        var expected2 = 1164939.125;
        assertEquals(expected2, actual2);

        var actual3 = edge.toPoint().e();
        var expected3 = 2535904.5625;
        assertEquals(expected3, actual3);

        var actual4 = edge.toPoint().n();
        var expected4 = 1164911.3125;
        assertEquals(expected4, actual4);

        var actual5 = edge.length();
        var expected5 = 36.9375;
        assertEquals(expected5, actual5);
    }

    @Test
    void edgePositionClosestToWorksOnKnownValues() {
        var p1 = new PointCh(2600123, 1200456);
        var p2 = new PointCh(2600456, 1200789);
        var p3 = new PointCh(2600789, 1200123);
        var p4 = new PointCh(2601000, 1201000);

        var edge1 = new Edge(1, 2, p1, p2, p1.distanceTo(p2), x -> Double.NaN);
        var edge2 = new Edge(3, 4, p3, p4, p3.distanceTo(p4), x -> Double.NaN);

        var actual1 = edge1.positionClosestTo(p3);
        var expected1 = 235.46655813512032;
        assertEquals(expected1, actual1, DELTA);

        var actual2 = edge1.positionClosestTo(p4);
        var expected2 = 1004.7987360660841;
        assertEquals(expected2, actual2, DELTA);

        var actual3 = edge2.positionClosestTo(p1);
        var expected3 = 167.971970023863;
        assertEquals(expected3, actual3, DELTA);

        var actual4 = edge2.positionClosestTo(p2);
        var expected4 = 569.6280214215838;
        assertEquals(expected4, actual4, DELTA);

        var actual5 = edge2.positionClosestTo(p3);
        var expected5 = 0.0;
        assertEquals(expected5, actual5, DELTA);

        var actual6 = edge2.positionClosestTo(p4);
        var expected6 = 902.025498530945;
        assertEquals(expected6, actual6, DELTA);
    }

    @Test
    void edgePointAtWorksBeyondOfEdgeLength() {
        var rng = newRandom();

        // Horizontal edges
        for (int i = 0; i < RANDOM_ITERATIONS; i += 1) {
            var e1 = rng.nextDouble(2_500_000, 2_600_000);
            var e2 = rng.nextDouble(2_600_000, 2_800_000);
            var n1 = rng.nextDouble(1_100_000, 1_200_000);

            var fPH = new PointCh(e1, n1);
            var tPH = new PointCh(e2, n1);
            var edgeLength = e2 - e1;
            var edge = new Edge(0, 1, fPH, tPH, edgeLength, d -> 100);

            assertEquals(new PointCh(e1 - 1, n1), edge.pointAt(-1));
            assertEquals(new PointCh(e2 + 1, n1), edge.pointAt(edgeLength + 1));
        }

        // Vertical edges
        for (int i = 0; i < RANDOM_ITERATIONS; i += 1) {
            var e1 = rng.nextDouble(2_500_000, 2_600_000);
            var e2 = rng.nextDouble(2_600_000, 2_800_000);
            var n1 = rng.nextDouble(1_100_000, 1_200_000);

            var fPH = new PointCh(e1, n1);
            var tPH = new PointCh(e2, n1);
            var edgeLength = e2 - e1;
            var edge = new Edge(0, 1, fPH, tPH, edgeLength, d -> 100);

            assertEquals(new PointCh(e1 - 1, n1), edge.pointAt(-1));
            assertEquals(new PointCh(e2 + 1, n1), edge.pointAt(edgeLength + 1));
        }
    }

    @Test
    void edgePointAtWorksWithEndAndMiddlePoints() {
        var rng = newRandom();
        for (int i = 0; i < RANDOM_ITERATIONS; i += 1) {
            var dE = rng.nextDouble(-50_000, +50_000);
            var dN = rng.nextDouble(-50_000, +50_000);

            var mP = new PointCh(2_600_000, 1_200_000);
            var fP = new PointCh(mP.e() + dE, mP.n() + dN);
            var tP = new PointCh(mP.e() - dE, mP.n() - dN);
            var edgeLength = Math.hypot(dE, dN);
            var edge = new Edge(0, 1, fP, tP, edgeLength, d -> Double.NaN);
            assertEquals(fP, edge.pointAt(0));
            assertEquals(mP, edge.pointAt(Math.scalb(edgeLength, -1)));
            assertEquals(tP, edge.pointAt(edgeLength));
        }
    }

    @Test
    void edgeElevationAtWorksWithConstantProfile() {
        var fP = new PointCh(2_600_000, 1_200_000);
        var tP = new PointCh(2_600_001, 1_200_001);
        var edge = new Edge(0, 1, fP, tP, Math.sqrt(2), d -> Double.NaN);
        for (double x = -20; x <= 20; x += 1)
            assertTrue(Double.isNaN(edge.elevationAt(x)));
    }

    @Test
    void edgeElevationAtWorksWithWavyProfile() {
        var fP = new PointCh(2_600_000, 1_200_000);
        var tP = new PointCh(2_600_001, 1_200_001);
        var edge = new Edge(0, 1, fP, tP, Math.sqrt(2), Math::sin);
        for (double x = -20; x <= 20; x += 1)
            assertEquals(Math.sin(x), edge.elevationAt(x));
    }

}

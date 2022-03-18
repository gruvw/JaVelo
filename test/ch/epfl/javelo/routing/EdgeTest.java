package ch.epfl.javelo.routing;

import static org.junit.jupiter.api.Assertions.assertEquals;
import java.io.IOException;
import java.nio.file.Path;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import ch.epfl.javelo.data.Graph;
import ch.epfl.javelo.projection.Ch1903;
import ch.epfl.javelo.projection.PointCh;
import ch.epfl.test.TestUtils;

public class EdgeTest {

    private static Graph graph;

    private static PointCh pointA = new PointCh(2535259.4802113194, 1154575.2654684375);
    private static PointCh pointB = new PointCh(2546875.820309356, 1183248.8220051485);

    private static Edge edgeAB = new Edge(2022, 2023, pointA, pointB, pointA.distanceTo(pointB),
            value -> Math.pow(value, 2));;

    @BeforeAll
    static void initGlobalVars() throws IOException {
        graph = Graph.loadFrom(Path.of("lausanne"));
        System.out.println(graph.edgeTargetNodeId(5413));

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

}

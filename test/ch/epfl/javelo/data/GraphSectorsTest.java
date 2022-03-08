package ch.epfl.javelo.data;

import static org.junit.Assert.assertEquals;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import ch.epfl.javelo.projection.PointCh;
import ch.epfl.javelo.projection.SwissBounds;

class GraphSectorsTest {

    private static GraphSectors gs;

    @BeforeAll
    static void initGlobalVars() {
        ByteBuffer b = ByteBuffer.allocate((Integer.SIZE + Short.SIZE) * 128 * 128);
        for (int i = 0; i < 128 * 128; i++) {
            b.putInt(i * (Integer.BYTES + Short.BYTES), i);
            b.putShort(4 + i * (Integer.BYTES + Short.BYTES), (short) 1);
        }
        gs = new GraphSectors(b.asReadOnlyBuffer());
    }

    @Test
    void sectorsInAreaTest() {
        PointCh bottomLeft = new PointCh(SwissBounds.MIN_E, SwissBounds.MIN_N);
        PointCh topRight = new PointCh(SwissBounds.MAX_E, SwissBounds.MAX_N);
        PointCh topLeft = new PointCh(SwissBounds.MIN_E, SwissBounds.MAX_N);
        PointCh pointOnS1Border = new PointCh(2726.5625 + SwissBounds.MIN_E,
                1726.5625 + SwissBounds.MIN_N);
        PointCh pointUnderS1Border = new PointCh(2726.5625 + SwissBounds.MIN_E - 1e-8,
                1726.5625 + SwissBounds.MIN_N - 1e-8);
        PointCh pointAboveS1 = new PointCh(2726.5625 + SwissBounds.MIN_E + 10,
                1726.5625 + SwissBounds.MIN_N + 10);
        PointCh pointCloseToBottomLeft = new PointCh(SwissBounds.MIN_E + 1, SwissBounds.MIN_N + 1);
        GraphSectors.Sector s0 = new GraphSectors.Sector(0, 1);
        GraphSectors.Sector s1 = new GraphSectors.Sector(1, 2);
        GraphSectors.Sector s128 = new GraphSectors.Sector(128, 129);
        GraphSectors.Sector s129 = new GraphSectors.Sector(129, 130);
        GraphSectors.Sector sLast = new GraphSectors.Sector(128 * 128 - 1, 128 * 128);
        // Bottom left
        List<GraphSectors.Sector> expected = new ArrayList<>();
        expected.add(s0);
        List<GraphSectors.Sector> actual = gs.sectorsInArea(bottomLeft, 0);
        assertEquals(expected, actual);
        // Top right
        expected = new ArrayList<>();
        expected.add(sLast);
        actual = gs.sectorsInArea(topRight, 0);
        assertEquals(expected, actual);
        // Point on S1 border
        expected = new ArrayList<>();
        expected.add(s129);
        actual = gs.sectorsInArea(pointOnS1Border, 0);
        assertEquals(expected, actual);
        // Point under S1 border
        expected = new ArrayList<>();
        expected.add(s0);
        actual = gs.sectorsInArea(pointUnderS1Border, 0);
        assertEquals(expected, actual);
        // Point close to bottom left
        expected = new ArrayList<>();
        expected.add(s0);
        actual = gs.sectorsInArea(pointCloseToBottomLeft, 0);
        assertEquals(expected, actual);
        // Point above S1
        expected = new ArrayList<>();
        expected.add(s0);
        expected.add(s1);
        expected.add(s128);
        expected.add(s129);
        actual = gs.sectorsInArea(pointAboveS1, 15);
        assertEquals(expected, actual);

        // TEST if clamp is needed -> test to right with distance
        // All sectors from bottom left
        actual = gs.sectorsInArea(bottomLeft, 1000000000);

        // All sectors from top right
        actual = gs.sectorsInArea(topRight, 1000000000);

        // Point on top left
        actual = gs.sectorsInArea(topLeft, 0);

    }

}

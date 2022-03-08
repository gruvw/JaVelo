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
        PointCh middle = new PointCh((SwissBounds.MAX_E + SwissBounds.MIN_E) / 2. -2, (SwissBounds.MAX_N + SwissBounds.MIN_N) / 2. - 2);
        GraphSectors.Sector s0 = new GraphSectors.Sector(0, 1);
        GraphSectors.Sector s1 = new GraphSectors.Sector(1, 2);
        GraphSectors.Sector s8192 = new GraphSectors.Sector(8192,8193);
        GraphSectors.Sector s128 = new GraphSectors.Sector(127, 128);
        GraphSectors.Sector sLast = new GraphSectors.Sector(128 * 128 - 1, 128 * 128);
        // Bottom left
        List<GraphSectors.Sector> expected = new ArrayList<>();
        expected.add(s0);
        List<GraphSectors.Sector> actual = gs.sectorsInArea(bottomLeft, 0);
        // Top right
        // expected = new ArrayList<>();
        // expected.add(sLast);
        // actual = gs.sectorsInArea(topRight, 0);
        // assertEquals(expected, actual);
        // Middle
        expected = new ArrayList<>();
        expected.add(s8192);
        actual = gs.sectorsInArea(middle, 1);
        assertEquals(expected, actual);
    }

}

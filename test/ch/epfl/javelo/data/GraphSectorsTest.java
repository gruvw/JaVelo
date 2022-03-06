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
        ByteBuffer b = ByteBuffer.allocate(12);
        b.putInt(0, 1); // first node id of the sector 0
        b.putShort(4, (short) 2); // number of nodes in the sector 0
        b.putInt(6, 5); // first node id of the sector 1
        b.putShort(10, (short) 3); // number of nodes in the sector 1
        gs = new GraphSectors(b.asReadOnlyBuffer());
    }

    @Test
    void sectorsInAreaTest() {
        PointCh bottomLeft = new PointCh(SwissBounds.MIN_E, SwissBounds.MIN_N);
        GraphSectors.Sector s0 = new GraphSectors.Sector(1, 3);
        GraphSectors.Sector s1 = new GraphSectors.Sector(3, 6);
        List<GraphSectors.Sector> expected = new ArrayList<>();
        expected.add(s0);
        List<GraphSectors.Sector> actual = gs.sectorsInArea(bottomLeft, 1);
        assertEquals(expected, actual);
        actual = gs.sectorsInArea(bottomLeft, SwissBounds.WIDTH);
        expected.add(s1);
        assertEquals(expected, actual);
    }

}

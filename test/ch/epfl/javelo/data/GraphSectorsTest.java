package ch.epfl.javelo.data;

import static org.junit.Assert.assertEquals;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import ch.epfl.javelo.projection.PointCh;
import ch.epfl.javelo.projection.SwissBounds;

class GraphSectorsTest {

    private static GraphSectors gs;
    private static List<GraphSectors.Sector> sectors = new ArrayList<>();

    private final static double SECTOR_WIDTH = 2726.5625;
    private final static double SECTOR_HEIGHT = 1726.5625;

    private final static PointCh bottomLeft = new PointCh(SwissBounds.MIN_E, SwissBounds.MIN_N);
    private final static PointCh bottomRight = new PointCh(SwissBounds.MAX_E, SwissBounds.MIN_N);
    private final static PointCh topRight = new PointCh(SwissBounds.MAX_E, SwissBounds.MAX_N);
    private final static PointCh topLeft = new PointCh(SwissBounds.MIN_E, SwissBounds.MAX_N);
    private final static PointCh pointOnS1Border = new PointCh(SECTOR_WIDTH + SwissBounds.MIN_E,
            SECTOR_HEIGHT + SwissBounds.MIN_N);
    private final static PointCh pointUnderS1Border = new PointCh(
            SECTOR_WIDTH + SwissBounds.MIN_E - 1e-8, SECTOR_HEIGHT + SwissBounds.MIN_N - 1e-8);
    private final static PointCh pointCloseToBottomLeft = new PointCh(SwissBounds.MIN_E + 1,
            SwissBounds.MIN_N + 1);
    private final static PointCh pointAboveS1 = new PointCh(SECTOR_WIDTH + SwissBounds.MIN_E + 10,
            SECTOR_HEIGHT + SwissBounds.MIN_N + 10);
    private final static PointCh middle = new PointCh((SwissBounds.MIN_E + SwissBounds.MAX_E) / 2,
            (SwissBounds.MIN_N + SwissBounds.MAX_N) / 2);
    private final static PointCh specPoint2 = new PointCh(
            123 * SECTOR_HEIGHT + SwissBounds.MIN_E + 200,
            15 * SECTOR_WIDTH + SwissBounds.MIN_N + 20);

    private final static int SMALL_DISTANCE = 30;
    private final static int INTERMEDIATE_DISTANCE = 3000;
    private final static double ALL_SECTORS_DISTANCE = SwissBounds.WIDTH;

    @BeforeAll
    static void initGlobalVars() {
        ByteBuffer b = ByteBuffer.allocate((Integer.SIZE + Short.SIZE) * 128 * 128);
        for (int i = 0; i < 128 * 128; i++) {
            b.putInt(i * (Integer.BYTES + Short.BYTES), i);
            b.putShort(4 + i * (Integer.BYTES + Short.BYTES), (short) 1);
            sectors.add(new GraphSectors.Sector(i, i + 1));
        }
        gs = new GraphSectors(b.asReadOnlyBuffer());
        sectors = Collections.unmodifiableList(sectors);
    }

    @Test
    void sectorsInAreaBottomLeftTest() {
        // Distance of 0
        List<GraphSectors.Sector> expected = new ArrayList<>();
        expected.add(sectors.get(0));
        List<GraphSectors.Sector> actual = gs.sectorsInArea(bottomLeft, 0);
        assertEquals(expected, actual);
        // Small distance
        actual = gs.sectorsInArea(bottomLeft, SMALL_DISTANCE);
        assertEquals(expected, actual);
        // Intermediate distance
        expected.add(sectors.get(1));
        expected.add(sectors.get(128));
        expected.add(sectors.get(129));
        actual = gs.sectorsInArea(bottomLeft, INTERMEDIATE_DISTANCE);
        assertEquals(expected, actual);
    }

    @Test
    void sectorsInAreaBottomRightTest() {
        // Distance of 0
        List<GraphSectors.Sector> expected = new ArrayList<>();
        expected.add(sectors.get(127));
        List<GraphSectors.Sector> actual = gs.sectorsInArea(bottomRight, 0);
        assertEquals(expected, actual);
        // Small distance
        actual = gs.sectorsInArea(bottomRight, SMALL_DISTANCE);
        assertEquals(expected, actual);
        // Intermediate distance
        expected.clear();
        expected.add(sectors.get(126));
        expected.add(sectors.get(127));
        expected.add(sectors.get(254));
        expected.add(sectors.get(255));
        actual = gs.sectorsInArea(bottomRight, INTERMEDIATE_DISTANCE);
    }

    @Test
    void sectorsInAreaTopRightTest() {
        // Distance of 0
        List<GraphSectors.Sector> expected = new ArrayList<>();
        expected.add(sectors.get(sectors.size() - 1));
        List<GraphSectors.Sector> actual = gs.sectorsInArea(topRight, 0);
        assertEquals(expected, actual);
        // Small distance
        actual = gs.sectorsInArea(topRight, SMALL_DISTANCE);
        assertEquals(expected, actual);
        // Intermediate distance
        expected.clear();
        expected.add(sectors.get(sectors.size() - 130));
        expected.add(sectors.get(sectors.size() - 129));
        expected.add(sectors.get(sectors.size() - 2));
        expected.add(sectors.get(sectors.size() - 1));
        actual = gs.sectorsInArea(topRight, INTERMEDIATE_DISTANCE);
        assertEquals(expected, actual);
    }

    @Test
    void sectorsInAreaTopLeftTest() {
        // Distance of 0
        List<GraphSectors.Sector> expected = new ArrayList<>();
        expected.add(sectors.get(sectors.size() - 128));
        List<GraphSectors.Sector> actual = gs.sectorsInArea(topLeft, 0);
        assertEquals(expected, actual);
        // Small distance
        actual = gs.sectorsInArea(topLeft, SMALL_DISTANCE);
        assertEquals(expected, actual);
        // Intermediate distance
        expected.clear();
        expected.add(sectors.get(sectors.size() - 256));
        expected.add(sectors.get(sectors.size() - 255));
        expected.add(sectors.get(sectors.size() - 128));
        expected.add(sectors.get(sectors.size() - 127));
        actual = gs.sectorsInArea(topLeft, INTERMEDIATE_DISTANCE);
        assertEquals(expected, actual);
    }

    @Test
    void sectorsInAreaS1BorderTest() {
        // Distance of 0
        List<GraphSectors.Sector> expected = new ArrayList<>();
        expected.add(sectors.get(129));
        List<GraphSectors.Sector> actual = gs.sectorsInArea(pointOnS1Border, 0);
        assertEquals(expected, actual);
        // Small distance
        expected.clear();
        expected.add(sectors.get(0));
        expected.add(sectors.get(1));
        expected.add(sectors.get(128));
        expected.add(sectors.get(129));
        actual = gs.sectorsInArea(pointOnS1Border, SMALL_DISTANCE);
        assertEquals(expected, actual);
        // Intermediate distance
        expected.clear();
        expected.add(sectors.get(0));
        expected.add(sectors.get(1));
        expected.add(sectors.get(2));
        expected.add(sectors.get(128));
        expected.add(sectors.get(129));
        expected.add(sectors.get(130));
        expected.add(sectors.get(256));
        expected.add(sectors.get(257));
        expected.add(sectors.get(258));
        actual = gs.sectorsInArea(pointOnS1Border, INTERMEDIATE_DISTANCE);
        assertEquals(expected, actual);
    }

    @Test
    void sectorsInAreaUnderS1BorderTest() {
        // Distance of 0
        List<GraphSectors.Sector> expected = new ArrayList<>();
        expected.add(sectors.get(0));
        List<GraphSectors.Sector> actual = gs.sectorsInArea(pointUnderS1Border, 0);
        assertEquals(expected, actual);
        // Small distance
        expected.clear();
        expected.add(sectors.get(0));
        expected.add(sectors.get(1));
        expected.add(sectors.get(128));
        expected.add(sectors.get(129));
        actual = gs.sectorsInArea(pointUnderS1Border, SMALL_DISTANCE);
        assertEquals(expected, actual);
        // Intermediate distance
        expected.clear();
        expected.add(sectors.get(0));
        expected.add(sectors.get(1));
        expected.add(sectors.get(2));
        expected.add(sectors.get(128));
        expected.add(sectors.get(129));
        expected.add(sectors.get(130));
        expected.add(sectors.get(256));
        expected.add(sectors.get(257));
        expected.add(sectors.get(258));
        actual = gs.sectorsInArea(pointUnderS1Border, INTERMEDIATE_DISTANCE);
        assertEquals(expected, actual);
    }

    @Test
    void sectorsInAreaCloseToBottomLeftTest() {
        // Distance of 0
        List<GraphSectors.Sector> expected = new ArrayList<>();
        expected.add(sectors.get(0));
        List<GraphSectors.Sector> actual = gs.sectorsInArea(pointCloseToBottomLeft, 0);
        assertEquals(expected, actual);
        // Small distance
        actual = gs.sectorsInArea(pointCloseToBottomLeft, SMALL_DISTANCE);
        assertEquals(expected, actual);
        // Intermediate distance
        expected.add(sectors.get(1));
        expected.add(sectors.get(128));
        expected.add(sectors.get(129));
        actual = gs.sectorsInArea(pointCloseToBottomLeft, INTERMEDIATE_DISTANCE);
        assertEquals(expected, actual);
    }

    @Test
    void sectorsInAreaAboveS1Test() {
        // Distance of 0
        List<GraphSectors.Sector> expected = new ArrayList<>();
        expected.add(sectors.get(129));
        List<GraphSectors.Sector> actual = gs.sectorsInArea(pointAboveS1, 0);
        assertEquals(expected, actual);
        // Small distance
        expected.clear();
        expected.add(sectors.get(0));
        expected.add(sectors.get(1));
        expected.add(sectors.get(128));
        expected.add(sectors.get(129));
        actual = gs.sectorsInArea(pointAboveS1, SMALL_DISTANCE);
        assertEquals(expected, actual);
        // Intermediate distance
        expected.clear();
        expected.add(sectors.get(0));
        expected.add(sectors.get(1));
        expected.add(sectors.get(2));
        expected.add(sectors.get(128));
        expected.add(sectors.get(129));
        expected.add(sectors.get(130));
        expected.add(sectors.get(256));
        expected.add(sectors.get(257));
        expected.add(sectors.get(258));
        actual = gs.sectorsInArea(pointAboveS1, INTERMEDIATE_DISTANCE);
        assertEquals(expected, actual);
    }

    @Test
    void sectorsInAreaMiddleTest() {
        // Distance of 0
        List<GraphSectors.Sector> expected = new ArrayList<>();
        expected.add(sectors.get(64 + 64 * 128));
        List<GraphSectors.Sector> actual = gs.sectorsInArea(middle, 0);
        assertEquals(expected, actual);
        // Small distance
        expected.clear();
        expected.add(sectors.get(63 + 63 * 128));
        expected.add(sectors.get(64 + 63 * 128));
        expected.add(sectors.get(63 + 64 * 128));
        expected.add(sectors.get(64 + 64 * 128));
        actual = gs.sectorsInArea(middle, SMALL_DISTANCE);
        assertEquals(expected, actual);
        // Intermediate distance
        expected.clear();
        expected.add(sectors.get(62 + 62 * 128));
        expected.add(sectors.get(63 + 62 * 128));
        expected.add(sectors.get(64 + 62 * 128));
        expected.add(sectors.get(65 + 62 * 128));
        expected.add(sectors.get(62 + 63 * 128));
        expected.add(sectors.get(63 + 63 * 128));
        expected.add(sectors.get(64 + 63 * 128));
        expected.add(sectors.get(65 + 63 * 128));
        expected.add(sectors.get(62 + 64 * 128));
        expected.add(sectors.get(63 + 64 * 128));
        expected.add(sectors.get(64 + 64 * 128));
        expected.add(sectors.get(65 + 64 * 128));
        expected.add(sectors.get(62 + 65 * 128));
        expected.add(sectors.get(63 + 65 * 128));
        expected.add(sectors.get(64 + 65 * 128));
        expected.add(sectors.get(65 + 65 * 128));

        actual = gs.sectorsInArea(middle, INTERMEDIATE_DISTANCE);
        assertEquals(expected, actual);
    }

    @Test
    void sectorsInAreaSpecificPoint1Test() {
        PointCh specPoint1 = new PointCh(123 * SECTOR_WIDTH + SwissBounds.MIN_E + 100,
                15 * SECTOR_HEIGHT + SwissBounds.MIN_N + 100);
        // Distance of 0
        List<GraphSectors.Sector> expected = new ArrayList<>();
        expected.add(sectors.get(123 + 15 * 128));
        List<GraphSectors.Sector> actual = gs.sectorsInArea(specPoint1, 0);
        assertEquals(expected, actual);
        // Small distance
        actual = gs.sectorsInArea(specPoint1, SMALL_DISTANCE);
        assertEquals(expected, actual);
        // Intermediate distance
        expected.clear();
        expected.add(sectors.get(121 + 13 * 128));
        expected.add(sectors.get(122 + 13 * 128));
        expected.add(sectors.get(123 + 13 * 128));
        expected.add(sectors.get(124 + 13 * 128));
        expected.add(sectors.get(121 + 14 * 128));
        expected.add(sectors.get(122 + 14 * 128));
        expected.add(sectors.get(123 + 14 * 128));
        expected.add(sectors.get(124 + 14 * 128));
        expected.add(sectors.get(121 + 15 * 128));
        expected.add(sectors.get(122 + 15 * 128));
        expected.add(sectors.get(123 + 15 * 128));
        expected.add(sectors.get(124 + 15 * 128));
        expected.add(sectors.get(121 + 16 * 128));
        expected.add(sectors.get(122 + 16 * 128));
        expected.add(sectors.get(123 + 16 * 128));
        expected.add(sectors.get(124 + 16 * 128));

        actual = gs.sectorsInArea(specPoint1, INTERMEDIATE_DISTANCE);
        assertEquals(expected, actual);
    }

    @Test
    void sectorsInAreaAllSectorsTest() {
        // Bottom left
        List<GraphSectors.Sector> actual = gs.sectorsInArea(bottomLeft, ALL_SECTORS_DISTANCE);
        assertEquals(sectors, actual);
        // Top right
        actual = gs.sectorsInArea(topRight, ALL_SECTORS_DISTANCE);
        assertEquals(sectors, actual);
        // Middle
        actual = gs.sectorsInArea(middle, ALL_SECTORS_DISTANCE);
        assertEquals(sectors, actual);
    }

    @Test
    void sectorsInAreaSquareBorderExpansionTest() {
        double distance = SECTOR_HEIGHT / 2;
        PointCh left = new PointCh(SwissBounds.MIN_E + 3 * SECTOR_WIDTH - distance,
                SwissBounds.MIN_N + 2 * SECTOR_HEIGHT + distance);
        List<GraphSectors.Sector> expected = new ArrayList<>();
        expected.add(sectors.get(2 + 2 * 128));
        expected.add(sectors.get(2 + 2 * 128 + 1));
        expected.add(sectors.get(2 + 2 * 128 + 128));
        expected.add(sectors.get(2 + 2 * 128 + 128 + 1));
        List<GraphSectors.Sector> actual = gs.sectorsInArea(left, distance);
        assertEquals(expected, actual);
        PointCh right = new PointCh(SwissBounds.MIN_E + 2 * SECTOR_WIDTH + distance,
                SwissBounds.MIN_N + 2 * SECTOR_HEIGHT + distance);
        expected.clear();
        expected.add(sectors.get(2 + 2 * 128));
        expected.add(sectors.get(2 + 2 * 128 + 128));
        actual = gs.sectorsInArea(right, distance);
        assertEquals(expected, actual);
    }

}

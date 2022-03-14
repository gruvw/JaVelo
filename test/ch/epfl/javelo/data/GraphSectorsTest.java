package ch.epfl.javelo.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
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

    // == Given Tests ==
    private static final double SWISS_MIN_E = 2_485_000;
    private static final double SWISS_MIN_N = 1_075_000;
    private static final double SWISS_WIDTH = 349_000;
    private static final double SWISS_HEIGHT = 221_000;

    private static final int SUBDIVISIONS_PER_SIDE = 128;
    private static final int SECTORS_COUNT = SUBDIVISIONS_PER_SIDE * SUBDIVISIONS_PER_SIDE;

    private static final ByteBuffer SECTORS_BUFFER = createSectorsBuffer();

    private static ByteBuffer createSectorsBuffer() {
        ByteBuffer sectorsBuffer = ByteBuffer.allocate(
                SECTORS_COUNT * (Integer.BYTES + Short.BYTES));
        for (int i = 0; i < SECTORS_COUNT; i += 1) {
            sectorsBuffer.putInt(i);
            sectorsBuffer.putShort((short) 1);
        }
        assert !sectorsBuffer.hasRemaining();
        return sectorsBuffer.rewind().asReadOnlyBuffer();
    }

    @Test
    void graphSectorsSectorsInAreaWorksForSingleSector() {
        var graphSectors = new GraphSectors(SECTORS_BUFFER);
        for (int i = 0; i < SECTORS_COUNT; i += 1) {
            var x = i % SUBDIVISIONS_PER_SIDE;
            var y = i / SUBDIVISIONS_PER_SIDE;
            var e = SWISS_MIN_E + (x + 0.5) * SECTOR_WIDTH;
            var n = SWISS_MIN_N + (y + 0.5) * SECTOR_HEIGHT;
            var sectors = graphSectors.sectorsInArea(new PointCh(e, n), 0.49 * SECTOR_HEIGHT);
            assertEquals(List.of(new GraphSectors.Sector(i, i + 1)), sectors);
        }
    }

    @Test
    void graphSectorsSectorsInAreaWorksFor4NeighbouringSectors() {
        var graphSectors = new GraphSectors(SECTORS_BUFFER);
        for (int x = 1; x <= SUBDIVISIONS_PER_SIDE - 1; x += 1) {
            for (int y = 1; y <= SUBDIVISIONS_PER_SIDE - 1; y += 1) {
                var e = SWISS_MIN_E + x * SECTOR_WIDTH;
                var n = SWISS_MIN_N + y * SECTOR_HEIGHT;
                var p = new PointCh(e, n);
                var sectors = graphSectors.sectorsInArea(p, SECTOR_HEIGHT / 2.0);
                sectors.sort(Comparator.comparingInt(GraphSectors.Sector::startNodeId));

                var i1 = sectorIndex(x - 1, y - 1);
                var i2 = sectorIndex(x, y - 1);
                var i3 = sectorIndex(x - 1, y);
                var i4 = sectorIndex(x, y);
                var expectedSectors = List.of(new GraphSectors.Sector(i1, i1 + 1),
                        new GraphSectors.Sector(i2, i2 + 1), new GraphSectors.Sector(i3, i3 + 1),
                        new GraphSectors.Sector(i4, i4 + 1));

                assertEquals(expectedSectors, sectors);
            }
        }
    }

    @Test
    void graphSectorsSectorsInAreaWorksFor8NeighbouringSectors() {
        var graphSectors = new GraphSectors(SECTORS_BUFFER);
        for (int x = 1; x <= SUBDIVISIONS_PER_SIDE - 1; x += 1) {
            for (int y = 2; y <= SUBDIVISIONS_PER_SIDE - 2; y += 1) {
                var e = SWISS_MIN_E + x * SECTOR_WIDTH;
                var n = SWISS_MIN_N + y * SECTOR_HEIGHT;
                var p = new PointCh(e, n);
                var sectors = graphSectors.sectorsInArea(p, SECTOR_HEIGHT * 1.1);
                sectors.sort(Comparator.comparingInt(GraphSectors.Sector::startNodeId));

                var i1 = sectorIndex(x - 1, y - 2);
                var i2 = sectorIndex(x, y - 2);
                var i3 = sectorIndex(x - 1, y - 1);
                var i4 = sectorIndex(x, y - 1);
                var i5 = sectorIndex(x - 1, y);
                var i6 = sectorIndex(x, y);
                var i7 = sectorIndex(x - 1, y + 1);
                var i8 = sectorIndex(x, y + 1);
                var expectedSectors = List.of(new GraphSectors.Sector(i1, i1 + 1),
                        new GraphSectors.Sector(i2, i2 + 1), new GraphSectors.Sector(i3, i3 + 1),
                        new GraphSectors.Sector(i4, i4 + 1), new GraphSectors.Sector(i5, i5 + 1),
                        new GraphSectors.Sector(i6, i6 + 1), new GraphSectors.Sector(i7, i7 + 1),
                        new GraphSectors.Sector(i8, i8 + 1));

                assertEquals(expectedSectors, sectors);
            }
        }
    }

    private int sectorIndex(int x, int y) {
        return y * SUBDIVISIONS_PER_SIDE + x;
    }

    @Test
    void graphSectorsSectorsInAreaWorksForSectorsWithLargeNumberOfNodes() {
        ByteBuffer sectorsBuffer = ByteBuffer.allocate(
                SECTORS_COUNT * (Integer.BYTES + Short.BYTES));
        var maxSectorSize = 0xFFFF;
        for (int i = 0; i < SECTORS_COUNT; i += 1) {
            sectorsBuffer.putInt(i * maxSectorSize);
            sectorsBuffer.putShort((short) maxSectorSize);
        }
        var readOnlySectorsBuffer = sectorsBuffer.rewind().asReadOnlyBuffer();
        var graphSectors = new GraphSectors(readOnlySectorsBuffer);
        var d = 100;
        var e = SWISS_MIN_E + 2 * d;
        var n = SWISS_MIN_N + 2 * d;
        var sectors = graphSectors.sectorsInArea(new PointCh(e, n), d);
        assertEquals(List.of(new GraphSectors.Sector(0, maxSectorSize)), sectors);
    }

    @Disabled
    @Test
    void graphSectorsSectorsInAreaWorksForAllOfThem() {
        var graphSectors = new GraphSectors(SECTORS_BUFFER);
        var e = SWISS_MIN_E + 0.5 * SWISS_WIDTH;
        var n = SWISS_MIN_N + 0.5 * SWISS_HEIGHT;
        var sectors = graphSectors.sectorsInArea(new PointCh(e, n), SWISS_WIDTH);
        assertEquals(SECTORS_COUNT, sectors.size());
        BitSet expectedSectors = new BitSet();
        expectedSectors.set(0, SECTORS_COUNT);
        for (GraphSectors.Sector sector : sectors) {
            assertTrue(expectedSectors.get(sector.startNodeId()));
            expectedSectors.clear(sector.startNodeId());
        }
        assertEquals(0, expectedSectors.cardinality());
    }

}

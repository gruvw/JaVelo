package ch.epfl.javelo.projection;

import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static ch.epfl.test.TestRandomizer.*;

class PointWebMercatorTest {

    private static final double DELTA = 1e-11;

    @ParameterizedTest
    @CsvSource({"-1,0", "0,-1", "-1,-1", "1.000001,1", "1,1.000001", "1.000001,1.000001"})
    void constructorThrowsTest(double x, double y) {
        assertThrows(IllegalArgumentException.class, () -> {
            new PointWebMercator(x, y);
        });
    }

    @ParameterizedTest
    @CsvSource({"4,0,0,0,0", "0,256,256,1,1", "2,1024,1024,1,1", "1,512,256,1,0.5",
                "1,256,512,0.5,1"})
    void ofTest(int zoomLevel, double x, double y, double expectedX, double expectedY) {
        PointWebMercator actual = PointWebMercator.of(zoomLevel, x, y);
        assertEquals(expectedX, actual.x());
        assertEquals(expectedY, actual.y());
    }

    @ParameterizedTest
    @CsvSource({"0,256,257", "0,257,256", "1,513,512"})
    void ofThrowsTest(int zoomLevel, double x, double y) {
        assertThrows(IllegalArgumentException.class, () -> {
            PointWebMercator.of(zoomLevel, x, y);
        });
    }

    @ParameterizedTest
    @CsvSource({"0,0,4,0,0", "1,1,0,256,256", "1,1,2,1024,1024", "0.5,1,1,256,512",
                "1,0.5,1,512,256"})
    void atZoomLevelTest(double x, double y, int zoomLevel, double expectedXAtZoom,
                         double expectedYAtZoom) {
        PointWebMercator actual = new PointWebMercator(x, y);
        assertEquals(expectedXAtZoom, actual.xAtZoomLevel(zoomLevel));
        assertEquals(expectedYAtZoom, actual.yAtZoomLevel(zoomLevel));
    }

    @ParameterizedTest
    @CsvSource({"0.518275214444,0.353664894749,6.5790772,46.5218976", "0.5,0.5,0,0",
                "0,0,-180,85.0511287799", "1,1,180,-85.0511287799"})
    void lonLatTest(double x, double y, double expectedLon, double expectedLat) {
        PointWebMercator actual = new PointWebMercator(x, y);
        assertEquals(Math.toRadians(expectedLon), actual.lon(), DELTA);
        assertEquals(Math.toRadians(expectedLat), actual.lat(), DELTA);
    }

    @ParameterizedTest
    @CsvSource({"6.5790772,46.5218976,0.518275214444,0.353664894749"})
    void ofPointChTest(double lon, double lat, double expectedX, double expectedY) {
        lon = Math.toRadians(lon);
        lat = Math.toRadians(lat);
        double e = Ch1903.e(lon, lat), n = Ch1903.n(lon, lat);
        PointWebMercator actual = PointWebMercator.ofPointCh(new PointCh(e, n));
        assertEquals(expectedX, actual.x(), 1e-7);
        assertEquals(expectedY, actual.y(), 1e-7);
    }

    @ParameterizedTest
    @CsvSource({"0.518275214444,0.353664894749,6.5790772,46.5218976"})
    void toPointChTest(double x, double y, double lon, double lat) {
        lon = Math.toRadians(lon);
        lat = Math.toRadians(lat);
        double expectedE = Ch1903.e(lon, lat), expectedN = Ch1903.n(lon, lat);
        PointCh actual = new PointWebMercator(x, y).toPointCh();
        assertEquals(expectedE, actual.e(), 1e-4);
        assertEquals(expectedN, actual.n(), 1e-4);
    }

    @ParameterizedTest
    @CsvSource({"0,0", "1,1", "0.5,0.5", "0.11439754201,0.72832791255",
                "0.11896632209,0.68347318382", "0.1420014678,0.70445493189",
                "0.07903161271,0.70125981531"})
    void toPointChNullTest(double x, double y) {
        PointCh actual = new PointWebMercator(x, y).toPointCh();
        assertNull(actual);
    }

    // == Given Tests ==

    @Test
    void pointWebMercatorThrowsOnInvalidCoordinates() {
        assertThrows(IllegalArgumentException.class,
                () -> new PointWebMercator(Math.nextDown(0), 0.5));
        assertThrows(IllegalArgumentException.class,
                () -> new PointWebMercator(Math.nextUp(1), 0.5));
        assertThrows(IllegalArgumentException.class,
                () -> new PointWebMercator(0.5, Math.nextDown(0)));
        assertThrows(IllegalArgumentException.class,
                () -> new PointWebMercator(0.5, Math.nextUp(1)));
    }

    @Test
    void pointWebMercatorDoesNotThrowOnValidCoordinates() {
        var rng = newRandom();
        for (int i = 0; i < RANDOM_ITERATIONS; i += 1) {
            var x = rng.nextDouble();
            var y = rng.nextDouble();
            assertDoesNotThrow(() -> new PointWebMercator(x, y));
        }
        assertDoesNotThrow(() -> new PointWebMercator(0, 0));
        assertDoesNotThrow(() -> new PointWebMercator(1, 1));
    }

    @Test
    void pointWebMercatorOfAndXYAtZoomLevelAreInverse() {
        var rng = newRandom();
        for (int i = 0; i < RANDOM_ITERATIONS; i += 1) {
            var z = rng.nextInt(20);
            var maxXY = Math.scalb(1d, z + 8);
            var x = rng.nextDouble(maxXY);
            var y = rng.nextDouble(maxXY);
            var p = PointWebMercator.of(z, x, y);
            assertEquals(x, p.xAtZoomLevel(z), 1e-8);
            assertEquals(y, p.yAtZoomLevel(z), 1e-8);
        }
    }

    @ParameterizedTest
    @CsvSource({"2_600_000,1_200_000,0.5206628811728395,0.3519253787614047",
                "2_533_132,1_152_206,0.5182423951719917,0.3536813812215855"})
    void pointWebMercatorOfPointChWorksOnKnownValues(double e, double n, double expectedX,
                                                     double expectedY) {
        var p = PointWebMercator.ofPointCh(new PointCh(e, n));
        assertEquals(expectedX, p.x(), DELTA);
        assertEquals(expectedY, p.y(), DELTA);
    }

    @ParameterizedTest
    @CsvSource({"0,0,-3.141592653589793", "0.25,0.25,-1.5707963267948966", "0.5,0.5,0",
                "0.75,0.75,1.5707963267948966", "1,1,3.141592653589793"})
    void pointWebMercatorLonWorksOnKnownValues(double x, double y, double expected) {
        var actual = new PointWebMercator(x, y).lon();
        assertEquals(expected, actual, DELTA);
    }

    @ParameterizedTest
    @CsvSource({"0,0,1.4844222297453324", "0.25,0.25,1.1608753909688045", "0.5,0.5,0",
                "0.75,0.75,-1.1608753909688045", "1,1,-1.4844222297453324"})
    void pointWebMercatorLatWorksOnKnownValues(double x, double y, double expected) {
        var actual = new PointWebMercator(x, y).lat();
        assertEquals(expected, actual, DELTA);
    }

    @ParameterizedTest
    @CsvSource({"0.5206628811728395,0.3519253787614047,2600000.346333851,1199999.8308213386",
                "0.5182423951719917,0.3536813812215855,2533131.6362025095,1152206.8789113415"})
    void pointWebMercatorToPointChWorksOnKnownValues(double x, double y, double expectedE,
                                                     double expectedN) {
        var p = new PointWebMercator(x, y).toPointCh();
        assertEquals(expectedE, p.e(), DELTA);
        assertEquals(expectedN, p.n(), DELTA);
    }

}

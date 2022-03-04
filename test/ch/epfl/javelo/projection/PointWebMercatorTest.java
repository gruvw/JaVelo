package ch.epfl.javelo.projection;

import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

public class PointWebMercatorTest {

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

    public static void main(String[] args) {
        double e = Ch1903.e(6.5790772, 46.5218976), n = Ch1903.n(6.5790772, 46.5218976);
        new PointCh(e, n);
    }

}

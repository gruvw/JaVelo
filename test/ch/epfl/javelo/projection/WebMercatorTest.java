package ch.epfl.javelo.projection;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

public class WebMercatorTest {

    private static final double DELTA = 1e-11;

    // Longitude, Latitude in degrees

    @ParameterizedTest
    @CsvSource({"6.5790772,0.518275214444", "0,0.5", "-180,0", "180,1", "48.8574173,0.63571504805"})
    void xTest(double lon, double expected) {
        double actual = WebMercator.x(Math.toRadians(lon));
        assertEquals(expected, actual, DELTA);
    }

    @ParameterizedTest
    @CsvSource({"46.5218976,0.353664894749", "0,0.5", "85.0511287799,0", "-85.0511287799,1"})
    void yTest(double lat, double expected) {
        double actual = WebMercator.y(Math.toRadians(lat));
        assertEquals(expected, actual, DELTA);
    }

    @ParameterizedTest
    @CsvSource({"0.518275214444,6.5790772", "0.5,0", "0,-180", "1,180"})
    void lonTest(double x, double expected) {
        double actual = WebMercator.lon(x);
        assertEquals(Math.toRadians(expected), actual, DELTA);
    }

    @ParameterizedTest
    @CsvSource({"0.353664894749,46.5218976", "0.5,0", "0,85.0511287799", "1,-85.0511287799"})
    void latTest(double y, double expected) {
        double actual = WebMercator.lat(y);
        assertEquals(Math.toRadians(expected), actual, DELTA);
    }

}

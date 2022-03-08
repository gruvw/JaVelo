package ch.epfl.javelo.projection;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class WebMercatorTest {

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

    // == Given Test ==

    @ParameterizedTest
    @CsvSource({"-180,0", "-90,0.25", "-45,0.375", "0,0.5", "45,0.625", "90,0.75", "180,1",
                "12.3456,0.5342933333333334"})
    void webMercatorXWorksOnKnownValues(double lon, double expected) {
        var actual = WebMercator.x(Math.toRadians(lon));
        assertEquals(expected, actual, 1e-7);
    }

    @ParameterizedTest
    @CsvSource({"-85,0.9983620852139422", "-45,0.640274963084795", "0,0.5",
                "45,0.35972503691520497", "85,0.0016379147860541708",
                "12.3456,0.46543818316651964"})
    void webMercatorYWorksOnKnownValues(double lat, double expected) {
        var actual = WebMercator.y(Math.toRadians(lat));
        assertEquals(expected, actual, 1e-7);
    }

    @ParameterizedTest
    @CsvSource({"0,-3.141592653589793", "0.25,-1.5707963267948966", "0.5,0",
                "0.75,1.5707963267948966", "1,3.141592653589793", "0.123456,-2.36589572830663"})
    void webMercatorLonWorksOnKnownValues(double x, double expected) {
        var actual = WebMercator.lon(x);
        assertEquals(expected, actual, 1e-7);
    }

    @ParameterizedTest
    @CsvSource({"0,1.4844222297453324", "0.25,1.1608753909688045", "0.5,0",
                "0.75,-1.1608753909688045", "1,-1.4844222297453324", "0.123456,1.3836144040217428"})
    void webMercatorLatWorksOnKnownValues(double y, double expected) {
        var actual = WebMercator.lat(y);
        assertEquals(expected, actual, 1e-7);
    }

}

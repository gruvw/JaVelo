package ch.epfl.javelo.projection;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import static org.junit.jupiter.api.Assertions.assertEquals;

class Ch1903Test {

    private static final double DELTA = 1e-7;

    @ParameterizedTest
    @CsvSource({"7,47,2566639.3048922247", "8.1,46.1,2651143.455888617",
            "9.23,46.23,2738187.9209433054", "10.456,46.456,2831760.145032002",
            "6.5,46.5,2527946.5323944297", "7.56789,47.56789,2609727.6473976434"})
    void eWorksOnKnownValues(double lon, double lat, double expected) {
        double actual = Ch1903.e(Math.toRadians(lon), Math.toRadians(lat));
        assertEquals(expected, actual, DELTA);
    }

    @ParameterizedTest
    @CsvSource({"7,47,1205531.9175609266", "8.1,46.1,1105603.2393465657",
            "9.23,46.23,1121416.662587053", "10.456,46.456,1149420.4005212034",
            "6.5,46.5,1150286.400502799", "7.56789,47.56789,1268583.970220614"})
    void nWorksOnKnownValues(double lon, double lat, double expected) {
        double actual = Ch1903.n(Math.toRadians(lon), Math.toRadians(lat));
        assertEquals(expected, actual, DELTA);
    }

    @ParameterizedTest
    @CsvSource({"2600000,1200000,0.12982871138918287", "2700000,1200000,0.15275334931474058",
            "2512345,1123456,0.10998789612608173", "2712345,1298765,0.15601548084110384",
            "2800000,1199999,0.17566529683995633", "2600000,1100000,0.12982871138918287"})
    void lonWorksOnKnownValues(double e, double n, double expected) {
        double actual = Ch1903.lon(e, n);
        assertEquals(expected, actual, DELTA);
    }

    @ParameterizedTest
    @CsvSource({"2600000,1200000,0.8194509527598063", "2700000,1200000,0.8193195789181267",
            "2512345,1123456,0.8073337829590987", "2712345,1298765,0.8347862855465886",
            "2800000,1199999,0.8189253004839152", "2600000,1100000,0.8037508202024347"})
    void latWorksOnKnownValues(double e, double n, double expected) {
        double actual = Ch1903.lat(e, n);
        assertEquals(expected, actual, DELTA);
    }

}

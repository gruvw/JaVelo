package ch.epfl.javelo.projection;

import static ch.epfl.test.TestRandomizer.RANDOM_ITERATIONS;
import static ch.epfl.test.TestRandomizer.newRandom;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PointChTest {

    public static final double DELTA = 1e-7;

    @ParameterizedTest
    @CsvSource({"2484999,1.2e6", "2834001,1.2e6", "2.6e6,1074999", "2.6e6,1296001"})
    void pointChConstructorThrowsOnInvalidCoordinates(double e, double n) {
        assertThrows(IllegalArgumentException.class, () -> {
            new PointCh(e, n);
        });
    }

    @Test
    void pointChConstructorWorksOnValidCoordinates() {
        var rng = newRandom();
        for (int i = 0; i < RANDOM_ITERATIONS; i++) {
            double e = rng.nextDouble(2485000, 2834000);
            double n = rng.nextDouble(1075000, 1296000);
            new PointCh(e, n);
        }
    }

    @ParameterizedTest
    @CsvSource({"2.6e6,1.2e6,2.6e6,1.2e6,0", "2.6e6,1.2e6,2600100,1.2e6,10000",
            "2.6e6,1.2e6,2.6e6,1200100,10000", "2.6e6,1.2e6,2601234,1201234,3045512"})
    void pointChSquaredDistanceToWorksOnKnownValues(double e1, double n1, double e2, double n2,
                                                    double expected) {
        double actual = new PointCh(e1, n1).squaredDistanceTo(new PointCh(e2, n2));
        assertEquals(expected, actual, DELTA);
    }

    @ParameterizedTest
    @CsvSource({"2.6e6,1.2e6,2.6e6,1.2e6,0", "2.6e6,1.2e6,2485001,1075001,169851.4645270979",
            "2485001,1075001,2833999,1295999,413085.60857042694",
            "2700000,1100000,2833999,1295999,237426.4938923203"})
    void pointChDistanceToWorksOnKnownValues(double e1, double n1, double e2, double n2,
                                             double expected) {
        double actual = new PointCh(e1, n1).distanceTo(new PointCh(e2, n2));
        assertEquals(expected, actual, DELTA);
    }

    @ParameterizedTest
    @CsvSource({"2.6e6,1.2e6,0.12982871138918287", "2485001,1075001,0.10400660553294673",
            "2833999,1295999,0.18432563294260465", "2700000,1100000,0.15237595870983656"})
    void pointChLonWorksWithKnownValues(double e, double n, double expected) {
        double actual = new PointCh(e, n).lon();
        assertEquals(expected, actual, DELTA);
    }

    @ParameterizedTest
    @CsvSource({"2.6e6,1.2e6,0.8194509527598063", "2485001,1075001,0.7996558818339784",
            "2833999,1295999,0.8337899321808625", "2700000,1100000,0.8036216134779096"})
    void pointChLatWorksWithKnownValues(double e, double n, double expected) {
        double actual = new PointCh(e, n).lat();
        assertEquals(expected, actual, DELTA);
    }

}

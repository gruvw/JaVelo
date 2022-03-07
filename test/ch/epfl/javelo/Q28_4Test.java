package ch.epfl.javelo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static ch.epfl.test.TestRandomizer.*;

class Q28_4Test {

    @ParameterizedTest
    @CsvSource({"0,0", "10,160", "-10,-160", "134217728,-2147483648", "268435456,0",
                "2147483647, -16"})
    void ofIntTest(int input, int expected) {
        int actual = Q28_4.ofInt(input);
        assertEquals(expected, actual);
    }

    @ParameterizedTest
    @CsvSource({"0,0", "1,0.0625", "-10,-0.625", "2147483,134217.6875", "536870911,33554431.9375",
                "2147483647,134217727.9375"})
    void asDoubleTest(int input, double expected) {
        double actual = Q28_4.asDouble(input);
        assertEquals(expected, actual);
    }

    @ParameterizedTest
    @CsvSource({"0,0", "10,0.625", "-10,-0.625", "2147483,134217.6875", "536870911,33554431.9375",
                "2147483647,134217727.9375"})
    void asFloatTest(int input, float expected) {
        float actual = Q28_4.asFloat(input);
        assertEquals(expected, actual);
    }

    // == Given Tests ==

    @Test
    void q28_4OfIntWorksWithRandomValues() {
        var rng = newRandom();
        for (int i = 0; i < RANDOM_ITERATIONS; i += 1) {
            var n = rng.nextInt(1 << 28);
            assertEquals(n, Q28_4.ofInt(n) >>> 4);
        }
    }

    @Test
    void q28_4AsDoubleWorksOnKnownValues() {
        assertEquals(1.0, Q28_4.asDouble(0b1_0000));
        assertEquals(1.5, Q28_4.asDouble(0b1_1000));
        assertEquals(1.25, Q28_4.asDouble(0b1_0100));
        assertEquals(1.125, Q28_4.asDouble(0b1_0010));
        assertEquals(1.0625, Q28_4.asDouble(0b1_0001));
        assertEquals(1.9375, Q28_4.asDouble(0b1_1111));
    }

    @Test
    void q28_4AsFloatWorksOnKnownValues() {
        assertEquals(1.0f, Q28_4.asFloat(0b1_0000));
        assertEquals(1.5f, Q28_4.asFloat(0b1_1000));
        assertEquals(1.25f, Q28_4.asFloat(0b1_0100));
        assertEquals(1.125f, Q28_4.asFloat(0b1_0010));
        assertEquals(1.0625f, Q28_4.asFloat(0b1_0001));
        assertEquals(1.9375f, Q28_4.asFloat(0b1_1111));
    }

    @Test
    void q28_4ofIntAndAsFloatDoubleAreInverse() {
        var rng = newRandom();
        for (int i = 0; i < RANDOM_ITERATIONS; i += 1) {
            var n = rng.nextInt(1 << 24);
            assertEquals(n, Q28_4.asFloat(Q28_4.ofInt(n)));
            assertEquals(n, Q28_4.asDouble(Q28_4.ofInt(n)));
        }
    }

}

package ch.epfl.javelo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

public class Q28_4Test {

    @ParameterizedTest
    @CsvSource({"0,0", "10,160", "-10,-160", "134217728,-2147483648", "268435456,0",
            "2147483647, -16"})
    void ofIntTest(int input, int expected) {
        int actual = Q28_4.ofInt(input);
        assertEquals(expected, actual);
    }

    @ParameterizedTest
    @CsvSource({"0,0", "1,0.0625", "-10,-0.625", "2147483647,134217727.938"})
    void asDoubleTest(int input, double expected) {
        // FIXME
        double actual = Q28_4.asDouble(input);
        assertEquals(expected, actual);
    }

    @ParameterizedTest
    @CsvSource({"0,0", "10,0.625", "-10,-0.625", "2147483647,134217727.938"})
    void asFloatTest(int input, double expected) {
        // FIXME
        float actual = Q28_4.asFloat(input);
        assertEquals(expected, actual);
    }

}

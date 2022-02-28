package ch.epfl.javelo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import java.util.function.DoubleUnaryOperator;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

public class FunctionsTest {

    private static final double DELTA = 1e-6;

    static final DoubleUnaryOperator f1 =
            Functions.sampled(new float[] {2.5f, 2, 1, 0.5f, 1, 1, 2}, 6);
    static final DoubleUnaryOperator f2 = Functions.sampled(new float[] {0, 2, 1, 1, 3.5f}, 2);
    static final DoubleUnaryOperator f3 = Functions.sampled(new float[] {5, 7, 1, 14}, 9);
    static final DoubleUnaryOperator f4 =
            Functions.sampled(new float[] {2.5f, 2, 1, 0.5f, 1, 1, 2}, 0);

    @ParameterizedTest
    @CsvSource({"0,2.5", "-5,2.5", "6,2", "10,2", "0.5,2.25", "1.5,1.5", "2.5,0.75", "3,0.5",
            "4.1,1", "5.56,1.56"})
    void sampledTest1(double input, double expected) {
        double actual = f1.applyAsDouble(input);
        assertEquals(expected, actual, DELTA);
    }

    @ParameterizedTest
    @CsvSource({"0,0", "-5,0", "3,3.5", "2,3.5", "0.2,0.8", "0.5,2", "0.8,1.4", "1.2,1",
            "1.91,3.05"})
    void sampledTest2(double input, double expected) {
        double actual = f2.applyAsDouble(input);
        assertEquals(expected, actual, DELTA);
    }

    @ParameterizedTest
    @CsvSource({"0,5", "-5,5", "9,14", "100,14", "0.6,5.4", "2,6.33333333", "5,3", "7.5,7.5"})
    void sampledTest3(double input, double expected) {
        double actual = f3.applyAsDouble(input);
        assertEquals(expected, actual, DELTA);
    }

    @ParameterizedTest
    @CsvSource({"0,2.5", "10,2", "-10,2.5"})
    void sampledTest4(double input, double expected) {
        double actual = f4.applyAsDouble(input);
        assertEquals(expected, actual, DELTA);
    }

    void sampledPreconditionsTest() {
        assertThrows(IllegalArgumentException.class, () -> {
            Functions.sampled(new float[] {2.5f, 2, 1, 0.5f, 1, 1, 2}, -1);
        });
        assertThrows(IllegalArgumentException.class, () -> {
            Functions.sampled(new float[] {2.5f}, 10);
        });
        assertThrows(IllegalArgumentException.class, () -> {
            Functions.sampled(new float[] {}, 10);
        });
    }

}

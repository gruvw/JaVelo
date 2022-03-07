package ch.epfl.javelo;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static ch.epfl.test.TestRandomizer.*;

class BitsTest {

    @ParameterizedTest
    @CsvSource({"-889275714,8,4,-6", "-1,4,10,-1", "-1,10,20,-1", "2147483647,0,0,0",
                "2147483647,31,1,0", "-1,31,1,-1", "14567,0,32,14567", "-1,0,32,-1", "-1,0,31,-1",
                "-1,30,2,-1", "0,0,0,0"})
    void extractSignedTest(int value, int start, int length, int expected) {
        int actual = Bits.extractSigned(value, start, length);
        assertEquals(expected, actual);
    }

    @ParameterizedTest
    @CsvSource({"-889275714,8,4,10", "-1,4,10,1023", "-1,10,10,1023", "-1,10,20,1048575",
                "2147483647,0,0,0", "14567,0,31,14567", "-1,0,31,2147483647", "1,30,2,0",
                "1,20,12,0", "1,32,0,0", "2147483647,30,2,1", "2147483647,30,1,1", "-1,30,2,3",
                "0,0,0,0"})
    void extractUnsignedTest(int value, int start, int length, int expected) {
        int actual = Bits.extractUnsigned(value, start, length);
        assertEquals(expected, actual);
    }

    @ParameterizedTest
    @CsvSource({"1,32,1", "1,31,2", "1,20,13", "1,-1,13", "1,10,-1", "-20,-20,20"})
    void extractSignedThrowsTest(int value, int start, int length) {
        assertThrows(IllegalArgumentException.class, () -> {
            Bits.extractSigned(value, start, length);
        });
    }

    @ParameterizedTest
    @CsvSource({"1,0,32", "1,0,43", "1,0,-43", "1,20,13", "1,-1,13", "1,10,-1"})
    void extractUnsignedThrowsTest(int value, int start, int length) {
        assertThrows(IllegalArgumentException.class, () -> {
            Bits.extractUnsigned(value, start, length);
        });
    }

    // == Given Tests ==

    @ParameterizedTest
    @CsvSource({"0,-1,1", "0,32,1", "0,-1,1", "0,32,1"})
    void bitsExtractThrowsWithInvalidStart(int value, int start, int length) {
        assertThrows(IllegalArgumentException.class, () -> {
            Bits.extractUnsigned(value, start, length);
        });
    }

    @ParameterizedTest
    @CsvSource({"0,10,-1", "0,0,32", "0,10,-1", "0,0,33"})
    void bitsExtractThrowsWithInvalidLength(int value, int start, int length) {
        assertThrows(IllegalArgumentException.class, () -> {
            Bits.extractUnsigned(value, start, length);
        });
    }

    @Test
    void bitsExtractWorksOnFullLength() {
        var rng = newRandom();
        for (int i = 0; i < RANDOM_ITERATIONS; i += 1) {
            var v = rng.nextInt();
            assertEquals(v, Bits.extractSigned(v, 0, Integer.SIZE));
        }
        for (int i = 0; i < RANDOM_ITERATIONS; i += 1) {
            var v = 1 + rng.nextInt(-1, Integer.MAX_VALUE);
            assertEquals(v, Bits.extractUnsigned(v, 0, Integer.SIZE - 1));
        }
    }

    @Test
    void bitsExtractWorksOnRandomValues() {
        var rng = newRandom();
        for (int i = 0; i < RANDOM_ITERATIONS; i += 1) {
            var value = rng.nextInt();
            var start = rng.nextInt(0, Integer.SIZE - 1);
            var length = rng.nextInt(1, Integer.SIZE - start);

            var expectedU = (value >> start) & (1 << length) - 1;
            var mask = 1 << (length - 1);
            var expectedS = (expectedU ^ mask) - mask;
            assertEquals(expectedU, Bits.extractUnsigned(value, start, length));
            assertEquals(expectedS, Bits.extractSigned(value, start, length));
        }
    }

}

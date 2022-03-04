package ch.epfl.javelo;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

public class BitsTest {

    @ParameterizedTest
    @CsvSource({"-889275714,8,4,-6", "-1,4,10,-1", "-1,10,20,-1", "2147483647,0,0,0",
            "2147483647,31,1,0", "-1,31,1,-1", "14567,0,32,14567", "-1,0,32,-1", "-1,0,31,-1"})
    void extractSignedTest(int value, int start, int length, int expected) {
        int actual = Bits.extractSigned(value, start, length);
        assertEquals(expected, actual);
    }

    @ParameterizedTest
    @CsvSource({"-889275714,8,4,10", "-1,4,10,1023", "-1,10,10,1023", "-1,10,20,1048575",
            "2147483647,0,0,0", "14567,0,31,14567", "-1,0,31,2147483647"})
    void extractUnsignedTest(int value, int start, int length, int expected) {
        int actual = Bits.extractUnsigned(value, start, length);
        assertEquals(expected, actual);
    }

    @ParameterizedTest
    @CsvSource({"1,32,1", "1,31,2", "1,20,13", "1,-1,13", "1,10,-1"})
    void extractSignedThrowsTest(int value, int start, int length) {
        assertThrows(IllegalArgumentException.class, () -> {
            Bits.extractSigned(value, start, length);
        });
    }

    @ParameterizedTest
    @CsvSource({"1,0,32", "1,32,0", "1,30,2", "1,20,13", "1,-1,13", "1,10,-1"})
    void extractUnsignedThrowsTest(int value, int start, int length) {
        assertThrows(IllegalArgumentException.class, () -> {
            Bits.extractUnsigned(value, start, length);
        });
    }

}

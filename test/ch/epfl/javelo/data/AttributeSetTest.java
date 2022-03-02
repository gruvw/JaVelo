package ch.epfl.javelo.data;

import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

public class AttributeSetTest {

    @ParameterizedTest
    @ValueSource(longs = {1l << 63, 1l << 62, 0b11l << 62, 1l << 62 | 1, 1l << 63 | 0b11})
    void constructorThrowsTest(long illegalBits) {
        assertThrows(IllegalArgumentException.class, () -> {
            new AttributeSet(illegalBits);
        });
    }

    @ParameterizedTest
    @ValueSource(longs = {0, 1l << 61, 0b11l << 30, 1l << 60 | 1l << 32})
    void constructorOkTest(long bits) {
        assertDoesNotThrow(() -> {
            new AttributeSet(bits);
        });
    }

    @Test
    void ofValuesTest() {
        AttributeSet set1 = AttributeSet.of(Attribute.HIGHWAY_SERVICE);
        assertEquals(1, set1.bits());
        AttributeSet set2 = AttributeSet.of(Attribute.LCN_YES);
        assertEquals(1L << 61, set2.bits());
        assertEquals(-1l, AttributeSet.of(Attribute.values()).bits());
    }

    @Test
    void ofTest() {
        assertDoesNotThrow(() -> {
            AttributeSet.of(Attribute.ACCESS_NO, Attribute.HIGHWAY_PATH, Attribute.VEHICLE_NO);
        });
        assertDoesNotThrow(() -> {
            AttributeSet.of();
        });
        assertDoesNotThrow(() -> {
            AttributeSet.of(Attribute.values());
        });
    }

    @Test
    void containsTest() {
        AttributeSet set1 = AttributeSet.of(Attribute.ACCESS_NO, Attribute.HIGHWAY_PATH,
                Attribute.VEHICLE_NO);
        assertTrue(set1.contains(Attribute.ACCESS_NO));
        assertTrue(set1.contains(Attribute.HIGHWAY_PATH));
        assertTrue(set1.contains(Attribute.VEHICLE_NO));
        assertFalse(set1.contains(Attribute.BICYCLE_DESIGNATED));
        AttributeSet set2 = AttributeSet.of();
        AttributeSet set3 = new AttributeSet(0);
        AttributeSet set4 = AttributeSet.of(Attribute.values());
        for (Attribute attribute : Attribute.values()) {
            assertFalse(set2.contains(attribute));
            assertFalse(set3.contains(attribute));
            assertTrue(set4.contains(attribute));
        }
    }

    @Test
    void intersectTest() {
        AttributeSet set1 = AttributeSet.of(Attribute.ACCESS_NO, Attribute.HIGHWAY_PATH,
                Attribute.VEHICLE_NO);
        AttributeSet set2 = AttributeSet.of(Attribute.ACCESS_NO);
        assertTrue(set1.intersects(set2));
        AttributeSet set3 = AttributeSet.of(Attribute.VEHICLE_PRIVATE);
        assertFalse(set1.intersects(set3));
        assertFalse(set2.intersects(set3));
        AttributeSet set4 = new AttributeSet(0);
        AttributeSet set5 = AttributeSet.of(Attribute.values());
        assertFalse(set4.intersects(set5));
        for (Attribute attribute : Attribute.values()) {
            AttributeSet current = AttributeSet.of(attribute);
            assertFalse(set4.intersects(current));
            assertTrue(set5.intersects(current));
        }
    }

    @Test
    void toStringTest() {
        AttributeSet set1 = AttributeSet.of(Attribute.ACCESS_NO, Attribute.HIGHWAY_PATH,
                Attribute.VEHICLE_NO);
        assertEquals("{highway=path,vehicle=no,access=no}", set1.toString());
        AttributeSet set2 = AttributeSet.of(Attribute.ACCESS_NO, Attribute.VEHICLE_NO,
                Attribute.HIGHWAY_PATH);
        assertEquals("{highway=path,vehicle=no,access=no}", set2.toString());
        AttributeSet set3 = new AttributeSet(0);
        assertEquals("{}", set3.toString());
    }

}

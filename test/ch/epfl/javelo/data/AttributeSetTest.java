package ch.epfl.javelo.data;

import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.util.Arrays;
import java.util.Collections;
import java.util.Random;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static ch.epfl.test.TestRandomizer.*;

class AttributeSetTest {

    private static final int ATTRIBUTES_COUNT = 62;

    @ParameterizedTest
    @ValueSource(longs = {1L << 63, 1L << 62, 0b11L << 62, 1L << 62 | 1, 1L << 63 | 0b11})
    void constructorThrowsTest(long illegalBits) {
        assertThrows(IllegalArgumentException.class, () -> {
            new AttributeSet(illegalBits);
        });
    }

    @ParameterizedTest
    @ValueSource(longs = {0, 1L << 61, 0b11L << 30, 1L << 60 | 1L << 32})
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
        assertEquals(-1L >>> (64 - Attribute.COUNT), AttributeSet.of(Attribute.values()).bits());
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

    // == Given Tests ==

    @Test
    void attributeSetConstructorWorksWithAllBitsSet() {
        assertDoesNotThrow(() -> {
            long allValidBits = (1L << ATTRIBUTES_COUNT) - 1;
            new AttributeSet(allValidBits);
        });
    }

    @Test
    void attributeSetConstructorThrowsWithInvalidBitsSet() {
        for (int i = ATTRIBUTES_COUNT; i < Long.SIZE; i += 1) {
            var invalidBits = 1L << i;
            assertThrows(IllegalArgumentException.class, () -> {
                new AttributeSet(invalidBits);
            });
        }
    }

    @Test
    void attributeSetOfWorksForEmptySet() {
        assertEquals(0L, AttributeSet.of().bits());
    }

    @Test
    void attributeSetOfWorksForFullSet() {
        var allAttributes = AttributeSet.of(Attribute.values());
        assertEquals((1L << ATTRIBUTES_COUNT) - 1, allAttributes.bits());
        assertEquals(ATTRIBUTES_COUNT, Long.bitCount(allAttributes.bits()));
    }

    @Test
    void attributeSetContainsWorksOnRandomSets() {
        var allAttributes = Attribute.values();
        assert allAttributes.length == ATTRIBUTES_COUNT;
        var rng = newRandom();
        for (int i = 0; i < RANDOM_ITERATIONS; i += 1) {
            Collections.shuffle(Arrays.asList(allAttributes), new Random(rng.nextLong()));
            var count = rng.nextInt(ATTRIBUTES_COUNT + 1);
            var attributes = Arrays.copyOf(allAttributes, count);
            var attributeSet = AttributeSet.of(attributes);
            assertEquals(count, Long.bitCount(attributeSet.bits()));
            for (int j = 0; j < count; j += 1)
                assertTrue(attributeSet.contains(allAttributes[j]));
            for (int j = count; j < ATTRIBUTES_COUNT; j += 1)
                assertFalse(attributeSet.contains(allAttributes[j]));
        }
    }

    @Test
    void attributeSetIntersectsWorksOnRandomSets() {
        var allAttributes = Attribute.values();
        assert allAttributes.length == ATTRIBUTES_COUNT;
        var rng = newRandom();
        for (int i = 0; i < RANDOM_ITERATIONS; i += 1) {
            Collections.shuffle(Arrays.asList(allAttributes), new Random(rng.nextLong()));
            var count = rng.nextInt(1, ATTRIBUTES_COUNT + 1);
            var attributes = Arrays.copyOf(allAttributes, count);
            var attributeSet = AttributeSet.of(attributes);
            var attributeSet1 = AttributeSet.of(attributes[0]);
            assertTrue(attributeSet.intersects(attributeSet1));
            assertTrue(attributeSet1.intersects(attributeSet));
        }
    }

    @Test
    void attributeSetIntersectsWorksOnComplementarySets() {
        var rng = newRandom();
        var validBitsMask = (1L << ATTRIBUTES_COUNT) - 1;
        for (int i = 0; i < RANDOM_ITERATIONS; i += 1) {
            var bits = rng.nextLong();
            var set = new AttributeSet(bits & validBitsMask);
            var setComplement = new AttributeSet(~bits & validBitsMask);
            assertFalse(set.intersects(setComplement));
            assertFalse(setComplement.intersects(set));
            assertTrue(set.intersects(set));
            assertTrue(setComplement.intersects(setComplement));
        }
    }

    @Test
    void attributeSetToStringWorksOnKnownValues() {
        assertEquals("{}", new AttributeSet(0).toString());

        for (var attribute : Attribute.values()) {
            var expected = "{" + attribute + "}";
            assertEquals(expected, AttributeSet.of(attribute).toString());
        }

        AttributeSet set = AttributeSet.of(Attribute.TRACKTYPE_GRADE1, Attribute.HIGHWAY_TRACK);
        assertEquals("{highway=track,tracktype=grade1}", set.toString());
    }

}

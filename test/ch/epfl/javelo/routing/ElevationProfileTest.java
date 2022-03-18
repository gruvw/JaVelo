package ch.epfl.javelo.routing;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;

public class ElevationProfileTest {

    private static final double DELTA = 1e-5;

    private static ElevationProfile profile = new ElevationProfile(9.5,
            new float[] {10.2F, 13.7F, 5.2F, -4F, 0F, 7.3F});

    @Test
    void elevationProfileThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new ElevationProfile(-10, new float[] {3f, 3f, 3f}));
        assertThrows(IllegalArgumentException.class,
                () -> new ElevationProfile(2, new float[] {3f}));
        assertThrows(IllegalArgumentException.class,
                () -> new ElevationProfile(0, new float[] {3f, 3f}));
    }

    @Test
    void lengthTest() {
        assertEquals(9.5, profile.length());
    }

    @Test
    void minElevationTest() {
        assertEquals(-4F, profile.minElevation());
    }

    @Test
    void maxElevationTest() {
        assertEquals(13.7F, profile.maxElevation());
    }

    @Test
    void totalAscent() {
        assertEquals(14.8F, profile.totalAscent());
    }

    @Test
    void totalDescent() {
        assertEquals(17.7F, profile.totalDescent(), DELTA);
    }

    @Test
    void elevationAtTest() {
        double samplesGap = 9.5 / 5.0;
        assertEquals(10.2F, profile.elevationAt(-10), DELTA);
        assertEquals(10.2F, profile.elevationAt(0), DELTA);
        assertEquals(13.7F, profile.elevationAt(samplesGap), DELTA);
        assertEquals(5.2F, profile.elevationAt(samplesGap * 2), DELTA);
        assertEquals(-4F, profile.elevationAt(samplesGap * 3), DELTA);
        assertEquals(0F, profile.elevationAt(samplesGap * 4), DELTA);
        assertEquals(7.3F, profile.elevationAt(samplesGap * 5), DELTA);
        assertEquals(7.3F, profile.elevationAt(10), DELTA);
        assertEquals(7.3f, profile.elevationAt(100), DELTA);
        assertEquals(11.3512079214F, profile.elevationAt(0.624941443F), DELTA);
        assertEquals(11.9819972132F, profile.elevationAt(2.2840241523F), DELTA);
        assertEquals(-1.4614903031F, profile.elevationAt(5.1757425626F), DELTA);
        assertEquals(-1.1867247078F, profile.elevationAt(7.0363057638F), DELTA);
        assertEquals(3.2409771716F, profile.elevationAt(8.4435420036F), DELTA);
    }

}

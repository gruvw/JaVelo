package ch.epfl.javelo.routing;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import java.util.DoubleSummaryStatistics;
import java.util.random.RandomGenerator;
import org.junit.jupiter.api.Test;

import static ch.epfl.test.TestRandomizer.RANDOM_ITERATIONS;
import static ch.epfl.test.TestRandomizer.newRandom;

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

    // == GIVEN TESTS ==

    @Test
    void elevationProfileConstructorThrowsWithNotEnoughSamples() {
        assertThrows(IllegalArgumentException.class, () -> {
            new ElevationProfile(1, new float[0]);
        });
        assertThrows(IllegalArgumentException.class, () -> {
            new ElevationProfile(1, new float[] {3.14f});
        });
    }

    @Test
    void elevationProfileConstructorThrowsWithZeroLength() {
        assertThrows(IllegalArgumentException.class, () -> {
            new ElevationProfile(0, new float[] {1, 2, 3});
        });
    }

    @Test
    void elevationProfileLengthReturnsLength() {
        var rng = newRandom();
        for (int i = 0; i < RANDOM_ITERATIONS; i += 1) {
            var length = Math.nextUp(rng.nextDouble(1000));
            var profile = new ElevationProfile(length, new float[] {1, 2, 3});
            assertEquals(length, profile.length());
        }
    }

    private static float[] randomSamples(RandomGenerator rng, int count) {
        var samples = new float[count];
        for (int i = 0; i < count; i += 1)
            samples[i] = rng.nextFloat(4096);
        return samples;
    }

    @Test
    void elevationProfileMinElevationReturnsMinElevation() {
        var rng = newRandom();
        for (int i = 0; i < RANDOM_ITERATIONS; i += 1) {
            var sampleCount = rng.nextInt(2, 1000);
            var elevationSamples = randomSamples(rng, sampleCount);
            var elevationStatistics = new DoubleSummaryStatistics();
            for (var s : elevationSamples)
                elevationStatistics.accept(s);
            var profile = new ElevationProfile(1000, elevationSamples);
            assertEquals(elevationStatistics.getMin(), profile.minElevation());
        }
    }

    @Test
    void elevationProfileMaxElevationReturnsMaxElevation() {
        var rng = newRandom();
        for (int i = 0; i < RANDOM_ITERATIONS; i += 1) {
            var sampleCount = rng.nextInt(2, 1000);
            var elevationSamples = randomSamples(rng, sampleCount);
            var elevationStatistics = new DoubleSummaryStatistics();
            for (var s : elevationSamples)
                elevationStatistics.accept(s);
            var profile = new ElevationProfile(1000, elevationSamples);
            assertEquals(elevationStatistics.getMax(), profile.maxElevation());
        }
    }

    @Test
    void elevationProfileTotalAscentReturnsTotalAscent() {
        var rng = newRandom();
        for (int i = 0; i < RANDOM_ITERATIONS; i += 1) {
            var sampleCount = rng.nextInt(2, 1000);
            var elevationSamples = randomSamples(rng, sampleCount);
            var totalAscent = 0d;
            for (int j = 1; j < sampleCount; j += 1) {
                var d = elevationSamples[j] - elevationSamples[j - 1];
                if (d > 0)
                    totalAscent += d;
            }
            var profile = new ElevationProfile(1000, elevationSamples);
            assertEquals(totalAscent, profile.totalAscent());
        }
    }

    @Test
    void elevationProfileTotalDescentReturnsTotalDescent() {
        var rng = newRandom();
        for (int i = 0; i < RANDOM_ITERATIONS; i += 1) {
            var sampleCount = rng.nextInt(2, 1000);
            var elevationSamples = randomSamples(rng, sampleCount);
            var totalDescent = 0d;
            for (int j = 1; j < sampleCount; j += 1) {
                var d = elevationSamples[j] - elevationSamples[j - 1];
                if (d < 0)
                    totalDescent -= d;
            }
            var profile = new ElevationProfile(1000, elevationSamples);
            assertEquals(totalDescent, profile.totalDescent());
        }
    }

    @Test
    void elevationProfileElevationAtWorksOnKnownValues() {
        var samples = new float[] {100.00f, 123.25f, 375.50f, 212.75f, 220.00f, 210.25f};
        var profile = new ElevationProfile(1000, samples);

        var actual1 = profile.elevationAt(0);
        var expected1 = 100.0;
        assertEquals(expected1, actual1);

        var actual2 = profile.elevationAt(200);
        var expected2 = 123.25;
        assertEquals(expected2, actual2);

        var actual3 = profile.elevationAt(400);
        var expected3 = 375.5;
        assertEquals(expected3, actual3);

        var actual4 = profile.elevationAt(600);
        var expected4 = 212.75;
        assertEquals(expected4, actual4);

        var actual5 = profile.elevationAt(800);
        var expected5 = 220.0;
        assertEquals(expected5, actual5);

        var actual6 = profile.elevationAt(1000);
        var expected6 = 210.25;
        assertEquals(expected6, actual6);

        var actual7 = profile.elevationAt(500);
        var expected7 = 294.125;
        assertEquals(expected7, actual7);
    }

}

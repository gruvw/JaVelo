package ch.epfl.javelo.routing;

import java.util.DoubleSummaryStatistics;
import java.util.function.DoubleUnaryOperator;
import ch.epfl.javelo.Functions;
import ch.epfl.javelo.Preconditions;

/**
 * Represents the elevation profile of a route.
 * <p>
 * Immutable.
 *
 * @author Lucas Jung (324724)
 * @author Florian Kolly (328313)
 */
public final class ElevationProfile {

    private final double length;
    private final float[] elevationSamples;

    /**
     * Statistics helper to calculate the minimum and maximum altitude.
     */
    private DoubleSummaryStatistics s;

    /**
     * Sampled function for the profile.
     */
    private DoubleUnaryOperator profile;

    /**
     * ElevationProfile's constructor.
     *
     * @param length           length of the route, in meters
     * @param elevationSamples uniformly distributed elevation samples
     * @throws IllegalArgumentException if the length is less than or equal to 0, or if
     *                                  {@code elevationSamples} contains less than 2 samples
     */
    public ElevationProfile(double length, float[] elevationSamples) {
        Preconditions.checkArgument(length > 0);
        Preconditions.checkArgument(elevationSamples.length >= 2);
        this.length = length;
        this.elevationSamples = elevationSamples.clone();
        s = new DoubleSummaryStatistics();
        for (int i = 0; i < this.elevationSamples.length; i++) {
            s.accept(this.elevationSamples[i]);
        }
        profile = Functions.sampled(this.elevationSamples, length);
    }

    /**
     * Retrieves the profile's length in meters.
     *
     * @return the profile's length in meters
     */
    public double length() {
        return length;
    }

    /**
     * Retrieves the minimum altitude in the profile.
     *
     * @return the minimum altitude in the profile
     */
    public double minElevation() {
        return s.getMin();
    }

    /**
     * Retrieves the maximum altitude in the profile.
     *
     * @return the maximum altitude in the profile
     */
    public double maxElevation() {
        return s.getMax();
    }

    /**
     * Computes the total ascent of the profile.
     *
     * @return the total ascent of the profile
     */
    public double totalAscent() {
        double ascent = 0;
        for (int i = 0; i < elevationSamples.length - 1; i++)
            if (elevationSamples[i + 1] > elevationSamples[i])
                ascent += elevationSamples[i + 1] - elevationSamples[i];
        return ascent;
    }

    /**
     * Computes the total descent of the profile.
     *
     * @return the total descent of the profile
     */
    public double totalDescent() {
        double descent = 0;
        for (int i = 0; i < elevationSamples.length - 1; i++)
            if (elevationSamples[i + 1] < elevationSamples[i])
                descent += elevationSamples[i] - elevationSamples[i + 1];
        return descent;
    }

    /**
     * Retrieves the altitude of the profile at a given position.
     *
     * @param position position in the profile
     * @return the altitude at the given position, clamped between 0 and the maximum position
     */
    public double elevationAt(double position) {
        return profile.applyAsDouble(position);
    }

}

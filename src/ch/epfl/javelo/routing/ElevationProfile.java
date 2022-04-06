// TODO: read Lucas
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
     * Statistics helper to calculate the minimum and maximum altitudes.
     */
    private final DoubleSummaryStatistics stats = new DoubleSummaryStatistics();;

    /**
     * Sampled function for the profile.
     */
    private final DoubleUnaryOperator profile;

    private final double totalAscent;
    private final double totalDescent;

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
        for (int i = 0; i < this.elevationSamples.length; i++) {
            stats.accept(this.elevationSamples[i]);
        }
        this.profile = Functions.sampled(elevationSamples, length);
        double totalAscent = 0, totalDescent = 0;
        for (int i = 0; i < elevationSamples.length - 1; i++)
            if (elevationSamples[i + 1] > elevationSamples[i])
                totalAscent += elevationSamples[i + 1] - elevationSamples[i];
            else
                totalDescent += elevationSamples[i] - elevationSamples[i + 1];
        this.totalAscent = totalAscent;
        this.totalDescent = totalDescent;
    }

    /**
     * Retrieves the profile's length.
     *
     * @return the profile's length, in meters
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
        return stats.getMin();
    }

    /**
     * Retrieves the maximum altitude in the profile.
     *
     * @return the maximum altitude in the profile
     */
    public double maxElevation() {
        return stats.getMax();
    }

    /**
     * Computes the total ascent of the profile.
     *
     * @return the total ascent of the profile
     */
    public double totalAscent() {
        return totalAscent;
    }

    /**
     * Computes the total descent of the profile.
     *
     * @return the total descent of the profile
     */
    public double totalDescent() {
        return totalDescent;
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

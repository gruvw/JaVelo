package ch.epfl.javelo.routing;

import ch.epfl.javelo.Preconditions;

/**
 * Represents the elevation profile of a route.
 *
 * @author Lucas Jung (324724)
 * @author Florian Kolly (328313)
 */
public final class ElevationProfile {

    private final double length;
    private final float[] elevationSamples;

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
    }

    /**
     * Retrieves the profile's length in meters.
     *
     * @return the profile's length in meters
     */
    public double length() {

    }

    /**
     * Retrieves the minimum altitude in the profile.
     *
     * @return the minimum altitude in the profile
     */
    public double minElevation() {

    }

    /**
     * Retrieves the maximum altitude in the profile.
     *
     * @return the maximum altitude in the profile
     */
    public double maxElevation() {

    }

    /**
     * Computes the total ascent of the profile.
     *
     * @return the total ascent of the profile
     */
    public double totalAscent() {

    }

    /**
     * Computes the total descent of the profile.
     *
     * @return the total descent of the profile
     */
    public double totalDescent() {

    }

    /**
     * Retrieves the altitude of the profile at a given position.
     *
     * @param position position in the profile
     * @return the altitude at the given position, clamped between 0 and the maximum position
     */
    public double elevationAt(double position) {

    }

}

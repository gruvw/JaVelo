package ch.epfl.javelo.routing;

import java.util.Arrays;

import ch.epfl.javelo.Math2;
import ch.epfl.javelo.Preconditions;

/**
 * Calculator for an elevation profile.
 * <p>
 * Non-instantiable.
 *
 * @author Lucas Jung (324724)
 * @author Florian Kolly (328313)
 */
public final class ElevationProfileComputer {

    private ElevationProfileComputer() {
    }

    /**
     * Computes the elevation profile of a route, ensuring that the spacing between
     * two samples is
     * maximum {@code maxStepLength} meters.
     *
     * @param route         route for which we compute the profile
     * @param maxStepLength maximum spacing between two samples of the profile
     * @return the elevation profile of the specified route. All samples with the
     *         value {@code Double.NaN} are replaced by interpolated values (or by 0
     *         if the sample is located at the beginning of the profile, until a
     *         valid sample is found). If the elevation contains only
     *         {@code Double.NaN}, the profile is filled with 0s.
     * @throws IllegalArgumentException if the spacing {@code maxStepLength} is not
     *                                  strictly positive
     */
    public static ElevationProfile elevationProfile(Route route, double maxStepLength) {
        int nbSamples = (int) Math.ceil(route.length() / maxStepLength) + 1;
        double sampleSpacing = route.length() / nbSamples;
        // Throws if spacing is not strictly positive
        Preconditions.checkArgument(sampleSpacing > 0);
        float[] elevations = new float[nbSamples];
        int firstValidPos = -1;
        // Fill the array
        for (int i = 0; i < nbSamples; i++) {
            float elevation = (float) route.elevationAt(sampleSpacing * i);
            elevations[i] = elevation;
            if (!Float.isNaN(elevation)) {
                firstValidPos = i;
            }
        }
        // Check for holes (i.e. NaN values) and change them by interpolating a value
        // from the surrounding valid samples
        // Fill holes at the beginning of the array
        if (firstValidPos == -1) {
            Arrays.fill(elevations, 0);
            return new ElevationProfile(route.length(), elevations);
        } else {
            Arrays.fill(elevations, 0, firstValidPos, 0);
        }
        // Find the last valid position (starting at the end)
        int lastValidPos = -1;
        for (int i = elevations.length - 1; i >= 0; i--) {
            if (!Float.isNaN(elevations[i])) {
                lastValidPos = i;
                break;
            }
        }
        // Fill holes at the end of the array
        if (lastValidPos != -1)
            Arrays.fill(elevations, lastValidPos, elevations.length, 0);
        // Fill holes in the middle of the array (no need to check the border)
        for (int i = 1; i < elevations.length - 1; i++) {
            if (Float.isNaN(elevations[i])) {
                int closestValidPos = -1; // always exists
                for (int j = i + 1; j < elevations.length; j++) {
                    if (!Float.isNaN(elevations[j])) {
                        closestValidPos = j;
                        break;
                    }
                }
                // Interpolate values for all holes between the two valid samples
                for (int j = i; j < closestValidPos; j++) {
                    double x = (j - i + 1) / (closestValidPos - i + 1);
                    elevations[j] = (float) Math2.interpolate(elevations[i - 1], elevations[closestValidPos], x);
                }
            }
        }
        return new ElevationProfile(route.length(), elevations);
    }

}

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

    private ElevationProfileComputer() {}

    /**
     * Computes the elevation profile of a route, ensuring that the spacing between two samples is
     * maximum {@code maxStepLength} meters.
     *
     * @param route         route from which we extract and compute the profile
     * @param maxStepLength maximum spacing between two samples of the profile
     * @return the elevation profile of the specified route. All samples with the value
     *         {@code Double.NaN} are replaced by interpolated values (or by 0 if the sample is
     *         located at the beginning of the profile, until a valid sample is found). If the
     *         elevation contains only {@code Double.NaN}, the profile is filled with 0s.
     * @throws IllegalArgumentException if the spacing {@code maxStepLength} is not strictly
     *                                  positive
     */
    public static ElevationProfile elevationProfile(Route route, double maxStepLength) {
        Preconditions.checkArgument(maxStepLength > 0);
        // TODO: single edge length 0
        int nbSamples = (int) Math.ceil(route.length() / maxStepLength) + 1;
        double sampleSpacing = route.length() / (nbSamples - 1);
        float[] elevations = new float[nbSamples];
        int firstValidPos = -1;
        // Fill the array (retrieve the data for each elevation) & find firstValidPos
        for (int i = 0; i < nbSamples; i++) {
            elevations[i] = (float) route.elevationAt(sampleSpacing * i);
            if (!Float.isNaN(elevations[i]) && firstValidPos < 0)
                firstValidPos = i;
        }
        // No valid samples: return 0s
        if (firstValidPos < 0) {
            Arrays.fill(elevations, 0);
            return new ElevationProfile(route.length(), elevations);
        }
        // Fill holes (i.e. NaN values) at the beginning of the array
        Arrays.fill(elevations, 0, firstValidPos, elevations[firstValidPos]);
        // Find the last valid position (starting from the end)
        int lastValidPos = elevations.length - 1;
        while (lastValidPos > firstValidPos && Float.isNaN(elevations[lastValidPos]))
            lastValidPos--;
        // Fill holes at the end of the array
        Arrays.fill(elevations, lastValidPos + 1, elevations.length, elevations[lastValidPos]);
        // Fill middle holes by interpolating surrounding valid samples
        for (int i = firstValidPos + 1; i < lastValidPos; i++) {
            if (Float.isNaN(elevations[i])) {
                // i: index of the first hole (previous sample is valid)
                int nextValidPos = i + 1; // always exists
                while (Float.isNaN(elevations[nextValidPos]) && nextValidPos < elevations.length)
                    nextValidPos++;
                int nbHoles = nextValidPos - i;
                // Interpolate values for the holes between the two valid samples
                for (int j = 0; j < nbHoles; j++) {
                    double xPos = j / (double) (nbHoles + 1);
                    elevations[i + j] = (float) Math2.interpolate(elevations[i - 1],
                            elevations[nextValidPos], xPos);
                }
            }
        }
        System.out.println(Arrays.toString(elevations));
        return new ElevationProfile(route.length(), elevations);
    }

}
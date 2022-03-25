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
     * Fill holes (i.e. NaN values) in {@code elevations}.
     *
     * @param elevations    array (potentially) containing holes
     * @param firstValidPos index of first non-NaN value in {@code elevations} (negative if full of
     *                      NaN)
     * @param lastValidPos  index of last non-NaN value in {@code elevations}
     */
    private static void fillHoles(float[] elevations, int firstValidPos, int lastValidPos) {
        // No valid samples: fill with 0s
        if (firstValidPos < 0) {
            Arrays.fill(elevations, 0);
            return;
        }
        // Fill holes at the beginning of the array
        Arrays.fill(elevations, 0, firstValidPos, elevations[firstValidPos]);
        // Fill holes at the end of the array
        Arrays.fill(elevations, lastValidPos + 1, elevations.length, elevations[lastValidPos]);
        // Fill middle holes by interpolating surrounding valid samples
        for (int i = firstValidPos + 1; i < lastValidPos; i++)
            if (Float.isNaN(elevations[i])) {
                // i: index of the first hole (previous sample is valid)
                int nextValidPos = i + 1; // should always exists
                while (Float.isNaN(elevations[nextValidPos]) && nextValidPos <= lastValidPos)
                    nextValidPos++;
                int nbHoles = nextValidPos - i;
                // Interpolate values for the holes between the two valid samples
                for (int j = 0; j < nbHoles; j++) {
                    double xPos = (j + 1) / (double) (nbHoles + 1);
                    elevations[i + j] = (float) Math2.interpolate(elevations[i - 1],
                            elevations[nextValidPos], xPos);
                }
            }
    }

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
        // TODO: route length 0 (one edge with length 0) => nbSamples = 1 => sampleSpacing = NaN
        // (div 0)
        int nbSamples = (int) Math.ceil(route.length() / maxStepLength) + 1;
        double sampleSpacing = route.length() / (nbSamples - 1);
        float[] elevations = new float[nbSamples];
        int firstValidPos = -1, lastValidPos = -1;
        // Fill the array (retrieve the data for each elevation)
        for (int i = 0; i < nbSamples; i++) {
            elevations[i] = (float) route.elevationAt(sampleSpacing * i);
            if (!Float.isNaN(elevations[i])) { // find firstValidPos & lastValidPos
                lastValidPos = i;
                if (firstValidPos < 0)
                    firstValidPos = i;
            }
        }
        fillHoles(elevations, firstValidPos, lastValidPos);
        return new ElevationProfile(route.length(), elevations);
    }

}

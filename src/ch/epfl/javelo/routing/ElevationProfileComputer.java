package ch.epfl.javelo.routing;

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
     * @param route         route for which we compute the profile
     * @param maxStepLength maximum spacing between two samples of the profile
     * @return the elevation profile of the specified route. All samples with the value
     *         {@code Double.NaN} are replaced by interpolated values (or by 0 if the sample is
     *         located at the beginning of the profile, until a valid sample is found). If the
     *         elevation contains only {@code Double.NaN}, the profile is filled with 0s.
     * @throws IllegalArgumentException if the spacing {@code maxStepLength} is not strictly
     *                                  positive
     */
    public static ElevationProfile elevationProfile(Route route, double maxStepLength) {
        // TODO
    }

}

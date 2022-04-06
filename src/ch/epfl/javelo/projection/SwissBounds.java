// TODO: read Lucas
package ch.epfl.javelo.projection;

/**
 * Switzerland's limits.
 * <p>
 * Non-instantiable.
 *
 * @author Lucas Jung (324724)
 * @author Florian Kolly (328313)
 */
public final class SwissBounds {

    private SwissBounds() {}

    /**
     * Smallest east coordinate in Switzerland.
     */
    public static final double MIN_E = 2_485_000;

    /**
     * Largest east coordinate in Switzerland.
     */
    public static final double MAX_E = 2_834_000;

    /**
     * Smallest north coordinate in Switzerland.
     */
    public static final double MIN_N = 1_075_000;

    /**
     * Largest north coordinate in Switzerland.
     */
    public static final double MAX_N = 1_296_000;

    /**
     * Width of Switzerland in meters.
     */
    public static final double WIDTH = MAX_E - MIN_E;

    /**
     * Height of Switzerland in meters.
     */
    public static final double HEIGHT = MAX_N - MIN_N;

    /**
     * Checks if the given Swiss coordinates are inside the Swiss boundaries.
     *
     * @param e east coordinate to verify
     * @param n north coordinate to verify
     * @return true if the coordinates are within Switzerland's limits, false otherwise
     */
    public static boolean containsEN(double e, double n) {
        boolean containsE = e <= MAX_E && e >= MIN_E;
        boolean containsN = n <= MAX_N && n >= MIN_N;
        return containsE && containsN;
    }

}

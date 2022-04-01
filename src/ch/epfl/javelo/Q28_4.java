package ch.epfl.javelo;

/**
 * Used to convert numbers between the Q28.4 representation and other representations.
 * <p>
 * Non-instantiable.
 *
 * @author Lucas Jung (324724)
 * @author Florian Kolly (328313)
 */
public final class Q28_4 {

    private Q28_4() {}

    /**
     * Converts the given integer to the Q28.4 representation.
     *
     * @param i number (integer) to convert
     * @return the value (Q28.4) corresponding to the given integer {@code i}
     */
    public static int ofInt(int i) {
        return i << 4;
    }

    /**
     * Converts the given Q28.4 representation to a double.
     *
     * @param q28_4 number (in Q28.4) to convert
     * @return the double value corresponding to the given Q28.4 number
     */
    public static double asDouble(int q28_4) {
        return Math.scalb((double) q28_4, -4);
    }

    /**
     * Converts the given Q28.4 representation to a float.
     *
     * @param q28_4 number (in Q28.4) to convert
     * @return the float value corresponding to the given Q28.4 number
     */
    public static float asFloat(int q28_4) {
        return Math.scalb((float) q28_4, -4);
    }

}

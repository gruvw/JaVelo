package ch.epfl.javelo;

/**
 * Used to convert numbers between the Q28.4 representation and other representations.
 * <p>
 * Non-instantiable.
 *
 * @author Lucas Jung (324724)
 */
public final class Q28_4 {

    private Q28_4() {}

    /**
     * @param i number (integer) to convert
     * @return value (Q28.4) corresponding to the given integer <code>i</code>
     */
    public static int ofInt(int i) {
        return i << 4;
    }

    /**
     * @param q28_4 number (in Q28.4) to convert
     * @return double value corresponding to the given Q28.4 number
     */
    public static double asDouble(int q28_4) {
        return Math.scalb((double) q28_4, -4);
    }

    /**
     * @param q28_4 number (in Q28.4) to convert
     * @return float value corresponding to the given Q28.4 number
     */
    public static float asFloat(int q28_4) {
        return Math.scalb((float) q28_4, -4);
    }

}

package ch.epfl.javelo;

/**
 * Slice a bit vector.
 *
 * Non-instantiable.
 *
 * @author Lucas Jung (324724)
 */
public final class Bits {

    private Bits() {}

    /**
     * @param value 32 bits vector, as signed integer
     * @param start starting bit index
     * @param length size of the slice
     * @return extracted slice from <code>value</code> of size <code>length</code> starting at
     *         <code>start</code> (signed)
     * @throws IllegalArgumentException if slice is not between 0 and 31 (included)
     */
    public static int extractSigned(int value, int start, int length) {

    }

    /**
     * @param value 32 bits vector, as unsigned integer
     * @param start starting bit index
     * @param length size of the slice
     * @return extracted slice from <code>value</code> of size <code>length</code> starting at
     *         <code>start</code> (unsigned)
     * @throws IllegalArgumentException if slice is not between 0 and 31 (included) or if
     *         <code>length</code> is larger than 31
     */
    public static int extractUnsigned(int value, int start, int length) {

    }

}

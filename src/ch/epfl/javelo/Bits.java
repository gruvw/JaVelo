package ch.epfl.javelo;

/**
 * Slices a bit vector.
 * <p>
 * Non-instantiable.
 *
 * @author Lucas Jung (324724)
 * @author Florian Kolly (328313)
 */
public final class Bits {

    private Bits() {}

    /**
     * Extracts from a 32 bits vector {@code value} the span of {@code length} bits beginning at
     * index {@code start} and interprets it as a 2-complement signed integer.
     *
     * @param value  32 bits vector, as signed integer
     * @param start  starting bit index
     * @param length size of the slice
     * @return the extracted slice from {@code value} of size {@code length} starting at
     *         {@code start} (signed)
     * @throws IllegalArgumentException if the slice is not between 0 and 31 (included)
     */
    public static int extractSigned(int value, int start, int length) {
        Preconditions.checkArgument(start >= 0);
        Preconditions.checkArgument(length >= 0 && start + length <= Integer.SIZE);
        if (length == 0)
            return 0;
        return value << (Integer.SIZE - start - length) >> (Integer.SIZE - length);
    }

    /**
     * Extracts from a 32 bits vector {@code value} the span of {@code length} bits beginning at
     * index {@code start} and interprets it as an unsigned value.
     *
     * @param value  32 bits vector, as unsigned integer
     * @param start  starting bit index
     * @param length size of the slice
     * @return the extracted slice from {@code value} of size {@code length} starting at
     *         {@code start} (unsigned)
     * @throws IllegalArgumentException if the slice is not between 0 and 31 (included) or if
     *                                  {@code length} is larger than 31
     */
    public static int extractUnsigned(int value, int start, int length) {
        Preconditions.checkArgument(start >= 0);
        Preconditions.checkArgument(
                length >= 0 && start + length <= Integer.SIZE && length < Integer.SIZE);
        if (length == 0)
            return 0;
        return value << (Integer.SIZE - start - length) >>> (Integer.SIZE - length);
    }

}

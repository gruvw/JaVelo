package ch.epfl.javelo;

/**
 * Slice a bit vector.
 * <p>
 * Non-instantiable.
 *
 * @author Lucas Jung (324724)
 */
public final class Bits {

    private Bits() {}

    /**
     * Extracts from the 32 bits vector {@code value} the span of {@code length} bits beginning at
     * index {@code start} and interpreting it as a 2 complement signed value.
     *
     * @param value 32 bits vector, as signed integer
     * @param start starting bit index
     * @param length size of the slice
     * @return the extracted slice from {@code value} of size {@code length} starting at
     *         {@code start} (signed)
     * @throws IllegalArgumentException if slice is not between 0 and 31 (included)
     */
    public static int extractSigned(int value, int start, int length) {
        Preconditions.checkArgument(start >= 0);
        Preconditions.checkArgument(length >= 0 && start + length <= 32);
        if (length == 0)
            return 0;
        return value << (32 - start - length) >> (32 - length);
    }

    /**
     * Extracts from the 32 bits vector {@code value} the span of {@code length} bits beginning at
     * index {@code start} and interpreting it as an unsigned value.
     *
     * @param value 32 bits vector, as unsigned integer
     * @param start starting bit index
     * @param length size of the slice
     * @return the extracted slice from {@code value} of size {@code length} starting at
     *         {@code start} (unsigned)
     * @throws IllegalArgumentException if slice is not between 0 and 31 (included) or if
     *         {@code length} is larger than 31
     */
    public static int extractUnsigned(int value, int start, int length) {
        Preconditions.checkArgument(start >= 0);
        Preconditions.checkArgument(length >= 0 && start + length <= 32 && length < 32);
        if (length == 0)
            return 0;
        return value << (32 - start - length) >>> (32 - length);
    }

}

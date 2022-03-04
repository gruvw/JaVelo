package ch.epfl.javelo;

/**
 * Preconditions used to verify method arguments.
 * <p>
 * Non-instantiable.
 *
 * @author Lucas Jung (324724)
 */
public final class Preconditions {

    private Preconditions() {}

    /**
     * @param shouldBeTrue tested argument condition
     * @throws IllegalArgumentException if <code>shouldBeTrue</code> is false
     */
    public static void checkArgument(boolean shouldBeTrue) {
        if (!shouldBeTrue)
            throw new IllegalArgumentException("Invalid argument!");
    }

}

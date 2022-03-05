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
     * Tests if the condition given in argument is fulfilled.
     *
     * @param shouldBeTrue tested argument condition
     * @throws IllegalArgumentException if {@code shouldBeTrue} is false
     */
    public static void checkArgument(boolean shouldBeTrue) {
        if (!shouldBeTrue)
            throw new IllegalArgumentException("Invalid argument!");
    }

}

package ch.epfl.javelo;

/**
 * Preconditions used to verify method arguments.
 * <p>
 * Non-instantiable.
 *
 * @author Lucas Jung (324724)
 * @author Florian Kolly (328313)
 */
public final class Preconditions {

    private Preconditions() {}

    /**
     * Tests if the condition passed as argument is fulfilled.
     *
     * @param shouldBeTrue condition the argument should fulfill
     * @throws IllegalArgumentException if {@code shouldBeTrue} is false
     */
    public static void checkArgument(boolean shouldBeTrue) {
        if (!shouldBeTrue)
            throw new IllegalArgumentException("Invalid argument!");
    }

}

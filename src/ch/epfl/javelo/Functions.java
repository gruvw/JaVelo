package ch.epfl.javelo;

import java.util.function.DoubleUnaryOperator;

/**
 * Generator for objects representing mathematical functions.
 * <p>
 * Non-instantiable.
 *
 * @author Lucas Jung (324724)
 * @author Florian Kolly (328313)
 */
public final class Functions {

    private Functions() {}

    /**
     * Computes the evaluation of a constant function, for which the returned value will always be
     * the {@code y} argument.
     *
     * @param y constant value of the function
     * @return a constant function of value {@code y}
     */
    public static DoubleUnaryOperator constant(double y) {
        return value -> y;
    }

    /**
     * Computes the evaluation of a function obtained by linearly interpolating the samples
     * {@code samples}, evenly spaced from 0 to {@code xMax}. Before 0, the function will return the
     * value at x=0 (i.e. the first value in the array) and after {@code xMax}, the function will
     * return the sample at {@code xMax}.
     *
     * @param samples uniformly distributed (from 0 to {@code xMax}) y values used for interpolation
     * @param xMax    maximum x value
     * @return the function obtained by linear interpolation of the given {@code samples}, evenly
     *         distributed from 0 to {@code xMax}
     * @throws IllegalArgumentException if there are strictly less than two samples or if
     *                                  {@code xMax} is less or equal to 0
     */
    public static DoubleUnaryOperator sampled(float[] samples, double xMax) {
        Preconditions.checkArgument(samples.length >= 2);
        Preconditions.checkArgument(xMax > 0);
        float[] samplesCopy = samples.clone(); // immutable DoubleUnaryOperator
        return value -> {
            if (value <= 0)
                return samplesCopy[0];
            if (value >= xMax)
                return samplesCopy[samplesCopy.length - 1];
            double dx = xMax / (samplesCopy.length - 1);
            double xPos = value / dx;
            int offset = (int) (xPos);
            return Math2.interpolate(samplesCopy[offset], samplesCopy[offset + 1], xPos - offset);
        };
    }

}

package ch.epfl.javelo;

import java.util.function.DoubleUnaryOperator;

/**
 * Creates objects representing mathematic functions.
 *
 * Non-instantiable.
 *
 * @author Lucas Jung (324724)
 */
public final class Functions {

    private Functions() {}

    /**
     * @param y contant value of the function
     * @return a constant function of value <code>y</code>
     */
    public static DoubleUnaryOperator constant(double y) {
        return value -> {
            return y;
        };
    }

    /**
     * @param samples uniformly spaced (from 0 to <code>xMax</code>) y values used for interpolation
     * @param xMax maximum x value
     * @return function obtained by linear interpolation of the given <code>samples</code>, evenly
     *         distributed from 0 to <code>xMax</code>
     * @throws IllegalArgumentException if there are less than two samples or if <code>xMax</code>
     *         is less than 0
     */
    public static DoubleUnaryOperator sampled(float[] samples, double xMax) {
        // FIXME use Math2.interpolate() ?
        Preconditions.checkArgument(samples.length >= 2);
        Preconditions.checkArgument(xMax >= 0);
        return value -> {
            if (value <= 0)
                return samples[0];
            else if (value >= xMax)
                return samples[samples.length - 1];
            double dx = xMax / (samples.length - 1);
            int offset = (int) (value / dx);
            return samples[offset]
                    + (value - offset * dx) * ((samples[offset + 1] - samples[offset]) / dx);
        };
    }

}

// TODO: read Lucas
package ch.epfl.javelo;

/**
 * Mathematical calculations, similar to {@code Math}.
 * <p>
 * Non-instantiable.
 *
 * @author Lucas Jung (324724)
 * @author Florian Kolly (328313)
 */
public final class Math2 {

    private Math2() {}

    /**
     * Computes the ceil division for positive integers.
     *
     * @param x numerator
     * @param y denominator
     * @return the ceiling of {@code x} divided by {@code y} (⌈x/y⌉)
     * @throws IllegalArgumentException if x < 0 or y <= 0
     */
    public static int ceilDiv(int x, int y) {
        Preconditions.checkArgument(x >= 0);
        Preconditions.checkArgument(y > 0);
        return (x + y - 1) / y;
    }

    /**
     * Computes the y coordinate of the point ({@code x}, y) standing on the line passing by (0,
     * {@code y0}) and (1, {@code y1}).
     *
     * @param y0 y value of the line where x=0
     * @param y1 y value of the line where x=1
     * @param x  x value to interpolate with the line
     * @return the y coordinate of the point {@code x}
     */
    public static double interpolate(double y0, double y1, double x) {
        return Math.fma(y1 - y0, x, y0);
    }

    /**
     * Limits the value of {@code v} to the closed interval [{@code min}, {@code max}].
     *
     * @param min minimum value for {@code v}
     * @param v   value to limit
     * @param max maximum value for {@code v}
     * @return {@code min} when {@code v} < {@code min}, {@code max} when {@code v} > {@code max},
     *         {@code v} otherwise
     * @throws IllegalArgumentException if {@code min} > {@code max}
     */
    public static int clamp(int min, int v, int max) {
        Preconditions.checkArgument(min <= max);
        return v < min ? min : v > max ? max : v;
    }

    /**
     * Limits the value of {@code v} to the closed interval [{@code min}, {@code max}].
     *
     * @param min minimum value for {@code v}
     * @param v   value to limit
     * @param max maximum value for {@code v}
     * @return {@code min} when {@code v} < {@code min}, {@code max} when {@code v} > {@code max},
     *         {@code v} otherwise
     * @throws IllegalArgumentException if {@code min} > {@code max}
     */
    public static double clamp(double min, double v, double max) {
        Preconditions.checkArgument(min <= max);
        return v < min ? min : v > max ? max : v;
    }

    /**
     * Computes the hyperbolic sine of the argument.
     *
     * @param x argument
     * @return arsinh({@code x})
     */
    public static double asinh(double x) {
        return Math.log(x + Math.sqrt(1 + Math.pow(x, 2)));
    }

    /**
     * Computes the dot product between the given vectors.
     *
     * @param uX x component of vector u
     * @param uY y component of vector u
     * @param vX x component of vector v
     * @param vY y component of vector v
     * @return the dot product of the vector u and v (u·v)
     */
    public static double dotProduct(double uX, double uY, double vX, double vY) {
        return Math.fma(uX, vX, uY * vY);
    }

    /**
     * Computes the squared norm of the vector through its components.
     *
     * @param uX x component of vector u
     * @param uY y component of vector u
     * @return the squared norm of the vector u (‖u‖²)
     */
    public static double squaredNorm(double uX, double uY) {
        return Math.pow(uX, 2) + Math.pow(uY, 2);
    }

    /**
     * Computes the norm of the vector through its components.
     *
     * @param uX x component of vector u
     * @param uY y component of vector u
     * @return the norm of the vector u (‖u‖)
     */
    public static double norm(double uX, double uY) {
        return Math.sqrt(squaredNorm(uX, uY));
    }

    /**
     * Computes the length of the projection of the vector u (going from A to P) on the vector v
     * (going from A to B).
     *
     * @param aX x coordinate of the point A
     * @param aY y coordinate of the point A
     * @param bX x coordinate of the point B
     * @param bY y coordinate of the point B
     * @param pX x coordinate of the point P
     * @param pY y coordinate of the point P
     * @return the length of the projected vector u on v
     */
    public static double projectionLength(double aX, double aY, double bX, double bY, double pX,
                                          double pY) {
        double vX = bX - aX, vY = bY - aY;
        return dotProduct(pX - aX, pY - aY, vX, vY) / norm(vX, vY);
    }

}

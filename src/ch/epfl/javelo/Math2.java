package ch.epfl.javelo;

/**
 * Mathematical calculations, similar to <code>Math</code>.
 *
 * Non-instantiable.
 *
 * @author Lucas Jung (324724)
 */
public final class Math2 {

    private Math2() {}

    /**
     * Ceil division for positive integers.
     *
     * @param x numerator
     * @param y denominator
     * @return ceiling of <code>x</code>/<code>y</code> (⌈x/y⌉)
     * @throws IllegalArgumentException if x < 0 or y <= 0
     */
    public static int ceilDiv(int x, int y) {
        Preconditions.checkArgument(x >= 0);
        Preconditions.checkArgument(y > 0);
        return (x + y - 1) / y;
    }

    /**
     * @param y0 y value of the line where x=0
     * @param y1 y value of the line where x=1
     * @param x x value to interpolate with the line
     * @return the y coordinate of the point (<code>x</code>, y) standing on the line passing by (0,
     *         <code>y0</code>) and (1, <code>y1</code>)
     */
    public static double interpolate(double y0, double y1, double x) {
        return Math.fma(y1 - y0, x, y0);
    }

    /**
     * Limits the value of v to the interval [min, max].
     *
     * @param min the minimum value for v
     * @param v value to limit
     * @param max the maximum value for v
     * @return <code>min</code> when <code>v</code> < <code>min</code>, <code>max</code> when
     *         <code>v</code> > <code>max</code>, <code>v</code> otherwise
     * @throws IllegalArgumentException if <code>min</code> > <code>max</code>
     */
    public static int clamp(int min, int v, int max) {
        Preconditions.checkArgument(min <= max);
        return v < min ? min : v > max ? max : v;
    }

    /**
     * Limits the value of v to the interval [min, max].
     *
     * @param min the minimum value for v
     * @param v value to limit
     * @param max the maximum value for v
     * @return <code>min</code> when <code>v</code> < <code>min</code>, <code>max</code> when
     *         <code>v</code> > <code>max</code>, <code>v</code> otherwise
     * @throws IllegalArgumentException if <code>min</code> > <code>max</code>
     */
    public static double clamp(double min, double v, double max) {
        Preconditions.checkArgument(min <= max);
        return v < min ? min : v > max ? max : v;
    }

    /**
     * @param x argument
     * @return arsinh(<code>x</code>)
     */
    public static double asinh(double x) {
        return Math.log(x + Math.sqrt(1 + Math.pow(x, 2)));
    }

    /**
     * @param uX x composant of vector u
     * @param uY y composant of vector u
     * @param vX x composant of vector v
     * @param vY y composant of vector v
     * @return the dot product of the vector u and v (u·v)
     */
    public static double dotProduct(double uX, double uY, double vX, double vY) {
        return Math.fma(uX, vX, uY * vY);
    }

    /**
     * @param uX x composant of vector u
     * @param uY y composant of vector u
     * @return the squared norm of the vector u (‖u‖²)
     */
    public static double squaredNorm(double uX, double uY) {
        return Math.pow(uX, 2) + Math.pow(uY, 2);
    }

    /**
     * @param uX x composant of vector u
     * @param uY y composant of vector u
     * @return the norm of the vector u (‖u‖)
     */
    public static double norm(double uX, double uY) {
        return Math.sqrt(squaredNorm(uX, uY));
    }

    /**
     * @param aX x coordinate of the point A
     * @param aY y coordinate of the point A
     * @param bX x coordinate of the point B
     * @param bY y coordinate of the point B
     * @param pX x coordinate of the point P
     * @param pY y coordinate of the point P
     * @return length of the projection of the vector u (going from A to P) on the vector v (going
     *         from A to B)
     */
    public static double projectionLength(double aX, double aY, double bX, double bY, double pX,
                                          double pY) {
        double vX = bX - aX, vY = bY - aY;
        return dotProduct(pX - aX, pY - aY, vX, vY) / norm(vX, vY);
    }

}

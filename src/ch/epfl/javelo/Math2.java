package ch.epfl.javelo;

// FIXME allowed to change the arguments name ?

public final class Math2 {
    private Math2() {
    } // non-instantiable

    /**
     * @param x
     * @param y
     * @return ceiling of x/y (⌈x/y⌉)
     */
    public static int ceilDiv(int x, int y) {
        return (x + y - 1) / y;
    }

    /**
     * @param y0
     * @param y1
     * @param x
     * @return the y coordinate of the point (x, y) standing on the line passing by
     *         (0, y0) and (1, y1)
     */
    public static double interpolate(double y0, double y1, double x) {
        return Math.fma(y1 - y0, x, y0);
    }

    // FIXME is this javadoc ok ?
    /**
     * Works just like {@link #clamp(double, double, double)} except it takes
     * arguments of type int.
     *
     * @see #clamp(double, double, double)
     */
    public static int clamp(int min, int v, int max) {
        // FIXME better to cast rather than rewriting the method ?
        return (int) clamp((double) min, (double) v, (double) max);
    }

    /**
     * Limits the value of v to the interval [min, max].
     *
     * @param min the minimum value for v
     * @param v
     * @param max the maximum value for v
     * @return min when v < min, max when v > max, v otherwise
     */
    public static double clamp(double min, double v, double max) {
        return v < min ? min : v > max ? max : v;
    }

    /**
     * @param x
     * @return arsinh(x)
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
    public static double squaredNorme(double uX, double uY) {
        return Math.pow(uX, 2) + Math.pow(uY, 2);
    }

    /**
     * @param uX x composant of vector u
     * @param uY y composant of vector u
     * @return the norm of the vector u (‖u‖)
     */
    public static double norm(double uX, double uY) {
        return Math.sqrt(squaredNorme(uX, uY));
    }

    /**
     * @param aX x coordinate of the point A
     * @param aY y coordinate of the point A
     * @param bX x coordinate of the point B
     * @param bY y coordinate of the point B
     * @param pX x coordinate of the point P
     * @param pY y coordinate of the point P
     * @return length of the projection of the vector going from A to P on the
     *         vector going from A to B
     */
    public static double projectionLength(double aX, double aY, double bX, double bY, double pX, double pY) {
        double vX = bX - aX, vY = bY - aY;
        return dotProduct(pX - aX, pY - aY, vX, vY) / norm(vX, vY);
    }
}

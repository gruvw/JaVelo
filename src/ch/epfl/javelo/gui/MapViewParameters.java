package ch.epfl.javelo.gui;

import ch.epfl.javelo.projection.PointWebMercator;
import javafx.geometry.Point2D;

/**
 * Represents the parameters of the background map. (record)
 *
 * @param zoom zoom level
 * @param x    x coordinate (Web Mercator) of the top left corner of the displayed portion of the
 *             map
 * @param y    y coordinate (Web Mercator) of the top left corner of the displayed portion of the
 *             map
 * @author Lucas Jung (324724)
 * @author Florian Kolly (328313)
 */
public record MapViewParameters(int zoom, double x, double y) {


    /**
     * Returns a {@code MapViewParameters} with the top left corner positioned at the given
     * coordinates, keeps the same zoom level as the current instance.
     *
     * @param newX new top left x coordinate (Web Mercator)
     * @param newY new top left y coordinate (Web Mercator)
     * @return the new instance with the top left corner positioned at {@code (newX, newY)} with the
     *         same zoom level as the current instance
     */
    public MapViewParameters withMinXY(double newX, double newY) {
        return new MapViewParameters(zoom, newX, newY);
    }

    /**
     * Retrieves the point at the top left corner of the displayed portion as a 2D point.
     *
     * @return the 2D point corresponding to the top left corner
     */
    public Point2D topLeft() {
        return new Point2D(x, y);
    }

    /**
     * Retrieves the point with the given coordinates as a {@code PointWebMercator}.
     *
     * @param x x coordinate of the point
     * @param y y coordinate of the point
     * @return a {@code PointWebMercator} representing the point with coordinates ({@code x},
     *         {@code y})
     */
    public PointWebMercator pointAt(double x, double y) {
        return PointWebMercator.of(zoom, x, y);
    }

    /**
     * Retrieves the horizontal distance from the top left corner to the given point.
     *
     * @param point point in the Web Mercator projection
     * @return the horizontal distance from the top left corner to the given Web Mercator point
     */
    public double viewX(PointWebMercator point) {
        return point.xAtZoomLevel(zoom) - x;
    }

    /**
     * Retrieves the vertical distance from the top left corner to the given point.
     *
     * @param point point in the Web Mercator projection
     * @return the vertical distance from the top left corner to the given Web Mercator point
     */
    public double viewY(PointWebMercator point) {
        return point.yAtZoomLevel(zoom) - y;
    }

}

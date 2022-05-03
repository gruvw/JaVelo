package ch.epfl.javelo.gui;

import ch.epfl.javelo.projection.PointWebMercator;
import javafx.geometry.Point2D;

/**
 * Represents the parameters of the background map. (record)
 *
 * @param zoomLevel zoom level of the map
 * @param minX      x coordinate (Web Mercator) of the top left corner of the displayed portion of
 *                  the map
 * @param minY      y coordinate (Web Mercator) of the top left corner of the displayed portion of
 *                  the map
 *
 * @author Lucas Jung (324724)
 * @author Florian Kolly (328313)
 */
public record MapViewParameters(int zoomLevel, double minX, double minY) {

    /**
     * Returns a {@code MapViewParameters} with the top left corner shifted by {@code xDelta} and
     * {@code yDelta}.
     *
     * @param xDelta distance to move to the left
     * @param yDelta distance to move to the top
     * @return the new instance with the top left corner moved by {@code xDelta} and {@code yDelta}
     *         with the same zoom level as the current instance
     */
    public MapViewParameters withShiftedBy(double xDelta, double yDelta) {
        if (xDelta == 0 && yDelta == 0)
            return this;
        return new MapViewParameters(zoomLevel, minX - xDelta, minY - yDelta);
    }

    /**
     * Retrieves the point at the top left corner of the displayed portion as a 2D point.
     *
     * @return the 2D point corresponding to the top left corner
     */
    public Point2D topLeft() {
        return new Point2D(minX, minY);
    }

    /**
     * Retrieves a point relative to the top left corner {@code PointWebMercator}.
     * <p>
     * Note: {@code x} and {@code y} in the window's coordinates system.
     *
     * @param x x coordinate of the point relative to the top left corner
     * @param y y coordinate of the point relative to the top left corner
     * @return a {@code PointWebMercator} representing the point with coordinates ({@code x},
     *         {@code y}) relative to the top left corner
     */
    public PointWebMercator pointAt(double x, double y) {
        return PointWebMercator.of(zoomLevel, this.minX + x, this.minY + y);
    }

    /**
     * Retrieves the horizontal distance from the top left corner to the given point.
     *
     * @param point point in the Web Mercator projection
     * @return the horizontal distance from the top left corner to the given Web Mercator point
     */
    public double viewX(PointWebMercator point) {
        return point.xAtZoomLevel(zoomLevel) - minX;
    }

    /**
     * Retrieves the vertical distance from the top left corner to the given point.
     *
     * @param point point in the Web Mercator projection
     * @return the vertical distance from the top left corner to the given Web Mercator point
     */
    public double viewY(PointWebMercator point) {
        return point.yAtZoomLevel(zoomLevel) - minY;
    }

}

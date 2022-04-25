package ch.epfl.javelo.gui;

import ch.epfl.javelo.projection.PointWebMercator;
import javafx.geometry.Point2D;

/**
 * Represents the parameters of the background map. (record)
 *
 * @param zoomLevel zoom level of the map
 * @param x         x coordinate (Web Mercator) of the top left corner of the displayed portion of
 *                  the map
 * @param y         y coordinate (Web Mercator) of the top left corner of the displayed portion of
 *                  the map
 *
 * @author Lucas Jung (324724)
 * @author Florian Kolly (328313)
 */
public record MapViewParameters(int zoomLevel, double x, double y) {

    /**
     * Returns a {@code MapViewParameters} with the top left corner moved by {@code xDelta} and
     * {@code yDelta}.
     *
     * @param xDelta distance to move to the left
     * @param yDelta distance to move to the top
     * @return the new instance with the top left corner moved by {@code xDelta} and {@code yDelta}
     *         with the same zoom level as the current instance
     */
    public MapViewParameters withMinXY(double xDelta, double yDelta) {
        // FIXME: right way ?
        return new MapViewParameters(zoomLevel, x - xDelta, y - yDelta);
    }

    /**
     * Retrieves the point at the top left corner of the displayed portion as a 2D point.
     *
     * @return the 2D point corresponding to the top left corner
     */
    public Point2D topLeft() {
        // TODO: Lucas fix Point2D (drag)
        return new Point2D(x, y);
    }

    /**
     * Retrieves a point relative to the top left corner {@code PointWebMercator}.
     *
     * @param x x coordinate of the point relative to the top left corner
     * @param y y coordinate of the point relative to the top left corner
     * @return a {@code PointWebMercator} representing the point with coordinates ({@code x},
     *         {@code y}) relative to the top left corner
     */
    public PointWebMercator pointAt(double x, double y) {
        // FIXME: correct way ?
        return PointWebMercator.of(zoomLevel, this.x + x, this.y + y);
    }

    /**
     * Retrieves the horizontal distance from the top left corner to the given point.
     *
     * @param point point in the Web Mercator projection
     * @return the horizontal distance from the top left corner to the given Web Mercator point
     */
    public double viewX(PointWebMercator point) {
        return point.xAtZoomLevel(zoomLevel) - x;
    }

    /**
     * Retrieves the vertical distance from the top left corner to the given point.
     *
     * @param point point in the Web Mercator projection
     * @return the vertical distance from the top left corner to the given Web Mercator point
     */
    public double viewY(PointWebMercator point) {
        return point.yAtZoomLevel(zoomLevel) - y;
    }

}

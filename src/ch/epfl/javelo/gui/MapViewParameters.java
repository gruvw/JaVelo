package ch.epfl.javelo.gui;

import ch.epfl.javelo.projection.PointWebMercator;
import javafx.geometry.Point2D;

/**
 * Represents the parameters of the map background.
 * 
 * @param zoom zoom level
 * @param x    x coordinate of the top left corner of the shown portion of the map
 * @param y    y coordinate of the top left corner of the shown portion of the map
 */
public record MapViewParameters(int zoom, double x, double y) {

    public Point2D topLeft() {

    }

    public MapViewParameters withMinXY() {

    }

    public PointWebMercator pointAt(double x, double y) {

    }

    public double viewX(PointWebMercator point) {

    }

    public double viewY(PointWebMercator point) {

    }

}

package ch.epfl.javelo.gui;

import java.util.List;
import ch.epfl.javelo.projection.PointCh;
import ch.epfl.javelo.projection.PointWebMercator;
import ch.epfl.javelo.routing.Route;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.geometry.Point2D;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Polyline;

/**
 * Handles display and interactions with the route and the highlighted point.
 * <p>
 * Arguments are not checked.
 *
 * @author Lucas Jung (324724)
 * @author Florian Kolly (328313)
 */
public final class RouteManager {

    private final RouteBean routeBean;
    private final ReadOnlyObjectProperty<MapViewParameters> mapParamsProperty;

    private final Pane pane;
    private final Polyline line;
    private final Circle circle;

    /**
     * Radius of the highlighted position circle.
     */
    private static final int CIRCLE_RADIUS = 5;

    /**
     * Constructor of a route manager.
     *
     * @param routeBean         the bean of the route
     * @param mapParamsProperty JavaFx read only property containing the parameters of the
     *                          background map
     */
    public RouteManager(RouteBean routeBean,
                        ReadOnlyObjectProperty<MapViewParameters> mapParamsProperty) {
        this.routeBean = routeBean;
        this.mapParamsProperty = mapParamsProperty;

        this.line = new Polyline();
        this.line.setId("route");

        this.circle = new Circle(CIRCLE_RADIUS);
        this.circle.setId("highlight");

        this.pane = new Pane(this.line, this.circle);
        this.pane.setPickOnBounds(false); // don't block background events

        registerListeners();
        registerHandlers();

        draw();
    }

    /**
     * Returns the pane rendering the route and the highlighted point.
     *
     * @return the pane rendering the route and the highlighted point
     */
    public Pane pane() {
        return pane;
    }

    /**
     * Registers bindings and listeners.
     */
    private void registerListeners() {
        circle.visibleProperty()
              .bind(routeBean.highlightedPositionProperty()
                             .greaterThanOrEqualTo(0)
                             .and(routeBean.routeProperty().isNotNull()));

        line.visibleProperty().bind(routeBean.routeProperty().isNotNull());

        mapParamsProperty.addListener((p, o, n) -> {
            // Redraw line only when zoom changes, reposition it otherwise
            if (o.zoomLevel() == n.zoomLevel() && routeBean.isRouteValid()) {
                placeLine();
                placeCircle();
            } else
                draw();
        });

        routeBean.routeProperty().addListener(route -> draw());
        routeBean.highlightedPositionProperty().addListener(hp -> placeCircle());
    }

    /**
     * Registers event handlers.
     */
    private void registerHandlers() {
        circle.setOnMouseClicked(e -> {
            Route route = routeBean.route();
            double highlightedPosition = routeBean.highlightedPosition();
            int closestNodeId = route.nodeClosestTo(highlightedPosition);
            Point2D position = circle.localToParent(e.getX(), e.getY());
            PointCh point = mapParamsProperty.get()
                                             .pointAt(position.getX(), position.getY())
                                             .toPointCh();
            Waypoint wp = new Waypoint(point, closestNodeId);
            routeBean.waypoints()
                     .add(routeBean.indexOfNonEmptySegmentAt(highlightedPosition) + 1, wp);
        });
    }

    /**
     * Draws and positions the line representing the route and the circle representing the
     * highlighted position when a valid route exists.
     */
    private void draw() {
        if (!routeBean.isRouteValid())
            return;

        drawLine();
        placeCircle();
    }

    /**
     * Computes and draws the line from the route.
     */
    private void drawLine() {
        List<PointCh> points = routeBean.route().points();
        Double[] positions = new Double[points.size() * 2];
        int zoomLevel = mapParamsProperty.get().zoomLevel();
        for (int i = 0; i < points.size(); i++) {
            PointWebMercator point = PointWebMercator.ofPointCh(points.get(i));
            positions[i * 2] = point.xAtZoomLevel(zoomLevel);
            positions[i * 2 + 1] = point.yAtZoomLevel(zoomLevel);
        }
        line.getPoints().clear(); // clear old line
        line.getPoints().addAll(positions); // repopulate line
        placeLine();
    }

    /**
     * Moves the line to the correct location on the pane.
     */
    private void placeLine() {
        Point2D topLeft = mapParamsProperty.get().topLeft();
        line.setLayoutX(-topLeft.getX());
        line.setLayoutY(-topLeft.getY());
    }

    /**
     * Moves the circle to the correct location on the pane.
     */
    private void placeCircle() {
        if (Double.isNaN(routeBean.highlightedPosition()))
            return;
        MapViewParameters mapParams = mapParamsProperty.get();
        int zoomLevel = mapParams.zoomLevel();
        Point2D topLeft = mapParams.topLeft();
        PointCh pointCh = routeBean.route().pointAt(routeBean.highlightedPosition());
        PointWebMercator point = PointWebMercator.ofPointCh(pointCh);
        circle.setCenterX(point.xAtZoomLevel(zoomLevel) - topLeft.getX());
        circle.setCenterY(point.yAtZoomLevel(zoomLevel) - topLeft.getY());
    }

}

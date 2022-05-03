package ch.epfl.javelo.gui;

import java.util.List;
import java.util.function.Consumer;
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
    private final Consumer<String> errorConsumer;

    private final Pane pane;
    private final Polyline line;
    private final Circle circle;

    private static final int CIRCLE_RADIUS = 5;

    /**
     * Constructor of a route manager.
     *
     * @param routeBean         the bean of the route
     * @param mapParamsProperty JavaFx read only property containing the parameters of the
     *                          background map
     * @param errorConsumer     handles errors
     */
    public RouteManager(RouteBean routeBean,
                        ReadOnlyObjectProperty<MapViewParameters> mapParamsProperty,
                        Consumer<String> errorConsumer) {
        this.routeBean = routeBean;
        this.mapParamsProperty = mapParamsProperty;
        this.errorConsumer = errorConsumer;

        this.pane = new Pane();
        this.pane.setPickOnBounds(false); // don't block background events

        this.line = new Polyline();
        this.line.setId("route");
        this.pane.getChildren().add(this.line);

        this.circle = new Circle(CIRCLE_RADIUS);
        this.circle.setId("highlight");
        this.pane.getChildren().add(this.circle);

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
     * Registers listeners to the nodes in the scene.
     */
    private void registerListeners() {
        mapParamsProperty.addListener((p, o, n) -> {
            // Redraw line only when zoom changes, reposition it otherwise
            if (o.zoomLevel() == n.zoomLevel() && isRouteValid()) {
                placeLine();
                placeCircle();
            } else
                draw();
        });
        routeBean.routeProperty().addListener(route -> draw());
        routeBean.highlightedPositionProperty().addListener(hp -> placeCircle());
    }

    /**
     * Registers event handlers to the nodes in the scene.
     */
    private void registerHandlers() {
        circle.setOnMouseClicked(e -> {
            Route route = routeBean.route();
            double highlightedPosition = routeBean.highlightedPosition();
            int closestNodeId = route.nodeClosestTo(highlightedPosition);
            for (Waypoint wp : routeBean.waypoints())
                if (wp.closestNodeId() == closestNodeId) {
                    errorConsumer.accept("Un point de passage est déjà présent à cet endroit !");
                    return;
                }
            Point2D position = circle.localToParent(e.getX(), e.getY());
            PointCh point = mapParamsProperty.get()
                                             .pointAt(position.getX(), position.getY())
                                             .toPointCh();
            Waypoint wp = new Waypoint(point, closestNodeId);
            routeBean.waypoints().add(route.indexOfSegmentAt(highlightedPosition) + 1, wp);
        });
    }

    /**
     * Draws and positions the line representing the route and the circle when a valid route is
     * found, hides them otherwise.
     */
    private void draw() {
        if (!isRouteValid()) {
            line.setVisible(false);
            circle.setVisible(false);
            return;
        }

        drawLine();
        placeCircle();
        line.setVisible(true);
        circle.setVisible(true);
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
     * Move the circle to the correct location on the pane.
     */
    private void placeCircle() {
        MapViewParameters mapParams = mapParamsProperty.get();
        int zoomLevel = mapParams.zoomLevel();
        Point2D topLeft = mapParams.topLeft();
        PointCh pointCh = routeBean.route().pointAt(routeBean.highlightedPosition());
        PointWebMercator point = PointWebMercator.ofPointCh(pointCh);
        circle.setCenterX(point.xAtZoomLevel(zoomLevel) - topLeft.getX());
        circle.setCenterY(point.yAtZoomLevel(zoomLevel) - topLeft.getY());
    }

    /**
     * Checks wether the route in the {@code routeBean} exists or not.
     *
     * @return true if a route exists, false otherwise
     */
    private boolean isRouteValid() {
        return routeBean.route() != null;
    }

}

package ch.epfl.javelo.gui;

import java.util.List;
import java.util.function.Consumer;
import ch.epfl.javelo.projection.PointCh;
import ch.epfl.javelo.projection.PointWebMercator;
import ch.epfl.javelo.routing.Route;
import javafx.beans.property.ReadOnlyProperty;
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
    private final ReadOnlyProperty<MapViewParameters> mapParamsProperty;
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
                        ReadOnlyProperty<MapViewParameters> mapParamsProperty,
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
        this.circle.setVisible(false);
        this.pane.getChildren().add(circle);

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
            // Re-draw line only when zoom changes
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
            PointCh point = mapParamsProperty.getValue()
                                             .pointAt(position.getX(), position.getY())
                                             .toPointCh();
            Waypoint wp = new Waypoint(point, closestNodeId);
            routeBean.waypoints().add(route.indexOfSegmentAt(highlightedPosition) + 1, wp);
        });
    }

    // TODO: document private methods

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

    private void drawLine() {
        List<PointCh> points = routeBean.route().points();
        Double[] positions = new Double[points.size() * 2];
        int zoomLevel = mapParamsProperty.getValue().zoomLevel();
        for (int i = 0; i < points.size(); i++) {
            PointWebMercator point = PointWebMercator.ofPointCh(points.get(i));
            positions[i * 2] = point.xAtZoomLevel(zoomLevel);
            positions[i * 2 + 1] = point.yAtZoomLevel(zoomLevel);
        }
        line.getPoints().clear(); // clear old line
        line.getPoints().addAll(positions); // repopulate line
        placeLine();
    }

    private void placeLine() {
        MapViewParameters mapParams = mapParamsProperty.getValue();
        int zoomLevel = mapParams.zoomLevel();
        PointWebMercator topLeft = mapParams.pointAt(0, 0);
        line.setLayoutX(-topLeft.xAtZoomLevel(zoomLevel));
        line.setLayoutY(-topLeft.yAtZoomLevel(zoomLevel));
    }

    private void placeCircle() {
        MapViewParameters mapParams = mapParamsProperty.getValue();
        int zoomLevel = mapParams.zoomLevel();
        PointWebMercator topLeft = mapParams.pointAt(0, 0);
        PointCh pointCh = routeBean.route().pointAt(routeBean.highlightedPosition());
        PointWebMercator point = PointWebMercator.ofPointCh(pointCh);
        circle.setCenterX(point.xAtZoomLevel(zoomLevel) - topLeft.xAtZoomLevel(zoomLevel));
        circle.setCenterY(point.yAtZoomLevel(zoomLevel) - topLeft.yAtZoomLevel(zoomLevel));
    }

    private boolean isRouteValid() {
        return routeBean.route() != null;
    }

}

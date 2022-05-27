package ch.epfl.javelo.gui;

import java.util.function.Consumer;
import ch.epfl.javelo.data.Graph;
import ch.epfl.javelo.projection.PointCh;
import ch.epfl.javelo.projection.PointWebMercator;
import ch.epfl.javelo.routing.RoutePoint;
import javafx.beans.binding.Bindings;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Point2D;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;

import static ch.epfl.javelo.gui.ElevationProfileManager.DISABLED_VALUE;

/**
 * Handles display and superposition of the background map, the route and the waypoints panes.
 *
 * @author Lucas Jung (324724)
 * @author Florian Kolly (328313)
 */
public final class AnnotatedMapManager {

    /**
     * Default parameters of the map.
     */
    private static final MapViewParameters DEFAULT_MAP_PARAMETERS = new MapViewParameters(12,
                                                                                          543_200,
                                                                                          370_650);

    /**
     * Maximum distance, in pixels, between the mouse and the route to register the highlighted
     * position.
     */
    private static final int MOUSE_POSITION_THRESHOLD = 15;

    /**
     * Value used as the position of the highlighted point when there is none.
     */
    private static final Point2D DISABLED_POINT = new Point2D(DISABLED_VALUE, DISABLED_VALUE);

    private final StackPane pane;
    private final RouteBean routeBean;

    private final ObjectProperty<MapViewParameters> mapParamsProperty;
    private final ObjectProperty<Point2D> mousePositionProperty;
    private final DoubleProperty mousePositionOnRouteProperty;

    private final BaseMapManager baseMapManager;
    private final WaypointsManager waypointsManager;
    private final RouteManager routeManager;

    /**
     * Constructor of an annotated map manager.
     *
     * @param graph         graph of the routes
     * @param tileManager   tiles manager
     * @param routeBean     the route bean
     * @param errorConsumer errors handler
     */
    public AnnotatedMapManager(Graph graph,
                               TileManager tileManager,
                               RouteBean routeBean,
                               Consumer<String> errorConsumer) {
        this.routeBean = routeBean;

        this.mapParamsProperty = new SimpleObjectProperty<>(DEFAULT_MAP_PARAMETERS);
        this.mousePositionOnRouteProperty = new SimpleDoubleProperty(DISABLED_VALUE);
        this.mousePositionProperty = new SimpleObjectProperty<>(DISABLED_POINT);

        this.waypointsManager = new WaypointsManager(graph, mapParamsProperty,
                                                     this.routeBean.waypoints(), errorConsumer);
        this.baseMapManager = new BaseMapManager(tileManager, waypointsManager, mapParamsProperty);
        this.routeManager = new RouteManager(routeBean, mapParamsProperty);

        this.pane = new StackPane(baseMapManager.pane(), routeManager.pane(),
                                  waypointsManager.pane());
        this.pane.getStylesheets().add("map.css");

        registerHandlers();
    }

    /**
     * Returns the pane rendering the annotated map.
     *
     * @return the pane rendering the annotated map
     */
    public Pane pane() {
        return pane;
    }

    /**
     * Returns the property containing the position of the mouse on the route.
     *
     * @return the property containing the position of the mouse on the route
     */
    public ReadOnlyDoubleProperty mousePositionOnRouteProperty() {
        return mousePositionOnRouteProperty;
    }

    /**
     * Computes the position of the mouse along the route. (JavaFX binding)
     * <p>
     * The position of the mouse on the route is NaN if the mouse cursor if further away than
     * {@code MOUSE_POSITION_THRESHOLD} pixels from the route.
     *
     * @param mousePosition position of the mouse on the screen, in pixels relative to the top left
     *                      corner
     * @param routeBean     the route bean
     * @param mapParams     JavaFX property containing the parameters of the background map
     * @return the position of the mouse along the route
     */
    private static double computeMousePosition(Point2D mousePosition,
                                               RouteBean routeBean,
                                               MapViewParameters mapParams) {
        if (!routeBean.isRouteValid() || Double.isNaN(mousePosition.getX()))
            return DISABLED_VALUE;
        PointWebMercator cursorPoint = mapParams.pointAt(mousePosition.getX(),
                mousePosition.getY());
        PointCh cursorPointCh = cursorPoint.toPointCh();
        if (cursorPointCh == null) // cursor outside of Switzerland
            return DISABLED_VALUE;
        RoutePoint closestRoutePoint = routeBean.route().pointClosestTo(cursorPointCh);
        PointWebMercator routePoint = PointWebMercator.ofPointCh(closestRoutePoint.point());
        Point2D cursorPointCoords = new Point2D(cursorPoint.xAtZoomLevel(mapParams.zoomLevel()),
                                                cursorPoint.yAtZoomLevel(mapParams.zoomLevel()));
        Point2D routePointCoords = new Point2D(routePoint.xAtZoomLevel(mapParams.zoomLevel()),
                                               routePoint.yAtZoomLevel(mapParams.zoomLevel()));
        return cursorPointCoords.distance(routePointCoords) <= MOUSE_POSITION_THRESHOLD
                ? closestRoutePoint.position()
                : DISABLED_VALUE;
    }

    /**
     * Registers handlers and bindings.
     */
    private void registerHandlers() {
        mousePositionOnRouteProperty.bind(Bindings.createDoubleBinding(
                () -> computeMousePosition(mousePositionProperty.get(), routeBean,
                        mapParamsProperty.get()),
                mousePositionProperty, routeBean.routeProperty(), mapParamsProperty));

        pane.setOnMouseMoved(e -> mousePositionProperty.set(new Point2D(e.getX(), e.getY())));
        // Solves bug about highlighted position when mouse dragged or drag exited
        pane.setOnMouseDragged(e -> mousePositionProperty.set(new Point2D(e.getX(), e.getY())));
        pane.setOnMouseExited(e -> mousePositionProperty.set(DISABLED_POINT));
    }

}

package ch.epfl.javelo.gui;

import java.util.function.Consumer;
import ch.epfl.javelo.data.Graph;
import ch.epfl.javelo.projection.PointWebMercator;
import ch.epfl.javelo.routing.RoutePoint;
import javafx.beans.InvalidationListener;
import javafx.beans.binding.Bindings;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Point2D;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;

/**
 * Handles display of the background map, the route and the waypoints.
 * <p>
 * Arguments are not checked.
 *
 * @author Lucas Jung (324724)
 * @author Florian Kolly (328313)
 */
public final class AnnotatedMapManager {

    /**
     * Default zoom level of the map.
     */
    private static final int DEFAULT_ZOOM = 12;

    /**
     * Default X coordinate for the top left corner of the map, in the Web Mercator system.
     */
    private static final int DEFAULT_MIN_X = 543200;

    /**
     * Default Y coordinate for the top left corner of the map, in the Web Mercator system.
     */
    private static final int DEFAULT_MIN_Y = 370650;

    /**
     *
     */
    private static final int MOUSE_POSITION_THRESHOLD = 15;

    private static final double DISABLED_VALUE = Double.NaN;
    private static final Point2D DISABLED_POINT = new Point2D(DISABLED_VALUE, DISABLED_VALUE);

    private final StackPane pane;
    private final RouteBean routeBean;

    private final ObjectProperty<MapViewParameters> mapParamsProperty;
    private final DoubleProperty mousePositionOnRouteProperty;
    private final ObjectProperty<Point2D> mousePositionProperty;

    private final BaseMapManager baseMapManager;
    private final WaypointsManager waypointsManager;
    private final RouteManager routeManager;

    private InvalidationListener listener;

    /**
     *
     * @param graph
     * @param tileManager
     * @param routeBean
     * @param errorConsumer
     */
    public AnnotatedMapManager(Graph graph,
                               TileManager tileManager,
                               RouteBean routeBean,
                               Consumer<String> errorConsumer) {
        this.routeBean = routeBean;

        this.mapParamsProperty = new SimpleObjectProperty<>(new MapViewParameters(DEFAULT_ZOOM,
                                                                                  DEFAULT_MIN_X,
                                                                                  DEFAULT_MIN_Y));
        this.mousePositionProperty = new SimpleObjectProperty<>(Point2D.ZERO); // TODO init with nan
        this.mousePositionOnRouteProperty = new SimpleDoubleProperty(DISABLED_VALUE);

        this.waypointsManager = new WaypointsManager(graph, mapParamsProperty,
                                                     this.routeBean.waypoints(), errorConsumer);
        this.baseMapManager = new BaseMapManager(tileManager, waypointsManager, mapParamsProperty);
        this.routeManager = new RouteManager(routeBean, mapParamsProperty);

        this.pane = new StackPane(baseMapManager.pane(), routeManager.pane(),
                                  waypointsManager.pane());
        this.pane.getStylesheets().add("map.css");

        registerListeners();
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
     * <p>
     * The position of the mouse on the route is NaN if the mouse cursor if further away than 15
     * pixels from the route.
     *
     * @return the property containing the position of the mouse on the route
     */
    public ReadOnlyDoubleProperty mousePositionOnRouteProperty() {
        return mousePositionOnRouteProperty;
    }

    /**
     * Registers listeners and bindings.
     */
    private void registerListeners() {
        mousePositionOnRouteProperty.bind(Bindings.createDoubleBinding(() -> {
            // System.out.println("called 2");
            Point2D mousePosition = mousePositionProperty.get();
            if (routeBean.route() == null || mousePosition.getX() == DISABLED_VALUE)
                return DISABLED_VALUE;
            MapViewParameters mapParams = mapParamsProperty.get();
            Point2D mousePointCoords = new Point2D(mapParams.minX() + mousePosition.getX(),
                                                   mapParams.minY() + mousePosition.getY());
            PointWebMercator cursorPoint = PointWebMercator.of(mapParams.zoomLevel(),
                    mousePointCoords.getX(), mousePointCoords.getY());
            RoutePoint routePoint = routeBean.route().pointClosestTo(cursorPoint.toPointCh());
            PointWebMercator mapPoint = PointWebMercator.ofPointCh(routePoint.point());
            Point2D mapPointCoords = new Point2D(mapPoint.xAtZoomLevel(mapParams.zoomLevel()),
                                                 mapPoint.yAtZoomLevel(mapParams.zoomLevel()));
            return mousePointCoords.distance(mapPointCoords) <= MOUSE_POSITION_THRESHOLD
                    ? routePoint.position()
                    : DISABLED_VALUE;
        }, mousePositionProperty, routeBean.routeProperty(), mapParamsProperty));

        listener = e -> {
            // System.out.println("called");
        };
        mousePositionProperty.addListener(listener);
        pane.setOnMouseMoved(e -> {
            Point2D p = new Point2D(e.getX(), e.getY());
            // System.out.println("Moved " + p);
            // System.out.println(mousePositionProperty.get());
            mousePositionProperty.set(p);
            // System.out.println(mousePositionProperty.get());
        });
        // TODO test + comment
        pane.setOnMouseDragged(e -> mousePositionProperty.set(new Point2D(e.getX(), e.getY())));
        pane.setOnMouseExited(e -> mousePositionProperty.set(DISABLED_POINT));
        pane.setOnMouseDragExited(e -> mousePositionProperty.set(DISABLED_POINT));
    }

}

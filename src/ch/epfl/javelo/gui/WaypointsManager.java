package ch.epfl.javelo.gui;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import ch.epfl.javelo.data.Graph;
import ch.epfl.javelo.projection.PointCh;
import ch.epfl.javelo.projection.PointWebMercator;
import javafx.beans.property.ObjectProperty;
import javafx.collections.ObservableList;
import javafx.collections.ListChangeListener.Change;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.layout.Pane;
import javafx.scene.shape.SVGPath;

/**
 * Manager for the display and the interaction with the waypoints.
 *
 * @author Lucas Jung (324724)
 * @author Florian Kolly (328313)
 */
public final class WaypointsManager {

    private final Graph graph;
    private final ObjectProperty<MapViewParameters> mapParametersProperty;
    private final ObservableList<Waypoint> waypoints;
    private final Consumer<String> errorConsumer;

    private final Pane pane;

    private Point2D lastMousePosition;
    private List<Group> pins;

    /**
     * Constructor of a waypoints manager.
     *
     * @param graph         graph of the routes
     * @param mapParameters property containing the parameters of the background map
     * @param waypoints     list of all the waypoints
     * @param errorConsumer consumer to handle errors
     */
    public WaypointsManager(Graph graph,
                            ObjectProperty<MapViewParameters> mapParametersProperty,
                            ObservableList<Waypoint> waypoints,
                            Consumer<String> errorConsumer) {
        this.graph = graph;
        this.mapParametersProperty = mapParametersProperty;
        this.waypoints = waypoints;
        this.errorConsumer = errorConsumer;
        this.pins = new ArrayList<>();
        this.pane = new Pane();
        this.pane.setPickOnBounds(false);
        mapParametersProperty.addListener((p, o, n) -> positionWaypoints());
        waypoints.addListener((Change<? extends Waypoint> wp) -> redrawWaypoints());
        redrawWaypoints();
    }

    /**
     * Returns the pane rendering the waypoints.
     *
     * @return the pane rendering the waypoints
     */
    public Pane pane() {
        return pane;
    }

    /**
     * Adds a waypoint at position {@code (x, y)} on the pane. A waypoint is added only if there is
     * at least one node in a square of side length of 1000 meters centered on the given
     * coordinates.
     *
     * @return true if the waypoint was added, false otherwise
     */
    public boolean addWaypoint(double x, double y) {
        int zoomLevel = mapParametersProperty.get().zoomLevel();
        PointWebMercator centerOfClick = PointWebMercator.of(zoomLevel,
                mapParametersProperty.get().minX() + x, mapParametersProperty.get().minY() + y);
        PointCh point = centerOfClick.toPointCh();
        int closestNodeId = graph.nodeClosestTo(point, 500);
        if (closestNodeId == -1) {
            errorConsumer.accept("Aucune route à proximité !");
            return false;
        }
        Waypoint wp = new Waypoint(point, closestNodeId);
        waypoints.add(wp);
        return true;
    }

    /**
     * Generates the {@code Group} objects for each waypoints.
     */
    private void createWaypoints() {
        for (int i = 0; i < waypoints.size(); i++) {
            Group pin = new Group();
            pin.getStyleClass().add("pin");
            // First SVG child (outside contour)
            SVGPath sv1 = new SVGPath();
            sv1.setContent("M-8-20C-5-14-2-7 0 0 2-7 5-14 8-20 20-40-20-40-8-20");
            sv1.getStyleClass().add("pin_outside");
            pin.getChildren().add(sv1);

            // Second SVG child (interior white disk)
            SVGPath sv2 = new SVGPath();
            sv2.setContent("M0-23A1 1 0 000-29 1 1 0 000-23");
            sv2.getStyleClass().add("pin_inside");
            pin.getChildren().add(sv2);

            // Add style to marker
            if (i == 0)
                pin.getStyleClass().add("first");
            else if (i == waypoints.size() - 1)
                pin.getStyleClass().add("last");
            else
                pin.getStyleClass().add("middle");

            // Add marker to pane
            pane.getChildren().add(pin);

            // Remove marker control
            int waypointIndex = i;
            pin.setOnMouseClicked(e -> {
                if (e.isStillSincePress()) {
                    pane.getChildren().remove(pin);
                    waypoints.remove(waypointIndex);
                }
            });

            // Move marker control
            pin.setOnMousePressed(e -> this.lastMousePosition = new Point2D(e.getX(), e.getY()));
            pin.setOnMouseDragged(e -> {
                Point2D deltaPosition = new Point2D(e.getX(), e.getY()).subtract(lastMousePosition);
                System.out.println(deltaPosition);
                pin.setLayoutX(pin.getLayoutX() + deltaPosition.getX());
                pin.setLayoutY(pin.getLayoutY() + deltaPosition.getY());
                this.lastMousePosition = new Point2D(e.getX(), e.getY());
            });

            pin.setOnMouseReleased(e -> {
                this.lastMousePosition = null;
                if (!e.isStillSincePress()) {
                    // TODO: change position of waypoint
                    // FIXME: Method to get the PointCh from the mouse position (code is reused from
                    // constructor)
                    int zoomLevel = mapParametersProperty.get().zoomLevel();
                    PointWebMercator centerOfClick = PointWebMercator.of(zoomLevel,
                            mapParametersProperty.get().minX() + e.getX(),
                            mapParametersProperty.get().minY() + e.getY());
                    Waypoint wp = new Waypoint(centerOfClick.toPointCh(),
                                               waypoints.get(waypointIndex).closestNodeId());
                    waypoints.set(waypointIndex, wp);
                }
            });
            pins.add(pin);
        }
    }

    /**
     * Positions all waypoints on the map.
     */
    private void positionWaypoints() {
        for (int i = 0; i < pins.size(); i++) {
            PointWebMercator point = PointWebMercator.ofPointCh(waypoints.get(i).position());
            pins.get(i).setLayoutX(mapParametersProperty.get().viewX(point));
            pins.get(i).setLayoutY(mapParametersProperty.get().viewY(point));
        }
    }

    private void redrawWaypoints() {
        pane.getChildren().clear();
        this.pins = new ArrayList<>();
        createWaypoints();
        positionWaypoints();
    }

}

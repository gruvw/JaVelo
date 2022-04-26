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

    private static final int SEARCH_DISTANCE = 500;
    private static final String CONTOUR_SVG = "M-8-20C-5-14-2-7 0 0 2-7 5-14 8-20 20-40-20-40-8-20";
    private static final String INTERIOR_SVG = "M0-23A1 1 0 000-29 1 1 0 000-23";

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
        this.pane.setPickOnBounds(false); // Send to background
        mapParametersProperty.addListener((p, o, n) -> positionPins());
        waypoints.addListener((Change<? extends Waypoint> wp) -> redrawPins());
        redrawPins();
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
     * at least one node in a square of side length of 1000 meters centered at the given
     * coordinates.
     *
     * @return true if the waypoint was added, false otherwise
     */
    public boolean addWaypoint(double x, double y) {
        PointCh point = mapParametersProperty.get().pointAt(x, y).toPointCh();
        Waypoint wp = waypointAt(point);
        if (wp == null)
            return false;
        waypoints.add(wp);
        return true;
    }

    private void redrawPins() {
        this.pane.getChildren().clear();
        this.pins.clear();
        createPins();
        positionPins();
    }

    /**
     * Generates the {@code Group} objects for each waypoint.
     */
    private void createPins() {
        for (int i = 0; i < waypoints.size(); i++) {
            Group pin = new Group();
            pin.getStyleClass().add("pin");

            // First SVG child (outside contour)
            SVGPath outlineSvg = new SVGPath();
            outlineSvg.setContent(CONTOUR_SVG);
            outlineSvg.getStyleClass().add("pin_outside");
            pin.getChildren().add(outlineSvg);

            // Second SVG child (interior white disk)
            SVGPath interiorSvg = new SVGPath();
            interiorSvg.setContent(INTERIOR_SVG);
            interiorSvg.getStyleClass().add("pin_inside");
            pin.getChildren().add(interiorSvg);

            // Add style to marker
            if (i == 0)
                pin.getStyleClass().add("first");
            else if (i == waypoints.size() - 1)
                pin.getStyleClass().add("last");
            else
                pin.getStyleClass().add("middle");

            pane.getChildren().add(pin);
            // FIXME: zoom does not work when mouse is over pin (try next line)
            // pin.setPickOnBounds(false);

            // Remove marker control
            final int waypointIndex = i;
            pin.setOnMouseClicked(e -> {
                if (e.isStillSincePress())
                    waypoints.remove(waypointIndex);
            });

            // Move marker control
            pin.setOnMousePressed(
                    e -> this.lastMousePosition = new Point2D(e.getSceneX(), e.getSceneY()));
            pin.setOnMouseDragged(e -> {
                Point2D movement = new Point2D(e.getSceneX(), e.getSceneY()).subtract(
                        lastMousePosition);
                pin.setLayoutX(pin.getLayoutX() + movement.getX());
                pin.setLayoutY(pin.getLayoutY() + movement.getY());
                this.lastMousePosition = new Point2D(e.getSceneX(), e.getSceneY());
            });

            // FIXME: slight displacement to the top when mouse is released
            pin.setOnMouseReleased(e -> {
                this.lastMousePosition = null;
                if (!e.isStillSincePress()) {
                    PointCh point = mapParametersProperty.get()
                                                         .pointAt(e.getSceneX() - e.getX(),
                                                                 e.getSceneY() - e.getY())
                                                         .toPointCh();
                    Waypoint wp = waypointAt(point);
                    // TODO: else reposition point to its starting position (currently throws error)
                    if (wp != null)
                        waypoints.set(waypointIndex, wp);
                }
            });
            pins.add(pin);
        }
    }

    /**
     * Positions all waypoints on the map.
     */
    private void positionPins() {
        for (int i = 0; i < pins.size(); i++) {
            PointWebMercator point = PointWebMercator.ofPointCh(waypoints.get(i).position());
            pins.get(i).setLayoutX(mapParametersProperty.get().viewX(point));
            pins.get(i).setLayoutY(mapParametersProperty.get().viewY(point));
        }
    }

    /**
     * Creates the waypoint at a given position in Switzerland.
     *
     * @param point the position of the waypoint in Switzerland
     * @return the waypoint at the given position in Switzerland if a graph node is found close to
     *         the position
     */
    private Waypoint waypointAt(PointCh point) {
        int closestNodeId = graph.nodeClosestTo(point, SEARCH_DISTANCE);
        if (closestNodeId == -1) {
            errorConsumer.accept("Aucune route à proximité !");
            return null;
        }
        return new Waypoint(point, closestNodeId);
    }

}

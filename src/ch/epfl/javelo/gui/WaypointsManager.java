package ch.epfl.javelo.gui;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import ch.epfl.javelo.data.Graph;
import ch.epfl.javelo.projection.PointCh;
import ch.epfl.javelo.projection.PointWebMercator;
import javafx.application.Platform;
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
    private final ObjectProperty<MapViewParameters> mapParamsProperty;
    private final ObservableList<Waypoint> waypoints;
    private final Consumer<String> errorConsumer;

    private final Pane pane;

    private Point2D lastMousePosition;
    private Point2D lastValidPinPosition;
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
     * @param errorConsumer errors handler
     */
    public WaypointsManager(Graph graph,
                            ObjectProperty<MapViewParameters> mapParamsProperty,
                            ObservableList<Waypoint> waypoints,
                            Consumer<String> errorConsumer) {
        this.graph = graph;
        this.mapParamsProperty = mapParamsProperty;
        this.waypoints = waypoints;
        this.errorConsumer = errorConsumer;

        this.pins = new ArrayList<>();
        this.pane = new Pane();
        this.pane.setPickOnBounds(false); // don't block background events

        this.mapParamsProperty.addListener((p, o, n) -> positionPins());
        this.waypoints.addListener((Change<? extends Waypoint> wp) -> redrawPins());

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
        PointCh point = mapParamsProperty.get().pointAt(x, y).toPointCh();
        Waypoint wp = waypointAt(point);
        if (wp == null)
            return false;
        waypoints.add(wp);
        return true;
    }

    /**
     * Recreates and positions every pin (one for each waypoint).
     */
    private void redrawPins() {
        pane.getChildren().clear();
        pins.clear();

        createPins();
        positionPins();
    }

    /**
     * Generates the pins ({@code Group} objects) for every waypoint.
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

            pane.getChildren().add(pin);

            // CHANGE: remove part that zooms over waypoint
            // Cascade zoom event from waypoint to map pane
            Platform.runLater(() -> pin.setOnScroll(pane.getParent()
                                                        .getChildrenUnmodifiable()
                                                        .filtered(n -> n.getId() == "mapPane")
                                                        .get(0) // map pane always exists
                                                        .getOnScroll()));

            registerPinHandlers(pin, i);

            // Add style to marker
            if (i == 0)
                pin.getStyleClass().add("first");
            else if (i == waypoints.size() - 1)
                pin.getStyleClass().add("last");
            else
                pin.getStyleClass().add("middle");
            pins.add(pin);
        }
    }

    /**
     * Registers event handlers to a given pin.
     *
     * @param pin           the pin that will register the event handlers
     * @param waypointIndex the index of the waypoint that the given pin represents
     */
    private void registerPinHandlers(Group pin, int waypointIndex) {
        // Remove marker control
        pin.setOnMouseClicked(e -> {
            if (e.isStillSincePress())
                waypoints.remove(waypointIndex);
        });

        // Move marker control
        pin.setOnMousePressed(e -> {
            lastMousePosition = new Point2D(e.getX(), e.getY());
            lastValidPinPosition = new Point2D(pin.getLayoutX(), pin.getLayoutY());
        });
        pin.setOnMouseDragged(e -> {
            Point2D movement = new Point2D(e.getX(), e.getY()).subtract(lastMousePosition);
            pin.setLayoutX(pin.getLayoutX() + movement.getX());
            pin.setLayoutY(pin.getLayoutY() + movement.getY());
        });
        pin.setOnMouseReleased(e -> {
            Point2D movement = new Point2D(e.getX(), e.getY()).subtract(lastMousePosition);
            if (!e.isStillSincePress()) {
                PointCh point = mapParamsProperty.get()
                                                 .pointAt(pin.getLayoutX() + movement.getX(),
                                                         pin.getLayoutY() + movement.getY())
                                                 .toPointCh();
                Waypoint wp = waypointAt(point);
                if (wp == null) {
                    pin.setLayoutX(lastValidPinPosition.getX());
                    pin.setLayoutY(lastValidPinPosition.getY());
                } else
                    waypoints.set(waypointIndex, wp);
            }
            lastValidPinPosition = null;
            lastMousePosition = null;
        });
    }

    /**
     * Positions all waypoints on the map.
     */
    private void positionPins() {
        for (int i = 0; i < pins.size(); i++) {
            PointWebMercator point = PointWebMercator.ofPointCh(waypoints.get(i).point());
            pins.get(i).setLayoutX(mapParamsProperty.get().viewX(point));
            pins.get(i).setLayoutY(mapParamsProperty.get().viewY(point));
        }
    }

    /**
     * Creates the waypoint at a given position in Switzerland.
     *
     * @param point the position of the waypoint in Switzerland
     * @return the waypoint at the given position in Switzerland if a graph node is found close to
     *         the position, {@code null} otherwise
     */
    private Waypoint waypointAt(PointCh point) {
        // Point could be null if set outside of Switzerland
        int closestNodeId = point != null ? graph.nodeClosestTo(point, SEARCH_DISTANCE) : -1;
        if (closestNodeId == -1) {
            errorConsumer.accept("Aucune route à proximité !");
            return null;
        }
        return new Waypoint(point, closestNodeId);
    }

}

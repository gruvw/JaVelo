package ch.epfl.javelo.gui;

import java.util.function.Consumer;
import ch.epfl.javelo.data.Graph;
import javafx.beans.property.ObjectProperty;
import javafx.collections.ObservableList;
import javafx.scene.canvas.Canvas;
import javafx.scene.layout.Pane;

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
    private final Canvas canvas;

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
        this.canvas = new Canvas();
        this.pane = new Pane();
        this.pane.setPickOnBounds(false);
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
        System.out.println("New waypoint");
        return false;
    }

}

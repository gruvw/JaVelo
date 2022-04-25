package ch.epfl.javelo.gui;

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
        this.pane = new Pane();
        this.pane.setPickOnBounds(false);
        pane.widthProperty().addListener((p, o, n) -> redrawWaypoints());
        pane.heightProperty().addListener((p, o, n) -> redrawWaypoints());
        mapParametersProperty.addListener((p, o, n) -> redrawWaypoints());
        waypoints.addListener((Change<? extends Waypoint> wp) -> redrawWaypoints());
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
                mapParametersProperty.get().x() + x, mapParametersProperty.get().y() + y);
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

    private Group createWaypoint() {
        Group wpGroup = new Group();
        wpGroup.getStyleClass().add("pin");
        SVGPath sv1 = new SVGPath();
        sv1.setContent("M-8-20C-5-14-2-7 0 0 2-7 5-14 8-20 20-40-20-40-8-20");
        sv1.getStyleClass().add("pin_outside");
        SVGPath sv2 = new SVGPath();
        sv2.setContent("M0-23A1 1 0 000-29 1 1 0 000-23");
        sv2.getStyleClass().add("pin_inside");
        wpGroup.getChildren().add(sv1);
        wpGroup.getChildren().add(sv2);
        pane.getChildren().add(wpGroup);
        return wpGroup;
    }

    private void positionWaypoint(Group wpGroup, Waypoint wp) {
        PointWebMercator point = PointWebMercator.ofPointCh(wp.position());
        wpGroup.setLayoutX(mapParametersProperty.get().viewX(point));
        wpGroup.setLayoutY(mapParametersProperty.get().viewY(point));
    }

    private void redrawWaypoints() {
        pane.getChildren().clear();
        for (int i = 0; i < waypoints.size(); i++) {
            Group wpGroup = createWaypoint();
            if (i == 0)
                wpGroup.getStyleClass().add("first");
            else if (i == waypoints.size() - 1)
                wpGroup.getStyleClass().add("last");
            else
                wpGroup.getStyleClass().add("middle");
            positionWaypoint(wpGroup, waypoints.get(i));
        }
    }

}

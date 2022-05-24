package ch.epfl.javelo.gui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import ch.epfl.javelo.routing.ElevationProfile;
import ch.epfl.javelo.routing.ElevationProfileComputer;
import ch.epfl.javelo.routing.MultiRoute;
import ch.epfl.javelo.routing.Route;
import ch.epfl.javelo.routing.RouteComputer;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ListChangeListener.Change;

/**
 * JavaFX bean containing the JavaFX properties of the waypoints and the corresponding route.
 * (JavaFX Bean)
 *
 * @author Lucas Jung (324724)
 * @author Florian Kolly (328313)
 */
public final class RouteBean {

    private final RouteComputer routeComputer;
    private final ObservableList<Waypoint> waypoints;
    private final ObjectProperty<Route> routeProperty;
    private final ObjectProperty<ElevationProfile> elevationProfileProperty;
    private final DoubleProperty highlightedPositionProperty;

    /**
     * Cache memory storing every {@code SingleRoute} used in the current route.
     */
    private final HashMap<NodeIdPair, Route> computedRoutes;

    /**
     * Maximum distance, in meters, between two elevation profile samples.
     */
    private static final int MAX_STEP_LENGTH = 5;

    /**
     * Constructor of a RouteBean.
     *
     * @param routeComputer a route planner
     */
    public RouteBean(RouteComputer routeComputer) {
        this.routeComputer = routeComputer;
        this.computedRoutes = new HashMap<>();

        this.waypoints = FXCollections.observableArrayList();
        this.routeProperty = new SimpleObjectProperty<>();
        this.elevationProfileProperty = new SimpleObjectProperty<>();
        this.highlightedPositionProperty = new SimpleDoubleProperty();

        this.waypoints.addListener((Change<? extends Waypoint> wp) -> computeRoute());

        computeRoute();
    }

    /**
     * Returns the observable list of waypoints.
     *
     * @return the observable list containing the waypoints
     */
    public ObservableList<Waypoint> waypoints() {
        return waypoints;
    }

    /**
     * Returns the property containing the route, as read-only.
     *
     * @return the property containing the route
     */
    public ReadOnlyObjectProperty<Route> routeProperty() {
        return routeProperty;
    }

    /**
     * Returns the route from the corresponding property.
     *
     * @return the route from the corresponding property
     */
    public Route route() {
        return routeProperty.get();
    }

    /**
     * Returns the property containing the elevation profile, as read-only.
     *
     * @return the property containing the elevation profile
     */
    public ReadOnlyObjectProperty<ElevationProfile> elevationProfileProperty() {
        return elevationProfileProperty;
    }

    /**
     * Returns the profile from the corresponding property.
     *
     * @return the profile from the corresponding property
     */
    public ElevationProfile elevationProfile() {
        return elevationProfileProperty.get();
    }

    /**
     * Returns the property containing the highlighted position, as read-only.
     *
     * @return the property containing the highlighted position
     */
    public DoubleProperty highlightedPositionProperty() {
        return highlightedPositionProperty;
    }

    /**
     * Returns the highlighted position from the corresponding property.
     *
     * @return the highlighted position from the corresponding property
     */
    public double highlightedPosition() {
        return highlightedPositionProperty.get();
    }

    /**
     * Checks whether the route exists or not.
     *
     * @return true if a route exists, false otherwise
     */
    public boolean isRouteValid() {
        return routeProperty.get() != null;
    }

    /**
     * Retrieves the index of the segment at a given position, ignoring the empty segments.
     *
     * @param position position on the route, in meters
     * @return the index of the segment at the position {@code position}
     */
    public int indexOfNonEmptySegmentAt(double position) {
        int index = route().indexOfSegmentAt(position);
        for (int i = 0; i <= index; i++) {
            int n1 = waypoints.get(i).closestNodeId();
            int n2 = waypoints.get(i + 1).closestNodeId();
            if (n1 == n2)
                index++;
        }
        return index;
    }

    /**
     * Computes the best routes between every waypoint and combine them into a {@code MultiRoute}.
     * If any of the route is {@code null} or if there are strictly less than 2 waypoints, the route
     * and its profile are set to {@code null}.
     */
    private void computeRoute() {
        if (waypoints.size() < 2) {
            emptyRoute();
            computedRoutes.clear();
            return;
        }
        List<Route> segments = new ArrayList<>();
        Set<NodeIdPair> keysToKeep = new HashSet<>();
        for (int i = 0; i < waypoints.size() - 1; i++) {
            int startNodeId = waypoints.get(i).closestNodeId();
            int destinationNodeId = waypoints.get(i + 1).closestNodeId();
            // Don't compute route if start and destination are associated to the same node id
            if (startNodeId == destinationNodeId)
                continue;
            NodeIdPair key = new NodeIdPair(startNodeId, destinationNodeId);
            Route bestRoute = computedRoutes.containsKey(key) ? computedRoutes.get(key)
                    : routeComputer.bestRouteBetween(startNodeId, destinationNodeId);
            computedRoutes.putIfAbsent(key, bestRoute);
            if (bestRoute == null) { // route could not be found
                emptyRoute();
                return;
            }
            keysToKeep.add(key);
            segments.add(bestRoute);
        }
        computedRoutes.keySet().retainAll(keysToKeep);
        Route combinedRoute = new MultiRoute(segments);
        ElevationProfile profile = ElevationProfileComputer.elevationProfile(combinedRoute,
                MAX_STEP_LENGTH);
        routeProperty.set(combinedRoute);
        elevationProfileProperty.set(profile);
    }

    private void emptyRoute() {
        routeProperty.set(null);
        elevationProfileProperty.set(null);
    }

    /**
     * Pair of node ids representing the start and the end of a route between two waypoints.
     * (record)
     *
     * @param startNodeId       id of the starting node
     * @param destinationNodeId id of the destination node
     */
    private record NodeIdPair(int startNodeId, int destinationNodeId) {

    }

}

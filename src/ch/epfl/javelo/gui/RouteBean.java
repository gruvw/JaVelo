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
import javafx.util.Pair;

/**
 * Beans containing the properties about the waypoints and the corresponding route. (JavaFX Bean)
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

    private final HashMap<Pair<Integer, Integer>, Route> computedRoutes;

    private static final int MAX_STEP_LENGTH = 5;

    /**
     * Constructor of a RouteBean.
     */
    public RouteBean(RouteComputer routeComputer) {
        this.routeComputer = routeComputer;
        this.computedRoutes = new HashMap<>();

        this.waypoints = FXCollections.observableArrayList();
        this.routeProperty = new SimpleObjectProperty<>();
        this.elevationProfileProperty = new SimpleObjectProperty<>();
        this.highlightedPositionProperty = new SimpleDoubleProperty();

        this.waypoints.addListener((Change<? extends Waypoint> wp) -> computeBestRoute());
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
     * Returns the property containing the route, as read only.
     *
     * @return the property containing the route
     */
    public ReadOnlyObjectProperty<Route> routeProperty() {
        return routeProperty;
    }

    /**
     * Returns the property containing the elevationProfile.
     *
     * @return the property containing the elevationProfile
     */
    public ReadOnlyObjectProperty<ElevationProfile> elevationProfileProperty() {
        return elevationProfileProperty;
    }

    /**
     * Returns the property containing the highlightedPosition, as read-only.
     *
     * @return the property containing the highlightedPosition
     */
    public DoubleProperty highlightedPositionProperty() {
        return highlightedPositionProperty;
    }

    /**
     * Computes the best routes between every waypoint and combine them into a {@code MultiRoute}.
     * If any of the route is {@code null} or if there are strictly less than 2 waypoints, the route
     * and its profile are set to {@code null}.
     */
    private void computeBestRoute() {
        if (waypoints.size() < 2) {
            emptyRoute();
            return;
        }
        List<Route> segments = new ArrayList<>();
        Set<Pair<Integer, Integer>> keysToKeep = new HashSet<>();
        for (int i = 0; i < waypoints.size() - 1; i++) {
            int startNodeId = waypoints.get(i).closestNodeId();
            int destinationNodeId = waypoints.get(i + 1).closestNodeId();
            Pair<Integer, Integer> key = new Pair<>(startNodeId, destinationNodeId);
            Route bestRoute;
            if (computedRoutes.containsKey(key))
                bestRoute = computedRoutes.get(key);
            else {
                bestRoute = routeComputer.bestRouteBetween(startNodeId, destinationNodeId);
                computedRoutes.put(key, bestRoute);
            }
            if (bestRoute == null) {
                emptyRoute();
                return;
            }
            keysToKeep.add(key);
            segments.add(bestRoute);
        }
        computedRoutes.keySet().retainAll(keysToKeep);
        Route finalRoute = new MultiRoute(segments);
        routeProperty.set(finalRoute);
        ElevationProfile profile = ElevationProfileComputer.elevationProfile(finalRoute,
                MAX_STEP_LENGTH);
        elevationProfileProperty.set(profile);
    }

    private void emptyRoute() {
        elevationProfileProperty.set(null);
        routeProperty.set(null);
    }

}

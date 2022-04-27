package ch.epfl.javelo.gui;

import ch.epfl.javelo.routing.ElevationProfile;
import ch.epfl.javelo.routing.Route;
import ch.epfl.javelo.routing.RouteComputer;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

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
    private final DoubleProperty highlightedPositionProperty;
    private final ObjectProperty<ElevationProfile> elevationProfileProperty;

    /**
     * Constructor of a RouteBean.
     */
    public RouteBean(RouteComputer routeComputer) {
        this.routeComputer = routeComputer;

        this.waypoints = FXCollections.observableArrayList();
        this.routeProperty = new SimpleObjectProperty<>();
        this.highlightedPositionProperty = new SimpleDoubleProperty();
        this.elevationProfileProperty = new SimpleObjectProperty<>();
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
     * Returns the property containing the highlightedPosition, as read-only.
     *
     * @return the property containing the highlightedPosition
     */
    public DoubleProperty highlightedPositionProperty() {
        return highlightedPositionProperty;
    }

    /**
     * Returns the property containing the elevationProfile.
     *
     * @return the property containing the elevationProfile
     */
    public ReadOnlyObjectProperty<ElevationProfile> elevationProfileProperty() {
        return elevationProfileProperty;
    }

}

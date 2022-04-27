package ch.epfl.javelo.gui;

import java.util.function.Consumer;
import javafx.beans.property.ObjectProperty;
import javafx.scene.layout.Pane;

/**
 * Handles display and interactions with the route and the highlighted point.
 * <p>
 * Arguments are not checked.
 *
 * @author Lucas Jung (324724)
 * @author Florian Kolly (328313)
 */
public final class RouteManager {

    private final RouteBean routeBean;
    private final ObjectProperty<MapViewParameters> mapParametersProperty;
    private final Consumer<String> errorConsumer;

    private final Pane pane;

    /**
     * Constructor of a route manager.
     *
     * @param routeBean
     * @param mapParametersProperty
     * @param errorConsumer
     */
    public RouteManager(RouteBean routeBean,
                        ObjectProperty<MapViewParameters> mapParametersProperty,
                        Consumer<String> errorConsumer) {
        this.routeBean = routeBean;
        this.mapParametersProperty = mapParametersProperty;
        this.errorConsumer = errorConsumer;

        this.pane = new Pane();
        this.pane.setPickOnBounds(false); // don't block background events

        addListeners();
    }

    /**
     * Returns the pane rendering the route and the highlighted point.
     *
     * @return the pane rendering the route and the highlighted point
     */
    public Pane pane() {
        return pane;
    }

    /**
     * Adds listeners to the nodes in the scene.
     */
    private void addListeners() {

    }

}

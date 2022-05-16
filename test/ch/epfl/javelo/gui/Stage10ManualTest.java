package ch.epfl.javelo.gui;

import java.nio.file.Path;
import ch.epfl.javelo.data.Graph;
import ch.epfl.javelo.routing.CityBikeCF;
import ch.epfl.javelo.routing.ElevationProfile;
import ch.epfl.javelo.routing.ElevationProfileComputer;
import ch.epfl.javelo.routing.Route;
import ch.epfl.javelo.routing.RouteComputer;
import javafx.application.Application;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Scene;
import javafx.stage.Stage;

public final class Stage10ManualTest extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        Graph graph = Graph.loadFrom(Path.of("data/lausanne"));
        CityBikeCF costFunction = new CityBikeCF(graph);
        RouteComputer routeComputer = new RouteComputer(graph, costFunction);

        Route route = routeComputer.bestRouteBetween(159049, 117669);
        ElevationProfile profile = ElevationProfileComputer.elevationProfile(route, 5);

        ObjectProperty<ElevationProfile> profileProperty = new SimpleObjectProperty<>(profile);
        DoubleProperty highlightProperty = new SimpleDoubleProperty(1500);

        ElevationProfileManager profileManager = new ElevationProfileManager(profileProperty,
                                                                             highlightProperty);

        // highlightProperty.bind(profileManager.mousePositionOnProfileProperty());

        Scene scene = new Scene(profileManager.pane());

        primaryStage.setMinWidth(600);
        primaryStage.setMinHeight(300);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

}

package ch.epfl.javelo.gui;

import java.nio.file.Path;
import ch.epfl.javelo.data.Graph;
import ch.epfl.javelo.routing.CityBikeCF;
import ch.epfl.javelo.routing.CostFunction;
import ch.epfl.javelo.routing.RouteComputer;
import javafx.application.Application;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public final class Stage9ManualTest extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        // TODO no connection => weird
        Graph graph = Graph.loadFrom(Path.of("data/lausanne"));
        Path cacheBasePath = Path.of("data/tiles");
        String tileServerHost = "tile.openstreetmap.org";
        TileManager tileManager = new TileManager(cacheBasePath, tileServerHost);

        MapViewParameters mapViewParameters = new MapViewParameters(12, 543200, 370650);
        ObjectProperty<MapViewParameters> mapViewParametersP = new SimpleObjectProperty<>(mapViewParameters);

        CostFunction costFunction = new CityBikeCF(graph);
        RouteComputer routeComputer = new RouteComputer(graph, costFunction);

        RouteBean routeBean = new RouteBean(routeComputer);

        WaypointsManager waypointsManager = new WaypointsManager(graph, mapViewParametersP,
                                                                 routeBean.waypoints(),
                                                                 System.out::println);
        BaseMapManager baseMapManager = new BaseMapManager(tileManager, waypointsManager,
                                                           mapViewParametersP);

        routeBean.setHighlightedPosition(1000);

        // RouteManager routeManager = new RouteManager(routeBean, mapViewParametersP,
        // System.out::println);
        RouteManager routeManager = new RouteManager(routeBean, mapViewParametersP);

        StackPane mainPane = new StackPane(baseMapManager.pane(), waypointsManager.pane(),
                                           routeManager.pane());
        mainPane.getStylesheets().add("map.css");

        primaryStage.setMinWidth(600);
        primaryStage.setMinHeight(300);
        primaryStage.setScene(new Scene(mainPane));
        primaryStage.show();
    }

}

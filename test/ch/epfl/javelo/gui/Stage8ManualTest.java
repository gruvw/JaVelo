package ch.epfl.javelo.gui;

import java.nio.file.Path;
import java.util.function.Consumer;
import ch.epfl.javelo.data.Graph;
import ch.epfl.javelo.projection.PointCh;
import javafx.application.Application;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public final class Stage8ManualTest extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        Graph graph = Graph.loadFrom(Path.of("data/lausanne"));
        Path cacheBasePath = Path.of("data/tiles");
        String tileServerHost = "tile.openstreetmap.org";
        TileManager tileManager = new TileManager(cacheBasePath, tileServerHost);

        MapViewParameters mapViewParameters = new MapViewParameters(12, 543200, 370650);
        ObjectProperty<MapViewParameters> mapViewParametersP = new SimpleObjectProperty<>(mapViewParameters);
        ObservableList<Waypoint> waypoints = FXCollections.observableArrayList(
                new Waypoint(new PointCh(2532697, 1152350), 159049),
                new Waypoint(new PointCh(2538659, 1154350), 117669));
        Consumer<String> errorConsumer = new ErrorConsumer();

        WaypointsManager waypointsManager = new WaypointsManager(graph, mapViewParametersP,
                                                                 waypoints, errorConsumer);
        BaseMapManager baseMapManager = new BaseMapManager(tileManager, waypointsManager,
                                                           mapViewParametersP);

        StackPane mainPane = new StackPane(baseMapManager.pane(), waypointsManager.pane());
        // FIXME: okay to add css like that?
        mainPane.getStylesheets().add("file:resources/map.css");
        primaryStage.setMinWidth(600);
        primaryStage.setMinHeight(300);
        primaryStage.setScene(new Scene(mainPane));
        primaryStage.show();
    }

    private static final class ErrorConsumer implements Consumer<String> {

        @Override
        public void accept(String s) {
            System.out.println(s);
        }

    }

}

package ch.epfl.javelo.gui;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import ch.epfl.javelo.data.Graph;
import ch.epfl.javelo.routing.CityBikeCF;
import ch.epfl.javelo.routing.CostFunction;
import ch.epfl.javelo.routing.GpxGenerator;
import ch.epfl.javelo.routing.RouteComputer;
import javafx.application.Application;
import javafx.geometry.Orientation;
import javafx.scene.Scene;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public final class JaVelo extends Application {

    private static final String GRAPH_DIRECTORY = "javelo-data";
    private static final String CACHE_DIRECTORY = "osm-cache";
    private static final String TILE_SERVER_NAME = "tile.openstreetmap.org";
    private static final int MIN_WIDTH = 800;
    private static final int MIN_HEIGHT = 600;
    private static final String WINDOW_TITLE = "JaVelo";

    private static final String MENU_TEXT = "Fichier";
    private static final String MENU_ITEM_TEXT = "Exporter GPX";

    private static final String GPX_FILE_NAME = "javelo.gpx";

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        Graph graph = Graph.loadFrom(Path.of(GRAPH_DIRECTORY));
        TileManager tileManager = new TileManager(Path.of(CACHE_DIRECTORY), TILE_SERVER_NAME);
        CostFunction costFunction = new CityBikeCF(graph);
        RouteComputer routeComputer = new RouteComputer(graph, costFunction);
        RouteBean routeBean = new RouteBean(routeComputer);
        ErrorManager errorManager = new ErrorManager();
        AnnotatedMapManager annotatedMapManager = new AnnotatedMapManager(graph, tileManager,
                                                                          routeBean,
                                                                          errorManager::displayError);
        ElevationProfileManager elevationProfileManager = new ElevationProfileManager(routeBean.elevationProfileProperty(),
                                                                                      routeBean.highlightedPositionProperty());

        // Menu
        MenuItem gpxItem = new MenuItem(MENU_ITEM_TEXT);
        gpxItem.disableProperty().bind(routeBean.elevationProfileProperty().isNull());
        gpxItem.setOnAction(e -> {
            try {
                GpxGenerator.writeGpx(GPX_FILE_NAME, routeBean.route(),
                        routeBean.elevationProfile());
            } catch (IOException except) {
                throw new UncheckedIOException(except); // should never happen
            }
        });
        Menu menu = new Menu(MENU_TEXT);
        menu.getItems().add(gpxItem);
        MenuBar menuBar = new MenuBar(menu);
        menuBar.setUseSystemMenuBar(true);

        // Pane for the map (route and waypoints) and the profile
        Pane profilePane = elevationProfileManager.pane();
        SplitPane splitPane = new SplitPane(annotatedMapManager.pane());
        routeBean.elevationProfileProperty().addListener((p, oldP, newP) -> {
            if (oldP != null && newP == null)
                splitPane.getItems().remove(profilePane);
            else if (oldP == null && newP != null)
                splitPane.getItems().add(profilePane);
        });
        SplitPane.setResizableWithParent(profilePane, false);
        splitPane.setOrientation(Orientation.VERTICAL);

        // Menu & Pane
        Pane mapPane = new StackPane(splitPane, errorManager.pane());
        BorderPane mainPane = new BorderPane(mapPane);
        mainPane.setTop(menuBar);

        // Stage
        Scene scene = new Scene(mainPane);
        primaryStage.setMinWidth(MIN_WIDTH);
        primaryStage.setMinHeight(MIN_HEIGHT);
        primaryStage.setTitle(WINDOW_TITLE);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

}

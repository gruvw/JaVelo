package ch.epfl.javelo.gui;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import ch.epfl.javelo.data.Graph;
import ch.epfl.javelo.routing.CityBikeCF;
import ch.epfl.javelo.routing.CostFunction;
import ch.epfl.javelo.routing.GpxGenerator;
import ch.epfl.javelo.routing.RouteComputer;
import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.geometry.Orientation;
import javafx.scene.Scene;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.FileChooser.ExtensionFilter;

/**
 * Primary class of the application. Handles the execution and the display of the window.
 *
 * @author Lucas Jung (324724)
 * @author Florian Kolly (328313)
 */
public final class JaVelo extends Application {

    /**
     * Directory of the graph.
     */
    private static final String GRAPH_DIRECTORY = "javelo-data";

    /**
     * Directory of the tiles disk cache.
     */
    private static final String CACHE_DIRECTORY = "osm-cache";

    /**
     * URL of the tile server.
     */
    private static final String TILE_SERVER_NAME = "tile.openstreetmap.org";

    /**
     * Title of the application's window.
     */
    private static final String WINDOW_TITLE = "JaVelo";

    /**
     * Minimum width of the window.
     */
    private static final int MIN_WIDTH = 800;

    /**
     * Minimum height of the window.
     */
    private static final int MIN_HEIGHT = 600;

    /**
     * Text of the menu item.
     */
    private static final String MENU_TEXT = "Fichier";

    /**
     * Text of the submenu action.
     */
    private static final String MENU_ITEM_TEXT = "Exporter GPX";

    /**
     * Default filename when saving the GPX file.
     */
    private static final String GPX_FILE_NAME = "javelo.gpx";

    /**
     * Entry point of the application.
     *
     * @param args Java command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
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

        // Bind highlighted position of the route with the annotated map and the elevation profile
        routeBean.highlightedPositionProperty()
                 .bind(Bindings.when(
                         annotatedMapManager.mousePositionOnRouteProperty().greaterThanOrEqualTo(0))
                               .then(annotatedMapManager.mousePositionOnRouteProperty())
                               .otherwise(
                                       elevationProfileManager.mousePositionOnProfileProperty()));

        // Menu
        MenuItem gpxItem = new MenuItem(MENU_ITEM_TEXT);
        gpxItem.disableProperty().bind(routeBean.elevationProfileProperty().isNull());

        // CHANGE: remove file chooser: default save on project root as default filename
        FileChooser fileChooser = new FileChooser();
        fileChooser.setInitialFileName(GPX_FILE_NAME);
        fileChooser.setTitle("Save GPX");
        fileChooser.getExtensionFilters().addAll(new ExtensionFilter("GPX Files", "*.gpx"));

        gpxItem.setOnAction(e -> {
            try {
                File file = fileChooser.showSaveDialog(stage);
                if (file != null)
                    GpxGenerator.writeGpx(file.getAbsolutePath(), routeBean.route(),
                            routeBean.elevationProfile());
            } catch (IOException except) {
                throw new UncheckedIOException(except); // should never happen
            }
        });
        Menu menu = new Menu(MENU_TEXT);
        menu.getItems().add(gpxItem);
        MenuBar menuBar = new MenuBar(menu);
        menuBar.setUseSystemMenuBar(true);

        // ASK rond point à contre sens

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
        stage.setMinWidth(MIN_WIDTH);
        stage.setMinHeight(MIN_HEIGHT);
        stage.setTitle(WINDOW_TITLE);
        stage.setScene(scene);
        stage.show();
    }

}

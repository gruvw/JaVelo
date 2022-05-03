package ch.epfl.javelo.gui;

import java.io.IOException;
import ch.epfl.javelo.Math2;
import ch.epfl.javelo.projection.PointWebMercator;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;

import static ch.epfl.javelo.gui.TileManager.TileId;

/**
 * Handles display and interactions with the background map.
 * <p>
 * Arguments are not checked.
 *
 * @author Lucas Jung (324724)
 * @author Florian Kolly (328313)
 */
public final class BaseMapManager {

    private static final int MIN_ZOOM_LEVEL = 8;
    private static final int MAX_ZOOM_LEVEL = 19;

    private final TileManager tileManager;
    private final WaypointsManager waypointsManager;
    private final ObjectProperty<MapViewParameters> mapParamsProperty;

    private final Pane pane;
    private final Canvas canvas;

    private boolean redrawNeeded;
    private Point2D lastMousePosition;


    /**
     * Constructor of a background map manager.
     *
     * @param tileManager       OSM tiles manager
     * @param waypointsManager  waypoints manager
     * @param mapParamsProperty JavaFx property containing the parameters of the background map
     */
    public BaseMapManager(TileManager tileManager,
                          WaypointsManager waypointsManager,
                          ObjectProperty<MapViewParameters> mapParamsProperty) {
        this.tileManager = tileManager;
        this.waypointsManager = waypointsManager;
        this.mapParamsProperty = mapParamsProperty;

        this.pane = new Pane();

        // CHANGE: remove part that zooms over waypoint
        this.pane.setId("mapPane"); // used to cascade zoom action from waypoint pin

        this.canvas = new Canvas();
        this.pane.getChildren().add(this.canvas);

        registerListeners();
        registerHandlers();

        redrawOnNextPulse();
    }

    /**
     * Returns the pane rendering the background map.
     *
     * @return the pane rendering the background map
     */
    public Pane pane() {
        return pane;
    }

    /**
     * Registers listeners to the nodes in the scene.
     */
    private void registerListeners() {
        canvas.widthProperty().bind(pane.widthProperty());
        canvas.heightProperty().bind(pane.heightProperty());

        // Redraw when resize
        canvas.widthProperty().addListener((p, o, n) -> redrawOnNextPulse());
        canvas.heightProperty().addListener((p, o, n) -> redrawOnNextPulse());
        // Redraw when anchor point moved or zoom changed
        mapParamsProperty.addListener((p, o, n) -> redrawOnNextPulse());

        // Redraw if needed at every pulse
        canvas.sceneProperty().addListener((p, oldS, newS) -> {
            assert oldS == null;
            newS.addPreLayoutPulseListener(this::redrawIfNeeded);
        });
    }

    /**
     * Registers event handlers to the nodes in the scene.
     */
    private void registerHandlers() {
        // Zoom control
        pane.setOnScroll(e -> {
            MapViewParameters mapParams = mapParamsProperty.get();
            double zoomDelta = Math.signum(e.getDeltaY());
            int newZoomLevel = Math2.clamp(MIN_ZOOM_LEVEL, mapParams.zoomLevel() + (int) zoomDelta,
                    MAX_ZOOM_LEVEL);
            // CHANGE: remove zooms over waypoint (remove position, position.getX() -> e.getX())
            Point2D position = ((Node) e.getSource()).localToParent(e.getX(), e.getY());
            PointWebMercator centerOfZoom = mapParams.pointAt(position.getX(), position.getY());
            mapParamsProperty.set(new MapViewParameters(newZoomLevel,
                                                        centerOfZoom.xAtZoomLevel(newZoomLevel)
                                                                - position.getX(),
                                                        centerOfZoom.yAtZoomLevel(newZoomLevel)
                                                                - position.getY()));
        });

        // Map movement control
        pane.setOnMousePressed(e -> lastMousePosition = new Point2D(e.getX(), e.getY()));
        pane.setOnMouseDragged(e -> {
            Point2D movement = new Point2D(e.getX(), e.getY()).subtract(lastMousePosition);
            mapParamsProperty.set(
                    mapParamsProperty.get().withShiftedBy(movement.getX(), movement.getY()));
            lastMousePosition = new Point2D(e.getX(), e.getY());
        });
        pane.setOnMouseReleased(e -> lastMousePosition = null);

        // New waypoint control
        pane.setOnMouseClicked(e -> {
            if (e.isStillSincePress())
                waypointsManager.addWaypoint(e.getX(), e.getY());
        });
    }

    /**
     * Redraws the tiles when needed. Called every JavaFX pulse.
     */
    private void redrawIfNeeded() {
        if (redrawNeeded) {
            redrawNeeded = false;
            drawTiles();
        }
    }

    /**
     * Triggers a redraw of the tiles on next JavaFX pulse.
     */
    private void redrawOnNextPulse() {
        redrawNeeded = true;
        Platform.requestNextPulse();
    }

    /**
     * Draws every visible tile on the canvas. Uses the tile manager to retrieve the tile images.
     * Ignores tiles that could not be retrieved.
     */
    private void drawTiles() {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        MapViewParameters mapParams = mapParamsProperty.get();

        int zoomLevel = mapParams.zoomLevel();
        PointWebMercator topLeft = mapParams.pointAt(0, 0);
        TileId topLeftTile = TileId.of(topLeft, zoomLevel);
        double offsetX = topLeftTile.x() * TileManager.TILE_SIDE_LENGTH
                - topLeft.xAtZoomLevel(zoomLevel);
        double offsetY = topLeftTile.y() * TileManager.TILE_SIDE_LENGTH
                - topLeft.yAtZoomLevel(zoomLevel);

        int tilesXNb = (int) Math.ceil(canvas.getWidth() / TileManager.TILE_SIDE_LENGTH);
        int tilesYNb = (int) Math.ceil(canvas.getHeight() / TileManager.TILE_SIDE_LENGTH);
        for (int x = 0; x <= tilesXNb; x++) {
            for (int y = 0; y <= tilesYNb; y++) {
                TileId tile = new TileId(zoomLevel, topLeftTile.x() + x, topLeftTile.y() + y);
                try {
                    Image image = tileManager.imageForTileAt(tile);
                    gc.drawImage(image, x * TileManager.TILE_SIDE_LENGTH + offsetX,
                            y * TileManager.TILE_SIDE_LENGTH + offsetY);
                } catch (IOException e) {
                    // Don't draw tile if image not found
                }
            }
        }
    }

}

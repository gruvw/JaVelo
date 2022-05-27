package ch.epfl.javelo.gui;

import java.io.IOException;
import ch.epfl.javelo.Math2;
import ch.epfl.javelo.projection.PointWebMercator;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;

import static ch.epfl.javelo.gui.TileManager.TileId;

/**
 * Handles display and interactions with the background map.
 *
 * @author Lucas Jung (324724)
 * @author Florian Kolly (328313)
 */
public final class BaseMapManager {

    /**
     * Time between two zoom events to be registered, in ms.
     */
    private static final int SCROLL_WAIT_TIME = 200;

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
     * Constructor of a base map manager.
     *
     * @param tileManager       tiles manager
     * @param waypointsManager  waypoints manager
     * @param mapParamsProperty JavaFX property containing the parameters of the background map
     */
    public BaseMapManager(TileManager tileManager,
                          WaypointsManager waypointsManager,
                          ObjectProperty<MapViewParameters> mapParamsProperty) {
        this.tileManager = tileManager;
        this.waypointsManager = waypointsManager;
        this.mapParamsProperty = mapParamsProperty;

        this.canvas = new Canvas();
        this.pane = new Pane(this.canvas);
        // CHANGE: remove part that zooms over waypoint
        this.pane.setId("mapPane"); // used to cascade zoom action from waypoint pin

        registerListeners();
        registerHandlers();
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
     * Registers listeners and bindings.
     */
    private void registerListeners() {
        canvas.widthProperty().bind(pane.widthProperty());
        canvas.heightProperty().bind(pane.heightProperty());

        // Redraw when resized
        canvas.widthProperty().addListener((p, o, n) -> redrawOnNextPulse());
        canvas.heightProperty().addListener((p, o, n) -> redrawOnNextPulse());
        // Redraw when anchor point moved or zoom changed
        mapParamsProperty.addListener((p, o, n) -> redrawOnNextPulse());

        // Redraw if needed at every pulse
        canvas.sceneProperty().addListener((p, o, n) -> {
            assert o == null;
            n.addPreLayoutPulseListener(this::redrawIfNeeded);
        });
    }

    /**
     * Registers event handlers.
     */
    private void registerHandlers() {
        // Zoom control
        SimpleLongProperty minScrollTime = new SimpleLongProperty();
        pane.setOnScroll(e -> {
            MapViewParameters mapParams = mapParamsProperty.get();
            if (e.getDeltaY() == 0d)
                return;
            long currentTime = System.currentTimeMillis();
            if (currentTime < minScrollTime.get())
                return;
            minScrollTime.set(currentTime + SCROLL_WAIT_TIME);
            int zoomDelta = (int) Math.signum(e.getDeltaY());
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
        // Clear the canvas to avoid visual bugs when no connection
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());

        MapViewParameters mapParams = mapParamsProperty.get();
        int zoomLevel = mapParams.zoomLevel();
        // ASK error when point outside of world (stage 8)
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
                int tileX = topLeftTile.x() + x;
                int tileY = topLeftTile.y() + y;
                // Check that tile exists (world border)
                if (!TileId.isValid(zoomLevel, tileX, tileY))
                    continue;
                TileId tile = new TileId(zoomLevel, tileX, tileY);
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

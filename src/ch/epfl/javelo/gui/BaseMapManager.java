package ch.epfl.javelo.gui;

import java.io.IOException;
import ch.epfl.javelo.Math2;
import ch.epfl.javelo.projection.PointWebMercator;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.geometry.Point2D;
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

    private final TileManager tileManager;
    private final WaypointsManager waypointsManager;
    private final ObjectProperty<MapViewParameters> mapParametersProperty;

    private final Pane pane;
    private final Canvas canvas;

    private boolean redrawNeeded;
    private Point2D lastMousePosition;

    /**
     * Constructor of a background map manager.
     *
     * @param tileManager           OSM tiles manager
     * @param waypointsManager      waypoints manager
     * @param mapParametersProperty JavaFx property containing the parameters of the background map
     */
    public BaseMapManager(TileManager tileManager,
                          WaypointsManager waypointsManager,
                          ObjectProperty<MapViewParameters> mapParametersProperty) {
        this.tileManager = tileManager;
        this.waypointsManager = waypointsManager;
        this.mapParametersProperty = mapParametersProperty;

        this.pane = new Pane();
        pane.setId("mapPane"); // used to cascade zoom action from waypoint pin
        this.canvas = new Canvas();
        pane.getChildren().add(canvas);

        addListeners();
        addHandlers();

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
     * Adds listeners to the nodes in the scene.
     */
    private void addListeners() {
        canvas.widthProperty().bind(pane.widthProperty());
        canvas.heightProperty().bind(pane.heightProperty());

        // Redraw when resize
        canvas.widthProperty().addListener((p, o, n) -> redrawOnNextPulse());
        canvas.heightProperty().addListener((p, o, n) -> redrawOnNextPulse());
        // Redraw when anchor point moved or zoom changed
        this.mapParametersProperty.addListener((p, o, n) -> redrawOnNextPulse());

        // Redraw if needed at every pulse
        canvas.sceneProperty().addListener((p, oldS, newS) -> {
            assert oldS == null;
            newS.addPreLayoutPulseListener(this::redrawIfNeeded);
        });
    }

    /**
     * Adds handlers to the nodes in the scene.
     */
    private void addHandlers() {
        // Zoom control
        pane.setOnScroll(e -> {
            MapViewParameters mapParams = mapParametersProperty.get();
            double zoomDelta = Math.signum(e.getDeltaY());
            int newZoomLevel = Math2.clamp(8, mapParams.zoomLevel() + (int) zoomDelta, 19);
            // FIXME: Forced to use pointWebMercator ?
            PointWebMercator centerOfZoom = mapParams.pointAt(e.getSceneX(), e.getSceneY());
            mapParametersProperty.set(
                    new MapViewParameters(newZoomLevel,
                                          centerOfZoom.xAtZoomLevel(newZoomLevel) - e.getSceneX(),
                                          centerOfZoom.yAtZoomLevel(newZoomLevel) - e.getSceneY()));
        });

        // Map movement control
        pane.setOnMousePressed(
                e -> this.lastMousePosition = new Point2D(e.getSceneX(), e.getSceneY()));
        pane.setOnMouseDragged(e -> {
            if (!e.isStillSincePress()) {
                Point2D movement = new Point2D(e.getSceneX(), e.getSceneY()).subtract(
                        lastMousePosition);
                mapParametersProperty.set(
                        mapParametersProperty.get().shiftedBy(movement.getX(), movement.getY()));
            }
            this.lastMousePosition = new Point2D(e.getSceneX(), e.getSceneY());
        });
        pane.setOnMouseReleased(e -> this.lastMousePosition = null);

        // New waypoint control
        // FIXME: slight top left movement when mouse is released / when we create point
        pane.setOnMouseClicked(e -> {
            if (e.isStillSincePress())
                waypointsManager.addWaypoint(e.getSceneX(), e.getSceneY());
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
        MapViewParameters mapParams = mapParametersProperty.get();

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

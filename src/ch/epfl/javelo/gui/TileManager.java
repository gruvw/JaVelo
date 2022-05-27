package ch.epfl.javelo.gui;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import ch.epfl.javelo.Preconditions;
import ch.epfl.javelo.projection.PointWebMercator;
import javafx.scene.image.Image;

/**
 * Represents a tiles manager, displaying tiles images of the map.
 * <p>
 * Arguments are not checked.
 *
 * @author Lucas Jung (324724)
 * @author Florian Kolly (328313)
 */
public final class TileManager {

    /**
     * Side length of a tile image, in pixels.
     */
    public static final int TILE_SIDE_LENGTH = 256;

    /**
     * Maximum number of tiles saved in cache memory.
     */
    private static final int MAX_ENTRIES = 100;

    private final Path tilesDirectory;
    private final String serverBaseUrl;

    private final Map<TileId, Image> cacheMemory;

    /**
     * Represents a tile. (record)
     *
     * @param zoomLevel zoom level of the tile
     * @param x         x index of the tile
     * @param y         y index of the tile
     */
    public record TileId(int zoomLevel, int x, int y) {

        /**
         * Constructor of a tile.
         *
         * @throws IllegalArgumentException if the tile is invalid
         */
        public TileId {
            Preconditions.checkArgument(isValid(zoomLevel, x, y));
        }

        /**
         * Checks if the given attributes form a valid tile.
         *
         * @param zoomLevel zoom level
         * @param x         x index of the tile
         * @param y         y index of the tile
         * @return true if the given attributes form a valid tile, false otherwise
         */
        public static boolean isValid(int zoomLevel, int x, int y) {
            double limit = Math.pow(2, zoomLevel);
            return (0 <= zoomLevel) && (0 <= x && x < limit) && (0 <= y && y < limit);
        }

        /**
         * Creates a tile from a point in Web Mercator projection, at a given zoom level.
         *
         * @param point     point inside the tile
         * @param zoomLevel zoom level
         * @return the tile at zoom level {@code zoomLevel} containing the given point
         */
        public static TileId of(PointWebMercator point, int zoomLevel) {
            int tileX = (int) Math.floor(point.xAtZoomLevel(zoomLevel) / TILE_SIDE_LENGTH);
            int tileY = (int) Math.floor(point.yAtZoomLevel(zoomLevel) / TILE_SIDE_LENGTH);
            return new TileId(zoomLevel, tileX, tileY);
        }

        /**
         * Forms the string path of a tile's image.
         *
         * @return the string path of a tile's image.
         */
        private String imagePath() {
            return String.format("%d/%d/%d.png", zoomLevel, x, y);
        }

    }

    /**
     * Constructor of a tile manager.
     *
     * @param tilesDirectory path to the directory containing the on-disk tiles storage
     * @param serverName     name of the tile server
     */
    public TileManager(Path tilesDirectory, String serverName) {
        this.tilesDirectory = tilesDirectory;
        this.serverBaseUrl = "https://" + serverName + "/";
        this.cacheMemory = new LinkedHashMap<>(MAX_ENTRIES, 0.75f, true);
    }

    /**
     * Retrieves the image of a given tile.
     * <p>
     * The image is first sought in the cache memory, then on the disk. If the image is not found in
     * either of them, it is downloaded from the tile server and loaded in the cache memory (and
     * also saved on the disk).
     *
     * @param tile tile to retrieve
     * @return the JavaFX image corresponding to the given tile
     * @throws IOException if any IO error occurs while accessing the disk or the server
     */
    public Image imageForTileAt(TileId tile) throws IOException {
        if (cacheMemory.containsKey(tile))
            return cacheMemory.get(tile);
        if (!Files.exists(pathOf(tile)))
            downloadImageFromServer(tile);
        try (InputStream in = new FileInputStream(pathOf(tile).toFile())) {
            Image tileImage = new Image(in);
            if (cacheMemory.size() >= MAX_ENTRIES)
                cacheMemory.remove(cacheMemory.entrySet().iterator().next().getKey());
            cacheMemory.put(tile, tileImage);
            return tileImage;
        }
    }

    /**
     * Retrieves a tile's image from the tile server and saves it to the disk.
     *
     * @param tile the tile of which we download the image
     * @throws IOException if any IO error occurs
     */
    private void downloadImageFromServer(TileId tile) throws IOException {
        URL imageURL = new URL(serverBaseUrl + tile.imagePath());
        URLConnection connection = imageURL.openConnection();
        connection.setRequestProperty("User-Agent", "JaVelo");
        Path filePath = pathOf(tile);
        Files.createDirectories(filePath.getParent());
        try (InputStream in = connection.getInputStream();
             OutputStream out = new FileOutputStream(filePath.toFile())) {
            in.transferTo(out);
        }
    }

    /**
     * Retrieves the path of the tile's image (on-disk).
     *
     * @param tile the tile of which we want the path
     * @return path of the tile's image on the disk
     */
    private Path pathOf(TileId tile) {
        return tilesDirectory.resolve(tile.imagePath());
    }

}

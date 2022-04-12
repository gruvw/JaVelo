package ch.epfl.javelo.gui;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import ch.epfl.javelo.Preconditions;
import javafx.scene.image.Image;

/**
 * Represents an OSM tiles manager.
 * <p>
 * Arguments are not checked.
 *
 * @author Lucas Jung (324724)
 * @author Florian Kolly (328313)
 */
public final class TileManager {

    private final Path tilesDirectory;
    private final String serverBaseUrl;
    private final LinkedHashMap<TileId, Image> cacheMemory;

    /**
     * Represents an OSM tile. (record)
     *
     * @param zoom zoom level of the tile
     * @param x    x index of the tile
     * @param y    y index of the tile
     */
    public record TileId(int zoom, double x, double y) {

        /**
         * Constructor of a tile.
         *
         * @throws IllegalArgumentException if the tile is invalid
         */
        public TileId {
            Preconditions.checkArgument(isValid(zoom, x, y));
        }

        /**
         * Verifies if the given attributes form a valid tile.
         *
         * @return true if the given attributes form a valid OSM tile, false otherwise
         */
        public static boolean isValid(int zoom, double x, double y) {
            return (0 <= x && x < Math.pow(4, zoom)) && (0 <= y && y < Math.pow(4, zoom));
        }

        private String path() {
            return String.format("%d/%d/%d.png", zoom, x, y);
        }

    }

    /**
     * Constructor for a tile manager.
     *
     * @param tilesDirectory path to the directory containing the on-disk tiles storage
     * @param serverName     name of the tiles server
     */
    public TileManager(Path tilesDirectory, String serverName) {
        this.tilesDirectory = tilesDirectory;
        this.serverBaseUrl = "https://" + serverName + "/";
        this.cacheMemory = new LinkedHashMap<>(100, 0, true);
    }

    /**
     * Retrieves the image with the given tile.
     * <p>
     * The image is first sought in the cache memory, then in the disk memory. If the image is not
     * found in either of them, it is downloaded from the tiles server and loaded in the memory
     * cache (also saved on the disk).
     *
     * @param tile tile to retrieve
     * @return the JavaFX image corresponding to the given tile
     * @throws IOException if an error occurs when retrieving the image from the server
     */
    public Image imageForTileAt(TileId tile) throws IOException {
        if (cacheMemory.containsKey(tile))
            return cacheMemory.get(tile);
        if (Files.exists(pathOf(tile)))
            return new Image(pathOf(tile).toString());
        return getImageFromServer(tile);
    }

    /**
     * Retrieves a tile's image from the tiles server. Saves the retrieved image to the disk memory.
     *
     * @param tile the tile of which we want the image
     * @return the image of the tile (retrieved from the tiles server)
     * @throws IOException if any IO error occurs
     */
    private Image getImageFromServer(TileId tile) throws IOException {
        URL u = urlOf(tile);
        URLConnection c = u.openConnection();
        c.setRequestProperty("User-Agent", "JaVelo");
        Path filePath = pathOf(tile);
        Files.createDirectories(filePath.getParent());
        try (InputStream in = c.getInputStream();
             OutputStream out = new FileOutputStream(filePath.toFile())) {
            in.transferTo(out);
            return new Image(in); // TODO in is empty !
        }
    }

    /**
     * Retrieves the path of the tile's image (on-disk).
     *
     * @return path of the tile's image on the disk
     */
    private Path pathOf(TileId tile) {
        return tilesDirectory.resolve(tile.path());
    }

    /**
     * Creates the url of the tile image on the tiles server.
     *
     * @return url of the tile's image on the tile server
     * @throws MalformedURLException (should never occur)
     */
    private URL urlOf(TileId tile) throws MalformedURLException {
        return new URL(serverBaseUrl + tile.path());
    }

}

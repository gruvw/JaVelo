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
import javafx.scene.image.Image;

/**
 * Represents an OSM tiles manager.
 * <p>
 * Arguments are not checked.
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
         * Verifies if the given attributes form a valid tile.
         *
         * @return true if the given attributes form a valid OSM tile, false otherwise
         */
        public static boolean isValid(int zoom, double x, double y) {
            return 0 <= x && x < Math.pow(4, zoom) && 0 <= y && y < Math.pow(4, zoom);
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
     *
     * @param tile tile to retrieve
     * @return the JavaFX image corresponding to the tile with identity {@code identity}. The image
     *         is first sought in the memory cache, then in the disk cache. If the image is not
     *         found in either of them, it is downloaded from the tile server and loaded in cache
     *         (disk and then memory cache).
     */
    public Image imageForTileAt(TileId tile) {
        Image image = cacheMemory.getOrDefault(tile, null);
        if (image != null)
            return image;
        if (Files.exists(pathOf(tile)))
            return new Image(pathOf(tile).toString());
        return getImageFromServer(tile); // FIXME what to do when error
    }


    private Image getImageFromServer(TileId tile) throws IOException {
        URL u = urlOf(tile);
        URLConnection c = u.openConnection();
        c.setRequestProperty("User-Agent", "JaVelo");
        try (InputStream in = c.getInputStream();
             OutputStream out = new FileOutputStream(String.valueOf(tile.y()))) {
            Files.createDirectories(pathOf(tile).getParent());
            in.transferTo(out);
            return new Image(in);
        }
    }

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

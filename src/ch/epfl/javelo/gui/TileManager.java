package ch.epfl.javelo.gui;

import java.nio.file.Path;
import javafx.scene.image.Image;

/**
 * Represents an OSM tiles manager.
 * <p>
 * Arguments are not checked.
 */
public final class TileManager {

    private final Path path;
    private final String server;

    /**
     * Represents the identity of an OSM tile. (record)
     */
    record TileId(int zoom, double x, double y) {

        /**
         * Verifies if the given attributes constitute a valid tile.
         * 
         * @return true if the given attributes constitute a valid OSM tile, false otherwise
         */
        public static boolean isValid(int zoom, double x, double y) {

        }

    }

    /**
     * Constructor for a tile manager.
     * 
     * @param path   path to the repertory containing the disk cache
     * @param server name of the tile server
     */
    public TileManager(Path path, String server) {
        this.path = path;
        this.server = server;
    }

    /**
     * Retrieves the image with identity {@code identity}.
     * 
     * @param identity identity of the tile to retrieve
     * @return the JavaFX image corresponding to the tile with identity {@code identity}. The image
     *         is first sought in the memory cache, then in the disk cache. If the image is not
     *         found in either of them, it is downloaded from the tile server and loaded in cache
     *         (disk and then memory cache).
     */
    public Image imageForTileAt(TileId identity) {

    }

}

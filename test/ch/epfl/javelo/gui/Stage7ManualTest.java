package ch.epfl.javelo.gui;

import java.io.IOException;
import java.nio.file.Path;
import ch.epfl.javelo.gui.TileManager.TileId;
import ch.epfl.javelo.projection.PointWebMercator;

public class Stage7ManualTest {

    public static void main(String[] args) throws IOException {
        TileManager tm = new TileManager(Path.of("data/tiles"), "tile.openstreetmap.org");
        tm.imageForTileAt(new TileId(19, 271725, 185422));
        tm.imageForTileAt(new TileId(19, 271725, 185422)); // retrieve from memory cache

        tm.imageForTileAt(new TileId(0, 0, 0));
        downloadEveryTiles(tm, 0); // same thing -> load from memory

        downloadEveryTiles(tm, 1);
        downloadEveryTiles(tm, 2);

        for (int x = 0; x < 100; x++)
            tm.imageForTileAt(new TileId(13, x, 5000));
        for (int x = 0; x < 100; x++)
            tm.imageForTileAt(new TileId(13, x, 5000)); // load from memory

        for (int y = 0; y < 100; y++)
            tm.imageForTileAt(new TileId(14, 10000, y));

        for (int x = 0; x < 100; x++)
            tm.imageForTileAt(new TileId(13, x, 5000)); // load from disk

        // Rolex Learning Center
        PointWebMercator rolex = PointWebMercator.of(46.518324, 6.567950);
        tm.imageForTileAt(TileId.of(rolex, 10));
        tm.imageForTileAt(TileId.of(rolex, 11));
        tm.imageForTileAt(TileId.of(rolex, 15));
        tm.imageForTileAt(TileId.of(rolex, 17));
    }

    private static void downloadEveryTiles(TileManager tm, int zoomLevel) throws IOException {
        for (int x = 0; x < Math.pow(2, zoomLevel); x++)
            for (int y = 0; y < Math.pow(2, zoomLevel); y++)
                tm.imageForTileAt(new TileId(zoomLevel, x, y));
    }

}

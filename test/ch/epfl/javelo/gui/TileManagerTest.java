package ch.epfl.javelo.gui;

import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;

public class TileManagerTest {

    @Test
    void tileIdThrows() {
        assertThrows(IllegalArgumentException.class, () -> new TileManager.TileId(0, -1, 0));
        assertThrows(IllegalArgumentException.class, () -> new TileManager.TileId(0, 0, -1));
        assertThrows(IllegalArgumentException.class, () -> new TileManager.TileId(0, 1, 0));
        assertThrows(IllegalArgumentException.class, () -> new TileManager.TileId(0, 0, -1));
    }

}

package ch.epfl.javelo.data;

import static org.junit.jupiter.api.Assertions.assertThrows;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import ch.epfl.javelo.Q28_4;
import ch.epfl.javelo.projection.PointCh;
import ch.epfl.javelo.projection.SwissBounds;

public class GraphTest {

    private static Graph graph;

    private static double SECTOR_WIDTH = 2726.5625;
    private static double SECTOR_HEIGHT = 1726.5625;

    private static void addEmptySectors(ByteBuffer buffer, int nb) {
        for (int i = 0; i < nb; i++) {
            // FIXME: no nodes -> whatever id of first node
            buffer.putInt(0);
            buffer.putShort((short) 0);
        }
    }

    @BeforeAll
    static void initGlobalVars() {
        IntBuffer nodesBuffer = IntBuffer.allocate(4);
        // Point 0
        nodesBuffer.put(Q28_4.ofInt((int) SwissBounds.MIN_E + 100));
        nodesBuffer.put(Q28_4.ofInt((int) SwissBounds.MIN_N + 100));
        nodesBuffer.put(0x10000000); // 1 outgoing edges, first edge is id 0
        // Point 1
        nodesBuffer.put(Q28_4.ofInt((int) (SwissBounds.MIN_E + SECTOR_WIDTH + 100)));
        nodesBuffer.put(Q28_4.ofInt((int) (SwissBounds.MIN_N + SECTOR_HEIGHT * 2 + 100)));
        nodesBuffer.put(0x20000001); // 2 outgoing edges, first edge is id 1
        // Point 2
        nodesBuffer.put(Q28_4.ofInt((int) (SwissBounds.MIN_E + SECTOR_WIDTH * 2 + 100)));
        nodesBuffer.put(Q28_4.ofInt((int) (SwissBounds.MIN_N + SECTOR_HEIGHT * 2 - 100)));
        // FIXME: no outgoing edge -> whatever for id of first edge
        nodesBuffer.put(0x00000000); // 0 outgoing edges, no first edge
        // Point 3
        nodesBuffer.put(Q28_4.ofInt((int) (SwissBounds.MIN_E + SECTOR_WIDTH * 4 - 100)));
        nodesBuffer.put(Q28_4.ofInt((int) (SwissBounds.MIN_N + SECTOR_HEIGHT + 100)));
        nodesBuffer.put(0x00000000); // 0 outgoing edges, no first edge

        ByteBuffer sectorsBuffer = ByteBuffer.allocate(128 * 128 * (Integer.BYTES + Short.BYTES));
        // Sector (0, 0)
        sectorsBuffer.putInt(0);
        sectorsBuffer.putShort((short) 1);
        addEmptySectors(sectorsBuffer, 127); // End of first line
        addEmptySectors(sectorsBuffer, 3);
        // Sector (3, 1)
        sectorsBuffer.putInt(3);
        sectorsBuffer.putShort((short) 1);
        addEmptySectors(sectorsBuffer, 124); // End of second line
        addEmptySectors(sectorsBuffer, 1);
        // Sector (1, 3)
        sectorsBuffer.putInt(1);
        sectorsBuffer.putShort((short) 1);
        addEmptySectors(sectorsBuffer, 126); // End of third line
        addEmptySectors(sectorsBuffer, (128 - 3) * 128);

        ByteBuffer edgesBuffer = ByteBuffer.allocate(3 * (Integer.BYTES + 3 * Short.BYTES));
        // Edge 0
        edgesBuffer.putInt(0x00000002);
        edgesBuffer.putShort((short) Short.MAX_VALUE); // spoofed length because points are too far
                                                       // appart
        edgesBuffer.putShort((short) 0);
        edgesBuffer.putShort((short) 0);
        // Edge 1
        edgesBuffer.putInt(0x80000000);
        edgesBuffer.putShort((short) 10); // spoofed length because points are too far appart
        edgesBuffer.putShort((short) 0);
        edgesBuffer.putShort((short) 0);
        // Edge 2
        edgesBuffer.putInt(0x00000002);
        edgesBuffer.putShort((short) 0b1010101011011110); // real length
        edgesBuffer.putShort((short) Q28_4.ofInt(8));
        edgesBuffer.putShort((short) 1);

        IntBuffer profileIds = IntBuffer.allocate(3);
        // FIXME: if the profile id is 0 -> whatever for the first profile id
        // Profile of Edge 0
        profileIds.put(0);
        // Profile of Edge 1
        profileIds.put(0);
        // Profile of Edge 2
        profileIds.put(0x80000000);

        // TO BE CONTINUED
        ShortBuffer elevations = ShortBuffer.wrap();
        List<AttributeSet> attributeSets = new ArrayList<AttributeSet>();
        attributeSets.add(new AttributeSet(0));
        attributeSets.add(new AttributeSet(0b100111L));
        graph = new Graph(new GraphNodes(nodesBuffer), new GraphSectors(sectorsBuffer),
                new GraphEdges(edgesBuffer, profileIds, elevations), attributeSets);
    }

    @Test
    void loadFromThrows() {
        assertThrows(IOException.class, () -> Graph.loadFrom(Path.of("idontexist")));
    }

}

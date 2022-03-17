package ch.epfl.javelo.data;

// FIXME import from assert or assertions ? diff ?
import static org.junit.jupiter.api.Assertions.assertEquals;
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
import ch.epfl.javelo.routing.EdgeTest;

public class GraphTest {

    private static Graph graph;

    private static double SECTOR_WIDTH = 2726.5625;
    private static double SECTOR_HEIGHT = 1726.5625;

    // PointCh[e=2485100.0, n=1075100.0]
    private static final PointCh pointNode0 = new PointCh(SwissBounds.MIN_E + 100,
            SwissBounds.MIN_N + 100);
    // PointCh[e=2487826.5625, n=1078553.125]
    private static final PointCh pointNode1 = new PointCh(SwissBounds.MIN_E + SECTOR_WIDTH + 100,
            SwissBounds.MIN_N + SECTOR_HEIGHT * 2 + 100);
    // PointCh[e=2490553.125, n=1078353.125]
    private static final PointCh pointNode2 = new PointCh(
            SwissBounds.MIN_E + SECTOR_WIDTH * 2 + 100,
            SwissBounds.MIN_N + SECTOR_HEIGHT * 2 - 100);
    // PointCh[e=2495806.25, n=1076826.5625]
    private static final PointCh pointNode3 = new PointCh(
            SwissBounds.MIN_E + SECTOR_WIDTH * 4 - 100, SwissBounds.MIN_N + SECTOR_HEIGHT + 100);

    // public static void main(String[] args) {
    // initGlobalVars();
    // System.out.println(graph.edgeLength(2));
    // }

    private static void addEmptySectors(ByteBuffer buffer, int nb) {
        for (int i = 0; i < nb; i++) {
            // FIXME: no nodes -> whatever id of first node
            buffer.putInt(0);
            buffer.putShort((short) 0);
        }
    }

    /**
     * Type 2 compressed samples of two elevation deltas.
     *
     * @param e1 first elevation delta (integer of 4 bits max)
     * @param e2 second elevation delta (integer of 4 bits max)
     * @return compressed deltas as short
     */
    private static short compressedElevations(int e1, int e2) {
        return (short) (Q28_4.ofInt(e1) << 8 | Q28_4.ofInt(e2));
    }

    @BeforeAll
    static void initGlobalVars() {
        IntBuffer nodesBuffer = IntBuffer.allocate(4 * 3);
        // Positions comes from pointNodeI converted to Q28_4
        // Point 0
        nodesBuffer.put(0b1001011110101101101100 << 4);
        nodesBuffer.put(0b100000110011110011100 << 4);
        nodesBuffer.put(0x10000000); // 1 outgoing edges, first edge is id 0
        // Point 1
        nodesBuffer.put(0b1001011111011000010010_1001);
        nodesBuffer.put(0b100000111010100011001_0010);
        nodesBuffer.put(0x20000001); // 2 outgoing edges, first edge is id 1
        // Point 2
        nodesBuffer.put(0b1001100000000010111001_0010);
        nodesBuffer.put(0b100000111010001010001_0010);
        // FIXME: no outgoing edge -> whatever for id of first edge
        nodesBuffer.put(0x00000000); // 0 outgoing edges, no first edge
        // Point 3
        nodesBuffer.put(0b1001100001010100111110_0100);
        nodesBuffer.put(0b100000110111001011010_1001);
        nodesBuffer.put(0x00000000); // 0 outgoing edges, no first edge

        ByteBuffer sectorsBuffer = ByteBuffer.allocate(128 * 128 * (Integer.BYTES + Short.BYTES));
        // Sector (0, 0)
        sectorsBuffer.putInt(0);
        sectorsBuffer.putShort((short) 1);
        addEmptySectors(sectorsBuffer, 127); // End of first line
        addEmptySectors(sectorsBuffer, 2);
        // Sector (2, 1)
        sectorsBuffer.putInt(2);
        sectorsBuffer.putShort((short) 1);
        // Sector (3, 1)
        sectorsBuffer.putInt(3);
        sectorsBuffer.putShort((short) 1);
        addEmptySectors(sectorsBuffer, 124); // End of second line
        addEmptySectors(sectorsBuffer, 1);
        // Sector (1, 2)
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

        // 1368 samples for a total length of 2733.875
        ShortBuffer elevations = ShortBuffer.allocate(685);
        elevations.put((short) Q28_4.ofInt(600));
        elevations.put(compressedElevations(-5, -4));
        elevations.put(compressedElevations(3, 0));
        elevations.put(compressedElevations(5, 0));
        // The remaining 1368-7 samples are filled with 0s
        for (int i = 0; i < Math.ceil((1368 - 7) / 2); i++) {
            elevations.put((short) 0);
        }
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

    @Test
    void nodesTest() {
        assertEquals(4, graph.nodeCount());
        EdgeTest.assertEqualsPointCh(pointNode0, graph.nodePoint(0), 1e-6);
        EdgeTest.assertEqualsPointCh(pointNode1, graph.nodePoint(1), 1e-6);
        EdgeTest.assertEqualsPointCh(pointNode2, graph.nodePoint(2), 1e-6);
        EdgeTest.assertEqualsPointCh(pointNode3, graph.nodePoint(3), 1e-6);
        assertEquals(1, graph.nodeOutDegree(0));
        assertEquals(2, graph.nodeOutDegree(1));
        assertEquals(0, graph.nodeOutDegree(2));
        assertEquals(0, graph.nodeOutDegree(3));
        assertEquals(0, graph.nodeOutEdgeId(0, 0));
        assertEquals(1, graph.nodeOutEdgeId(1, 0));
        assertEquals(2, graph.nodeOutEdgeId(1, 1));
        assertEquals(2, graph.nodeClosestTo(pointNode2, 1.5 * SECTOR_WIDTH));
        assertEquals(2, graph.nodeClosestTo(pointNode2, 0));
        assertEquals(-1, graph.nodeClosestTo(new PointCh(SwissBounds.MIN_E + 1.5 * SECTOR_WIDTH,
                SwissBounds.MIN_N + 0.5 * SECTOR_HEIGHT), 200));
        // TO BE CONTINUED distances are fucked up
        assertEquals(0, graph.nodeClosestTo(new PointCh(SwissBounds.MIN_E + 1.2 * SECTOR_WIDTH,
                SwissBounds.MIN_N + 0.3 * SECTOR_HEIGHT), 2 * SECTOR_WIDTH));
        assertEquals(3, graph.nodeClosestTo(new PointCh(SwissBounds.MIN_E + 3 * SECTOR_WIDTH + 100,
                SwissBounds.MIN_N + SECTOR_HEIGHT), 2 * SECTOR_WIDTH));
    }

}

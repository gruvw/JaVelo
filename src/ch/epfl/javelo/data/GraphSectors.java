package ch.epfl.javelo.data;

import java.nio.ByteBuffer;
import java.util.List;
import ch.epfl.javelo.projection.PointCh;
import ch.epfl.javelo.projection.SwissBounds;

/**
 * All the 16384 sectors in JaVelo (record).
 * <p>
 * Arguments are not checked.
 * <p>
 * Sector attributes: (int) id of the first node, (short) number of nodes.
 *
 * @param buffer buffer memory containing the value of each attribute for all sectors
 *
 * @author Lucas Jung (324724)
 * @author Florian Kolly (328313)
 */
public record GraphSectors(ByteBuffer buffer) {

    // FIXME: correct offsets?
    /**
     * Position of the first node's id within a buffer range corresponding to a sector.
     */
    private final static byte OFFSET_FIRST_NODE = 0;

    /**
     * Position of the number of nodes (in the sector) within a buffer range corresponding to a
     * sector.
     */
    private final static byte OFFSET_NODE_COUNT = OFFSET_FIRST_NODE + Integer.BYTES;

    /**
     * A single sector.
     *
     * @param startNodeId id (index) of the first node in the sector
     * @param endNodeId id (index) of the node located right after the last node in the sector
     */
    public record Sector(int startNodeId, int endNodeId) {
    }

    /**
     * Lists all sectors having an intersection with a given {@code center} and of length equal to
     * twice the given {@code distance}.
     *
     * @param center point in the middle of the square
     * @param distance distance from the center of a square to any one of its four sides
     * @return a list of all sectors intersecting with the square centered at {@code center} and of
     *         side length equals to twice the {@code distance}
     */
    public List<Sector> sectorsInArea(PointCh center, double distance) {
        double xMin = center.e() - distance;
        double xMax = center.e() + distance;
        double yMin = center.n() - distance;
        double yMax = center.n() + distance;
        double sectorWidth = SwissBounds.WIDTH / 128.0;
        double sectorHeight = SwissBounds.HEIGHT / 128.0;
        int firstBorderSectorX = (int) (xMin / sectorWidth);
        int firstBorderSectorY = (int) (yMin / sectorHeight);
        int borderSectorIndex = firstBorderSectorX * 128 + firstBorderSectorY;
        int nbSectorsX = (int) Math.ceil(distance * 2 / sectorWidth);
        int nbSectorY = (int) Math.ceil(distance * 2 / sectorHeight);

    }

}

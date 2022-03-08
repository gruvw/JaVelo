package ch.epfl.javelo.data;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import ch.epfl.javelo.Math2;
import ch.epfl.javelo.projection.PointCh;
import ch.epfl.javelo.projection.SwissBounds;

/**
 * All the sectors in JaVelo (record).
 * <p>
 * Arguments are not checked.
 * <p>
 * Sector attributes: (int - U32) id of the first node, (short - U16) number of nodes.
 *
 * @param buffer buffer memory containing the value of each attribute for all sectors
 *
 * @author Lucas Jung (324724)
 * @author Florian Kolly (328313)
 */
public record GraphSectors(ByteBuffer buffer) {

    // == BUFFER ==

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
     * Size of a sector range within the buffer (in bytes).
     */
    private final static byte SECTOR_SIZE = OFFSET_NODE_COUNT + Short.BYTES;

    // == SECTORS GRID ==

    /**
     * Number of sectors per axis on the grid covering Switzerland. The total number of sectors is
     * therefore {@code SECTORS_PER_AXIS}².
     */
    private final static int SECTORS_PER_AXIS = 128;

    /**
     * The width (along the x-axis) of a sector.
     */
    private final static double SECTOR_WIDTH = SwissBounds.WIDTH / SECTORS_PER_AXIS;

    /**
     * The height (along the y-axis) of a sector.
     */
    private final static double SECTOR_HEIGHT = SwissBounds.HEIGHT / SECTORS_PER_AXIS;

    /**
     * A single sector.
     *
     * @param startNodeId id (index) of the first node in the sector
     * @param endNodeId   id (index) of the node located right after the last node in the sector
     */
    public record Sector(int startNodeId, int endNodeId) {

    }

    /**
     * Lists all sectors having an intersection with a given {@code center} and of length equal to
     * twice the given {@code distance}.
     *
     * @param center   point in the middle of the square
     * @param distance distance from the center of a square to any one of its four sides
     * @return a list of all sectors intersecting with the square centered at {@code center} and of
     *         side length equals to twice the {@code distance}
     */
    public List<Sector> sectorsInArea(PointCh center, double distance) {
        // FIXME: distance >= 0
        int lowestWidth = Math2.clamp(0,
                (int) ((center.e() - SwissBounds.MIN_E - distance) / SECTOR_WIDTH),
                SECTORS_PER_AXIS - 1);
        int lowestHeight = Math2.clamp(0,
                (int) ((center.n() - SwissBounds.MIN_N - distance) / SECTOR_HEIGHT),
                SECTORS_PER_AXIS - 1);
        int highestWidth = Math2.clamp(0,
                (int) ((center.e() - SwissBounds.MIN_E + distance) / SECTOR_WIDTH),
                SECTORS_PER_AXIS - 1);
        int highestHeight = Math2.clamp(0,
                (int) ((center.n() - SwissBounds.MIN_N + distance) / SECTOR_HEIGHT),
                SECTORS_PER_AXIS - 1);
        System.out.println("Lowest width: " + lowestWidth + "("
                + (int) ((center.e() - SwissBounds.MIN_E - distance) / SECTOR_WIDTH) + ")");
        System.out.println("Lowest height: " + lowestHeight + "("
                + (int) ((center.n() - SwissBounds.MIN_N - distance) / SECTOR_HEIGHT) + ")");
        System.out.println("Highest width: " + highestWidth + "("
                + (int) ((center.e() - SwissBounds.MIN_E + distance) / SECTOR_WIDTH) + ")");
        System.out.println("Highest height " + highestHeight + "("
                + (int) ((center.n() - SwissBounds.MIN_N + distance) / SECTOR_HEIGHT) + ")");
        int firstSector = lowestHeight * SECTORS_PER_AXIS + lowestWidth;
        int lastSector = highestHeight * SECTORS_PER_AXIS + highestWidth;
        System.out.println("First sector: " + firstSector);
        System.out.println("Last sector: " + lastSector);
        // Number of sectors to the right of firstSector (included)
        int width = (lastSector - firstSector) % SECTORS_PER_AXIS;
        // Number of sectors above firstSector (included)
        int height = (lastSector - firstSector) / SECTORS_PER_AXIS;
        System.out.println("==============================");

        List<Sector> sectors = new ArrayList<Sector>();
        for (int y = 0; y <= height; y++)
            for (int x = 0; x <= width; x++) {
                int sectorNb = firstSector + x + y * SECTORS_PER_AXIS;
                // As unsigned but small enough -> no need to convert to unsigned
                int startNodeId = buffer.getInt(sectorNb * SECTOR_SIZE);
                int nodeCount = Short.toUnsignedInt(
                        buffer.getShort(sectorNb * SECTOR_SIZE + OFFSET_NODE_COUNT));
                int endNodeId = startNodeId + nodeCount;
                sectors.add(new Sector(startNodeId, endNodeId));
            }
        return sectors;
    }

}

package ch.epfl.javelo.gui;

import ch.epfl.javelo.projection.PointCh;

/**
 * Represents a waypoint. (record)
 *
 * @param point         point in the Swiss coordinate system
 * @param closestNodeId identity of the closest graph node to the point
 *
 * @author Lucas Jung (324724)
 * @author Florian Kolly (328313)
 */
public record Waypoint(PointCh point, int closestNodeId) {
}

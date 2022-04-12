package ch.epfl.javelo.gui;

/**
 * Represents a crossing point. (record)
 * 
 * @param position      position of the crossing point in the Swiss coordinate system
 * @param closestNodeId identity of the closest node for the crossing point
 */
public record Waypoint(double position, int closestNodeId) {

}

package ch.epfl.javelo.data;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.function.DoubleUnaryOperator;
import ch.epfl.javelo.projection.PointCh;

/**
 * Represents the JaVelo graph.
 * <p>
 * Arguments are not checked.
 * <p>
 *
 * @author Lucas Jung (324724)
 * @author Florian Kolly (328313)
 */
public final class Graph {

  private final GraphNodes nodes;
  private final GraphSectors sectors;
  private final GraphEdges edges;
  private final List<AttributeSet> attributeSet;

  /**
   * Loads and creates the graph.
   *
   * @param nodes        graph's nodes
   * @param sectors      graph's sectors
   * @param edges        graph's edges
   * @param attributeSet graph's set of OSM attributes
   */
  public Graph(GraphNodes nodes, GraphSectors sectors, GraphEdges edges,
               List<AttributeSet> attributeSet) {
    this.nodes = nodes;
    this.sectors = sectors;
    this.edges = edges;
    this.attributeSet = List.copyOf(attributeSet);
  }

  /**
   * Retrieves the graph from the files in the directory indicated by {@code basePath}.
   *
   * @param basePath directory of the files
   * @return the graph with the nodes, the sectors, the edges and the attribute set
   * @throws IOException if any input/output errors is thrown during file related operations
   */
  public static Graph loadFrom(Path basePath) throws IOException {

  }

  /**
   * Retrieves the total number of nodes in the graph.
   *
   * @return the total number of nodes
   */
  public int nodeCount() {

  }

  /**
   * Retrieves the position of the given node.
   *
   * @param nodeId id (index) of the node
   * @return the position of the node corresponding to the given id
   */
  public PointCh nodePoint(int nodeId) {

  }

  /**
   * Retrieves the number of outgoing edges from the given node.
   *
   * @param nodeId id (index) of the node
   * @return the number of outgoing edges
   */
  public int nodeOutDegree(int nodeId) {

  }

  /**
   * Retrieves the index of the {@code edgeIndex}-th edge going out of the node.
   *
   * @param nodeId    id (index) of the node
   * @param edgeIndex id (index) of the edge
   * @return the index of the outgoing {@code edgeIndex}-th edge of the node corresponding to the
   *         given id
   */
  public int nodeOutEdgeId(int nodeId, int edgeIndex) {

  }

  /**
   * Retrieves the index of the node closest to the given point within a maximum distance (in
   * meters) of {@code searchDistance}.
   *
   * @param point          center around which the search is done
   * @param searchDistance maximum distance around the point
   * @return the closest node's index, -1 if no node respects the conditions
   */
  public int nodeClosestTo(PointCh point, double searchDistance) {

  }

  /**
   * Retrieves the index of an edge's destination node.
   *
   * @param edgeId id (index) of the edge
   * @return the index of the destination node
   */
  public int edgeTargetNodeId(int edgeId) {

  }

  /**
   * Checks if an edge is inverted compared to the OSM way it represents.
   *
   * @param edgeId id (index) of the edge
   * @return true if the edge corresponding to the given id is inverted, false otherwise
   */
  public boolean edgeIsInverted(int edgeId) {

  }

  /**
   * Retrieves the OSM attributes' set of an edge.
   *
   * @param edgeId id (index) of the edge
   * @return the OSM attributes' set of the edge corresponding to the given id
   */
  public AttributeSet edgeAttributes(int edgeId) {

  }

  /**
   * Retrieves the length of the edge in meters.
   *
   * @param edgeId id (index) of the edge
   * @return the length of the edge corresponding to the given id
   */
  public double edgeLength(int edgeId) {

  }

  /**
   * Retrieves the elevation gain of the edge, in meters.
   *
   * @param edgeId id (index) of the edge
   * @return the elevation gain of the edge corresponding to the given id
   */
  public double edgeElevationGain(int edgeId) {

  }

  /**
   * Retrieves the profile of an edge, as a function. If the edge does not have a profile, the
   * returned function will always return {@code Double.NaN}.
   *
   * @param edgeId id (index) of the edge
   * @return the profile of the edge corresponding to the given id, as a function or a function
   *         always returning {@code Double.NaN} if the edge does not have a profile
   */
  public DoubleUnaryOperator edgeProfile(int edgeId) {

  }

}

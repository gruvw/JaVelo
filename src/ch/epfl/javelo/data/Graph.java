package ch.epfl.javelo.data;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.function.DoubleUnaryOperator;
import ch.epfl.javelo.projection.PointCh;

public final class Graph {

  /**
   * All the edges of the JaVelo graph (record).
   * <p>
   * Arguments are not checked.
   * <p>
   * 
   * @param nodes
   * @param sectors
   * @param edges
   * @param attributeSet
   * 
   * @author Lucas Jung (324724)
   * @author Florian Kolly (328313)
   */
  public Graph(GraphNodes nodes, GraphSectors sectors, GraphEdges edges,
               List<AttributeSet> attributeSet) {

  }

  /**
   * Retrieves the graph from the files in the directory indicated by {@code basePath}.
   * 
   * @param basePath directory of the files
   * @return the graph with the nodes, the sectors, the edges and the attribute set
   * @throws IOException if any input/output errors is thrown during the file operation
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
   * Retrieves the position of the node with id {@code nodeId}.
   * 
   * @param nodeId id (index) of the node
   * @return the position of the node with given id
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
   * Retrieves the index of the {@code edgeIndex}-th edge going out from the node with id
   * {@code nodeId}.
   * 
   * @param nodeId    id (index) of the node
   * @param edgeIndex id (index) of the edge
   * @return the id (index) of the {@code edgeIndex}-th edge
   */
  public int nodeOutEdgeId(int nodeId, int edgeIndex) {

  }

  /**
   * Retrieves the index of the node closest to the given point within a maximum distance (in
   * meters) of {@code searchDistance}.
   * 
   * @param point
   * @param searchDistance maximum distance around the point
   * @return the index of the node, -1 if no node respects the conditions
   */
  public int nodeClosestTo(PointCh point, double searchDistance) {

  }

  /**
   * Retrieves the index of the given edge's destination node.
   * 
   * @param edgeId id (index) of the edge
   * @return the id (index) of the destination node
   */
  public int edgeTargetNodeId(int edgeId) {

  }

  /**
   * Checks if the given edge is inverted compared to the OSM way it represents.
   * 
   * @param edgeId id (index) of the edge
   * @return true if the edge is inverted, false otherwise
   */
  public boolean edgeIsInverted(int edgeId) {

  }

  /**
   * Retrieves the OSM attributes' set of the given edge.
   * 
   * @param edgeId id (index) of the edge
   * @return the OSM attributes' set of the edge
   */
  public AttributeSet edgeAttributes(int edgeId) {

  }

  /**
   * Retrieves the length (in meters) of the given edge.
   * 
   * @param edgeId id (index) of the edge
   * @return the length of the edge
   */
  public double edgeLength(int edgeId) {

  }

  /**
   * Retrieves the elevation gain (in meters) of the given edge.
   * 
   * @param edgeId id (index) of the edge
   * @return the elevation gain of the edge
   */
  public double edgeElevationGain(int edgeId) {

  }

  /**
   * Retrieves the profile of the given edge as a function. If the edge does not possess a profile,
   * the function returned will always map any input to {@code Double.NaN}.
   * 
   * @param edgeId id (index) of the edge
   * @return the profile of the edge as a function or a function always returning {@code Double.NaN}
   *         if the edge does not have a profil
   */
  public DoubleUnaryOperator edgeProfile(int edgeId) {

  }

}

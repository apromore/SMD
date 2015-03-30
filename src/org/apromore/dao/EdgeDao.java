package org.apromore.dao;

import org.apromore.dao.model.Edge;
import org.apromore.dao.model.GEdge;

import java.util.List;

/**
 * Interface domain model Data access object Edge.
 *
 * @author <a href="mailto:cam.james@gmail.com">Cameron James</a>
 * @version 1.0
 * @see org.apromore.dao.model.Edge
 */
public interface EdgeDao {


    /**
     * Returns the Edge records for the ContentId.
     * @param contentID the content id
     * @return the list of Edges or null.
     */
    List<Edge> getEdgesByContent(String contentID);

    /**
     * Returns the count of stored edges in the db.
     * @return the count of edges in the system.
     */
    Integer getStoredEdges();




    /**
     * Save the edge.
     * @param edge the edge to persist
     */
    void save(Edge edge);

    /**
     * Update the edge.
     * @param edge the edge to update
     */
    Edge update(Edge edge);

    /**
     * Remove the edge.
     * @param edge the edge to remove
     */
    void delete(Edge edge);

	List<Edge> getEdgesByFragment(String fragmentID);

	List<GEdge> getGEdgesByContent(String contentID);

}

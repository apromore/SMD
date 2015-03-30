package org.apromore.dao;

import java.util.List;
import java.util.Map;

import org.apromore.dao.model.FragmentVersion;
import org.apromore.dao.model.FragmentVersionDag;
import org.apromore.dao.model.FragmentVersionDagId;

/**
 * Interface domain model Data access object FragmentVersionDag.
 *
 * @author <a href="mailto:cam.james@gmail.com">Cameron James</a>
 * @version 1.0
 * @see org.apromore.dao.model.FragmentVersionDag
 */
public interface FragmentVersionDagDao {

    /**
     * Returns a single FragmentVersionDag based on the primary Key.
     * @param fragmentVersionDagId the Fragment Id
     * @return the found FragmentVersionDag
     */
    FragmentVersionDag findFragmentVersionDag(String fragmentVersionDagId);


    /**
     * Returns all the child mappings for the FragmentId.
     * @param fragmentId the fragment id
     * @return the list of child fragments
     */
    List<FragmentVersionDagId> getChildMappings(String fragmentId);
    
    /**
     * Returns all parent-child mappings between all fragments
     * @return mappings fragment Id -> list of child Ids for all fragments that has at least one child 
     */
    Map<String, List<String>> getAllParentChildMappings();
    
    /**
     * Returns all child-parent mappings between all fragments
     * @return mappings fragment Id -> list of parent Ids for all non-root fragments 
     */
    Map<String, List<String>> getAllChildParentMappings();
    
    List<FragmentVersionDag> getAllDAGEntries(int minimumChildFragmentSize);

    /**
     * the child Fragments from the fragment Version.
     * @param fragmentVersionId  the fragment version id
     * @return the list of child fragments.
     */
    List<FragmentVersion> getChildFragmentsByFragmentVersion(String fragmentVersionId);



    /**
     * Save the FragmentVersionDag.
     * @param fragmentVersionDag the FragmentVersionDag to persist
     */
    void save(FragmentVersionDag fragmentVersionDag);

    /**
     * Update the FragmentVersionDag.
     * @param fragmentVersionDag the FragmentVersionDag to update
     */
    FragmentVersionDag update(FragmentVersionDag fragmentVersionDag);

    /**
     * Remove the FragmentVersionDag.
     * @param fragmentVersionDag the FragmentVersionDag to remove
     */
    void delete(FragmentVersionDag fragmentVersionDag);


	void replaceChildren(String replacement, String occurance);


	void replaceParents(String replacement, String occurance);

}

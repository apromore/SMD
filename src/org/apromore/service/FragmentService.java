package org.apromore.service;

import org.apromore.dao.model.Content;
import org.apromore.dao.model.FragmentVersion;
import org.apromore.exception.ExceptionDao;
import org.apromore.exception.LockFailedException;
import org.apromore.exception.RepositoryException;
import org.apromore.graph.JBPT.CPF;
import org.apromore.model.FragmentData;
import org.apromore.service.impl.RPSTNodeCopy;
import org.apromore.service.model.FragmentAssociation;
import org.jbpt.pm.FlowNode;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Analysis Service. Used for the Node Usage Analyser and parts of the Repository Analyser.
 *
 * @author <a href="mailto:cam.james@gmail.com">Cameron James</a>
 */
public interface FragmentService {

    /**
     * Add a process Fragment Mapping record to the DB.
     * @param pmvid the process Model Version
     * @param composingFragmentIds the composing fragment id's
     * @throws ExceptionDao if the DB throws an exception
     */
    void addProcessFragmentMappings(Integer pmvid, List<String> composingFragmentIds) throws ExceptionDao;


    /**
     * Get the Fragment id.
     * @param pmvid the process fragment Version id
     * @param g the process model graph
     * @param nodes the list of nodes
     * @return the fragment Id
     */
    String getFragmentId(Integer pmvid, CPF g, List<String> nodes);

    /**
     * Get a particular Fragment based on fragmentId.
     * @param fragmentId the fragment to return
     * @param lock do we lock the table or not.
     * @return the found processModel.
     * @throws LockFailedException if we failed to obtain a lock on the table
     */
    CPF getFragment(String fragmentId, boolean lock) throws LockFailedException;

    /**
     * Get a particular Fragment version based on fragmentVersionId.
     * @param fragmentVersionId the fragment version to return
     * @return the found fragment version.
     */
    FragmentVersion getFragmentVersion(String fragmentVersionId);
    
    List<FragmentAssociation> getSharedFragments(int minSharings, int minFragmentSize);

    /**
     * Used to save a new Fragment to the DB.
     * @param cid
     * @param childMappings
     * @param derivedFrom
     * @param lockStatus
     * @param lockCount
     * @param originalSize
     * @param fragmentType
     */
    FragmentVersion addFragmentVersion(Content cid, Map<String, String> childMappings, String derivedFrom,
            int lockStatus, int lockCount, int originalSize, String fragmentType);

    /**
     * Used to Save a child mapping to the DB.
     * @param fragVer
     * @param childMappings
     * @throws ExceptionDao
     */
    void addChildMappings(FragmentVersion fragVer, Map<String, String> childMappings);

    /**
     * Gets the Matching Fragment Versions.
     * @param contentId
     * @param childMappings
     * @return
     */
    FragmentVersion getMatchingFragmentVersionId(final String contentId, final Map<String, String> childMappings);


    /**
     * Deletes the Fragment Version.
     * @param fvid the fragment Version id.
     */
    void deleteFragmentVersion(String fvid);

    /**
     * Deletes all the child relationships from a fragment Version.
     * @param fvid the fragment Version id.
     */
    void deleteChildRelationships(String fvid);

    /**
     * Update to have the new Derived fragments.
     * @param fvid the fragment Version Id
     * @param derivedFromFragmentId the id it was derived from.
     */
    void setDerivation(String fvid, String derivedFromFragmentId);


    /**
     * Temporarily method to serialise a given fragment into an EPML string. This is used for
     * visualising fragments in a cluster. Replace this later by using proper canonising to
     * support all process formats.
     * 
     * @param fragmentId Id of the required fragment
     * @return EPML string
     * @throws RepositoryException 
     */
	String getFragmentAsEPML(String fragmentId) throws RepositoryException;


	FragmentVersion storeFragment(String fragmentCode, RPSTNodeCopy fCopy, CPF g);


	List<FragmentVersion> getFragmentsOfProcess(String processName, int minSize);


	String getFragmentAsFormattedEPML(String fragmentId)
			throws RepositoryException;


	CPF getFragmentWithSubprocesses(String fragmentId, Collection<String> subprocesses) throws RepositoryException;


	CPF getFragmentWithSubprocessesGT(String fragmentId, Collection<String> subprocesses) throws RepositoryException;


	CPF getFragmentWithSubprocessesDWS(String fragmentId, Collection<String> subprocesses) throws RepositoryException;

}


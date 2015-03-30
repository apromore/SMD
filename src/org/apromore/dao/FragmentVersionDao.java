package org.apromore.dao;

import org.apromore.dao.model.FragmentVersion;
import org.apromore.dao.model.FragmentVersionDag;
import org.apromore.service.model.FragmentAssociation;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Define the Fragment Version DAO.
 *
 * @author <a href="mailto:cam.james@gmail.com">Cameron James</a>
 */
public interface FragmentVersionDao {
	
	Map<String, Integer> getAllFragmentIdsWithSize();

    /**
     * Get a single Fragment Version record.
     * @param fragmentId the fragment version id
     * @return the found fragmentVersion
     */
    FragmentVersion findFragmentVersion(String fragmentId);

    /**
     * Get all the Fragment Version records.
     * @return the list of records
     */
    List<FragmentVersion> getAllFragmentVersion();
    
    /**
     *
     * @param contentId
     * @param childMappingCode
     * @return
     */
    FragmentVersion getMatchingFragmentVersionId(String contentId, String childMappingCode);

    /**
     *
     * @param fvid
     * @return
     */
    Integer getUsedProcessModels(String fvid);

    /**
     *
     * @param fvid
     * @return
     */
    List<FragmentVersion> getParentFragments(String fvid);

    /**
     *
     * @param fvid
     * @return
     */
    List<String> getLockedParentFragmentIds(String fvid);

    /**
     *
     * @param fvid
     * @return
     */
    Map<String, Integer> getChildFragmentsWithSize(String fvid);

    /**
     *
     * @param fvid
     * @return
     */
    Map<Integer, String> getChildFragmentsWithType(int fvid);

    /**
     *
     * @param fvid
     * @return
     */
    String getContentId(String fvid);

    /**
     *
     * @param pmvid
     * @return
     */
    List<FragmentVersion> getFragmentDataOfProcessModel(String pmvid);

    /**
     *
     * @param fragmentId
     * @return
     */
    FragmentVersion getFragmentData(String fragmentId);

    /**
     *
     * @param nodes
     * @return
     */
    List<String> getContainingFragments(List<String> nodes);

    /**
     *
     * @param fragmentId
     * @return
     */
    List<Integer> getContainedProcessModels(int fragmentId);

    /**
     *
     * @param matchingContentId
     * @return
     */
    List<String> getUsedFragmentIds(String matchingContentId);

    /**
     *
     * @param minSize
     * @param maxSize
     * @return
     */
    List<FragmentVersion> getSimilarFragmentsBySize(int minSize, int maxSize);

    /**
     *
     * @param minSize
     * @param maxSize
     * @return
     */
    List<FragmentVersion> getSimilarFragmentsBySizeAndType(int minSize, int maxSize, String type);


    /**
     * Save the FragmentVersion.
     * @param fragVersion the FragmentVersion to persist
     */
    void save(FragmentVersion fragVersion);

    /**
     * Update the FragmentVersion.
     * @param fragVersion the FragmentVersion to update
     */
    FragmentVersion update(FragmentVersion fragVersion);

    /**
     * Remove the FragmentVersion.
     * @param fragVersion the FragmentVersion to remove
     */
    void delete(FragmentVersion fragVersion);

	List<FragmentVersion> getFragmentsOfProcess(String processName, int minSize);

	List<String> getSharedFragmentIds(int minSharings, int minFragmentSize);
	
	List<String> getProcessNamesOfFragment(String fragmentId);

	void setLockStatus(String fragmentVersionId, Integer lockStatus);

	void deleteFragmentVersion(String fragmentVersionId);

	void deleteFragments(Collection<String> fids);

	Collection<String> getFragmentIdsOfProcess(Integer pmvId);

	Collection<String> getProcessNamesOfFragmentId(String fid);

}

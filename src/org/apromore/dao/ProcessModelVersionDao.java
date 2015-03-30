package org.apromore.dao;

import org.apromore.dao.model.ProcessBranch;
import org.apromore.dao.model.ProcessModelVersion;

import java.util.List;
import java.util.Map;

/**
 * Interface domain model Data access object ProcessModelVersion.
 *
 * @see org.apromore.dao.model.Process
 * @author <a href="mailto:cam.james@gmail.com">Cameron James</a>
 * @version 1.0
 */
public interface ProcessModelVersionDao {

    /**
     * Returns a process model version.
     * @param processModelVersionId the process model version id
     * @return the ProcessModelVersion
     */
    ProcessModelVersion findProcessModelVersion(Integer processModelVersionId);


    /**
     * Returns the process model version by the branch id.
     * @param branchId the branch Id
     * @param branchName the branch Name
     * @return the found process model version
     */
    ProcessModelVersion findProcessModelVersionByBranch(Integer branchId, String branchName);

    /**
     * Gets the list of Used Fragment Models for a fragment version Id.
     * @param fragmentVersionId the fragment version if we are searching for used models
     * @return the list of found fragment model versions
     */
    List<ProcessModelVersion> getUsedProcessModelVersions(String fragmentVersionId);

    /**
     * find the current process model version for the branch provided.
     * @param branchName the branch name
     * @return the process model version.
     */
    ProcessModelVersion getCurrentProcessModelVersion(String branchName);

    /**
     * find the current process model version for the processname and branch provided.
     * @param processName the process name
     * @param branchName the branch name
     * @return the process model version.
     */
    ProcessModelVersion getCurrentProcessModelVersion(String processName, String branchName);

    /**
     * find the current process model version for the processname and branch provided.
     * @param processName the process name
     * @param branchName the branch name
     * @param versionName the version name
     * @return the process model version.
     */
    ProcessModelVersion getCurrentProcessModelVersion(String processName, String branchName, String versionName);

    /**
     * The Map of max model versions.
     * @param fragmentVersionId the fragment id
     * @return the mapped results
     */
    Map<String, Integer> getMaxModelVersions(String fragmentVersionId);

    /**
     * The Map of current model versions.
     * @param fragmentVersionId the fragment id
     * @return the mapped results
     */
    Map<String, Integer> getCurrentModelVersions(String fragmentVersionId);

    /**
     * Finds the max version of a process model for a particular branch.
     * @param branch the process branch
     * @return the processModel found.
     */
    ProcessModelVersion getMaxVersionProcessModel(ProcessBranch branch);

    /**
     * Returns all the ProcessModels for all version or the latest versions.
     * @param isLatestVersion are we looking for the latest or all models
     * @return returns the list of processModelVersions
     */
    List<ProcessModelVersion> getAllProcessModelVersions(boolean isLatestVersion);



    /**
     * Save the ProcessModelVersion.
     * @param processModelVersion the ProcessModelVersion to persist
     */
    void save(ProcessModelVersion processModelVersion);

    /**
     * Update the ProcessModelVersion.
     * @param processModelVersion the ProcessModelVersion to update
     */
    ProcessModelVersion update(ProcessModelVersion processModelVersion);

    /**
     * Remove the ProcessModelVersion.
     * @param processModelVersion the ProcessModelVersion to remove
     */
    void delete(ProcessModelVersion processModelVersion);


	List<String> getRootFragments(int minSize);


	String getCurrentRootFragmentId(int processId, String string);


	void replaceRoot(String newRoot, String oldRoot);


	void replaceProcessFragmentMaps(String rep, String member);


	void deleteProcessModel(Integer pmvId);

}

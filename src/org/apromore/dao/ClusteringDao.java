/**
 * 
 */
package org.apromore.dao;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.map.MultiKeyMap;
import org.apromore.dao.model.ClusterInfo;
import org.apromore.dao.model.ClusteringSummary;
import org.apromore.dao.model.FragmentDistance;
import org.apromore.dao.model.FragmentVersion;
import org.apromore.service.model.ClusterFilter;
import org.apromore.toolbox.clustering.algorithms.dbscan.FragmentDataObject;
import org.apromore.toolbox.clustering.algorithms.dbscan.FragmentPair;
import org.apromore.toolbox.clustering.algorithms.dbscan.InMemoryCluster;

/**
 * @author Chathura C. Ekanayake
 *
 */
public interface ClusteringDao {
	
	void insertDistances(MultiKeyMap dissimmap);

	List<ClusterInfo> getAllClusters();
	
	List<ClusterInfo> getFilteredClusters(ClusterFilter filter);
	
	List<String> getFragmentIds(String clusterId);
	
	List<FragmentVersion> getFragments(String clusterId);

	double getDistance(String fragmentId1, String fragmentId2);
	
	Map<FragmentPair, Double> getDistances(final double threshold);
	
	void persistClusters(Collection<ClusterInfo> clusters);

	void persistClusterAssignments(Collection<InMemoryCluster> values);

	List<FragmentDataObject> getUnprocessedFragments();

	List<FragmentDataObject> getUnprocessedFragmentsOfProcesses(List<Integer> processIds);

	void clearClusters();

	ClusteringSummary getClusteringSummary();

	ClusterInfo getClusterSummary(String clusterId);

	void clearDistances();

	List<FragmentDistance> getRawDistances(double threshold);

	List<String> getClustersOfFragment(String fragmentId);

	Map<FragmentPair, Double> getFragmentsWithDistance(double distance);

	void deleteDistance(double distance);

	void deleteDistancesWithFragment(String fragmentId);

	List<String> getSharedFragmentIds();

	Map<String, List<String>> getAllFragmentClusterMappings();

	void deleteDistances(Collection<String> fids);

	void insertDistancesJDBC(MultiKeyMap dissimmap);
}

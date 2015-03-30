/**
 * 
 */
package org.apromore.service;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apromore.dao.model.ClusterInfo;
import org.apromore.dao.model.ClusteringSummary;
import org.apromore.exception.RepositoryException;
import org.apromore.graph.JBPT.CPF;
import org.apromore.service.model.Cluster;
import org.apromore.service.model.ClusterFilter;
import org.apromore.service.model.ClusterSettings;
import org.apromore.toolbox.clustering.algorithms.dbscan.FragmentPair;

/**
 * @author Chathura C. Ekanayake
 *
 */
public interface ClusteringService {
	
//	List<String> getClusterIds();
	
//	ClusterInfo getCluster(String clusterId);
	
	void computeGEDMatrix();
	
	void cluster(ClusterSettings settings) throws RepositoryException;

	ClusteringSummary getClusteringSummary();
	
	List<ClusterInfo> getClusters();
	
	List<Cluster> getClusters(ClusterFilter filter);
	
	List<String> getFragmentIds(String clusterId);

	Map<FragmentPair, Double> getPairDistances(List<String> fragmentIds) throws RepositoryException;

	List<ClusterInfo> getClusterSummaries(ClusterFilter filter);

	Cluster getCluster(String clusterId);

	void appendGEDMatrix(Collection<String> newRoots);

	void serializeClusters(String outPath);

//	double getGED(String fragmentId1, String fragmentId2, boolean computeIfNecessary);
}

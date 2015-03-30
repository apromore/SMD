package org.apromore.mining.standardize;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apromore.dao.ClusteringDao;
import org.apromore.dao.FragmentVersionDagDao;
import org.apromore.dao.model.ClusterInfo;
import org.apromore.service.utils.OverlapResolver;
import org.jbpt.graph.DirectedEdge;
import org.jbpt.graph.DirectedGraph;
import org.jbpt.graph.MultiDirectedGraph;
import org.jbpt.graph.algo.DirectedGraphAlgorithms;
import org.jbpt.hypergraph.abs.Vertex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

/**
 * Standardizes all clusters based on a given set of rules. But does not remove any cluster or a member fragment of
 * a cluster, as they are considered as options for standardization (e.g. if users want to manually refine
 * standardizations). Also each cluster is considered as a subprocess, even it is completely contained in another
 * cluster, because it captures methods performing a procedure in alternative ways.
 * 
 * Each cluster is standardized based on given rules. Current rules are select one fragment automatically, select one
 * fragment manually and select all (i.e. standardized fragment can be selected at deployment or execution time).
 * 
 * @author cn
 *
 */
public class RuleBasedClusterStandardizer {
	
	private final Logger logger = LoggerFactory.getLogger(RuleBasedClusterStandardizer.class);

	@Autowired
	@Qualifier("FragmentVersionDagDao")
	private FragmentVersionDagDao fragmentVersionDagDao;

	@Autowired
	@Qualifier("ClusteringDao")
	private ClusteringDao clusteringDao;

	@Autowired
	private OverlapResolver overlapResolver;

	private Map<String, List<String>> parentChildMap = null;
	private Map<String, List<String>> childParentMap = null;
	private Map<String, List<String>> fragmentClusterMap = null;
	private DirectedGraphAlgorithms<DirectedEdge, Vertex> galgo = new DirectedGraphAlgorithms<DirectedEdge, Vertex>();
	private Map<String, List<String>> cs = null;
	private Map<String, Vertex> idVertexMap = new HashMap<String, Vertex>();
	private Map<String, ClusterInfo> clusterData = new HashMap<String, ClusterInfo>();

	public Map<String, StandardizedCluster> getStandardizedClusters() {

		Map<String, StandardizedCluster> clusters = new HashMap<String, StandardizedCluster>();
		
		initializeClusterData();

		for (String cid : cs.keySet()) {
			StandardizedCluster stdCluster = standardize(cid);
			clusters.put(cid, stdCluster);
		}
		return clusters;
	}

	private StandardizedCluster standardize(String cid) {
		
		StandardizedCluster standardizedCluster = new StandardizedCluster(cid);
		List<String> fids = cs.get(cid);
		
		Collection<String> removedFIds = new HashSet<String>();
		
		standardizedCluster.setFragmentIds(fids);
//		standardizedCluster.addStandardFragment(fids.get(0));
		standardizedCluster.getStandardFragmentIds().addAll(fids);
		standardizedCluster.setRemovedFragmentIds(removedFIds);
		
		logger.debug("Standardized the cluster {} of size {} with all fragments", 
				new Object[] {cid, fids.size()});
		return standardizedCluster;
	}
	
	public Map<String, List<String>> getFragmentClusterMap() {
		return fragmentClusterMap;
	}

	private void initializeClusterData() {

		parentChildMap = fragmentVersionDagDao.getAllParentChildMappings();
		childParentMap = fragmentVersionDagDao.getAllChildParentMappings();
		fragmentClusterMap = clusteringDao.getAllFragmentClusterMappings();
		cs = overlapResolver.fetchAllClusters();
		
		List<ClusterInfo> cinfos = clusteringDao.getAllClusters();
		for (ClusterInfo cinfo : cinfos) {
			clusterData.put(cinfo.getClusterId(), cinfo);
		}
	}
}

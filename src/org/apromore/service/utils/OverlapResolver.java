package org.apromore.service.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apromore.dao.ClusteringDao;
import org.apromore.dao.FragmentVersionDao;
import org.apromore.dao.model.ClusterInfo;
import org.apromore.exception.RepositoryException;
import org.apromore.service.model.ClusterSettings;
import org.apromore.toolbox.clustering.algorithms.dbscan.FragmentDataObject;
import org.apromore.toolbox.clustering.algorithms.dbscan.FragmentPair;
import org.apromore.toolbox.clustering.algorithms.dbscan.InMemoryCluster;
import org.apromore.toolbox.clustering.algorithms.dbscan.InMemoryGEDMatrix;
import org.apromore.toolbox.clustering.analyzers.ClusterAnalyzer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

public class OverlapResolver {
	
	private static final Logger logger = LoggerFactory.getLogger(OverlapResolver.class);
	
	@Autowired @Qualifier("ClusteringDao")
    private ClusteringDao clusteringDao;
	
	@Autowired
	private ClusterAnalyzer clusterAnalyzer;
	
	@Autowired @Qualifier("FragmentVersionDao")
	private FragmentVersionDao fragmentVersionDao;
	
	@Autowired
	private InMemoryGEDMatrix gedMatrix;

	private Map<String, Integer> fragmentSizes = null;
	private Map<FragmentPair, Double> distances = null;
	
	public void resolveOverlaps() throws RepositoryException {
		
		fragmentSizes = fragmentVersionDao.getAllFragmentIdsWithSize();
		distances = clusteringDao.getDistances(0.5d);
		
		List<String> sharedFids = clusteringDao.getSharedFragmentIds();
		
		// fragment Id -> cluster Ids
		Map<String, List<String>> overlappingClusters = buildFragmentClusterMap(sharedFids);
		logger.debug("Resolving {} overlappings fragments...", overlappingClusters.size());
		
		// cluster Id -> fragment Ids
		Map<String, List<String>> cs = fetchAllClusters();
		
		if (logger.isTraceEnabled()) {
			StringBuffer b = new StringBuffer();
			for (String cid : cs.keySet()) {
				b.append("C" + cid + ":" + cs.get(cid).size() + ", ");
			}
			logger.trace("Clusters before resolving overlaps: " + b.toString());
		}
		
		for (String fid : overlappingClusters.keySet()) {
			Map<String, List<String>> sharedClusters = new HashMap<String, List<String>>();
			for (String sharedCId : overlappingClusters.get(fid)) {
				// cluster may be removed, if its membership has gone below 2 while processing overlappings
				if (cs.containsKey(sharedCId)) {
					sharedClusters.put(sharedCId, cs.get(sharedCId));
				}
			}
			
			String bestCId =  findBestCluster(fid, sharedClusters);
			
			for (String sharedCId : sharedClusters.keySet()) {
				if (!sharedCId.equals(bestCId)) {
					List<String> members = sharedClusters.get(sharedCId);
					members.remove(fid);
					if (members.size() < 2) {
						// this is a singleton cluster. so remove it.
						cs.remove(sharedCId);
					}
				}
			}
		}
		
		if (logger.isTraceEnabled()) {
			StringBuffer b = new StringBuffer();
			for (String cid : cs.keySet()) {
				b.append("C" + cid + ":" + cs.get(cid).size() + ", ");
			}
			logger.trace("Clusters after resolving overlaps: " + b.toString());
		}
		
		persistClusters(cs);
	}
	
	public Map<String, List<String>> fetchAllClusters() {
		
		Map<String, List<String>> cs = new HashMap<String, List<String>>();
		List<ClusterInfo> cinfos = clusteringDao.getAllClusters();
		for (ClusterInfo cinfo : cinfos) {
			String cid = cinfo.getClusterId();
			List<String> members = clusteringDao.getFragmentIds(cid);
			cs.put(cid, members);
		}
		return cs;
	}

	public Map<String, List<String>> buildFragmentClusterMap(List<String> fids) {
		
		Map<String, List<String>> fragmentClusterMap = new HashMap<String, List<String>>();
		for (String fid : fids) {
			List<String> cids = clusteringDao.getClustersOfFragment(fid);
			fragmentClusterMap.put(fid, cids);
		}
		return fragmentClusterMap;
	}

	private void persistClusters(Map<String, List<String>> cs) throws RepositoryException {
		
		ClusterSettings settings = new ClusterSettings();
		settings.setDbscanClustering(true);
		settings.setMaxNeighborGraphEditDistance(0.4d);
		gedMatrix.initialize(settings, null, null, null);
		
		Collection<InMemoryCluster> inMemoryClusters = buildInMemoryClusters(cs);
		clusterAnalyzer.loadFragmentSizes();
		List<ClusterInfo> cds = new ArrayList<ClusterInfo>();
		for (InMemoryCluster cluster : inMemoryClusters) {
			ClusterInfo cd = clusterAnalyzer.analyzeCluster(cluster, settings);
			cds.add(cd);
		}
		
		Set<String> dids = findDuplicates(cds);
		
		clusteringDao.clearClusters();
		clusteringDao.persistClusters(cds);
		clusteringDao.persistClusterAssignments(inMemoryClusters);
	}

	private Set<String> findDuplicates(List<ClusterInfo> cds) {
		Set<String> ids = new HashSet<String>();
		Set<String> dids = new HashSet<String>();
		for (ClusterInfo c : cds) {
			if (ids.contains(c.getClusterId())) {
				dids.add(c.getClusterId());
			}
			ids.add(c.getClusterId());
		}
		return dids;
	}

	private Collection<InMemoryCluster> buildInMemoryClusters(Map<String, List<String>> cs) {
		
		List<InMemoryCluster> inMemoryClusters = new ArrayList<InMemoryCluster>();
		for (String cid : cs.keySet()) {
			InMemoryCluster inMemoryCluster = new InMemoryCluster(cid, "NA");
			List<FragmentDataObject> fragments = new ArrayList<FragmentDataObject>();
			for (String fid : cs.get(cid)) {
				FragmentDataObject fragment = new FragmentDataObject(fid);
				fragments.add(fragment);
			}
			inMemoryCluster.setFragments(fragments);
			inMemoryClusters.add(inMemoryCluster);
		}
		return inMemoryClusters;
	}

	private String findBestCluster(String fid, Map<String, List<String>> sharedClusters) {
		
		String bestCid = null;
		double maxBCRGain = Double.NEGATIVE_INFINITY;
		
		for (String cid : sharedClusters.keySet()) {
			List<String> excludedMembers = new ArrayList<String>(sharedClusters.get(cid));
			excludedMembers.remove(fid);
			double excludedBCR = computeMaximumBCR(excludedMembers);
			double includedBCR = computeMaximumBCR(sharedClusters.get(cid));
			double bcrGain = includedBCR - excludedBCR;
			if (bcrGain > maxBCRGain) {
				maxBCRGain = bcrGain;
				bestCid = cid;
			}
		}
		return bestCid;
	}
	
	private String findBestCluster2(String fid, Map<String, List<String>> sharedClusters) {
		
		String bestCid = null;
		double maxBCR = Double.NEGATIVE_INFINITY;
		
		for (String cid : sharedClusters.keySet()) {
			double bcr = computeMaximumBCR(sharedClusters.get(cid));
			if (bcr > maxBCR) {
				maxBCR = bcr;
				bestCid = cid;
			}
		}
		return bestCid;
	}

	private double computeMaximumBCR(List<String> members) {
		
		int sumOfSizes = 0;
		for (String member : members) {
			int size = fragmentSizes.get(member);
			sumOfSizes += size;
		}
		
		double maxBCR = 0;
		for (String member : members) {
			double bcr = computeBCR(member, members, sumOfSizes);
			if (bcr > maxBCR) {
				maxBCR = bcr;
			}
		}

		return maxBCR;
	}
	
	private double computeBCR(String medoid, List<String> members, int sumOfSizes) {
		
		double standardizingEffort = 0; // sum of absolute geds
		int refactGain = 0;
		double benifitCostRatio = 0;
		
		for (String member : members) {
			Double distance = distances.get(new FragmentPair(medoid, member));
			if (distance == null) {
				distance = 1d;
			}
			double cost = distance * (fragmentSizes.get(medoid) + fragmentSizes.get(member));
			standardizingEffort += cost;
		}
		
		int medoidSize = fragmentSizes.get(medoid);
		refactGain = (sumOfSizes - medoidSize) - (members.size() - 1);
		benifitCostRatio = refactGain / standardizingEffort;
		
		return benifitCostRatio;
	}

	public Map<String, Integer> getFragmentSizes() {
		if (fragmentSizes == null) {
			fragmentSizes = fragmentVersionDao.getAllFragmentIdsWithSize();
		}
		return fragmentSizes;
	}

	public Map<FragmentPair, Double> getDistances() {
		if (distances == null) {
			distances = clusteringDao.getDistances(0.5d);
		}
		return distances;
	}
}

package org.apromore.mining.standardize;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apromore.dao.ClusteringDao;
import org.apromore.dao.FragmentVersionDagDao;
import org.apromore.mining.MiningConfig;
import org.apromore.service.utils.IDGenerator;
import org.apromore.service.utils.OverlapResolver;
import org.apromore.toolbox.clustering.algorithms.dbscan.FragmentPair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

public class ExactCloneDetector {
	
	private static final Logger logger = LoggerFactory.getLogger(ExactCloneDetector.class);
	
	@Autowired
	private ClusteringDao clusteringDao;
	
	@Autowired
	private OverlapResolver overlapResolver;
	
	private Map<String, List<String>> childParentMap = null;
	
	@Autowired @Qualifier("FragmentVersionDagDao")
	private FragmentVersionDagDao fragmentVersionDagDao;

	public Map<String, StandardizedCluster> detectClones() {

		Map<String, Integer> fsizes = overlapResolver.getFragmentSizes();
		childParentMap = fragmentVersionDagDao.getAllChildParentMappings();
		Map<FragmentPair, Double> geds = clusteringDao.getDistances(0.01d);
		Set<FragmentPair> clonePairs = geds.keySet();
		
		logger.debug("Exact clone pairs identified by GED: {}", clonePairs.size());
		
		// filter out non-maximal fragments
		Set<FragmentPair> invalidPairs = new HashSet<FragmentPair>();
		Collection<String> exactCloneFIds = getAllFids(clonePairs);
		for (FragmentPair pair : clonePairs) {
			Collection<String> fid1Ancestors = getAnscestors(pair.getFid1());
			fid1Ancestors.remove(pair.getFid1());
			if (!Collections.disjoint(fid1Ancestors, exactCloneFIds)) {
				invalidPairs.add(pair);
				continue;
			}
			
			Collection<String> fid2Ancestors = getAnscestors(pair.getFid2());
			fid2Ancestors.remove(pair.getFid2());
			if (!Collections.disjoint(fid2Ancestors, exactCloneFIds)) {
				invalidPairs.add(pair);
				continue;
			}
			
			int fid1Size = 0;
			if (fsizes.get(pair.getFid1()) != null) {
				fid1Size = fsizes.get(pair.getFid1());
			}
			int fid2Size = 0;
			if (fsizes.get(pair.getFid2()) != null) {
				fid2Size = fsizes.get(pair.getFid2());
			}
			if (fid1Size < MiningConfig.MIN_EXACT_CLONE_FRAGMENT_SIZE || 
					fid2Size < MiningConfig.MIN_EXACT_CLONE_FRAGMENT_SIZE) {
				invalidPairs.add(pair);
				continue;
			}
		}
		clonePairs.removeAll(invalidPairs);
		logger.debug("Exact clone pairs after filtering: {}", clonePairs.size());
		
		// join clones to form clusters
		Collection<StandardizedCluster> clusters = new HashSet<StandardizedCluster>();
		for (FragmentPair pair : clonePairs) {
			String fid1 = pair.getFid1();
			String fid2 = pair.getFid2();
			boolean clusterFound = false;
			for (StandardizedCluster c : clusters) {
				if (c.getFragmentIds().contains(fid1)) {
					c.getFragmentIds().add(fid2);
					clusterFound = true;
					break;
				}
				
				if (c.getFragmentIds().contains(fid2)) {
					c.getFragmentIds().add(fid1);
					clusterFound = true;
					break;
				}
			}
			
			if (!clusterFound) {
				StandardizedCluster c = new StandardizedCluster(IDGenerator.generateExactCloneID());
				List<String> fids = new ArrayList<String>();
				fids.add(fid1);
				fids.add(fid2);
				c.setFragmentIds(fids);
				clusters.add(c);
			}
		}
		if (logger.isDebugEnabled()) {
			int totalFragments = 0;
			for (StandardizedCluster c : clusters) {
				totalFragments += c.getFragmentIds().size();
			}
			logger.debug("Found {} exact clone clusters containg {} fragments.", clusters.size(), totalFragments);
		}
		
		// put clusters in the required format
		Map<String, StandardizedCluster> cmap = new HashMap<String, StandardizedCluster>();
		for (StandardizedCluster c : clusters) {
			Collection<String> removedFIds = new HashSet<String>();
			String representativeFid = c.getFragmentIds().get(0);
			c.setRepresentativeFragmentId(representativeFid);
			c.getStandardFragmentIds().add(representativeFid);
			c.setRemovedFragmentIds(removedFIds);
			cmap.put(c.getClusterId(), c);
		}
		
		return cmap;
	}
	
	private Collection<String> getAllFids(Set<FragmentPair> clonePairs) {
		Set<String> fids = new HashSet<String>();
		for (FragmentPair pair : clonePairs) {
			fids.add(pair.getFid1());
			fids.add(pair.getFid2());
		}
		return fids;
	}

	private Collection<String> getAnscestors(String fid) {
		Set<String> ancestorFIds = new HashSet<String>();
		fillAncestors(fid, ancestorFIds);
		return ancestorFIds;
	}

	private void fillAncestors(String fid, Set<String> ancestorFIds) {
		List<String> parentIds = childParentMap.get(fid);
		if (parentIds == null) {
			return;
		}
		
		ancestorFIds.addAll(parentIds);
		for (String parentFId : parentIds) {
			fillAncestors(parentFId, ancestorFIds);
		}
	}

}

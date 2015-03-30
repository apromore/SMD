package org.apromore.toolbox.clustering.algorithms.expansion;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apromore.dao.ClusteringDao;
import org.apromore.toolbox.clustering.algorithms.dbscan.FragmentPair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import clustering.containment.ContainmentRelationImpl;
import clustering.hierarchy.GEDSimilaritySearcher;
import clustering.hierarchy.ResultFragment;

public class ClusterExpander {
	
	@Autowired
	private ClusteringDao clusteringDao;
	
	@Autowired
	private GEDSimilaritySearcher gedSearcher;
	
	@Autowired @Qualifier("ContainmentRelation")
	private ContainmentRelationImpl crel;
	
	private Map<FragmentPair, Double> inMemoryGEDs;
	
	public void initialize() {
		inMemoryGEDs = clusteringDao.getDistances(0.5);
		crel.setMinSize(3);
		gedSearcher.initialize();
		try {
			crel.initialize();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public List<ExpansionFragment> getExpansionFragments(String clusterId, double minDistance, double maxDistance) {
		List<String> fragmentIds = clusteringDao.getFragmentIds(clusterId);
		return getExpansionFragments(fragmentIds, minDistance, maxDistance);
	}
	
	public List<ExpansionFragment> getExpansionFragments(List<String> fragmentIds, double minDistance, double maxDistance) {
		
		List<ExpansionFragment> expansions = new ArrayList<ExpansionFragment>();
		
		for (String fid : fragmentIds) {
			List<ExpansionFragment> neighbours = getNeighbours2(fid, minDistance, maxDistance);
			for (ExpansionFragment ef : neighbours) {
				
				if (fragmentIds.contains(ef.getFragmentId())) {
					continue;
				}
				
				if (expansions.contains(ef)) {
					ExpansionFragment oldEF = expansions.get(expansions.indexOf(ef));
					if (oldEF.getDistanceToMemberFragment() > ef.getDistanceToMemberFragment()) {
						expansions.remove(ef);
					} else {
						continue;
					}
				}
				
				String nid = ef.getFragmentId();
				int inid = crel.getFragmentIndex(nid);
				
				boolean inContainment = false;
				for (String memberId : fragmentIds) {
					int iMemberId = crel.getFragmentIndex(memberId);
					if (crel.areInContainmentRelation(iMemberId, inid)) {
						inContainment = true;
						break;
					}
				}
				
				if (!inContainment) {
					
					double sizeDifference = Math.abs(crel.getFragmentSize(fid) - ef.getFragmentSize());
					double sizeRatio = sizeDifference / crel.getFragmentSize(fid);
					ef.setSizeRatio(sizeRatio);
					
					int numClusters = clusteringDao.getClustersOfFragment(nid).size();
					ef.setNumClusters(numClusters);
					
					expansions.add(ef);
				}
			}
		}
		return expansions;
	}
	
	private List<ExpansionFragment> getNeighbours2(String fid, double minDistance, double maxDistance) {
		
		List<ExpansionFragment> neighbours = new ArrayList<ExpansionFragment>();
		gedSearcher.setDissThreshold(maxDistance);
		List<ResultFragment> results = gedSearcher.search(fid);
		
		for (ResultFragment rf : results) {
			double distance = rf.getDistance();
			if (distance > minDistance && distance < maxDistance) {
				ExpansionFragment ef = new ExpansionFragment();
				ef.setFragmentId(rf.getFragmentId());
				ef.setMemberFragmentId(fid);
				ef.setDistanceToMemberFragment(distance);
				ef.setFragmentSize(rf.getFragmentSize());
				neighbours.add(ef);
			}
		}
		return neighbours;
	}

	private List<ExpansionFragment> getNeighbours(String fid, double minDistance, double maxDistance) {
		
		List<ExpansionFragment> neighbours = new ArrayList<ExpansionFragment>();
		
		for (FragmentPair pair : inMemoryGEDs.keySet()) {
			if (pair.hasFragment(fid)) {
				double distance = inMemoryGEDs.get(pair);
				if (distance > minDistance && distance < maxDistance) {
					ExpansionFragment ef = new ExpansionFragment();
					String nid = pair.getFid1() == fid? pair.getFid2() : pair.getFid2();
					ef.setFragmentId(nid);
					ef.setMemberFragmentId(fid);
					ef.setDistanceToMemberFragment(distance);
					neighbours.add(ef);
				}
			}
		}
		return neighbours;
	}
}

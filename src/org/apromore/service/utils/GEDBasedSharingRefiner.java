package org.apromore.service.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apromore.dao.ClusteringDao;
import org.apromore.dao.FragmentVersionDagDao;
import org.apromore.dao.FragmentVersionDao;
import org.apromore.dao.ProcessModelVersionDao;
import org.apromore.toolbox.clustering.algorithms.dbscan.FragmentPair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

public class GEDBasedSharingRefiner {
	
	@Autowired
	private ClusteringDao clusteringDao;
	
	@Autowired @Qualifier("FragmentVersionDagDao")
	private FragmentVersionDagDao fvdagDao;
	
	@Autowired @Qualifier("ProcessModelVersionDao")
	private ProcessModelVersionDao pmvDao;
	
	@Autowired @Qualifier("FragmentVersionDao")
	private FragmentVersionDao fvDao;
	
	public Set<Set<String>> getExactClones() {
		Map<FragmentPair, Double> pairs = clusteringDao.getFragmentsWithDistance(0);
		Set<Set<String>> clones = calculateClones(pairs);
		return clones;
	}

	public void refine() {
		
		Map<FragmentPair, Double> pairs = clusteringDao.getFragmentsWithDistance(0);
		Set<Set<String>> clones = calculateClones(pairs);
		
		for (Set<String> clone : clones) {
			refineClone(clone);
		}
		
		clusteringDao.deleteDistance(0);
	}

	private void refineClone(Set<String> clone) {
		
		if (clone.contains("ff8081813987cbe5013987cdfc030a82")) {
			System.out.println("Processing clone with ff8081813987cbe5013987cdfc030a82");
		}
		
		String rep = null;
		
		for (String member : clone) {
			if (rep == null) {
				rep = member;
				continue;
			}
			System.out.println("Replacing parent " + member + " with " + rep);
			fvdagDao.replaceParents(rep, member);
			
			System.out.println("Replacing child " + member + " with " + rep);
			fvdagDao.replaceChildren(rep, member);
			
			System.out.println("Replacing root " + member + " with " + rep);
			pmvDao.replaceRoot(rep, member);
			
			System.out.println("Deleting " + member);
			fvDao.deleteFragmentVersion(member);
			
			clusteringDao.deleteDistancesWithFragment(member);
			
			pmvDao.replaceProcessFragmentMaps(rep, member);
			
			if (member.equals("ff8081813987cbe5013987cdfc030a82")) {
				System.out.println("ff8081813987cbe50139895027330e08 DELETED");
			}
		}
	}
	
	private Set<Set<String>> calculateClones(Map<FragmentPair, Double> pairs) {
		
		Map<String, List<String>> parentChildMap = fvdagDao.getAllParentChildMappings();

		Set<Set<String>> clones = new HashSet<Set<String>>();
		
		Set<FragmentPair> pairKeys = pairs.keySet();
		for (FragmentPair pair : pairKeys) {
			boolean addedToClone = false;
			for (Set<String> clone : clones) {
				if (clone.contains(pair.getFid1())) {
					if (!clone.contains(pair.getFid2())) {
						clone.add(pair.getFid2());
					}
					addedToClone = true;
				} else if (clone.contains(pair.getFid2())) {
					if (!clone.contains(pair.getFid1())) {
						clone.add(pair.getFid1());
					}
					addedToClone = true;
				} 
			}
			
			if (!addedToClone) {
				Set<String> clone = new HashSet<String>();
				clone.add(pair.getFid1());
				clone.add(pair.getFid2());
				clones.add(clone);
			}
		}
		
		Set<Set<String>> refinedClones = new HashSet<Set<String>>();
		for (Set<String> pclone : clones) {
			boolean found = false;
			for (Set<String> rclone : refinedClones) {
				if (isIdentical(pclone, rclone)) {
					found = true;
					break;
				}
			}
			if (!found) {
				refinedClones.add(pclone);
			}
		}
		
		for (Set<String> clone : refinedClones) {
			refineParents(clone, parentChildMap);
		}
		
		return refinedClones;
	}
	
	private void refineParents(Set<String> clone, Map<String, List<String>> parentChildMap) {
		
		Set<String> tobeRemoved = new HashSet<String>();
		for (String member : clone) {
			List<String> children = parentChildMap.get(member);
			if (children != null) {
				for (String other : clone) {
					if (!other.equals(member)) {
						if (children.contains(other)) {
							tobeRemoved.add(member);
							break;
						}
					}
				}
			}
		}
		clone.removeAll(tobeRemoved);
	}
	
	private boolean isIdentical(Set<String> s1, Set<String> s2) {
		return s1.containsAll(s2);
	}
	
	private Set<Set<String>> getContainingClones(String fid, Set<Set<String>> clones) {
		
		Set<Set<String>> containingClones = new HashSet<Set<String>>();
		for (Set<String> clone : clones) {
			if (clone.contains(fid)) {
				containingClones.add(clone);
			}
		}
		return containingClones;
	}
}

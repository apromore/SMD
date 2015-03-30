package org.apromore.toolbox.clustering.algorithms.dbscan.refinements;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import org.apromore.dao.ClusteringDao;
import org.apromore.dao.FragmentVersionDagDao;
import org.apromore.dao.model.ClusterInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

public class MaximalFragmentFilter {
	
	private Map<String, List<String>> childParentMappings = null;
	
	@Autowired
	private ClusteringDao cdao;
	
	@Autowired @Qualifier("FragmentVersionDagDao")
	private FragmentVersionDagDao fdag;
	
	public void removeNonMaximalFragments() {
		
		if (childParentMappings == null) {
			childParentMappings = fdag.getAllChildParentMappings();
		}
		
		List<ClusterInfo> cs = cdao.getAllClusters();
		for (ClusterInfo c : cs) {
			removeNonMaximalFragments(c);
		}
	}

	private void removeNonMaximalFragments(ClusterInfo c) {

		List<String> fids = cdao.getFragmentIds(c.getClusterId());
		Collection<String> fragmentsToRemove = new HashSet<String>();
		
		for (String fid : fids) {
			if (!fragmentsToRemove.contains(fid)) {
				if (!checkAvoidingPaths(fid, fids)) {
					fragmentsToRemove.add(fid);
				}
			}
		}
		
		System.out.println(c.getClusterId() + " -> " + fragmentsToRemove.size());
	}

	private boolean checkAvoidingPaths(String fid, List<String> fids) {
		
		boolean pathFound = true;
		
		Queue<String> q = new LinkedList<String>();
		q.add(fid);
		
		while (!q.isEmpty()) {
			String currentFid = q.poll();
			List<String> pids = childParentMappings.get(currentFid);
			if (pids == null || pids.isEmpty()) {
				continue;
			}
			
			if (!Collections.disjoint(pids, fids)) {
				pathFound = false;
				break;
			} else {
				q.addAll(pids);
			}
		}
		
		return pathFound;
	}

}

package org.apromore.service.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apromore.dao.ClusteringDao;
import org.apromore.dao.FragmentVersionDagDao;
import org.jbpt.graph.DirectedEdge;
import org.jbpt.graph.DirectedGraph;
import org.jbpt.graph.algo.DirectedGraphAlgorithms;
import org.jbpt.hypergraph.abs.Vertex;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

public class ClusterHierarchyResolver {

	@Autowired @Qualifier("FragmentVersionDagDao")
	private FragmentVersionDagDao fragmentVersionDagDao;
	
	@Autowired @Qualifier("ClusteringDao")
    private ClusteringDao clusteringDao;

	@Autowired
	private OverlapResolver overlapResolver;

	private Map<String, List<String>> parentChildMap = null;
	private Map<String, List<String>> childParentMap = null;
	private Map<String, List<String>> fragmentClusterMap = null;
	private DirectedGraphAlgorithms<DirectedEdge, Vertex> galgo = new DirectedGraphAlgorithms<DirectedEdge, Vertex>();
	private Map<String, List<String>> cs = null;
	private Map<String, Vertex> idVertexMap = new HashMap<String, Vertex>();

	public void resolveHierarchy() {

		parentChildMap = fragmentVersionDagDao.getAllParentChildMappings();
		childParentMap = fragmentVersionDagDao.getAllChildParentMappings();
		fragmentClusterMap = clusteringDao.getAllFragmentClusterMappings();
		cs = overlapResolver.fetchAllClusters();

		List<DirectedGraph> cgs = new ArrayList<DirectedGraph>();
		Set<String> processedCIds = new HashSet<String>();

		for (String cid : cs.keySet()) {
			if (!processedCIds.contains(cid)) {
				DirectedGraph cg = new DirectedGraph();
				buildHierarchy(cid, cg, processedCIds);
				cgs.add(cg);
			}
		}
		
		int total = 0;
		for (DirectedGraph cg: cgs) {
			if (galgo.isAcyclic(cg)) {
				if (cg.getVertices().size() > 1) {
					System.out.println(cg.getVertices().size());
					total += cg.getVertices().size();
				}
			} else {
				System.out.println("CYCLIC: " + cg.getVertices().size());
				
				if (cg.getVertices().size() > 100) {
					System.out.println("==================================");
					Set<String> hfids = new HashSet<String>();
					for (Vertex cv : cg.getVertices()) {
						List<String> hfs = cs.get(cv.getId());
						hfids.addAll(hfs);
					}
					
					String out = "";
					for (String hfid : hfids) {
						out += hfid + ", ";
					}
					System.out.println("Sources: " + galgo.getSources(cg).size());
					System.out.println("Sinks: " + galgo.getSinks(cg).size());
					
					System.out.println("Count: " + hfids.size());
					System.out.println(out);
					System.out.println("\n==================================");
				}
			}
		}
		System.out.println("Total: " + total);
	}

	private void buildHierarchy(String cid, DirectedGraph cg,
			Set<String> processedCIds) {

		if (processedCIds.contains(cid)) {
			return;
		}

		Vertex v = new Vertex();
		v.setId(cid);
		idVertexMap.put(cid, v);
		if (!cg.contains(v)) {
			cg.addVertex(v);
		}

		Set<String> immediateHierarchy = new HashSet<String>();
		Set<String> parentClusters = getParentClusters(cid);
		for (String pcid : parentClusters) {
			immediateHierarchy.add(pcid);
			if (!edgeExists(cg, pcid, cid)) {
				addEdge(cg, pcid, cid);
			}
		}

		Set<String> childClusters = getChildClusters(cid);
		for (String ccid : childClusters) {
			immediateHierarchy.add(ccid);
			if (!edgeExists(cg, cid, ccid)) {
				addEdge(cg, cid, ccid);
			}
		}

		processedCIds.add(cid);
		for (String hcid : immediateHierarchy) {
			buildHierarchy(hcid, cg, processedCIds);
		}

	}

	private void addEdge(DirectedGraph cg, String parentId, String childId) {
		Vertex parent = idVertexMap.get(parentId);
		if (parent == null) {
			parent = new Vertex();
			parent.setId(parentId);
		}
		
		Vertex child = idVertexMap.get(childId);
		if (child == null) {
			child = new Vertex();
			child.setId(childId);
		}
		
		cg.addEdge(parent, child);
	}

	private boolean edgeExists(DirectedGraph cg, String parentId, String childId) {
		Vertex parent = idVertexMap.get(parentId);
		Vertex child = idVertexMap.get(childId);
		
		boolean edgeExists = false;
		Collection<DirectedEdge> es = cg.getEdges();
		for (DirectedEdge e : es) {
			if (e.getSource().equals(parent) && e.getTarget().equals(child)) {
				edgeExists = true;
			}
		}
		return edgeExists;
	}

	private Set<String> getChildClusters(String cid) {
		
		Set<String> childCIds = new HashSet<String>();
		List<String> fids = cs.get(cid);
		for (String fid : fids) {
			List<String> childFIds = parentChildMap.get(fid);
			if (childFIds != null) {
				for (String cfid : childFIds) {
					List<String> ccids = fragmentClusterMap.get(cfid);
					if (ccids != null) {
						childCIds.addAll(ccids);
					}
				}
			}
		}
		return childCIds;
	}

	private Set<String> getParentClusters(String cid) {

		Set<String> parentCIds = new HashSet<String>();
		List<String> fids = cs.get(cid);
		for (String fid : fids) {
			List<String> parentFIds = childParentMap.get(fid);
			if (parentFIds != null) {
				for (String pfid : parentFIds) {
					List<String> pcids = fragmentClusterMap.get(pfid);
					if (pcids != null) {
						parentCIds.addAll(pcids);
					}
				}
			}
		}
		return parentCIds;
	}
}

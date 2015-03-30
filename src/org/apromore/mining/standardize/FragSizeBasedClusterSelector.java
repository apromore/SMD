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
import org.apromore.exception.RepositoryException;
import org.apromore.service.utils.OverlapResolver;
import org.apromore.toolbox.clustering.algorithms.dbscan.InMemoryGEDMatrix;
import org.jbpt.graph.DirectedEdge;
import org.jbpt.graph.MultiDirectedGraph;
import org.jbpt.graph.algo.DirectedGraphAlgorithms;
import org.jbpt.hypergraph.abs.Vertex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

public class FragSizeBasedClusterSelector {
	
	private final Logger logger = LoggerFactory.getLogger(FragSizeBasedClusterSelector.class);

	@Autowired
	@Qualifier("FragmentVersionDagDao")
	private FragmentVersionDagDao fragmentVersionDagDao;

	@Autowired
	@Qualifier("ClusteringDao")
	private ClusteringDao clusteringDao;

	@Autowired
	private OverlapResolver overlapResolver;
	
	@Autowired
	private InMemoryGEDMatrix gedMatrix;

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

		List<MultiDirectedGraph> cgs = new ArrayList<MultiDirectedGraph>();
		Set<String> processedCIds = new HashSet<String>();

		for (String cid : cs.keySet()) {
			if (!processedCIds.contains(cid)) {
				MultiDirectedGraph cg = new MultiDirectedGraph();
				buildHierarchy(cid, cg, processedCIds);
				cgs.add(cg);
			}
		}
		logger.debug("Found {} clusters in {} connected graphs.", cs.size(), cgs.size());

		for (MultiDirectedGraph cg : cgs) {
			standardize(cg, clusters);
		}

		return clusters;
	}

	private void standardize(MultiDirectedGraph cg, Map<String, StandardizedCluster> clusters) {
		
		logger.trace("Standardizing a cluster graph with {} vertices...", cg.getVertices().size());
		
		Set<String> processedClusterIds = new HashSet<String>();
		
		Vertex cv = getNextCluster(cg, processedClusterIds);
		while (cv != null) {
//			StandardizedCluster standardizedCluster = standardizeWithAll(cv.getId());
			StandardizedCluster standardizedCluster = standardize(cv.getId());
			clusters.put(cv.getId(), standardizedCluster);
			processedClusterIds.add(cv.getId());
			
			Collection<String> removedFragmentIds = standardizedCluster.getRemovedFragmentIds();
			
			refineParentClusters(cv, cg, removedFragmentIds);
			refineChildClusters(cv, cg, removedFragmentIds);
			
			cv = getNextCluster(cg, processedClusterIds);
		}
	}
	
	private StandardizedCluster standardize(String cid) {

		StandardizedCluster standardizedCluster = new StandardizedCluster(cid);
		List<String> fids = cs.get(cid);
		
//		String stdFragmentId = fids.get(0); // TODO: original code
		
		// test code
		String stdFragmentId = null;
		try {
			stdFragmentId = findMedoid(fids);
		} catch (RepositoryException e) {
			logger.error("Failed to find the medoid of the cluster {}. Assigning a random standard fragment...", cid);
			stdFragmentId = fids.get(0);
		}
		// end of test code
		
		Collection<String> removedFIds = new HashSet<String>(fids);
		removedFIds.remove(stdFragmentId);
		
		standardizedCluster.setFragmentIds(fids);
		standardizedCluster.addStandardFragment(stdFragmentId);
		standardizedCluster.setRemovedFragmentIds(removedFIds);
		
		logger.trace("Standardized the cluster {} of size {} with fragment {}", 
				new Object[] {cid, fids.size(), stdFragmentId});
		return standardizedCluster;
	}
	
	private String findMedoid(Collection<String> fids) throws RepositoryException {
		
		String medoid = null;
		double lowestDistance = Double.MAX_VALUE;
		
		for (String candidate : fids) {
			double totalDistance = 0;
			for (String other : fids) {
				if (!candidate.equals(other)) {
					double distance = gedMatrix.getGED(candidate, other);
					totalDistance += distance;
				}
			}
			double avgDistance = totalDistance / (fids.size() - 1);
			if (avgDistance < lowestDistance) {
				lowestDistance = avgDistance;
				medoid = candidate;
			}
		}
		return medoid;
	}
	
	private StandardizedCluster standardizeWithAll(String cid) {

		StandardizedCluster standardizedCluster = new StandardizedCluster(cid);
		List<String> fids = cs.get(cid);
		
		Collection<String> removedFIds = new HashSet<String>();
		
		standardizedCluster.setFragmentIds(fids);
		standardizedCluster.getStandardFragmentIds().addAll(fids);
		standardizedCluster.setRemovedFragmentIds(removedFIds);
		
		logger.debug("Standardized the cluster {} of size {} with all fragments", 
				new Object[] {cid, fids.size()});
		return standardizedCluster;
	}

	private Vertex getNextCluster(MultiDirectedGraph cg, Set<String> processedClusterIds) {
		
		Vertex selectedCV = null;
		double maxAvgFragSize = Double.MIN_VALUE;
		Collection<Vertex> vs = cg.getVertices();
		for (Vertex cv : vs) {
			if (!processedClusterIds.contains(cv.getId())) {
				ClusterInfo cinfo = clusterData.get(cv.getId());
				if (cinfo.getAvgFragmentSize() > maxAvgFragSize) {
					selectedCV = cv;
					maxAvgFragSize = cinfo.getAvgFragmentSize();
				}
			}
		}
		return selectedCV;
	}
	
	private Vertex getNextCluster2(MultiDirectedGraph cg, Set<String> processedClusterIds) {
		
		Vertex selectedCV = null;
		double minAvgFragSize = Double.MAX_VALUE;
		Collection<Vertex> vs = cg.getVertices();
		for (Vertex cv : vs) {
			if (!processedClusterIds.contains(cv.getId())) {
				ClusterInfo cinfo = clusterData.get(cv.getId());
				if (cinfo.getAvgFragmentSize() < minAvgFragSize) {
					selectedCV = cv;
					minAvgFragSize = cinfo.getAvgFragmentSize();
				}
			}
		}
		return selectedCV;
	}

	private void refineChildClusters(Vertex cv, MultiDirectedGraph cg, Collection<String> removedFragmentIds) {
		
		Collection<Vertex> verticesToBeRemoved = new HashSet<Vertex>();
		Collection<DirectedEdge> edgesToBeRemoved = new HashSet<DirectedEdge>();
		Collection<Vertex> childCVs = cg.getDirectSuccessors(cv);
		for (Vertex childCV : childCVs) {
			Collection<DirectedEdge> edges = cg.getEdgesWithSourceAndTarget(cv, childCV);
			for (DirectedEdge e : edges) {
				String[] parts = e.getDescription().split("_");
				String parentFragmentId = parts[0];
				String childFragmentId = parts[1];
				if (removedFragmentIds.contains(parentFragmentId)) {
					edgesToBeRemoved.add(e);
					List<String> childFIds = cs.get(childCV.getId());
					childFIds.remove(childFragmentId);
					fragmentClusterMap.remove(childFragmentId);
					if (childFIds.size() < 2) {
						verticesToBeRemoved.add(childCV);
						
						String ccid = childCV.getId();
						List<String> cfids = cs.get(ccid);
						if (cfids.size() == 1) {
							String remainingCFid = cfids.get(0);
							fragmentClusterMap.remove(remainingCFid);
						}
						cs.remove(ccid);
						
						logger.trace("Child cluster " + childCV.getId() + " was removed due to removing " + childFragmentId + " | New size: " + childFIds.size());
						// we have marked the child cluster of this cluster to be removed. no need to process the
						// edges to the child cluster any further.
						break;
					}
					logger.trace("Child cluster " + childCV.getId() + " was refined by removing " + childFragmentId + " | New size: " + childFIds.size());
				}
			}
		}
		
		cg.removeVertices(verticesToBeRemoved);
		cg.removeEdges(edgesToBeRemoved);
		
		logger.trace("Refined child log clusters of {} by removing {} vertices and {} edges.", 
				new Object[] {cv.getId(), verticesToBeRemoved.size(), edgesToBeRemoved.size()});
	}

	private void refineParentClusters(Vertex cv, MultiDirectedGraph cg, Collection<String> removedFragmentIds) {

		Collection<Vertex> verticesToBeRemoved = new HashSet<Vertex>();
		Collection<DirectedEdge> edgesToBeRemoved = new HashSet<DirectedEdge>();
		Collection<Vertex> parentCVs = cg.getDirectPredecessors(cv);
		for (Vertex parentCV : parentCVs) {
			Collection<DirectedEdge> edges = cg.getEdgesWithSourceAndTarget(parentCV, cv);
			for (DirectedEdge e : edges) {
				String[] parts = e.getDescription().split("_");
				String parentFragmentId = parts[0];
				String childFragmentId = parts[1];
				
				// remove all linked fragments in the parent cluster. once we standardize this cluster, it should be
				// allowed to override this standardization by standardizing a parent cluster.
				edgesToBeRemoved.add(e);
				List<String> parentFIds = cs.get(parentCV.getId());
				parentFIds.remove(parentFragmentId);
				fragmentClusterMap.remove(parentFragmentId);
				if (parentFIds.size() < 2) {
					verticesToBeRemoved.add(parentCV);
					String pcid = parentCV.getId();
					List<String> pfids = cs.get(pcid);
					if (pfids.size() == 1) {
						String remainingPFid = pfids.get(0);
						fragmentClusterMap.remove(remainingPFid);
					}
					cs.remove(pcid);
					
					logger.trace("Parent cluster " + parentCV.getId() + " was removed due to removing " + parentFragmentId + " | New size: " + parentFIds.size());
					// we have marked the parent cluster of this cluster to be removed. no need to process the
					// edges from the parent cluster any further.
					break;
				} 
				logger.trace("Parent cluster " + parentCV.getId() + " was refined by removing " + parentFragmentId + " | New size: " + parentFIds.size());
			}
		}
		
		cg.removeVertices(verticesToBeRemoved);
		cg.removeEdges(edgesToBeRemoved);
		
		logger.trace("Refined parent log clusters of {} by removing {} vertices and {} edges.", 
				new Object[] {cv.getId(), verticesToBeRemoved.size(), edgesToBeRemoved.size()});
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

	private void buildHierarchy(String cid, MultiDirectedGraph cg, Set<String> processedCIds) {

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
		Map<String, String> parentClusters = getParentClusters(cid);
		for (String pcid : parentClusters.keySet()) {
			immediateHierarchy.add(pcid);
			if (!edgeExists(cg, parentClusters.get(pcid))) {
				addEdge(cg, pcid, cid, parentClusters.get(pcid));
			}
		}

		Map<String, String> childClusters = getChildClusters(cid);
		for (String ccid : childClusters.keySet()) {
			immediateHierarchy.add(ccid);
			if (!edgeExists(cg, childClusters.get(ccid))) {
				addEdge(cg, cid, ccid, childClusters.get(ccid));
			}
		}

		processedCIds.add(cid);
		for (String hcid : immediateHierarchy) {
			buildHierarchy(hcid, cg, processedCIds);
		}

	}

	private void addEdge(MultiDirectedGraph cg, String parentId, String childId, String description) {
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

		DirectedEdge e = cg.addEdge(parent, child);
		e.setDescription(description);
	}

	private boolean edgeExists(MultiDirectedGraph cg, String description) {
		boolean edgeExists = false;
		Collection<DirectedEdge> es = cg.getEdges();
		for (DirectedEdge e : es) {
			if (e.getDescription().equals(description)) {
				edgeExists = true;
				break;
			}
		}
		return edgeExists;
	}

	private Map<String, String> getChildClusters(String cid) {

		Map<String, String> childCIds = new HashMap<String, String>();
		List<String> fids = cs.get(cid);
		for (String fid : fids) {
//			List<String> descendantFIds = parentChildMap.get(fid);
			Collection<String> descendantFIds = getDescendants(fid);
			if (descendantFIds != null) {
				for (String cfid : descendantFIds) {
					List<String> ccids = fragmentClusterMap.get(cfid);
					if (ccids != null) {
						childCIds.put(ccids.get(0), fid + "_" + cfid);
					}
				}
			}
		}
		return childCIds;
	}

	private Collection<String> getDescendants(String fid) {
		Set<String> descendantFIds = new HashSet<String>();
		fillDescendants(fid, descendantFIds);
		return descendantFIds;
	}
	
	private void fillDescendants(String fid, Set<String> descendantFIds) {
		List<String> childIds = parentChildMap.get(fid);
		if (childIds == null) {
			return;
		}
		
		descendantFIds.addAll(childIds);
		for (String childFId : childIds) {
			fillDescendants(childFId, descendantFIds);
		}
	}

	/**
	 * 
	 * @param cid
	 * @return parent cluster Id -> Id to be used for the edge from this parent 
	 */
	private Map<String, String> getParentClusters(String cid) {

		Map<String, String> parentCIds = new HashMap<String, String>();
		List<String> fids = cs.get(cid);
		for (String fid : fids) {
			Collection<String> ancestorFIds = getAnscestors(fid);
			if (ancestorFIds != null) {
				for (String pfid : ancestorFIds) {
					List<String> pcids = fragmentClusterMap.get(pfid);
					if (pcids != null) {
						parentCIds.put(pcids.get(0), pfid + "_" + fid);
					}
				}
			}
		}
		return parentCIds;
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
	
	public Map<String, List<String>> getFragmentClusterMap() {
		return fragmentClusterMap;
	}

	private void log(String msg) {
		System.out.println(msg);
	}
}

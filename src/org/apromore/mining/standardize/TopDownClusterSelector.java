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
import org.apromore.dao.FragmentVersionDao;
import org.apromore.dao.model.ClusterInfo;
import org.apromore.exception.RepositoryException;
import org.apromore.graph.JBPT.CPF;
import org.apromore.mining.complexity.AggregatedComplexityChecker;
import org.apromore.service.utils.OverlapResolver;
import org.apromore.toolbox.clustering.algorithms.dbscan.FragmentPair;
import org.apromore.toolbox.clustering.algorithms.dbscan.InMemoryGEDMatrix;
import org.apromore.util.DebugUtil;
import org.jbpt.graph.DirectedEdge;
import org.jbpt.graph.MultiDirectedGraph;
import org.jbpt.graph.algo.DirectedGraphAlgorithms;
import org.jbpt.graph.algo.StronglyConnectedComponents;
import org.jbpt.hypergraph.abs.Vertex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

public class TopDownClusterSelector {
	
	private final Logger logger = LoggerFactory.getLogger(TopDownClusterSelector.class);

	@Autowired
	@Qualifier("FragmentVersionDagDao")
	private FragmentVersionDagDao fragmentVersionDagDao;
	
	@Autowired @Qualifier("ClusteringDao")
	private ClusteringDao clusteringDao;

	@Autowired
	private OverlapResolver overlapResolver;
	
	@Autowired
	private InMemoryGEDMatrix gedMatrix;
	
	@Autowired
	private ProcessMerger processMerger;
	
	@Autowired
	private AggregatedComplexityChecker complexityChecker;

	private Map<String, List<String>> parentChildMap = null;
	private Map<String, List<String>> childParentMap = null;
	private Map<String, List<String>> fragmentClusterMap = null;
	private DirectedGraphAlgorithms<DirectedEdge, Vertex> ga = new DirectedGraphAlgorithms<DirectedEdge, Vertex>();
	private static StronglyConnectedComponents<DirectedEdge, Vertex> sa = new StronglyConnectedComponents<DirectedEdge, Vertex>();
	private Map<String, List<String>> cs = null;
	private Map<String, Vertex> idVertexMap = new HashMap<String, Vertex>();
	private Map<String, ClusterInfo> clusterData = new HashMap<String, ClusterInfo>();
	
	private Map<String, Integer> fragmentSizes = null;
	private Map<String, String> fragmentTypes = null;
	private Map<FragmentPair, Double> distances = null;

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
			resolveCycles(cg);
			standardize(cg, clusters);
		}
		
		if (logger.isTraceEnabled()) {
			StringBuffer b = new StringBuffer();
			b.append("Standardized clusters: ");
			for (String cid : clusters.keySet()) {
				StandardizedCluster sc = clusters.get(cid);
				b.append("C" + cid + ":" + sc.getFragmentIds().get(0) + ":");
				StringBuffer b2 = new StringBuffer();
				b2.append("{");
				for (String sfid : sc.getFragmentIds()) {
					b2.append(sfid + ", ");
				}
				b2.append("}");
				b.append(b2.toString() + ", ");
			}
			logger.trace(b.toString());
		}

		return clusters;
	}

	private void resolveCycles(MultiDirectedGraph cg) {
		
		boolean hadCycles = false;
		if (logger.isTraceEnabled()) {
			if (ga.isCyclic(cg)) {
				hadCycles = true;
				logger.trace("Resolving cycles of a cluster graph of size: {}", cg.getVertices().size());
			}
		}
		
		while (ga.isCyclic(cg)) {
			Set<Set<Vertex>> sccs = sa.compute(cg);
			
			for (Set<Vertex> scc : sccs) {
				if (scc.size() < 2) {
					continue;
				}
				
				boolean fixed = resolveCycle(scc, cg);
				if (fixed) {
					break;
				}
			}
		}
		
		if (logger.isTraceEnabled()) {
			if (hadCycles) {
				logger.trace("Cycles resolved for a cluster graph of size: {}", cg.getVertices().size());
			}
		}
	}

	private boolean resolveCycle(Set<Vertex> scc, MultiDirectedGraph cg) {
		
		logger.trace("Resolving a cycle of size: {}", scc.size());
		
		Vertex selectedV = selectVertex(scc);
		String cid = selectedV.getId();
		List<String> fids = cs.get(cid);
		logger.trace("Cluster ID:{} with size {} is selected for modification", cid, fids.size());
		
		if (fids.size() < 3) {
			// cannot remove any fragments. we have to remove this cluster.
			cg.removeVertex(selectedV);
			for (String fid : fids) {
				fragmentClusterMap.remove(fid);
			}
			cs.remove(cid);
			logger.trace("Cluster ID:{} with size {} is removed in as it participated in a cycle.", cid, fids.size());
			
		} else {
			
			// analyze all removal options
			List<Collection<DirectedEdge>> removableIncomingEdges = new ArrayList<Collection<DirectedEdge>>();
			List<Collection<DirectedEdge>> removableOutgoingEdges = new ArrayList<Collection<DirectedEdge>>();
			Collection<Vertex> ps = cg.getDirectPredecessors(selectedV);
			Collection<Vertex> ss = cg.getDirectSuccessors(selectedV);
			for (Vertex sccV : scc) {
				if (ps.contains(sccV)) {
					removableIncomingEdges.add(cg.getEdgesWithSourceAndTarget(sccV, selectedV));
				}

				if (ss.contains(sccV)) {
					removableOutgoingEdges.add(cg.getEdgesWithSourceAndTarget(selectedV, sccV));
				}
			}
			
			// select the removable option with smallest number of edges 
			// (i.e. we need to remove minimum number of fragments)
			Collection<DirectedEdge> selectedIncomingEdges = new ArrayList<DirectedEdge>();
			Collection<DirectedEdge> selectedOutgoingEdges = new ArrayList<DirectedEdge>();
			
			for (Collection<DirectedEdge> inEdges : removableIncomingEdges) {
				if (selectedIncomingEdges.isEmpty()) {
					selectedIncomingEdges = inEdges;
				} else {
					if (inEdges.size() < selectedIncomingEdges.size()) {
						selectedIncomingEdges = inEdges;
					}
				}
			}
			
			for (Collection<DirectedEdge> outEdges : removableOutgoingEdges) {
				if (selectedOutgoingEdges.isEmpty()) {
					selectedOutgoingEdges = outEdges;
				} else {
					if (outEdges.size() < selectedOutgoingEdges.size()) {
						selectedOutgoingEdges = outEdges;
					}
				}
			}
			
			// identify fragments to be removed from the selected cluster
			List<String> selectedFragments = new ArrayList<String>();
			if (selectedOutgoingEdges.isEmpty() || 
					(!selectedIncomingEdges.isEmpty() && selectedIncomingEdges.size() < selectedOutgoingEdges.size())) {
				for (DirectedEdge selectedEdge : selectedIncomingEdges) {
					String selectedFId = selectedEdge.getDescription().split("_")[1];
					selectedFragments.add(selectedFId);
				}
				cg.removeEdges(selectedIncomingEdges);
			} else {
				for (DirectedEdge selectedEdge : selectedOutgoingEdges) {
					String selectedFId = selectedEdge.getDescription().split("_")[0];
					selectedFragments.add(selectedFId);
				}
				cg.removeEdges(selectedOutgoingEdges);
			}
			
			if (fids.size() - selectedFragments.size() < 2) {
				// cluster contains less than 2 fragments after removing fragments. so just remove the cluster.
				cg.removeVertex(selectedV);
				for (String fid : fids) {
					fragmentClusterMap.remove(fid);
				}
				cs.remove(cid);
				logger.trace("Cluster ID:{} with size {} is removed in as it participated in a cycle.", cid, fids.size());
			} else {
				// remove selected fragments from the cluster.
				fids.removeAll(selectedFragments);
				for (String selectedFid : selectedFragments) {
					fragmentClusterMap.remove(selectedFid);
				}
				logger.trace("{} fragments removed from {} to eliminate a cycle. Removed fragments: {}", 
						new Object[] {selectedFragments.size(), cid, DebugUtil.getAsString(selectedFragments)});
			}
		}
		
		if (logger.isTraceEnabled()) {
			logger.trace("Cycle of size {} resolved. New cluster graph contains {} vertices and {} edges.", 
					new Object[] {scc.size(), cg.getVertices().size(), cg.getEdges().size()});
		}
		
		return true;
	}
	
	private Vertex selectVertex(Set<Vertex> scc) {
		// select the cluster with minimum BCR
		Vertex selectedV = null;
		double minBCR = Double.MAX_VALUE;
		for (Vertex v : scc) {
			double bcr = computeMaximumBCR(cs.get(v.getId()));
			if (bcr < minBCR) {
				selectedV = v;
				minBCR = bcr;
			}
		}
		return selectedV;
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

	private void standardize(MultiDirectedGraph cg, Map<String, StandardizedCluster> clusters) {
		
		logger.trace("Standardizing a cluster graph with {} vertices...", cg.getVertices().size());
		
		Set<Vertex> processedVs = new HashSet<Vertex>();
		
		Vertex cv = getNextCluster(cg, processedVs);
		while (cv != null) {
			processedVs.add(cv);
			
			StandardizedCluster standardizedCluster = standardizeWithMerge(cv, cg);
//			StandardizedCluster standardizedCluster = standardizeWithAll(cv.getId());
//			StandardizedCluster standardizedCluster = standardize(cv.getId());
			if (standardizedCluster != null) {
				clusters.put(cv.getId(), standardizedCluster);
				Collection<String> removedFragmentIds = standardizedCluster.getRemovedFragmentIds();
				refineChildClusters(cv, cg, removedFragmentIds);
			}
			
			cv = getNextCluster(cg, processedVs);
		}
	}
	
	private StandardizedCluster standardizeWithMerge(Vertex cv, MultiDirectedGraph cg) {
		
		String cid = cv.getId();
		List<String> fids = cs.get(cid);
		String mergedFragmentId = processMerger.merge(fids);
		CPF mergedFragment = processMerger.getMergedFragment(mergedFragmentId);
		if (complexityChecker.isComplex(mergedFragment)) {
			logger.info("Cluster {} is discarded as the merged fragments is complex. Merged fragment size: {}", 
					cid, mergedFragment.getVertices().size());
//			return null;
		}
		int numExtractableNodes = computeExtractableNodes(cv, cg);
		int repFragmentSize = mergedFragment.getVertices().size() - numExtractableNodes;
		
		StandardizedCluster standardizedCluster = new StandardizedCluster(cid);
		Collection<String> removedFIds = new HashSet<String>();
		// merged fragments contain all fragments used for the merging. therefore, we don't want to remove any
		// fragment from the repository.
		// removedFIds.addAll(fids);
		standardizedCluster.setRemovedFragmentIds(removedFIds);
		
		standardizedCluster.setFragmentIds(fids);
		String representativeFid = mergedFragmentId;
		standardizedCluster.setRepresentativeFragmentId(representativeFid);
		standardizedCluster.setRepFragmentSize(repFragmentSize);
		standardizedCluster.getStandardFragmentIds().add(representativeFid);
		
		logger.trace("Standardized the cluster {} of size {} with the merged fragment {} the representative fragment.", 
				new Object[] {cid, fids.size(), representativeFid});
		return standardizedCluster;
	}

	private int computeExtractableNodes(Vertex cv, MultiDirectedGraph cg) {
		
		int numExtractableNodes = 0;
		Collection<Vertex> childCVs = cg.getDirectSuccessors(cv);
		for (Vertex childCV : childCVs) {
			Collection<DirectedEdge> edges = cg.getEdgesWithSourceAndTarget(cv, childCV);
			for (DirectedEdge e : edges) {
				String[] parts = e.getDescription().split("_");
				String parentFragmentId = parts[0];
				String childFragmentId = parts[1];
				String parentFragmentType = "P"; // fragmentTypes.get(parentFragmentId);
				String childFragmentType = "P"; // fragmentTypes.get(childFragmentId);
				if (parentFragmentType != null && childFragmentType != null) {
					if (parentFragmentType.equals("R") || childFragmentType.equals("R")) {
						// regid fragments may be changed in merged models
						continue;
					}
				}
				
				Integer childSize = fragmentSizes.get(childFragmentId);
				if (childSize != null) {
					numExtractableNodes += childSize;
					numExtractableNodes--; // we have to subtract the place holder node
				}
			}
		}
		return numExtractableNodes;
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
		standardizedCluster.setRepresentativeFragmentId(stdFragmentId);
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
		
		String representativeFid = null;
		try {
			representativeFid = findMedoid(fids);
		} catch (RepositoryException e) {
			logger.error("Failed to find the medoid of the cluster {}. Assigning a random standard fragment...", cid);
			representativeFid = fids.get(0);
		}
		standardizedCluster.setRepresentativeFragmentId(representativeFid);
		
		standardizedCluster.getStandardFragmentIds().addAll(fids);
//		standardizedCluster.getStandardFragmentIds().add(representativeFid);
		standardizedCluster.setRemovedFragmentIds(removedFIds);
		
		logger.trace("Standardized the cluster {} of size {} with all fragments and {} selected as the representative fragment.", 
				new Object[] {cid, fids.size(), representativeFid});
		return standardizedCluster;
	}

	private Vertex getNextCluster(MultiDirectedGraph cg, Set<Vertex> processedClusters) {
		
		Vertex selectedCV = null;
		
		// if there are any unprocessed sources, let's process it first
		Collection<Vertex> sources = ga.getSources(cg);
		for (Vertex source : sources) {
			if (!processedClusters.contains(source)) {
				selectedCV = source;
				break;
			}
		}
		
		if (selectedCV != null) {
			logger.trace("Root cluster ID: {} selected for standardization.", selectedCV.getId());
			return selectedCV;
		}
		
		// parent clusters of all processed clusters have been processed.
		// therefore, there should be at least one child of processed clusters, whose all parent clusters have been
		// processed. let's find it :)
		for (Vertex processedVertex : processedClusters) {
			Collection<Vertex> candidates = cg.getDirectSuccessors(processedVertex);
			for (Vertex candidate : candidates) {
				if (!processedClusters.contains(candidate)) {
					Collection<Vertex> candidateParents = cg.getDirectPredecessors(candidate);
					if (processedClusters.containsAll(candidateParents)) {
						selectedCV = candidate;
						break;
					}
				}
			}
		}
		
		if (selectedCV != null) {
			logger.trace("Non root cluster ID: {} selected for standardization.", selectedCV.getId());
		}
		return selectedCV;
	}
	
	private void refineChildClusters(Vertex cv, MultiDirectedGraph cg, Collection<String> removedFragmentIds) {
		
		if (removedFragmentIds == null || removedFragmentIds.isEmpty()) {
			return;
		}
		
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

	private void initializeClusterData() {

		fragmentSizes = overlapResolver.getFragmentSizes();
		distances = overlapResolver.getDistances();
		
		parentChildMap = fragmentVersionDagDao.getAllParentChildMappings();
		childParentMap = fragmentVersionDagDao.getAllChildParentMappings();
		fragmentClusterMap = clusteringDao.getAllFragmentClusterMappings();
		cs = overlapResolver.fetchAllClusters();
		
		if (logger.isTraceEnabled()) {
			StringBuffer b = new StringBuffer();
			for (String cid : cs.keySet()) {
				b.append("C" + cid + ":" + cs.get(cid).size() + ", ");
			}
			logger.trace("Fetched clusters: " + b.toString());
		}
		
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
		Map<String, List<String>> parentClusters = getParentClusters(cid);
		for (String pcid : parentClusters.keySet()) {
			immediateHierarchy.add(pcid);
			List<String> edgeDescriptions = parentClusters.get(pcid);
			for (String edgeDescription : edgeDescriptions) {
				if (!edgeExists(cg, edgeDescription)) {
					addEdge(cg, pcid, cid, edgeDescription);
				}
			}
		}

		Map<String, List<String>> childClusters = getChildClusters(cid);
		for (String ccid : childClusters.keySet()) {
			immediateHierarchy.add(ccid);
			List<String> edgeDescriptions = childClusters.get(ccid);
			for (String edgeDescription : edgeDescriptions) {
				if (!edgeExists(cg, edgeDescription)) {
					addEdge(cg, cid, ccid, edgeDescription);
				}
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

	private Map<String, List<String>> getChildClusters(String cid) {

		Map<String, List<String>> childLinks = new HashMap<String, List<String>>();
		List<String> fids = cs.get(cid);
		for (String fid : fids) {
//			List<String> descendantFIds = parentChildMap.get(fid);
			Collection<String> descendantFIds = getDescendants(fid);
			if (descendantFIds != null) {
				for (String cfid : descendantFIds) {
					List<String> ccids = fragmentClusterMap.get(cfid);
					if (ccids != null) {
						String ccid = ccids.get(0);
						List<String> edgeDescriptions = childLinks.get(ccid);
						if (edgeDescriptions == null) {
							edgeDescriptions = new ArrayList<String>();
							childLinks.put(ccid, edgeDescriptions);
						}
						edgeDescriptions.add(fid + "_" + cfid);
					}
				}
			}
		}
		return childLinks;
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
	 * @return parent cluster Id -> Ids to be used for each edge from this parent 
	 */
	private Map<String, List<String>> getParentClusters(String cid) {

		Map<String, List<String>> parentLinks = new HashMap<String, List<String>>();
		List<String> fids = cs.get(cid);
		for (String fid : fids) {
			Collection<String> ancestorFIds = getAnscestors(fid);
			if (ancestorFIds != null) {
				for (String pfid : ancestorFIds) {
					List<String> pcids = fragmentClusterMap.get(pfid);
					if (pcids != null) {
						String pcid = pcids.get(0);
						List<String> edgeDescriptions = parentLinks.get(pcid);
						if (edgeDescriptions == null) {
							edgeDescriptions = new ArrayList<String>();
							parentLinks.put(pcid, edgeDescriptions);
						}
						edgeDescriptions.add(pfid + "_" + fid);
					}
				}
			}
		}
		return parentLinks;
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

	public void setExactClones(Map<String, StandardizedCluster> exactClones) {
		
	}
}

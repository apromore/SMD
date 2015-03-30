package org.apromore.mining.dws.pcm;

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
import org.apromore.dao.ProcessDao;
import org.apromore.dao.model.ClusterInfo;
import org.apromore.graph.JBPT.CPF;
import org.apromore.mining.MiningConfig;
import org.apromore.mining.standardize.ProcessMerger;
import org.apromore.mining.standardize.StandardizedCluster;
import org.apromore.service.FragmentService;
import org.apromore.service.utils.OverlapResolver;
import org.apromore.toolbox.clustering.algorithms.dbscan.FragmentPair;
import org.apromore.toolbox.clustering.algorithms.dbscan.InMemoryGEDMatrix;
import org.jbpt.graph.DirectedEdge;
import org.jbpt.graph.MultiDirectedGraph;
import org.jbpt.graph.algo.DirectedGraphAlgorithms;
import org.jbpt.graph.algo.StronglyConnectedComponents;
import org.jbpt.hypergraph.abs.Vertex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

public class DWSClusterSelector {
	
	private final Logger logger = LoggerFactory.getLogger(DWSClusterSelector.class);

	@Autowired
	@Qualifier("FragmentVersionDagDao")
	private FragmentVersionDagDao fragmentVersionDagDao;
	
	@Autowired @Qualifier("ProcessDao")
	private ProcessDao pdoa;
	
	@Autowired @Qualifier("FragmentService")
	private FragmentService fsrv;
	
	@Autowired @Qualifier("FragmentVersionDao")
	private FragmentVersionDao fdao;
	
	@Autowired @Qualifier("ClusteringDao")
	private ClusteringDao clusteringDao;

	@Autowired
	private OverlapResolver overlapResolver;
	
	@Autowired
	private ProcessMerger processMerger;
	
	@Autowired
	private InMemoryGEDMatrix gedMatrix;
	
	@Autowired
	private DWSComplexityChecker complexityChecker;

	private Map<String, List<String>> parentChildMap = null;
	private Map<String, List<String>> childParentMap = null;
	private Map<String, List<String>> fragmentClusterMap = null;
	private DirectedGraphAlgorithms<DirectedEdge, Vertex> ga = new DirectedGraphAlgorithms<DirectedEdge, Vertex>();
	private static StronglyConnectedComponents<DirectedEdge, Vertex> sa = new StronglyConnectedComponents<DirectedEdge, Vertex>();
	private Map<String, List<String>> cs = null;
	private Map<String, Vertex> idVertexMap = new HashMap<String, Vertex>();
	private Map<String, ClusterInfo> clusterData = new HashMap<String, ClusterInfo>();
	
	private Map<String, Integer> fragmentSizes = null;
	private Map<FragmentPair, Double> distances = null;
	
	Map<String, StandardizedCluster> exactClones = null;
	
	public void setExactClones(Map<String, StandardizedCluster> exactClones) {
		this.exactClones = exactClones;
	}

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
			resolveClusters(cg, clusters);
		}
		
		// we don't have graphs of clusters any more. so we can standardize individual clusters.
//		for (String cid : cs.keySet()) {
//			
//			if (cid.startsWith("E")) {
//				StandardizedCluster stdCluster = standardizeExactClone(cid);
//				if (stdCluster != null) {
//					clusters.put(cid, stdCluster);
//				}
//			} else {
//				StandardizedCluster stdCluster = standardizeWithMerge(cid);
//				if (stdCluster != null) {
//					clusters.put(cid, stdCluster);
//				}
//			}
//		}
		
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

	private StandardizedCluster standardizeExactClone(String cid) {

		List<String> fids = cs.get(cid);
		StandardizedCluster standardizedCluster = new StandardizedCluster(cid);
		Collection<String> removedFIds = new HashSet<String>();
		standardizedCluster.setFragmentIds(fids);
		String representativeFid = fids.get(0);
		standardizedCluster.setRepresentativeFragmentId(representativeFid);
		standardizedCluster.getStandardFragmentIds().add(representativeFid);
		standardizedCluster.setRemovedFragmentIds(removedFIds);
		
		logger.debug("Standardized the exact clone {} of size {} and {} selected as the representative fragment.", 
				new Object[] {cid, fids.size(), representativeFid});
		return standardizedCluster;
	}

	private void resolveClusters(MultiDirectedGraph cg, Map<String, StandardizedCluster> clusters) {
		
		while (!cg.getVertices().isEmpty()) {
		
			// get next cluster to keep intact based on parameters (e.g. bcr, avg frag size)
			Vertex c = getNextCluster(cg);
			List<String> fragmentIds = cs.get(c.getId());
			
			String cid = c.getId();
			if (c.getId().startsWith("E")) {
				StandardizedCluster stdCluster = standardizeExactClone(cid);
				if (stdCluster != null) {
					clusters.put(cid, stdCluster);
				}
			} else {
				
				StandardizedCluster stdCluster = standardizeWithMerge(cid);
				if (stdCluster != null) {
					fragmentIds = stdCluster.getFragmentIds();
					clusters.put(cid, stdCluster);
				} else {
					cg.removeVertex(c);
					continue;
				}
			}
			
			// remove all child fragments from other clusters
			refineChildClusters(c, cg, fragmentIds);
			
			// remove all parent fragments from other clusters
			refineParentClusters(c, cg, fragmentIds);
			
			// remove the current cluster from the graph as it no longer has child or parent fragments in other clusters
			cg.removeVertex(c);
		}
	}

	private Vertex getNextCluster(MultiDirectedGraph cg) {
		
		Vertex nextCluster = null;
		
		// first check if there are any exact clones
		for (Vertex v : cg.getVertices()) {
			if (v.getId().startsWith("E")) {
				nextCluster = v;
				break;
			}
		}
		
		// then check if can find a cluster in preferred range
		if (nextCluster == null) {
			for (Vertex v : cg.getVertices()) {
				double avg = getAverageFragmentSize(v.getId());
				if (avg > MiningConfig.SELECTTION_1_LOWERBOUND && avg <= MiningConfig.SELECTTION_1_UPPERBOUND) {
					nextCluster = v;
					break;
				} else if (avg > MiningConfig.SELECTTION_2_LOWERBOUND && avg <= MiningConfig.SELECTTION_2_UPPERBOUND) {
					nextCluster = v;
					break;
				} else if (avg > MiningConfig.SELECTTION_3_LOWERBOUND && avg <= MiningConfig.SELECTTION_3_UPPERBOUND) {
					nextCluster = v;
					break;
				}
			}
		}
		
		// if none of above succeeds, find a random cluster
		if (nextCluster == null) {
			nextCluster = cg.getVertices().iterator().next();
		}
		
		return nextCluster;
	}

	private double getAverageFragmentSize(String id) {
		
		List<String> fids = cs.get(id);
		if (fids == null) {
			return 0;
		}
		
		double totalSize = 0;
		for (String fid : fids) {
			totalSize += fragmentSizes.get(fid);
		}
		double avg = totalSize / (double) fids.size();
		return avg;
	}

	private StandardizedCluster standardizeWithMerge(String cid) {

		List<String> fids = cs.get(cid);
		List<String> requiredFids = getFragmentsOfComplexProcesses(fids);
		if (requiredFids.isEmpty()) {
			return null;
		} else if (requiredFids.size() == 1) {
			String requiredFid = requiredFids.get(0);
			String supportFid = null;
			double shortestDistance = Double.MAX_VALUE;
			for (String candidateFid : fids) {
				if (!candidateFid.equals(requiredFid)) {
					if (supportFid == null) {
						supportFid = candidateFid;
					}
					Double distance = distances.get(new FragmentPair(requiredFid, candidateFid));
					if (distance != null) {
						if (distance < shortestDistance) {
							shortestDistance = distance;
							supportFid = candidateFid;
						}
					}
				}
			}
			requiredFids.add(supportFid);
		}
		
		fids = requiredFids;
		String mergedFragmentId = processMerger.merge(fids);
		CPF mergedFragment = processMerger.getMergedFragment(mergedFragmentId);
		if (mergedFragment == null) {
			DWSEvaluatorUtil.incrementFailedMerges();
			logger.info("Failed to merge fragments of the cluster ID: {}.", cid);
			return null;
		}
		
		if (complexityChecker.isComplex(mergedFragment)) {
			DWSEvaluatorUtil.incrementDiscardedMerges();
			logger.info("Cluster {} is discarded as the merged fragments is complex. Merged fragment size: {}", 
					cid, mergedFragment.getVertices().size());
			return null;
		}

		StandardizedCluster standardizedCluster = new StandardizedCluster(cid);
		Collection<String> removedFIds = new HashSet<String>();
		standardizedCluster.setFragmentIds(fids);
		String representativeFid = mergedFragmentId;
		standardizedCluster.setRepresentativeFragmentId(representativeFid);
		standardizedCluster.getStandardFragmentIds().add(mergedFragmentId);
		standardizedCluster.setRemovedFragmentIds(removedFIds);
		
		logger.trace("Standardized the cluster {} of size {} with all fragments and {} selected as the representative fragment.", 
				new Object[] {cid, fids.size(), representativeFid});
		return standardizedCluster;
	}
	
	private List<String> getFragmentsOfComplexProcesses(List<String> fids) {

		List<String> fragmentsOfComplexProcesses = new ArrayList<String>();
		for (String fid : fids) {
			Collection<String> pnames = fdao.getProcessNamesOfFragmentId(fid);
			if (pnames != null && pnames.size() > 0) {
				String processName = pnames.iterator().next();
				String rootfid = pdoa.getRootFragmentId(processName);
				Integer rootSize = fragmentSizes.get(rootfid);
				if (rootSize != null) {
					if (rootSize > MiningConfig.COMPLEXITY_MATRIC_N) {
						fragmentsOfComplexProcesses.add(fid);
					}
				} else {
					logger.error("Process {} does not have a root fragment. Cannot consider it for reprocessing.", 
							processName);
				}
			}
		}
		return fragmentsOfComplexProcesses;
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
					if (childFIds == null) {
						verticesToBeRemoved.add(childCV);
						logger.error("Child cluster {} of cluster {} has already been removed. Possibly as a result of participation in another cluster graph.", childCV.getId(), cv.getId());
						break;
					} else {
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
				
				edgesToBeRemoved.add(e);
				List<String> parentFIds = cs.get(parentCV.getId());
				if (parentFIds == null) {
					verticesToBeRemoved.add(parentCV);
					logger.error("Parent cluster {} has of cluster {} already been removed. Possibly as a result of participation in another cluster graph.", parentCV.getId(), cv.getId());
					break;
				} else {
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
				}
				logger.trace("Parent cluster " + parentCV.getId() + " was refined by removing " + parentFragmentId + " | New size: " + parentFIds.size());
			}
		}
		
		cg.removeVertices(verticesToBeRemoved);
		cg.removeEdges(edgesToBeRemoved);
		
		logger.trace("Refined parent log clusters of {} by removing {} vertices and {} edges.", 
				new Object[] {cv.getId(), verticesToBeRemoved.size(), edgesToBeRemoved.size()});
	}
	
	private Set<String> getAllFragments(Map<String, StandardizedCluster> clusters) {
		Set<String> fragmentIds = new HashSet<String>();
		for (String cid : clusters.keySet()) {
			fragmentIds.addAll(clusters.get(cid).getFragmentIds());
		}
		return fragmentIds;
	}

	private void initializeClusterData() {

		fragmentSizes = overlapResolver.getFragmentSizes();
		distances = overlapResolver.getDistances();
		
		parentChildMap = fragmentVersionDagDao.getAllParentChildMappings();
		childParentMap = fragmentVersionDagDao.getAllChildParentMappings();
		fragmentClusterMap = clusteringDao.getAllFragmentClusterMappings();
		cs = overlapResolver.fetchAllClusters();
		
		if (exactClones != null) {
			
			// remove all exact clones from approximate clones.
			// we need this as there is a bug in preventing exact clone inclusion for approx clones in dbscan
			// (this is not implemented in hac)
			Set<String> exactCloneFragments = getAllFragments(exactClones);
			Set<String> approxClonesToRemove = new HashSet<String>();
			for (String cid : cs.keySet()) {
				List<String> cfids = cs.get(cid);
				cfids.removeAll(exactCloneFragments);
				if (cfids.size() < 2) {
					approxClonesToRemove.add(cid);
				}
			}
			for (String acid : approxClonesToRemove) {
				cs.remove(acid);
			}
			
			for (String ecloneFid : exactCloneFragments) {
				fragmentClusterMap.remove(ecloneFid);
			}
			// end of exact clone removal
			
			for (String eid : exactClones.keySet()) {
				StandardizedCluster e = exactClones.get(eid);
				cs.put(eid, e.getFragmentIds());
				
				for (String efid : e.getFragmentIds()) {
					List<String> ecids = new ArrayList<String>();
					ecids.add(eid);
					fragmentClusterMap.put(efid, ecids);
				}
			}
		}
		
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
}

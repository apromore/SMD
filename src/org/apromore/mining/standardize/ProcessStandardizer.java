package org.apromore.mining.standardize;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apromore.dao.ProcessDao;
import org.apromore.dao.model.Process;
import org.apromore.exception.LockFailedException;
import org.apromore.exception.RepositoryException;
import org.apromore.graph.JBPT.CPF;
import org.apromore.mining.EvaluatorUtil;
import org.apromore.mining.MiningConfig;
import org.apromore.mining.ProcessModelImporter;
import org.apromore.mining.standardize.InteractiveClusterSelector;
import org.apromore.mining.utils.MiningUtils;
import org.apromore.service.ClusteringService;
import org.apromore.service.FragmentService;
import org.apromore.service.ProcessService;
import org.apromore.service.model.ClusterSettings;
import org.apromore.service.utils.OverlapResolver;
import org.apromore.toolbox.clustering.algorithms.dbscan.InMemoryGEDMatrix;
import org.apromore.util.DebugUtil;
import org.prom5.analysis.traceclustering.algorithm.AgglomerativeHierarchicalAlgorithm.MyCluster;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

public class ProcessStandardizer {
	
	private static final Logger logger = LoggerFactory.getLogger(ProcessStandardizer.class);
	
	private Map<MyCluster, CPF> standardizedProcesses = new HashMap<MyCluster, CPF>();
	private Map<String, CPF> standardizedSubprocesses = new HashMap<String, CPF>();
	private Map<String, Integer> standardizedSubprocessSize = new HashMap<String, Integer>();
	private Map<String, CPF> representativeFragments = new HashMap<String, CPF>();
	
	private Map<String, StandardizedCluster> standardizedClusters;
	
	@Autowired
	private ClusteringService csrv;
	
	@Autowired @Qualifier("ProcessService")
	private ProcessService psrv;
	
	@Autowired @Qualifier("FragmentService")
	private FragmentService fsrv;
	
	@Autowired
	private ProcessMerger processMerger;
	
//	@Autowired
//	private InteractiveClusterSelector clusterSelector;
	
	@Autowired
	private ParameterBasedClusterSelector clusterSelector;
	
//	@Autowired
//	private TopDownClusterSelector clusterSelector;
	
//	@Autowired
//	private RuleBasedClusterStandardizer clusterSelector;
	
	@Autowired
	private ProcessModelImporter processModelImporter;
	
	@Autowired
	private OverlapResolver overlapResolver;
	
	@Autowired
	private ExactCloneDetector exactCloneDetector;
	
	@Autowired @Qualifier("ProcessDao")
	private ProcessDao pdoa;
	
	/**
	 * Cluster the repository
	 * Build the cluster DAG
	 * Select clusters to standardize based on some criteria
	 * 
	 * Retrieve all process models.E
	 * If a process model will be affected by standardization, retrieve it with subprocesses.
	 * 
	 * Handling complex subprocesses:
	 * - simple option is to always select non-complex subprocesses
	 * 
	 * Retrieve all subprocesses as well. If a subprocess will be affected by standardization, 
	 * retrieve it with subprocesses. Since the standardizer decides how to standardize each cluster 
	 * (based on a given set of standardization rules), it can compute final clusters and standardize subprocesses.
	 * 
	 * NOTE: If some process models are complex, it is possible to recluster one or more of those process models 
	 * and run the standardizatin again. 
	 * If a subprocess is complex, all processes sharing that subprocess have to be reclustered.
	 * 
	 * Third option would be support manual standardization of clusters and support interactive complexity reduction.
	 * Standardizer selects a set of initial clusters (C) based on some criteria. Then users can standardize them. Once
	 * standardized, standardizer tries to find subprocesses in either C or in already standardized clusters. 
	 * 
	 * @throws RepositoryException 
	 */
	public void standardize() throws RepositoryException {
		
		EvaluatorUtil.refacStart();
		
		standardizedProcesses.clear();
		standardizedSubprocesses.clear();
		
		Map<String, StandardizedCluster> exactClones = new HashMap<String, StandardizedCluster>();
		if (MiningConfig.IDENTIFY_EXACT_CLONES_SEPARATELY) {
			exactClones = exactCloneDetector.detectClones();
		}
		Set<String> exactCloneFragments = getAllFragments(exactClones);
		if (logger.isDebugEnabled()) {
			logger.debug("Exact clone fragments: {}", DebugUtil.getAsString(exactCloneFragments));
		}
		
		logger.debug("Clustering the repository for standardization...");
		ClusterSettings settings = new ClusterSettings();
		settings.getFidsToAvoid().addAll(exactCloneFragments); // mark exact clone fragments to be avoided
		if (MiningConfig.CLUSTERING_ALGORITHM.equals("HAC")) {
			logger.info("Using the HAC algorithm for fragment clustering.");
			settings.setAlgorithm("HAC");
		} else {
			logger.info("Using the DBSCAN algorithm for fragment clustering.");
			settings.setAlgorithm("DBSCAN");
		}
		settings.setMaxNeighborGraphEditDistance(MiningConfig.GED_THRESHOLD);
		csrv.cluster(settings);
		overlapResolver.resolveOverlaps();
		
		// we compute exact clones and approximate clones separately, in order to avoid adding exact clone fragments
		// to approximate clones.
		// give these separately computed exact clones to the cluster selector. these will be subjected to cluster
		// graph resolution with the cluster selector.
		clusterSelector.setExactClones(exactClones);
		
		// standardize all clusters (according to standardization rules) here and 
		// present the final set of clusters to the rest of the code
		standardizedClusters = clusterSelector.getStandardizedClusters();
		
		Set<String> allSubprocesses = getAllFragments(standardizedClusters);
		logger.debug("{} fragments can be standardized with {} subprocesses.", allSubprocesses.size(), standardizedClusters.size());
		
		List<Process> processes = pdoa.getProcessesJDBC();
		for (Process p : processes) {
			logger.trace("Standardizing the process {}: {}", p.getProcessId(), p.getName());
			String rootfid = pdoa.getRootFragmentId(p.getName());
			if (rootfid != null) {
				CPF cpf = fsrv.getFragmentWithSubprocesses(rootfid, allSubprocesses);
//				int logClusterId = getLogClusterId(p.getName());
				MyCluster logCluster = processModelImporter.getLogClusterOfProcess(p.getName());
				standardizedProcesses.put(logCluster, cpf);
			} else {
				logger.error("Process {}: {} does not have a root fragment. Skipping the process...", 
						p.getProcessId(), p.getName());
			}
		}
		
		// code for merge with extraction
//		for (String scid : standardizedClusters.keySet()) {
//			StandardizedCluster stdCluster = standardizedClusters.get(scid);
//			String subprocessId = stdCluster.getRepresentativeFragmentId();
//			CPF cpf = processMerger.getMergedFragment(subprocessId);
//			if (cpf == null) {
//				cpf = fsrv.getFragmentWithSubprocesses(subprocessId, allSubprocesses);
//			}
//			standardizedSubprocesses.put(subprocessId, cpf);
//			standardizedSubprocessSize.put(subprocessId, stdCluster.getRepFragmentSize());
//		}
		// end of code for merge with extraction
		
		
		Set<String> stdSubprocesses = getStandardFragments(standardizedClusters);
		for (String subprocessId : stdSubprocesses) {
			
			// new code
			CPF cpf = processMerger.getMergedFragment(subprocessId);
			if (cpf == null) {
				cpf = fsrv.getFragmentWithSubprocesses(subprocessId, allSubprocesses);
			}
			// end of new code
			
			// old code
//			CPF cpf = null;
//			if (clusterSelector.getClass().equals(ParameterBasedClusterSelector.class)) {
//				cpf = processMerger.getMergedFragment(subprocessId);
//				// if the subprocess is an exact clone, we have to get that from the repository
//				if (cpf == null) {
//					cpf = fsrv.getFragmentWithSubprocesses(subprocessId, allSubprocesses);
//				}
//			} else {
//				cpf = fsrv.getFragmentWithSubprocesses(subprocessId, allSubprocesses);
//			}
			// end of old code
			standardizedSubprocesses.put(subprocessId, cpf);
		}
		// retrieve all fragments with subprocesses and fill in the map
		
		EvaluatorUtil.refacEnd();
	}
	
	/**
	 * If a fragment belongs to a cluster and it is standardized, that fragment has a standard replacement. This method
	 * is used to get such standardized replacement for a fragment. If there is no such replacement, this returns null.
	 * 
	 * @param fragmentId ID of the fragment, to which the standardized replacement is required.
	 * @return Standard fragment to be used in place of the given fragment.
	 */
	public String getStandardizedFragment(String fragmentId) {
		
		List<String> cids = clusterSelector.getFragmentClusterMap().get(fragmentId);
		if (cids == null) {
			return null;
		}
		
		StandardizedCluster stdCluster = standardizedClusters.get(cids.get(0));
		if (stdCluster == null) {
			return null;
		}
		
		if (logger.isTraceEnabled()) {
			logger.trace("Standardizing fragment {} with {} in cluster {}", new Object[] {fragmentId, stdCluster.getStandardFragmentIds().get(0), cids.get(0)});
		}
		// TODO: old code
//		return stdCluster.getStandardFragmentIds().get(0);
		// end of old code
		
		return stdCluster.getRepresentativeFragmentId();
	}
	
	public Map<String, StandardizedCluster> getStandardizedClusters() {
		return standardizedClusters;
	}

	private int getLogClusterId(String name) {
		String idString = name.substring("p_".length());
		int id = Integer.parseInt(idString);
		return id;
	}

	private Set<String> getAllFragments(Map<String, StandardizedCluster> clusters) {
		Set<String> fragmentIds = new HashSet<String>();
		for (String cid : clusters.keySet()) {
			fragmentIds.addAll(clusters.get(cid).getFragmentIds());
		}
		return fragmentIds;
	}
	
	private Set<String> getStandardFragments(Map<String, StandardizedCluster> clusters) {
		Set<String> fragmentIds = new HashSet<String>();
		for (String cid : clusters.keySet()) {
			fragmentIds.addAll(clusters.get(cid).getStandardFragmentIds());
		}
		return fragmentIds;
	}

	/**
	 * Returns the process models after standardization.
	 * 
	 * @return Map from log_cluster_id -> standardized process model
	 */
	public Map<MyCluster, CPF> getStandardizedProcesses() {
		return standardizedProcesses;
	}
	
	/**
	 * Returns subprocesses after standardization.
	 * 
	 * @return Map from root_fragment_id -> standardized subprocess
	 */
	public Map<String, CPF> getStandardizedSubprocesses() {
		return standardizedSubprocesses;
	}
	
	/**
	 * Removes all clusters that are not in the standardized set of clusters.
	 * Removes all fragments from remaining clusters, if they have been removed from the standardized clusters. 
	 */
	public void apply() {
		if (logger.isDebugEnabled()) {
			int numReducedProcesses = 0;
			for (MyCluster logCluster : standardizedProcesses.keySet()) {
				try {
					CPF cpf = standardizedProcesses.get(logCluster);
					String processName = processModelImporter.getProcessNameOfLogCluster(logCluster);
					String root = pdoa.getRootFragmentId(processName);
					CPF plaincpf = fsrv.getFragment(root, false);
					if (plaincpf.getVertices().size() > cpf.getVertices().size()) {
						numReducedProcesses++;
						logger.debug("Process {}: Plain = {}, Standardized = {}", 
								new Object[] {processName, plaincpf.getVertices().size(), cpf.getVertices().size()});
					}
					
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			
			int numReducedSubprocesses = 0;
			if (clusterSelector.getClass().equals(ParameterBasedClusterSelector.class)) {
				for (String subprocessId : standardizedSubprocesses.keySet()) {
					try {
						CPF cpf = standardizedSubprocesses.get(subprocessId);
						logger.debug("Subrocess {}: Plain = {}", new Object[] {subprocessId, cpf.getVertices().size()});
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				
			} else if (clusterSelector.getClass().equals(BottomUpClusterSelector.class)) {
				for (String subprocessId : standardizedSubprocesses.keySet()) {
					try {
						CPF cpf = standardizedSubprocesses.get(subprocessId);
						logger.debug("Subrocess {}: Plain = {}", new Object[] {subprocessId, cpf.getVertices().size()});
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				
			} else { 
			
				for (String subprocessId : standardizedSubprocesses.keySet()) {
					try {
						CPF cpf = standardizedSubprocesses.get(subprocessId);
						CPF plaincpf = fsrv.getFragment(subprocessId, false);
						if (plaincpf.getVertices().size() > cpf.getVertices().size()) {
							numReducedSubprocesses++;
							logger.debug("Subrocess {}: Plain = {}, Standardized = {}", 
								new Object[] {subprocessId, plaincpf.getVertices().size(), cpf.getVertices().size()});
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
			
			logger.debug("{} processes were simplified out of {}", numReducedProcesses, standardizedProcesses.size());
			logger.debug("{} subprocesses were simplified out of {}", numReducedSubprocesses, standardizedSubprocesses.size());
			
			serializeStandardizedClusters();
		}
	}
	
	private void serializeStandardizedClusters() {
		
		if (standardizedClusters == null) {
			return;
		}
		
		String outFilePath = "/home/cn/eps/bpm-demo-new1/bpm-demo/Apromore-Core/apromore-service/data/pm1/t1/pcm/stdclusters.csv";
		logger.debug("Serializing {} standardized clusters to file {} ...", standardizedClusters.size(), outFilePath);
		
		StringBuffer b = new StringBuffer();
		b.append("Cluster_ID;Fragment_IDs;Representative FID;Standard_Fragment_IDs;Removed_Fragment_IDs\n");
		for (String cid : standardizedClusters.keySet()) {
			StandardizedCluster s = standardizedClusters.get(cid);
			if (s == null) {
				continue;
			}
			
			String fids = "";
			if (s.getFragmentIds() != null) {
				for (String fid : s.getFragmentIds()) {
					fids += fid + ",";
				}
			}
			
			String stdFids = "";
			if (s.getStandardFragmentIds() != null) {
				for (String stdFid : s.getStandardFragmentIds()) {
					stdFids += stdFid + ",";
				}
			}
			
			String removedFids = "";
			if (s.getRemovedFragmentIds() != null) {
				for (String removedFid : s.getRemovedFragmentIds()) {
					removedFids += removedFid + ",";
				}
			}
			
			b.append(s.getClusterId() + ";" + fids + ";" + s.getRepresentativeFragmentId() + ";" + stdFids + ";" + removedFids + "\n");
		}
		
		File outFile = new File(outFilePath);
		try {
			FileUtils.write(outFile, b.toString());
		} catch (IOException e) {
			logger.error("Failed to write {} standardized clusters to file {}. Program will terminate normally.", 
					standardizedClusters.size(), outFilePath);
		}
	}

	public Map<String, Integer> getStandardizedSubprocessSizes() {
		return standardizedSubprocessSize;
	}
}

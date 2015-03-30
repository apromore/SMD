package org.apromore.mining.test;

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
import org.apromore.exception.RepositoryException;
import org.apromore.graph.JBPT.CPF;
import org.apromore.mining.EvaluatorUtil;
import org.apromore.mining.MiningConfig;
import org.apromore.mining.standardize.StandardizedCluster;
import org.apromore.mining.standardize.TopDownClusterSelector;
import org.apromore.service.ClusteringService;
import org.apromore.service.FragmentService;
import org.apromore.service.ProcessService;
import org.apromore.service.model.ClusterSettings;
import org.apromore.service.utils.FormattableEPCSerializer;
import org.apromore.service.utils.OverlapResolver;
import org.apromore.toolbox.clustering.algorithms.dbscan.InMemoryGEDMatrix;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

public class ProcessStandardizerTest {
	
	private static final Logger logger = LoggerFactory.getLogger(ProcessStandardizerTest.class);
	
	private Map<String, CPF> standardizedProcesses = new HashMap<String, CPF>();
	private Map<String, CPF> standardizedSubprocesses = new HashMap<String, CPF>();
	
	private FormattableEPCSerializer formattableEPCSerializer = new FormattableEPCSerializer();
	
	private Map<String, StandardizedCluster> standardizedClusters;
	
	@Autowired
	private ClusteringService csrv;
	
	@Autowired @Qualifier("ProcessService")
	private ProcessService psrv;
	
	@Autowired @Qualifier("FragmentService")
	private FragmentService fsrv;
	
	@Autowired
	private InMemoryGEDMatrix gedMatrix;
	
//	@Autowired
//	private InteractiveClusterSelector clusterSelector;
	
//	@Autowired
//	private FragSizeBasedClusterSelector clusterSelector;
	
	@Autowired
	private TopDownClusterSelector clusterSelector;
	
//	@Autowired
//	private RuleBasedClusterStandardizer clusterSelector;
	
	@Autowired
	private OverlapResolver overlapResolver;
	
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
		
		ClusterSettings settings = new ClusterSettings();
		settings.setDbscanClustering(false);
		settings.setMaxNeighborGraphEditDistance(MiningConfig.GED_THRESHOLD);
		gedMatrix.initialize(settings, null, null, null);
		
		standardizedProcesses.clear();
		standardizedSubprocesses.clear();
		
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
				standardizedProcesses.put(p.getName(), cpf);
			} else {
				logger.error("Process {}: {} does not have a root fragment. Skipping the process...", 
						p.getProcessId(), p.getName());
			}
		}
		
		Set<String> stdSubprocesses = getStandardFragments(standardizedClusters);
		for (String subprocessId : stdSubprocesses) {
			CPF cpf = fsrv.getFragmentWithSubprocesses(subprocessId, allSubprocesses);
			standardizedSubprocesses.put(subprocessId, cpf);
		}
		// retrieve all fragments with subprocesses and fill in the map
		
		apply();
		String outFolder1 = "/home/cn/eps/bpm-demo-new1/bpm-demo/Apromore-Core/apromore-service/data/pm1/t1/gs1";
		String outFolder2 = "/home/cn/eps/bpm-demo-new1/bpm-demo/Apromore-Core/apromore-service/data/pm1/t1/gs2";
		serialize(outFolder1, this.getStandardizedProcesses());
		serializeSubprocesses(outFolder2, this.getStandardizedSubprocesses());
		
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
		if (logger.isTraceEnabled()) {
			logger.trace("Standardizing fragment {} with {} in cluster {}", new Object[] {fragmentId, stdCluster.getStandardFragmentIds().get(0), cids.get(0)});
		}
		return stdCluster.getStandardFragmentIds().get(0);
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
	public Map<String, CPF> getStandardizedProcesses() {
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
			for (String pname : standardizedProcesses.keySet()) {
				try {
					CPF cpf = standardizedProcesses.get(pname);
					String root = pdoa.getRootFragmentId(pname);
					CPF plaincpf = fsrv.getFragment(root, false);
					if (plaincpf.getVertices().size() > cpf.getVertices().size()) {
						numReducedProcesses++;
						logger.debug("Process {}: Plain = {}, Standardized = {}", 
								new Object[] {pname, plaincpf.getVertices().size(), cpf.getVertices().size()});
					}
					
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			
			int numReducedSubprocesses = 0;
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
			
			logger.debug("{} processes were simplified out of {}", numReducedProcesses, standardizedProcesses.size());
			logger.debug("{} subprocesses were simplified out of {}", numReducedSubprocesses, standardizedSubprocesses.size());
			
			serializeStandardizedClusters();
		}
	}
	
	private void serializeStandardizedClusters() {
		
		if (standardizedClusters == null) {
			return;
		}
		
		String outFilePath = "/home/cn/eps/bpm-demo-new1/bpm-demo/Apromore-Core/apromore-service/data/pm1/t1/results/stdclusters.csv";
		logger.debug("Serializing {} standardized clusters to file {} ...", standardizedClusters.size(), outFilePath);
		
		StringBuffer b = new StringBuffer();
		b.append("Cluster_ID;Fragment_IDs;Standard_Fragment_IDs;Removed_Fragment_IDs\n");
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
			
			b.append(s.getClusterId() + ";" + fids + ";" + stdFids + ";" + removedFids + "\n");
		}
		
		File outFile = new File(outFilePath);
		try {
			FileUtils.write(outFile, b.toString());
		} catch (IOException e) {
			logger.error("Failed to write {} standardized clusters to file {}. Program will terminate normally.", 
					standardizedClusters.size(), outFilePath);
		}
	}
	
	private void serialize(String outFolder, Map<String, CPF> gs) {
		
		try {
			FileUtils.cleanDirectory(new File(outFolder));
		} catch (IOException e) {
			System.out.println("Failed to clear folder " + outFolder);
		}
		
		int modelNumber = 0;
		for (String processName : gs.keySet()) {
			modelNumber++;
			File modelFile = new File(outFolder, processName + ".epml");
			formattableEPCSerializer.serialize(gs.get(processName), modelFile.getAbsolutePath());
		}
	}
	
	private void serializeSubprocesses(String outFolder, Map<String, CPF> gs) {
		
		try {
			FileUtils.cleanDirectory(new File(outFolder));
		} catch (IOException e) {
			System.out.println("Failed to clear folder " + outFolder);
		}
		
		int modelNumber = 0;
		for (String id : gs.keySet()) {
			modelNumber++;
			File modelFile = new File(outFolder, id + ".epml");
			formattableEPCSerializer.serialize(gs.get(id), modelFile.getAbsolutePath());
		}
	}
}

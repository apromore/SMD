package org.apromore.mining.dws.pcm;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apromore.dao.ProcessDao;
import org.apromore.graph.JBPT.CPF;
import org.apromore.mining.MiningConfig;
import org.apromore.mining.ProcessMiner;
import org.apromore.mining.complexity.ComplexityEvaluatorTool;
import org.apromore.mining.dws.pcm.DWSClusterer.DWSNode;
import org.apromore.mining.standardize.ProcessMerger;
import org.apromore.mining.standardize.StandardizedCluster;
import org.apromore.service.ClusteringService;
import org.apromore.service.RepositoryService;
import org.apromore.service.utils.EPCDeserializer;
import org.apromore.service.utils.FormattableEPCSerializer;
import org.apromore.util.DebugUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

public class DWSBasedProcessCollectionMiner {
	
	private static final Logger logger = LoggerFactory.getLogger(DWSBasedProcessCollectionMiner.class);
	
	@Autowired
	private DWSClusterer clusterer;
	
	@Autowired
	private DWSProcessImporter processImporter;
	
	@Autowired
	private ProcessMerger processMerger;
	
	@Autowired
	private DWSProcessMiner miner;
	
	@Autowired
	private ClusteringService csrv;
	
	@Autowired
	private DWSComplexityChecker complexityChecker;
	
	@Autowired
	private DWSProcessStandardizer standardizer;
	
	@Autowired @Qualifier("RepositoryService")
	RepositoryService rsrv;
	
	@Autowired @Qualifier("ProcessDao")
	private ProcessDao pdao;
	
	private FormattableEPCSerializer formattableEPCSerializer = new FormattableEPCSerializer();
	
	private EPCDeserializer epcDeserializer = new EPCDeserializer();
	
	private String processesPath = "processes";
	private String processesDataPath = "m_c_processes_data.csv";
	private String subprocessesPath = "subprocesses";
	private String subprocessesDataPath = "m_c_subprocesses_data.csv";
	private String combinedPath = "combined";
	private String combinedDataPath = "m_c_combined_data.csv";
	private final String invalidModelsPath = "invalid";
	
	public void mineCollection(String inPath, String outPath) throws Exception {
		
		long t1 = System.currentTimeMillis();
		
		initializePaths(outPath);
		
		if (MiningConfig.WRITE_ADDITIONAL_DATA) {
			DWSEvaluatorUtil.init(outPath);
			DebugUtil.initOutPath(outPath);
		}
		processMerger.initialize();
		
		String logPath = inPath;
		logger.info("Mining a process collection from the log file: {}", logPath);
		
		clusterer.initialize(logPath);
		List<DWSNode> initialModels = getInitialNodes(MiningConfig.INITIAL_PROCESS_SIZE);
		
		List<DWSNode> addedModels = processImporter.importModels(initialModels);
		csrv.computeGEDMatrix();
		
		Collection<DWSNode> complexModels = complexityChecker.getComplexModelsToReprocess(addedModels);
		if (complexModels.isEmpty()) {
			standardizer.standardize();
		}
		
		Set<DWSNode> undividableModels = new HashSet<DWSNode>();
		
		while (!complexModels.isEmpty()) {
			
			standardizer.standardize();
			complexModels = complexityChecker.getComplexProcessesToReprocessAll(standardizer, undividableModels);
			
			if (logger.isDebugEnabled()) {
				StringBuffer b = new StringBuffer();
				for (DWSNode n : complexModels) {
					b.append(n.getID() + ",");
				}
				logger.debug("Complex models selected for simplification: {}", b.toString());
			}
			
			List<DWSNode> allNewProcesses = new ArrayList<DWSNode>();
			for (DWSNode node : complexModels) {
				
				List<DWSNode> nextLevelModels = getDirectNextLevelModels(node, undividableModels);
				
				if (!undividableModels.contains(node)) {
					String processNameToDelete = processImporter.getProcessNameOfNode(node);
					logger.debug("Deleting the process {} to replace with its next level models.", processNameToDelete);
					rsrv.deleteProcess(processNameToDelete);
					DWSEvaluatorUtil.removeCluster(processNameToDelete);
					List<DWSNode> addedNewProcesses = processImporter.importModels(nextLevelModels);
					allNewProcesses.addAll(addedNewProcesses);
				}
			}
			
			appendGEDMatrix(allNewProcesses);
		} 
		
		standardizer.apply();
		
		long duration = System.currentTimeMillis() - t1;
		serialize(processesPath, standardizer.getStandardizedProcesses());
		serializeSubprocesses(subprocessesPath, standardizer);

		if (MiningConfig.WRITE_ADDITIONAL_DATA) {
			DWSEvaluatorUtil.writeData(clusterer, duration);
		}
		
		if (MiningConfig.WRITE_EVAL_DATA) {
			writeEvalData(duration);
		}
	}
	
	private void initializePaths(String outPath) {

		File outFolder = new File(outPath);
		try {
			FileUtils.cleanDirectory(outFolder);
		} catch (IOException e) {
			e.printStackTrace();
		}

		File processesFolder = new File(outFolder, "processes");
		processesFolder.mkdir();
		processesPath = processesFolder.getAbsolutePath();
		
		File subprocessesFolder = new File(outFolder, "subprocesses");
		subprocessesFolder.mkdir();
		subprocessesPath = subprocessesFolder.getAbsolutePath();
	}
	
	private void writeEvalData(long duration) {
		ComplexityEvaluatorTool evaluatorTool = new ComplexityEvaluatorTool();
		evaluatorTool.writeComplexities(processesPath, processesDataPath, duration);
		evaluatorTool.writeComplexities(subprocessesPath, subprocessesDataPath, duration);
		
		try {
			File processesFolder = new File(processesPath);
			File subprocessesFolder = new File(subprocessesPath);
			File combinedFolder = new File(combinedPath);
			FileUtils.cleanDirectory(combinedFolder);
			FileUtils.copyDirectory(processesFolder, combinedFolder);
			FileUtils.copyDirectory(subprocessesFolder, combinedFolder);
			evaluatorTool.writeComplexities(combinedPath, combinedDataPath, duration, processesFolder.list().length, subprocessesFolder.list().length);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void appendGEDMatrix(Collection<DWSNode> allNewProcesses) {
		
		if (logger.isDebugEnabled()) {
			StringBuffer b = new StringBuffer();
			for (DWSNode n : allNewProcesses) {
				b.append(n.getID() + ",");
			}
			logger.debug("Appending the distance matrix for {} new processes: {}", allNewProcesses.size(), b.toString());
		}
		Collection<String> newRoots = new HashSet<String>();
		for (DWSNode c : allNewProcesses) {
			String processName = processImporter.getProcessNameOfNode(c); 
			String rootId = pdao.getRootFragmentId(processName);
			newRoots.add(rootId);
		}
		
		csrv.appendGEDMatrix(newRoots);
	}
	
	private List<DWSNode> getDirectNextLevelModels(DWSNode node, Set<DWSNode> undividableModels) throws Exception {
		List<DWSNode> nextLevelModels = new ArrayList<DWSNode>();
		
		Queue<DWSNode> complexLogClusters = new LinkedList<DWSNode>();
		complexLogClusters.add(node);
		while (!complexLogClusters.isEmpty()) {
			DWSNode c = complexLogClusters.poll();
			Collection<DWSNode> children = clusterer.getChildren(c);
			if (children.isEmpty()) {
				// complex model "c" does not have child logs. so cannot be divided into simple models.
				// we have report this as an undividable model, so that it will be outputted as it is.
				undividableModels.add(c);
			} else {
				for (DWSNode childLogCluster : children) {
					String epml = miner.mineEPC(childLogCluster);
					CPF childModel = epcDeserializer.deserializeInputStream(IOUtils.toInputStream(epml));
					childLogCluster.cpf = childModel;
					nextLevelModels.add(childLogCluster);
				}
			}
		}
		
		if (logger.isDebugEnabled()) {
			StringBuffer b = new StringBuffer();
			for (DWSNode n : nextLevelModels) {
				b.append(n.getID() + ",");
			}
			logger.debug("Next level models generated for the log cluster Id {} -> {}", 
					node.getGroupId() + "_" + node.getID(), b.toString());
		}
		return nextLevelModels;
	}

	private List<DWSNode> getInitialNodes(int maxSize) throws Exception {
		List<DWSNode> initialNodes = new ArrayList<DWSNode>();
		DWSNode root = clusterer.getRoot();
		Queue<DWSNode> unprocessedNodes = new LinkedList<DWSNode>();
		unprocessedNodes.add(root);
		
		while (!unprocessedNodes.isEmpty()) {
			DWSNode node = unprocessedNodes.poll();
			String epml = miner.mineEPC(node);
			CPF cpf = epcDeserializer.deserializeInputStream(IOUtils.toInputStream(epml));
			if (cpf.getVertices().size() > maxSize) {
				Collection<DWSNode> childNodes = clusterer.getChildren(node);
				if (!childNodes.isEmpty()) {
					unprocessedNodes.addAll(childNodes);
				} else {
					node.cpf = cpf;
					initialNodes.add(node);
				}
			} else {
				node.cpf = cpf;
				initialNodes.add(node);
			}
		}
		logger.debug("{} intial models created below the size {}.", initialNodes.size(), maxSize);
		return initialNodes;
	}
	
	private void serialize(String outFolder, Map<DWSNode, CPF> gs) {
		
		try {
			FileUtils.cleanDirectory(new File(outFolder));
		} catch (IOException e) {
			System.out.println("Failed to clear folder " + outFolder);
		}
		
		int modelNumber = 0;
		for (DWSNode logCluster : gs.keySet()) {
			modelNumber++;
			String processName = processImporter.getProcessNameOfNode(logCluster);
			File modelFile = new File(outFolder, processName + ".epml");
			formattableEPCSerializer.serialize(gs.get(logCluster), modelFile.getAbsolutePath());
		}
	}
	
	private void serializeSubprocesses(String outFolder, DWSProcessStandardizer standardizer) {
		
		try {
			FileUtils.cleanDirectory(new File(outFolder));
		} catch (IOException e) {
			System.out.println("Failed to clear folder " + outFolder);
		}
		Map<String, CPF> gs = standardizer.getStandardizedSubprocesses();
		Map<String, StandardizedCluster> cs = standardizer.getStandardizedClusters();
		int modelNumber = 0;
		for (String cid : cs.keySet()) {
			StandardizedCluster c = cs.get(cid);
			String repFragmentId = c.getRepresentativeFragmentId();
			CPF cpf = gs.get(repFragmentId);
			modelNumber++;
			File modelFile = new File(outFolder, repFragmentId + ".epml");
			formattableEPCSerializer.serialize(cpf, modelFile.getAbsolutePath());
		}
	}

}

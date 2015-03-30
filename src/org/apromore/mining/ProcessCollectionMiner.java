package org.apromore.mining;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apromore.dao.FragmentVersionDao;
import org.apromore.dao.ProcessDao;
import org.apromore.graph.JBPT.CPF;
import org.apromore.mining.complexity.AggregatedComplexityChecker;
import org.apromore.mining.complexity.AggregatedMetricComplexityChecker;
import org.apromore.mining.complexity.ComplexityCalculator;
import org.apromore.mining.complexity.ComplexityEvaluatorTool;
import org.apromore.mining.complexity.SelectiveProcessComplexityChecker;
import org.apromore.mining.complexity.SizePriorityBasedProcessComplexityChecker;
import org.apromore.mining.standardize.ProcessMerger;
import org.apromore.mining.standardize.ProcessStandardizer;
import org.apromore.mining.standardize.StandardizedCluster;
import org.apromore.mining.utils.MiningUtils;
import org.apromore.service.ClusteringService;
import org.apromore.service.ProcessService;
import org.apromore.service.RepositoryService;
import org.apromore.service.utils.EPCDeserializer;
import org.apromore.service.utils.EPCSerializer;
import org.apromore.service.utils.FormattableEPCSerializer;
import org.apromore.util.DebugUtil;
import org.prom5.analysis.traceclustering.algorithm.AgglomerativeHierarchicalAlgorithm.MyCluster;
import org.prom5.framework.log.LogReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

public class ProcessCollectionMiner {
	
	private final Logger logger = LoggerFactory.getLogger(ProcessCollectionMiner.class);
	
	@Autowired
	LogComplexityChecker logComplexityChecker;
	
	@Autowired
	AggregatedLogClusterer logClusterer;
	
	@Autowired
	private ProcessMiner processMiner;
	
	@Autowired
	ProcessModelImporter processImporter;
	
	@Autowired
	private ClusteringService csrv;
	
	@Autowired
	ProcessStandardizer standardizer;
	
//	@Autowired
//	ProcessComplexityChecker complexityChecker;
	
//	@Autowired
//	SelectiveProcessComplexityChecker complexityChecker;
	
	// original code
//	@Autowired
//	SizePriorityBasedProcessComplexityChecker complexityChecker;
	
	@Autowired
	private AggregatedComplexityChecker complexityChecker;
	
//	@Autowired
//	private AggregatedMetricComplexityChecker complexityChecker;
	
	@Autowired @Qualifier("ProcessService")
	ProcessService psrv;
	
	@Autowired @Qualifier("RepositoryService")
	RepositoryService rsrv;
	
	@Autowired @Qualifier("ProcessDao")
	private ProcessDao pdao;
	
	@Autowired @Qualifier("FragmentVersionDao")
	private FragmentVersionDao fdao;
	
	@Autowired
	private ProcessMerger processMerger;

	private EPCDeserializer epcDeserializer = new EPCDeserializer();
	private EPCSerializer epcSerializer = new EPCSerializer();
	private FormattableEPCSerializer formattableEPCSerializer = new FormattableEPCSerializer();
	
	private String processesPath = "processes";
	private String processesDataPath = "s_c_processes_data.csv";
	private String subprocessesPath = "subprocesses";
	private String subprocessesDataPath = "s_c_subprocesses_data.csv";
	private String combinedPath = "combined";
	private String combinedDataPath = "s_c_combined_data.csv";
	private final String invalidModelsPath = "invalid";
	
	public void mineCollection(String logPath, String outPath) throws Exception {
		
		logger.info("========== STARTING S_C TEST ===========");
		
		long t1 = System.currentTimeMillis();
		
		initializePaths(outPath);
		
		EvaluatorUtil.init(outPath);
//		DebugUtil.initOutPath(invalidModelsPath);
		processMerger.initialize();
		
		File logFile = new File(logPath);
		Collection<String> logPaths = new HashSet<String>();
		logPaths.add(logFile.getAbsolutePath());
		long duration =  mineCollection(logPaths);
		
		writeEvalData(duration);
		
		logger.info("========== S_C TEST COMPLETED ===========");
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
		
		if (!MiningConfig.WRITE_EVAL_DATA) {
			return;
		}
		
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

	private Map<MyCluster, CPF> getInitialModels(AggregatedLogClusterer logClusterer, int NThreshold) throws Exception {
		Map<MyCluster, CPF> initialModels = new HashMap<MyCluster, CPF>();
		Map<MyCluster, LogReader> roots = logClusterer.getRoots();
		
		for (MyCluster rootCluster : roots.keySet()) {
			
			Queue<MyCluster> q = new LinkedList<MyCluster>();
			q.add(rootCluster);
			
			while (!q.isEmpty()) {
				MyCluster currentCluster = q.poll();
				LogReader currentLog = logClusterer.getLog(currentCluster);
				String currentEPML = processMiner.mineEPC(currentLog);
				CPF currentModel = epcDeserializer.deserializeInputStream(IOUtils.toInputStream(currentEPML));
				int N = ComplexityCalculator.getNOAJS(currentModel);
				Map<MyCluster, LogReader> children = logClusterer.getChildren(currentCluster);
				if (N <= NThreshold || children.isEmpty()) {
					initialModels.put(currentCluster, currentModel);
				} else {
					for (MyCluster childCluster : children.keySet()) {
						q.add(childCluster);
					}
				}
			}
		}
		logger.debug("Identified {} initial models below N:{}", initialModels.size(), NThreshold);
		return initialModels;
	}
	
	public long mineCollection(Collection<String> logPaths) throws Exception {

		long startTime = System.currentTimeMillis();
		
		// initialize the cluster hierarchy of the traces.
		// this cluster hierarchy is used though out the mining process.
		logger.debug("Initializing the log clusterers...");
		logClusterer.initialize(logPaths);
		
		// TODO: original code
//		Map<MyCluster, CPF> allMinedModels = new HashMap<MyCluster, CPF>();
//		Map<MyCluster, LogReader> roots = logClusterer.getRoots();
//		for (MyCluster rootCluster : roots.keySet()) {
//			
//			MiningData data = new MiningData();
//			data.setMasterLog(roots.get(rootCluster));
//			data.setLog(roots.get(rootCluster));
//			data.setLogCluster(rootCluster);
//			
//			// real code
//			logComplexityChecker.process(data);
//			allMinedModels.putAll(data.getMinedModels());
//			logger.debug("{} process models have been mined from the log.", data.getMinedModels().size());
//		}
		// end of original code
		
		// new code
		Map<MyCluster, CPF> allMinedModels = getInitialModels(logClusterer, MiningConfig.INITIAL_PROCESS_SIZE);
		// end of new code
		
		Map<MyCluster, CPF> addedProcesses = processImporter.process(allMinedModels);
		csrv.computeGEDMatrix();
		Map<Integer, CPF> addedProcessesById = convertToIdBasedMap(addedProcesses);
		Set<MyCluster> complexModels = complexityChecker.getComplexProcessesToReprocessAll(addedProcesses);
		
		// test code
//		Set<Integer> complexModels = new HashSet<Integer>();
//		complexModels.add(500);
		
		if (complexModels.isEmpty()) {
			standardizer.standardize();
		}
		
		// models that cannot be further divided by trace clustering their traces
		Set<MyCluster> undividableModels = new HashSet<MyCluster>();
		
		while (!complexModels.isEmpty()) {
			
			standardizer.standardize();
			Map<MyCluster, CPF> standardizedProcesses = standardizer.getStandardizedProcesses();
			Map<String, CPF> standardizedSubprocesses = standardizer.getStandardizedSubprocesses();
			
			// original code
//			Set<String> complexFragmentIds = 
//					complexityChecker.getComplexSubprocessesToReprocess(standardizedSubprocesses);
//			// prioritise the simplification of complex fragments over complex processes.
//			if (!complexFragmentIds.isEmpty()) {
//				complexModels.clear();
//				complexModels.addAll(getSharedProcesses(complexFragmentIds));
//			} else {
//				complexModels = complexityChecker.getComplexProcessesToReprocess(standardizedProcesses);
//			}
			// end of original code
			complexModels = complexityChecker.getComplexProcessesToReprocessAll(standardizer, undividableModels);
			
			if (logger.isDebugEnabled()) {
				StringBuffer b = new StringBuffer();
				for (MyCluster n : complexModels) {
					b.append(n.getID() + ",");
				}
				logger.debug("Complex models selected for simplification: {}", b.toString());
			}
			
//			complexModels.clear(); // for testing
			Map<MyCluster, CPF> allNewProcesses = new HashMap<MyCluster, CPF>();
			for (MyCluster logCluster : complexModels) {
				
				Map<MyCluster, CPF> nextLevelModels = getDirectNextLevelModels(logCluster, undividableModels);
//				rsrv.deleteProcess(MiningUtils.getProcessName(logClusterID));
				
				if (!undividableModels.contains(logCluster)) {
					String processNameToDelete = processImporter.getProcessNameOfLogCluster(logCluster);
					logger.debug("Deleting the process {} to replace with its next level models.", processNameToDelete);
					rsrv.deleteProcess(processNameToDelete);
					EvaluatorUtil.removeCluster(processNameToDelete);
					Map<MyCluster, CPF> addedNewProcesses = processImporter.process(nextLevelModels);
					allNewProcesses.putAll(addedNewProcesses);
				}
			}
			
			appendGEDMatrix(allNewProcesses);
		} 
		
		standardizer.apply();
		
		long endTime = System.currentTimeMillis();
		long duration = endTime - startTime;
		
		serialize(processesPath, standardizer.getStandardizedProcesses());
		serializeSubprocesses(subprocessesPath, standardizer);
		
		if (MiningConfig.WRITE_ADDITIONAL_DATA) {
			EvaluatorUtil.writeData(logClusterer, duration);
		}
		
		return duration;
		
//		Map<MyCluster, CPF> models = data.getMinedModels();
//		Set<MyCluster> invalidModels = addModels(models);
//		while (!invalidModels.isEmpty()) {
//			
//		}
		
		// testing code follows
//		String outFolder = "/home/cn/eps/bpm-demo-new1/bpm-demo/Apromore-Core/apromore-service/data/pm1/t1/models";
//		Map<MyCluster, CPF> models = data.getMinedModels();
//		int modelNumber = 0;
//		
//		for (MyCluster c : models.keySet()) {
//			modelNumber++;
//			CPF model = models.get(c);
//			File modelFile = new File(outFolder, "e_" + modelNumber + ".epml");
//			epcSerializer.serializeToFile(model, modelFile.getAbsolutePath());
//		}
	}

	/**
	 * Returns all process models in which given fragments are shared. If given fragments are complex, we have to 
	 * recluster process models which contain those fragments.
	 * 
	 * @param complexFragmentIds
	 * @return
	 */
	public Set<MyCluster> getSharedProcesses(Set<String> complexFragmentIds) {
		
		Set<MyCluster> logClusterIds = new HashSet<MyCluster>();
		for (String fid : complexFragmentIds) {
			Collection<String> pnames = fdao.getProcessNamesOfFragmentId(fid);
			for (String pname : pnames) {
				MyCluster logCluster = processImporter.getLogClusterOfProcess(pname);
//				int logClusterId = MiningUtils.getLogClusterId(pname);
				logClusterIds.add(logCluster);
			}
		}
		return logClusterIds;
	}

	private Map<MyCluster, CPF> getNextLevelModels(MyCluster logCluster) throws Exception {
		Map<MyCluster, CPF> nextLevelModels = new HashMap<MyCluster, CPF>();
//		MyCluster logCluster = logClusterer.getLogCluster(logClusterID);
		
		Queue<MyCluster> complexLogClusters = new LinkedList<MyCluster>();
		complexLogClusters.add(logCluster);
		while (!complexLogClusters.isEmpty()) {
			MyCluster c = complexLogClusters.poll();
			Map<MyCluster, LogReader> children = logClusterer.getChildren(c);
			for (MyCluster childLogCluster : children.keySet()) {
				String epmlString = processMiner.mineEPC(children.get(childLogCluster));
				CPF childModel = epcDeserializer.deserializeInputStream(IOUtils.toInputStream(epmlString));
				if (!complexityChecker.isComplex(childModel)) {
					nextLevelModels.put(childLogCluster, childModel);
				} else {
					complexLogClusters.add(childLogCluster);
				}
			}
		}
		logger.debug("{} next level models generated for the log cluster Id {}", 
				nextLevelModels.size(), logCluster.getGroupId() + "_" + logCluster.getID());
		return nextLevelModels;
	}
	
	private Map<MyCluster, CPF> getDirectNextLevelModels(MyCluster logCluster, Set<MyCluster> undividableModels) throws Exception {
		Map<MyCluster, CPF> nextLevelModels = new HashMap<MyCluster, CPF>();
//		MyCluster logCluster = logClusterer.getLogCluster(logClusterID);
		
		Queue<MyCluster> complexLogClusters = new LinkedList<MyCluster>();
		complexLogClusters.add(logCluster);
		while (!complexLogClusters.isEmpty()) {
			MyCluster c = complexLogClusters.poll();
			Map<MyCluster, LogReader> children = logClusterer.getChildren(c);
			if (children.isEmpty()) {
				// complex model "c" does not have child logs. so cannot be divided into simple models.
				// we have report this as an undividable model, so that it will be outputted as it is.
				undividableModels.add(c);
			} else {
				for (MyCluster childLogCluster : children.keySet()) {
					String epmlString = processMiner.mineEPC(children.get(childLogCluster));
					CPF childModel = epcDeserializer.deserializeInputStream(IOUtils.toInputStream(epmlString));
					nextLevelModels.put(childLogCluster, childModel);
	//				if (!complexityChecker.isComplex(childModel)) {
	//					nextLevelModels.put(childLogCluster, childModel);
	//				} else {
	//					complexLogClusters.add(childLogCluster);
	//				}
				}
			}
		}
		
		if (logger.isDebugEnabled()) {
			StringBuffer b = new StringBuffer();
			for (MyCluster n : nextLevelModels.keySet()) {
				b.append(n.getID() + ",");
			}
			logger.debug("Next level models generated for the log cluster Id {} -> {}", 
					logCluster.getGroupId() + "_" + logCluster.getID(), b.toString());
		}
		return nextLevelModels;
	}

	private void appendGEDMatrix(Map<MyCluster, CPF> allNewProcesses) {
		
		if (logger.isDebugEnabled()) {
			StringBuffer b = new StringBuffer();
			for (MyCluster n : allNewProcesses.keySet()) {
				b.append(n.getID() + ",");
			}
			logger.debug("Appending the distance matrix for {} new processes: {}", allNewProcesses.size(), b.toString());
		}
		Collection<String> newRoots = new HashSet<String>();
		for (MyCluster c : allNewProcesses.keySet()) {
//			String processName = MiningUtils.getProcessName(c.getID()); 
			String processName = processImporter.getProcessNameOfLogCluster(c); 
			String rootId = pdao.getRootFragmentId(processName);
			newRoots.add(rootId);
		}
		
		csrv.appendGEDMatrix(newRoots);
	}

	private void serialize(String outFolder, Map<MyCluster, CPF> gs) {
		
		try {
			FileUtils.cleanDirectory(new File(outFolder));
		} catch (IOException e) {
			System.out.println("Failed to clear folder " + outFolder);
		}
		
		int modelNumber = 0;
		for (MyCluster logCluster : gs.keySet()) {
			modelNumber++;
			String processName = processImporter.getProcessNameOfLogCluster(logCluster);
			File modelFile = new File(outFolder, processName + ".epml");
			formattableEPCSerializer.serialize(gs.get(logCluster), modelFile.getAbsolutePath());
		}
	}
	
	private void serializeSubprocesses(String outFolder, ProcessStandardizer standardizer) {
		
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
		
//		Map<String, CPF> gs = standardizer.getStandardizedSubprocesses();
//		try {
//			FileUtils.cleanDirectory(new File(outFolder));
//		} catch (IOException e) {
//			System.out.println("Failed to clear folder " + outFolder);
//		}
//		
//		int modelNumber = 0;
//		for (String id : gs.keySet()) {
//			String repFragment = standardizer.getStandardizedFragment(id);
//			if (id.equals(repFragment)) {
//				modelNumber++;
//				File modelFile = new File(outFolder, id + ".epml");
//				formattableEPCSerializer.serialize(gs.get(id), modelFile.getAbsolutePath());
//			}
//		}
	}

	private Map<Integer, CPF> convertToIdBasedMap(Map<MyCluster, CPF> addedProcesses) {
		Map<Integer, CPF> idBasedMap = new HashMap<Integer, CPF>();
		for (MyCluster c : addedProcesses.keySet()) {
			idBasedMap.put(c.getID(), addedProcesses.get(c));
		}
		return idBasedMap;
	}
	
	private void log(String msg) {
		System.out.println(msg);
	}
}

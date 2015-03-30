package org.apromore.mining;

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
import org.apromore.dao.ProcessDao;
import org.apromore.graph.JBPT.CPF;
import org.apromore.mining.complexity.ComplexityEvaluatorTool;
import org.apromore.mining.complexity.SizePriorityBasedProcessComplexityChecker;
import org.apromore.mining.standardize.ProcessStandardizer;
import org.apromore.mining.utils.MiningUtils;
import org.apromore.service.ClusteringService;
import org.apromore.service.ProcessService;
import org.apromore.service.RepositoryService;
import org.apromore.service.utils.EPCDeserializer;
import org.apromore.service.utils.EPCSerializer;
import org.apromore.service.utils.FormattableEPCSerializer;
import org.apromore.util.DebugUtil;
import org.omg.PortableInterceptor.INACTIVE;
import org.prom5.analysis.traceclustering.algorithm.AgglomerativeHierarchicalAlgorithm.MyCluster;
import org.prom5.framework.log.LogFile;
import org.prom5.framework.log.LogReader;
import org.prom5.framework.log.rfb.BufferedLogReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

public class LogClusterBasedProcessMiner {
	
	private static final Logger logger = LoggerFactory.getLogger(LogClusterBasedProcessMiner.class);
	
	@Autowired
	LogComplexityChecker logComplexityChecker;
	
	@Autowired
	AggregatedLogClusterer logClusterer;
	
	@Autowired
	private ProcessMiner processMiner;
	
	@Autowired
	ProcessModelImporter processImporter;
	
	@Autowired
	ProcessComplexityChecker complexityChecker;
	
//	@Autowired
//	SizePriorityBasedProcessComplexityChecker complexityChecker;
	
	@Autowired @Qualifier("ProcessService")
	ProcessService psrv;
	
	@Autowired @Qualifier("RepositoryService")
	RepositoryService rsrv;
	
	@Autowired @Qualifier("ProcessDao")
	private ProcessDao pdao;

	private EPCDeserializer epcDeserializer = new EPCDeserializer();
	private EPCSerializer epcSerializer = new EPCSerializer();
	private FormattableEPCSerializer formattableEPCSerializer = new FormattableEPCSerializer();
	
	private String processesPath = "processes";
	private String processesDataPath = "s_processes_data.csv";
	private static final String evalFilePath = ".";
	private static final String invalidModelsPath = "invalid";
	
	public void mineCollection(String inPath, String outPath) throws Exception {
		
		long t1 = System.currentTimeMillis();
		
		initializePaths(outPath);
		
		if (MiningConfig.WRITE_ADDITIONAL_DATA) {
			EvaluatorUtil.init(outPath);
			DebugUtil.initOutPath(outPath);
		}
		
		Collection<String> logPaths = new HashSet<String>();
		File logFile = new File(inPath);
		logPaths.add(logFile.getAbsolutePath());
		mineCollection(logPaths);
		
		long duration = System.currentTimeMillis() - t1;
		
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
	}
	
	private void writeEvalData(long duration) {
		ComplexityEvaluatorTool evaluatorTool = new ComplexityEvaluatorTool();
		evaluatorTool.writeComplexities(processesPath, processesDataPath, duration);
	}
	
	public void mineCollection(Collection<String> logPaths) throws Exception {
		
		// initialize the cluster hierarchy of the traces.
		// this cluster hierarchy is used though out the mining process.
		long startTime = System.currentTimeMillis();
		
		logger.debug("Initializing the log clusterer...");
		logClusterer.initialize(logPaths);
		
		Map<MyCluster, CPF> allMinedModels = new HashMap<MyCluster, CPF>();
		Map<MyCluster, LogReader> roots = logClusterer.getRoots();
		for (MyCluster rootCluster : roots.keySet()) {
			
			MiningData data = new MiningData();
			data.setMasterLog(roots.get(rootCluster));
			data.setLog(roots.get(rootCluster));
			data.setLogCluster(rootCluster);
			
			// real code
			logComplexityChecker.process(data);
			allMinedModels.putAll(data.getMinedModels());
			logger.debug("{} process models have been mined from the log.", data.getMinedModels().size());
		}
		
//		MiningData data = new MiningData();
//		data.setMasterLog(lr);
//		data.setLog(lr);
//		data.setLogCluster(logClusterer.getRoot());
//		
//		// real code
//		logComplexityChecker.process(data);
		
		Map<MyCluster, CPF> addedProcesses = processImporter.process(allMinedModels);
		Map<Integer, CPF> addedProcessesById = convertToIdBasedMap(addedProcesses);
		Set<MyCluster> complexModels = complexityChecker.getComplexProcessesToReprocess(addedProcesses);
		
		Map<MyCluster, CPF> allProcesses = new HashMap<MyCluster, CPF>(addedProcesses);
		
//		complexModels.clear(); // test code
		if (!complexModels.isEmpty()) {
			
			Map<MyCluster, CPF> allNewProcesses = new HashMap<MyCluster, CPF>();
			for (MyCluster logCluster : complexModels) {
				Map<MyCluster, CPF> nextLevelModels = getNextLevelModels(logCluster);
				if (!nextLevelModels.isEmpty()) {
					allProcesses.remove(logCluster);
					String processNameToDelete = processImporter.getProcessNameOfLogCluster(logCluster);
					rsrv.deleteProcess(processNameToDelete);
					EvaluatorUtil.removeCluster(processNameToDelete);
					Map<MyCluster, CPF> addedNewProcesses = processImporter.process(nextLevelModels);
					allNewProcesses.putAll(addedNewProcesses);
				}
			}
			logger.debug("{} new processes were added after simplification.", allNewProcesses.size());
			allProcesses.putAll(allNewProcesses);
		}

		long endTime = System.currentTimeMillis();
		long duration = endTime - startTime;
		
		serialize(processesPath, allProcesses);
		
		if (MiningConfig.WRITE_ADDITIONAL_DATA) {
			EvaluatorUtil.writeData(logClusterer, duration);
		}
	}

	/**
	 * Returns all process models in which given fragments are shared. If given fragments are complex, we have to 
	 * recluster process models which contain those fragments.
	 * 
	 * @param complexFragmentIds
	 * @return
	 */
	private Set<Integer> getSharedProcesses(Set<String> complexFragmentIds) {
		return null;
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
				if (!complexityChecker.isComplexWithBuffer(childModel) || !logClusterer.hasChildren(childLogCluster)) {
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

	private void serialize(String outFolder, Map<MyCluster, CPF> allProcesses) {
		
		try {
			FileUtils.cleanDirectory(new File(outFolder));
		} catch (IOException e) {
			System.out.println("Failed to clear folder " + outFolder);
		}
		
		int modelNumber = 0;
		for (MyCluster c : allProcesses.keySet()) {
			modelNumber++;
			File modelFile = new File(outFolder, "p_" + c.getID() + ".epml");
			CPF model = allProcesses.get(c);
			logger.debug("Process {} - Size: {}", c.getID(), model.getVertices().size());
			formattableEPCSerializer.serialize(model, modelFile.getAbsolutePath());
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
			File modelFile = new File(outFolder, "p_" + id + ".epml");
			formattableEPCSerializer.serialize(gs.get(id), modelFile.getAbsolutePath());
		}
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

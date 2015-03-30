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
import org.apromore.dao.FragmentVersionDao;
import org.apromore.dao.ProcessDao;
import org.apromore.graph.JBPT.CPF;
import org.apromore.mining.standardize.ProcessStandardizer;
import org.apromore.mining.utils.MiningUtils;
import org.apromore.service.ClusteringService;
import org.apromore.service.ProcessService;
import org.apromore.service.RepositoryService;
import org.apromore.service.utils.EPCDeserializer;
import org.apromore.service.utils.EPCSerializer;
import org.apromore.service.utils.FormattableEPCSerializer;
import org.prom5.analysis.traceclustering.algorithm.AgglomerativeHierarchicalAlgorithm.MyCluster;
import org.prom5.framework.log.LogReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

public class StandardizedProcessMiner {
	
	private final Logger logger = LoggerFactory.getLogger(StandardizedProcessMiner.class);
	
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
	
	@Autowired
	ProcessComplexityChecker complexityChecker;
	
	@Autowired @Qualifier("ProcessService")
	ProcessService psrv;
	
	@Autowired @Qualifier("RepositoryService")
	RepositoryService rsrv;
	
	@Autowired @Qualifier("ProcessDao")
	private ProcessDao pdao;
	
	@Autowired @Qualifier("FragmentVersionDao")
	private FragmentVersionDao fdao;

	private EPCDeserializer epcDeserializer = new EPCDeserializer();
	private EPCSerializer epcSerializer = new EPCSerializer();
	private FormattableEPCSerializer formattableEPCSerializer = new FormattableEPCSerializer();
	
	public void mineCollection(String logsPath) throws Exception {
		File logsFolder = new File(logsPath);

		Collection<String> logPaths = new HashSet<String>();
		File[] logFiles = logsFolder.listFiles();
		for (File logFile: logFiles) {
			String logFilePath = logFile.getAbsolutePath();
			logPaths.add(logFilePath);
		}
		mineCollection(logPaths);
	}
	
	public void mineCollection(Collection<String> logPaths) throws Exception {

		// initialize the cluster hierarchy of the traces.
		// this cluster hierarchy is used though out the mining process.
		logger.debug("Initializing the log clusterers...");
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
		
		Map<MyCluster, CPF> addedProcesses = processImporter.process(allMinedModels);
		csrv.computeGEDMatrix();
//		Set<MyCluster> complexModels = complexityChecker.getComplexProcessesToReprocess(addedProcesses);
		
		// test code
//		Set<Integer> complexModels = new HashSet<Integer>();
//		complexModels.add(500);
		
		standardizer.standardize();
		
//		while (!complexModels.isEmpty()) {
//			
//			standardizer.standardize();
//			Map<MyCluster, CPF> standardizedProcesses = standardizer.getStandardizedProcesses();
//			Map<String, CPF> standardizedSubprocesses = standardizer.getStandardizedSubprocesses();
//			
//			complexModels = complexityChecker.getComplexProcessesToReprocess(standardizedProcesses);
//			
//			Set<String> complexFragmentIds = 
//					complexityChecker.getComplexSubprocessesToReprocess(standardizedSubprocesses);
//			complexModels.addAll(getSharedProcesses(complexFragmentIds));
//			
////			complexModels.clear(); // for testing
//			Map<MyCluster, CPF> allNewProcesses = new HashMap<MyCluster, CPF>();
//			for (MyCluster logCluster : complexModels) {
//				Map<MyCluster, CPF> nextLevelModels = getDirectNextLevelModels(logCluster);
////				rsrv.deleteProcess(MiningUtils.getProcessName(logClusterID));
//				rsrv.deleteProcess(processImporter.getProcessNameOfLogCluster(logCluster));
//				Map<MyCluster, CPF> addedNewProcesses = processImporter.process(nextLevelModels);
//				allNewProcesses.putAll(addedNewProcesses);
//			}
//			
//			appendGEDMatrix(allNewProcesses);
//		} 
		
		standardizer.apply();
		
		String outFolder1 = "/home/cn/eps/bpm-demo-new1/bpm-demo/Apromore-Core/apromore-service/data/pm1/t1/ss1";
		String outFolder2 = "/home/cn/eps/bpm-demo-new1/bpm-demo/Apromore-Core/apromore-service/data/pm1/t1/ss2";
		serialize(outFolder1, standardizer.getStandardizedProcesses());
		serializeSubprocesses(outFolder2, standardizer.getStandardizedSubprocesses());
		
		
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
	private Set<MyCluster> getSharedProcesses(Set<String> complexFragmentIds) {
		
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
	
	private Map<MyCluster, CPF> getDirectNextLevelModels(MyCluster logCluster) throws Exception {
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
				nextLevelModels.put(childLogCluster, childModel);
//				if (!complexityChecker.isComplex(childModel)) {
//					nextLevelModels.put(childLogCluster, childModel);
//				} else {
//					complexLogClusters.add(childLogCluster);
//				}
			}
		}
		logger.debug("{} next level models generated for the log cluster Id {}", 
				nextLevelModels.size(), logCluster.getGroupId() + "_" + logCluster.getID());
		return nextLevelModels;
	}

	private void appendGEDMatrix(Map<MyCluster, CPF> allNewProcesses) {
		
		logger.debug("Appending the distance matrix for {} new processes...", allNewProcesses.size());
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
			File modelFile = new File(outFolder, "p_" + processName + ".epml");
			formattableEPCSerializer.serialize(gs.get(logCluster), modelFile.getAbsolutePath());
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

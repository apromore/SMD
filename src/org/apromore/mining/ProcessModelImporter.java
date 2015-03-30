package org.apromore.mining;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apromore.dao.ProcessDao;
import org.apromore.exception.ImportException;
import org.apromore.graph.JBPT.CPF;
import org.apromore.mining.utils.CPFTransformer;
import org.apromore.mining.utils.CPFtoMultiDirectedGraphConverter;
import org.apromore.mining.utils.CycleFixer;
import org.apromore.mining.utils.MiningUtils;
import org.apromore.mining.utils.SingleTerminalCycleFormer;
import org.apromore.service.ProcessService;
import org.apromore.service.utils.EPCDeserializer;
import org.apromore.service.utils.EPCSerializer;
import org.apromore.service.utils.IDGenerator;
import org.apromore.util.DebugUtil;
import org.jbpt.graph.DirectedEdge;
import org.jbpt.graph.MultiDirectedGraph;
import org.jbpt.graph.algo.DirectedGraphAlgorithms;
import org.jbpt.hypergraph.abs.Vertex;
import org.jfree.util.Log;
import org.prom5.analysis.traceclustering.algorithm.AgglomerativeHierarchicalAlgorithm.MyCluster;
import org.prom5.exporting.log.MXMLibPlainLogExport;
import org.prom5.framework.log.LogReader;
import org.prom5.framework.plugin.ProvidedObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

public class ProcessModelImporter {
	
	private final Logger logger = LoggerFactory.getLogger(ProcessModelImporter.class);
	
	@Autowired
	private ProcessService psrv;
	
	@Autowired
	private ProcessMiner miner;
	
	@Autowired
	private AggregatedLogClusterer logClusterer;
	
	@Autowired
	ProcessComplexityChecker complexityChecker;
	
	@Autowired @Qualifier("ProcessDao")
	private ProcessDao pdao;
	
	private EPCSerializer epcSerializer = new EPCSerializer();
	private EPCDeserializer epcDeserializer = new EPCDeserializer();
	private DirectedGraphAlgorithms<DirectedEdge, Vertex> algo = new DirectedGraphAlgorithms<DirectedEdge, Vertex>();
	
	private Map<String, MyCluster> processes = new HashMap<String, MyCluster>();
	private Map<MyCluster, String> processNames = new HashMap<MyCluster, String>();

	public Map<MyCluster, CPF> process(MiningData data) throws Exception {
		return process(data.getMinedModels());
	}
	
	public Map<MyCluster, CPF> process(Map<MyCluster, CPF> models) throws Exception {
		
		if (logger.isDebugEnabled()) {
			StringBuffer b = new StringBuffer();
			for (MyCluster n : models.keySet()) {
				b.append(n.getID() + ",");
			}
			logger.debug("Importing process models: {}", b.toString());
		}
		
		Map<MyCluster, CPF> addedModels = new HashMap<MyCluster, CPF>(models);
		Set<MyCluster> invalidLogClusters = addModels(models);
		removeAll(addedModels, invalidLogClusters);
		
		while (!invalidLogClusters.isEmpty()) {
			
			Map<MyCluster, CPF> nextLevelModels = new HashMap<MyCluster, CPF>();
			for (MyCluster invalidLogCluster : invalidLogClusters) {
				Map<MyCluster, LogReader> childLogs = logClusterer.getChildren(invalidLogCluster);
				if (childLogs.isEmpty()) {
					DebugUtil.invalidModelsCount++;
					
					if (MiningConfig.SERIALIZE_INVALID_MODELS) {
						LogReader invalidLog = logClusterer.getLog(invalidLogCluster);
						String invalidEPML = miner.mineEPC(invalidLog);
						CPF invalidModel = epcDeserializer.deserializeInputStream(IOUtils.toInputStream(invalidEPML));
						DebugUtil.writeModel("invalid", invalidModel);
					}
				}
				
				for (MyCluster childLogCluster : childLogs.keySet()) {
					LogReader childLog = childLogs.get(childLogCluster);
					String epml = miner.mineEPC(childLog);
					CPF childModel = epcDeserializer.deserializeInputStream(IOUtils.toInputStream(epml));
					nextLevelModels.put(childLogCluster, childModel);
				}
				
				if (logger.isDebugEnabled()) {
					StringBuffer b = new StringBuffer();
					for (MyCluster n : childLogs.keySet()) {
						b.append(n.getID() + ",");
					}
					logger.debug("Invalid model {} is splitted in to following models using log clustering: {}", 
							invalidLogCluster.getID(), b.toString());
				}
			}
			addedModels.putAll(nextLevelModels);
			invalidLogClusters = addModels(nextLevelModels);
			removeAll(addedModels, invalidLogClusters);
		}
		
		if (logger.isDebugEnabled()) {
			StringBuffer b = new StringBuffer();
			for (MyCluster n : addedModels.keySet()) {
				b.append(n.getID() + ",");
			}
			
			StringBuffer b2 = new StringBuffer();
			for (MyCluster n : models.keySet()) {
				b2.append(n.getID() + ",");
			}
			logger.debug("{} models imported from {} input process models. (Imported models: {} | Original models: {})", 
					new Object[] {addedModels.size(), models.size(), b.toString(), b2.toString()});
		}
		
		for (MyCluster addedCluster : addedModels.keySet()) {
			String addedProcessName = getProcessNameOfLogCluster(addedCluster);
			EvaluatorUtil.addCluster(addedProcessName, addedCluster);
		}
		return addedModels;
	}
	
	public MyCluster getLogClusterOfProcess(String processName) {
		return processes.get(processName);
	}
	
	public String getProcessNameOfLogCluster(MyCluster logCluster) {
		return processNames.get(logCluster);
	}
	
	private void removeAll(Map<MyCluster, CPF> addedModels, Set<MyCluster> invalidLogClusters) {
		for (MyCluster c : invalidLogClusters) {
			addedModels.remove(c);
		}
	}

	private Set<MyCluster> addModels(Map<MyCluster, CPF> models) {
		Set<MyCluster> invalidModels = new HashSet<MyCluster>();
		int modelNumber = 0;
		for (MyCluster c : models.keySet()) {
			modelNumber++;
			CPF model = models.get(c);
			
			if (MiningConfig.SERIALIZE_NONMULTITERMINAL_LOGS) {
				try {
					MultiDirectedGraph mdg0 = CPFtoMultiDirectedGraphConverter.covert(model);
					if (!algo.isMultiTerminal(mdg0)) {
						LogReader invalidLog = logClusterer.getLog(c);
						String logName = MiningUtils.getLogFileName(c, invalidLog);
						MiningUtils.serializeLog(invalidLog, logName);
						logger.info(logName + " results in a non-multiterminal model.");
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			
			CPFTransformer.correct(model);
//			CycleRemover.removeCycles(model, false);
			SingleTerminalCycleFormer.formSingleTerminalCycles(model);
			String epmlString = epcSerializer.serializeToString(model);
			
//			String processName = "p_" + c.getGroupId() + "_" + c.getID(); // TODO: original code
			String processName = IDGenerator.generateProcessID();
			
			try {
				if (pdao.getProcess(processName) == null) {
					MultiDirectedGraph mdg = CPFtoMultiDirectedGraphConverter.covert(model);
		            if (algo.isMultiTerminal(mdg)) {
		            	psrv.addProcessModel(processName, epmlString);
		            	processes.put(processName, c);
		            	processNames.put(c, processName);
		            } else {
		            	logger.info("Process model {} is not multiterminal. Removing cycles...", processName);
//		            	boolean multiterminal = CycleRemover.removeCycles(model, mdg, true); // TODO: old code
		            	boolean multiterminal = CycleFixer.fixCycles(model);
		            	if (multiterminal) {
		            		logger.debug("Process model {} is multiterminal after removing cycles. Attempting to add the refined model...", processName);
		            		epmlString = epcSerializer.serializeToString(model);
		            		psrv.addProcessModel(processName, epmlString);
		            		processes.put(processName, c);
		            		processNames.put(c, processName);
		            		logger.debug("Process model {} of size {} is successfully added after cycle removal.", processName, model.getVertices().size());
		            	} else {
//		            		DebugUtil.writeModel("failed", model);
		            		throw new ImportException("Process model is not multiterminal after cycle removal.");
		            	}
		            }
					
				} else {
					String msg = processName + " already exists. Skipped the import.";
					logger.info(msg);
				}
				
			} catch (Exception e) {
				logger.error("Process model {} is not valid. Queuing it for reclustering...", processName);
				invalidModels.add(c);
			}
		}
		return invalidModels;
	}
}

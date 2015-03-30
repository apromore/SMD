package org.apromore.mining.guidetree.pcm;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apromore.dao.ProcessDao;
import org.apromore.exception.ImportException;
import org.apromore.graph.JBPT.CPF;
import org.apromore.mining.guidetree.Prom5BasedMiner;
import org.apromore.mining.guidetree.pcm.GTClusterer.GTNode;
import org.apromore.mining.utils.CPFTransformer;
import org.apromore.mining.utils.CPFtoMultiDirectedGraphConverter;
import org.apromore.mining.utils.CycleFixer;
import org.apromore.mining.utils.SingleTerminalCycleFormer;
import org.apromore.service.ProcessService;
import org.apromore.service.utils.EPCDeserializer;
import org.apromore.service.utils.EPCSerializer;
import org.apromore.service.utils.IDGenerator;
import org.apromore.util.DebugUtil;
import org.deckfour.xes.model.XLog;
import org.jbpt.graph.DirectedEdge;
import org.jbpt.graph.MultiDirectedGraph;
import org.jbpt.graph.algo.DirectedGraphAlgorithms;
import org.jbpt.hypergraph.abs.Vertex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

public class GTProcessImporter {
	
	private static final Logger logger = LoggerFactory.getLogger(GTProcessImporter.class);
	
	@Autowired
	private GTClusterer clusterer;
	
	@Autowired
	private Prom5BasedMiner miner;
	
	@Autowired
	private ProcessService psrv;
	
	@Autowired @Qualifier("ProcessDao")
	private ProcessDao pdao;
	
	private EPCSerializer epcSerializer = new EPCSerializer();
	private EPCDeserializer epcDeserializer = new EPCDeserializer();
	private DirectedGraphAlgorithms<DirectedEdge, Vertex> algo = new DirectedGraphAlgorithms<DirectedEdge, Vertex>();
	
	private Map<String, GTNode> processes = new HashMap<String, GTNode>();
	private Map<GTNode, String> processNames = new HashMap<GTNode, String>();

	public String getProcessNameOfNode(GTNode node) {
		return processNames.get(node);
	}

	public GTNode getNodeOfProcess(String processName) {
		return processes.get(processName);
	}
	
	public List<GTNode> importModels(List<GTNode> models) throws Exception {
		if (logger.isDebugEnabled()) {
			StringBuffer b = new StringBuffer();
			for (GTNode n : models) {
				b.append(n.getID() + ",");
			}
			logger.debug("Importing process models: {}", b.toString());
		}
		
		List<GTNode> addedModels = new ArrayList<GTNode>(models);
		Collection<GTNode> invalidLogClusters = addModels(models);
		removeAll(addedModels, invalidLogClusters);
		
		while (!invalidLogClusters.isEmpty()) {
			
			List<GTNode> nextLevelModels = new ArrayList<GTNode>();
			for (GTNode invalidLogCluster : invalidLogClusters) {
				Collection<GTNode> childLogs = clusterer.getChildren(invalidLogCluster);
				if (childLogs.isEmpty()) {
					DebugUtil.invalidModelsCount++;
					
					XLog invalidLog = invalidLogCluster.log;
					CPF invalidModel = miner.mineModel(invalidLog);
					DebugUtil.writeModel("invalid", invalidModel);
				}
				
				for (GTNode childLogCluster : childLogs) {
					XLog childLog = childLogCluster.log;
					CPF childModel = miner.mineModel(childLog);
					childLogCluster.cpf = childModel;
					nextLevelModels.add(childLogCluster);
				}
				
				if (logger.isDebugEnabled()) {
					StringBuffer b = new StringBuffer();
					for (GTNode n : childLogs) {
						b.append(n.getID() + ",");
					}
					logger.debug("Invalid model {} is splitted in to following models using log clustering: {}", 
							invalidLogCluster.getID(), b.toString());
				}
			}
			addedModels.addAll(nextLevelModels);
			invalidLogClusters = addModels(nextLevelModels);
			removeAll(addedModels, invalidLogClusters);
		}
		
		if (logger.isDebugEnabled()) {
			StringBuffer b = new StringBuffer();
			for (GTNode n : addedModels) {
				b.append(n.getID() + ",");
			}
			
			StringBuffer b2 = new StringBuffer();
			for (GTNode n : models) {
				b2.append(n.getID() + ",");
			}
			logger.debug("{} models imported from {} input process models. (Imported models: {} | Original models: {})", 
					new Object[] {addedModels.size(), models.size(), b.toString(), b2.toString()});
		}
		
		for (GTNode addedCluster : addedModels) {
			String addedProcessName = getProcessNameOfNode(addedCluster);
			GTEvaluatorUtil.addCluster(addedProcessName, addedCluster);
		}
		return addedModels;
	}

	private Set<GTNode> addModels(List<GTNode> models) {
		Set<GTNode> invalidModels = new HashSet<GTNode>();
		int modelNumber = 0;
		for (GTNode c : models) {
			modelNumber++;
			CPF model = c.cpf;
			CPFTransformer.correct(model);
			SingleTerminalCycleFormer.formSingleTerminalCycles(model);
			String epmlString = epcSerializer.serializeToString(model);
			
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
		            	boolean multiterminal = CycleFixer.fixCycles(model);
		            	if (multiterminal) {
		            		logger.debug("Process model {} is multiterminal after removing cycles. Attempting to add the refined model...", processName);
		            		epmlString = epcSerializer.serializeToString(model);
		            		psrv.addProcessModel(processName, epmlString);
		            		processes.put(processName, c);
		            		processNames.put(c, processName);
		            		logger.debug("Process model {} of size {} is successfully added after cycle removal.", processName, model.getVertices().size());
		            	} else {
		            		DebugUtil.writeModel("failed", model);
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
	
	private void removeAll(Collection<GTNode> addedModels, Collection<GTNode> invalidLogClusters) {
		for (GTNode c : invalidLogClusters) {
			addedModels.remove(c);
		}
	}

	

}

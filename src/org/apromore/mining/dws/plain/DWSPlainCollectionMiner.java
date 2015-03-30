package org.apromore.mining.dws.plain;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apromore.graph.JBPT.CPF;
import org.apromore.graph.JBPT.CpfEvent;
import org.apromore.mining.MiningConfig;
import org.apromore.mining.ProcessComplexityChecker;
import org.apromore.mining.complexity.ComplexityCalculator;
import org.apromore.mining.complexity.ComplexityEvaluatorTool;
import org.apromore.mining.dws.pcm.DWSClusterer;
import org.apromore.mining.dws.pcm.DWSProcessImporter;
import org.apromore.mining.dws.pcm.DWSProcessMiner;
import org.apromore.mining.dws.pcm.DWSClusterer.DWSNode;
import org.apromore.mining.sc.CPFImporter;
import org.apromore.mining.utils.CPFTransformer;
import org.apromore.mining.utils.ProcessSerializer;
import org.apromore.service.utils.EPCDeserializer;
import org.apromore.service.utils.FormattableEPCSerializer;
import org.jbpt.pm.ControlFlow;
import org.jbpt.pm.FlowNode;
import org.prom5.analysis.dws.Cluster;
import org.prom5.analysis.dws.VectorialPoint;
import org.prom5.converting.HNNetToEPCConverter;
import org.prom5.exporting.epcs.EpmlExport;
import org.prom5.framework.log.LogFile;
import org.prom5.framework.log.LogReader;
import org.prom5.framework.log.rfb.BufferedLogReader;
import org.prom5.framework.models.epcpack.ConfigurableEPC;
import org.prom5.framework.models.heuristics.HeuristicsNet;
import org.prom5.framework.plugin.ProvidedObject;
import org.prom5.mining.epcmining.EPCResult;
import org.prom5.mining.heuristicsmining.HeuristicsMiner;
import org.prom5.mining.heuristicsmining.HeuristicsMinerParameters;
import org.prom5.mining.heuristicsmining.HeuristicsNetResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

public class DWSPlainCollectionMiner {
	
	private static final Logger logger = LoggerFactory.getLogger(DWSPlainCollectionMiner.class);
	
	private EPCDeserializer epcDeserializer = new EPCDeserializer();
	
	@Autowired
	private DWSClusterer clusterer;
	
	@Autowired
	private DWSProcessMiner miner;
	
	@Autowired
	private DWSProcessImporter processImporter;
	
	@Autowired
	private ProcessComplexityChecker complexityChecker;
	
	@Autowired
	private ProcessSerializer processSerializer;
	
	private double sigma1 = 0.004; // 0.00005
	private double gamma1 = 0.002; // 0.00001
	
	private double sigma2 = 0.004; // 0.004
	private double gamma2 = 0.002; // 0.002
	
	private int k = 2; // 2
	private double sigma = sigma1; // 0.004
	private double gamma = gamma1; // 0.002
	private int l = 5; // 5
	private int maxFeatures = 5; // 5 | 100 in new tests
	
//	private int k = 2; // 2
//	private double sigma = 0.004; // 0.004
//	private double gamma = 0.002; // 0.002
//	private int l = 5; // 5
//	private int maxFeatures = 5; // 5
	
	private int noiseThreshold = 1; // 1
	
	private String processesPath = "/home/cn/eps/bpm-demo-new1/bpm-demo/Apromore-Core/apromore-service/data/pm1/t1/dws/processes";
	private String processesDataPath = "/home/cn/eps/bpm-demo-new1/bpm-demo/Apromore-Core/apromore-service/data/pm1/t1/dws/m_processes_data.csv";
	
	private Map<String, CPF> invalidModels = new HashMap<String, CPF>(); 
	
	public void mineCollectionFromFolder(String logsPath) {
		File folder = new File(logsPath);
		File[] fs = folder.listFiles();
		String logPath = fs[0].getAbsolutePath();
		mineCollection(logPath);
	}
	
	public void mineCollection(String logPath) {
		
//		long t1 = System.currentTimeMillis();
//		try {
//			clusterer.initialize(logPath);
//			DWSNode rootNode = clusterer.getRoot();
//			
//			Queue<DWSNode> unprocessedClusters = new LinkedList<DWSNode>();
//			unprocessedClusters.add(rootNode);
//			
//			int currentModelNumber = 0;
//			while (!unprocessedClusters.isEmpty()) {
//				currentModelNumber++;
//				
//				DWSNode c = unprocessedClusters.poll();
//				String processName = "P_" + currentModelNumber;
//				String epmlModel = miner.mineEPC(c);
//				CPF currentModel = epcDeserializer.deserializeInputStream(IOUtils.toInputStream(epmlModel));
//				c.cpf = currentModel;
//				if (logger.isDebugEnabled()) {
//					logDetails(processName, currentModel);
//				}
//				
//				if (complexityChecker.isComplexWithBuffer(currentModel)) {
//					
//					// mined model is complex. divide it into more log clusters.
//					logger.debug("{} is complex. Adding next level clusters for the model...", processName);
//					boolean populated = populateNextLevelClusters(c, unprocessedClusters);
//					if (!populated) {
//						logger.debug("Next level clusters are not found for the model {}. Adding the complex model...", 
//								processName);
//						boolean success = processImporter.importModel(processName, currentModel);
//						if (!success) {
//							logger.debug("{} is not valid. Next level clusters could not be generated for this. Adding this to invalid models...", processName);
//							invalidModels.put(processName, currentModel);
//						}
//					}
//					
//				} else {
//					
//					// mined model is not complex. add it to the repository.
//					// we may need to further cluster the log, if there are errors in adding the model to the
//					// repository.
//					logger.debug("{} is not complex. Adding it to the repository...", processName);
//					boolean success = cpfImporter.importModel(processName, currentModel);
//					if (!success) {
//						logger.debug("{} is not valid. Adding next level clusters...", processName);
//						boolean populated = populateNextLevelClusters(c, unprocessedClusters);
//						if (!populated) {
//							logger.debug("Next level clusters could not be generated for {}. Adding this to invalid models...", processName);
//							invalidModels.put(processName, currentModel);
//						}
//					}
//				}
//			}
//			
//			processSerializer.serializeAllProcesses(processesPath);
//			
//			logger.debug("Serializing {} complex or invalid models...", invalidModels.size());
//			String outPath2 = "/home/cn/eps/bpm-demo-new1/bpm-demo/Apromore-Core/apromore-service/data/pm1/t1/dws/invalid";
//			serializeModels(invalidModels, outPath2);
//			
//			
//		} catch (Exception e) {
//			logger.error("Failed to mine a process collection using DWS.", e);
//		}
//		long duration = System.currentTimeMillis() - t1;
//		writeEvalData(duration);
	}
	
	private void writeEvalData(long duration) {
		ComplexityEvaluatorTool evaluatorTool = new ComplexityEvaluatorTool();
		evaluatorTool.writeComplexities(processesPath, processesDataPath, duration);
	}
	
	private CPF removeEvents(CPF model) {
		
		CPFTransformer.correct(model);
		
		for (FlowNode v : model.getVertices()) {
			if (v instanceof CpfEvent) {
				String name = v.getName();
				if ("fictive start".equals(name) || "fictive end".equals(name)) {
					continue;
				}
				
				Collection<FlowNode> preset = model.getDirectPredecessors(v);
				Collection<FlowNode> postset = model.getDirectSuccessors(v);
				if (preset.size() > 1 || postset.size() > 1) {
					continue;
				}
				
				Collection<ControlFlow<FlowNode>> edgeToRemove = new ArrayList<ControlFlow<FlowNode>>();
				edgeToRemove.addAll(model.getIncomingEdges(v));
				edgeToRemove.addAll(model.getOutgoingEdges(v));
				model.removeEdges(edgeToRemove);
				model.removeVertex(v);
				
				if (preset.size() == 1 && postset.size() == 1) {
					FlowNode n1 = preset.iterator().next();
					FlowNode n2 = postset.iterator().next();
					model.addEdge(n1, n2);
				}
			}
		}
		
		return model;
	}
	
	private void serializeModels(Map<String, CPF> models, String outPath) {

		File outFolder = new File(outPath);
		try {
			FileUtils.cleanDirectory(outFolder);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		FormattableEPCSerializer epcSerializer = new FormattableEPCSerializer();
		for (String processName : models.keySet()) {
			File modelFile = new File(outPath, "i_" + processName + ".epml");
			CPF cpf = models.get(processName);
			epcSerializer.serialize(cpf, modelFile.getAbsolutePath());
		}
	}
	
	private boolean populateNextLevelClusters(DWSNode c, Queue<DWSNode> unprocessedClusters) {
		VectorialPoint.PROJECTION_METHOD = 0;
		boolean populated = populateNextLevelClustersInternal(c, unprocessedClusters);
		if (MiningConfig.DWS_USE_ALTERNATE_CLUSTER_DIVIDING) {
			if (!populated) {
				logger.debug("Original feature projection didn't give child clusters. Using direct feature projection...");
				VectorialPoint.PROJECTION_METHOD = 1;
				populated = populateNextLevelClustersInternal(c, unprocessedClusters);
				
				VectorialPoint.PROJECTION_METHOD = 0;
			}
			
			if (!populated) {
				logger.debug("Primary parameters didn't give child clusters. Using the secondary parameters...");
				sigma = sigma2;
				gamma = gamma2;
				populated = populateNextLevelClustersInternal(c, unprocessedClusters);
				
				sigma = sigma1;
				gamma = gamma1;
			}
		}
		return populated;
	}

	private boolean populateNextLevelClustersInternal(DWSNode c, Queue<DWSNode> unprocessedClusters) {
		
		boolean populated = false;
		
		Collection<DWSNode> childNodes = clusterer.getChildren(c);
		for (DWSNode child : childNodes) {
			LogReader cl = child.getLog();
			int pis = cl.getLogSummary().getNumberOfUniqueProcessInstances();
			if (pis >= noiseThreshold) {
				populated = true;
				unprocessedClusters.add(child);
			}
		}
		
		return populated;
	}

	private boolean isComplex(Cluster c) {
		return true;
	}
	
	private HeuristicsNet mineHNet(LogReader log) {
		HeuristicsMiner miner = new HeuristicsMiner();
		HeuristicsMinerParameters params = miner.getParameters();
		params.setAndThreshold(0.1);
		
		HeuristicsNetResult result = (HeuristicsNetResult) miner.mine(log);
		HeuristicsNet net = result.getHeuriticsNet();
		return net;
	}
	
	private String convertToEPML(HeuristicsNet hnet, LogReader log) {
		
		try {
			ProvidedObject po = new ProvidedObject("hm_results", new Object[] {hnet, log});
			HNNetToEPCConverter converter = new HNNetToEPCConverter();
			EPCResult epcResult = (EPCResult) converter.convert(po);
			
			ConfigurableEPC epc = epcResult.getEPC();
			ProvidedObject po2 = new ProvidedObject("epc_result", new Object[] {epc});
			
			ByteArrayOutputStream output = new ByteArrayOutputStream();
			
			EpmlExport exporter = new EpmlExport();
			exporter.export(po2, output);
			
			String epml = output.toString();
			epml = epml.replaceAll("&", "-");
			return epml;
			
		} catch (IOException e) {
			logger.error("Failed to convert HNet to EPML.", e);
		}
		
		return null;
	}
	
	private void logDetails(String processName, CPF model) {
		logger.debug("Current model {} - N: {}, CNC: {}, CFC: {}", new Object[] {
				processName,
				ComplexityCalculator.getNOAJS(model),
				ComplexityCalculator.getCNC(model),
				ComplexityCalculator.getCFC(model)
		});
	}
	
	public static void main(String[] args) {
		String logsPath = "/home/cn/eps/bpm-demo-new1/bpm-demo/Apromore-Core/apromore-service/data/pm1/t1/logs1";
		new DWSPlainCollectionMiner().mineCollectionFromFolder(logsPath);
	}

}

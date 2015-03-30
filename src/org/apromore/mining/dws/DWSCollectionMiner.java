package org.apromore.mining.dws;

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
import org.prom5.exporting.log.MXMLibPlainLogExport;
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

public class DWSCollectionMiner {
	
	private static final Logger logger = LoggerFactory.getLogger(DWSCollectionMiner.class);
	
	private EPCDeserializer epcDeserializer = new EPCDeserializer();
	
	@Autowired
	private ProcessComplexityChecker complexityChecker;
	
	@Autowired
	private CPFImporter cpfImporter;
	
	@Autowired
	private ProcessSerializer processSerializer;
	
	private double sigma1 = 0.00005; // 0.00005
	private double gamma1 = 0.00001; // 0.00001
	
	private double sigma2 = 0.004; // 0.004
	private double gamma2 = 0.002; // 0.002
	
	private int k = 2; // 2
	private double sigma = sigma1; // 0.004
	private double gamma = gamma1; // 0.002
	private int l = 5; // 5
	private int maxFeatures = 100; // 5 | 100 in new tests
	
//	private int k = 2; // 2
//	private double sigma = 0.004; // 0.004
//	private double gamma = 0.002; // 0.002
//	private int l = 5; // 5
//	private int maxFeatures = 5; // 5
	
	private int noiseThreshold = 1; // 1
	
//	private String logsPath = "logs";
	private String outPath = null;
	private String processesPath = "processes";
	private String processesDataPath = "m_processes_data.csv";
	
	private Map<String, CPF> invalidModels = new HashMap<String, CPF>(); 
	private Map<String, LogReader> addedLogs = new HashMap<String, LogReader>();
	
	public void mineCollectionFromFolder(String inPath, String outPath) {
		initializePaths(outPath);
		String logPath = inPath;
		mineCollection(logPath);
	}
	
	private void initializePaths(String outPath) {

		this.outPath = outPath;
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
	
	public void mineCollection(String logPath) {
		
		File logFile = new File(logPath);
		if (logFile.getName().startsWith("bpi")) {
			maxFeatures = 100;
		} else {
			maxFeatures = 5;
		}
		
		long t1 = System.currentTimeMillis();
		try {
			LogFile lf = LogFile.getInstance(logPath);
			LogReader rootLog = BufferedLogReader.createInstance(null, lf);
			
			Cluster rootCluster = new Cluster(rootLog, "R", k, sigma, gamma, l, maxFeatures);
			
			Queue<Cluster> unprocessedClusters = new LinkedList<Cluster>();
			unprocessedClusters.add(rootCluster);
			
			int currentModelNumber = 0;
			while (!unprocessedClusters.isEmpty()) {
				currentModelNumber++;
				
				Cluster c = unprocessedClusters.poll();
				String processName = "P_" + currentModelNumber;
				LogReader cLog = c.getLog();
				HeuristicsNet hnet = mineHNet(cLog);
				c.setHeuristicsNet(hnet);
				String epmlModel = convertToEPML(hnet, cLog);
				CPF currentModel = epcDeserializer.deserializeInputStream(IOUtils.toInputStream(epmlModel));
				if (MiningConfig.REMOVE_EVENTS) {
					currentModel = removeEvents(currentModel);
				}
				if (logger.isDebugEnabled()) {
					logDetails(processName, currentModel);
				}
				
				if (complexityChecker.isComplexWithBuffer(currentModel)) {
					
					// mined model is complex. divide it into more log clusters.
					logger.debug("{} is complex. Adding next level clusters for the model...", processName);
					boolean populated = populateNextLevelClusters(c, unprocessedClusters);
					if (!populated) {
						logger.debug("Next level clusters are not found for the model {}. Adding the complex model...", 
								processName);
						boolean success = cpfImporter.importModel(processName, currentModel);
						addedLogs.put(processName, cLog);
						if (!success) {
							logger.debug("{} is not valid. Next level clusters could not be generated for this. Adding this to invalid models...", processName);
							invalidModels.put(processName, currentModel);
						}
					}
					
				} else {
					
					// mined model is not complex. add it to the repository.
					// we may need to further cluster the log, if there are errors in adding the model to the
					// repository.
					logger.debug("{} is not complex. Adding it to the repository...", processName);
					boolean success = cpfImporter.importModel(processName, currentModel);
					if (success) {
						addedLogs.put(processName, cLog);
					} else {
						logger.debug("{} is not valid. Adding next level clusters...", processName);
						boolean populated = populateNextLevelClusters(c, unprocessedClusters);
						if (!populated) {
//							addedLogs.put(processName, cLog);
							logger.debug("Next level clusters could not be generated for {}. Adding this to invalid models...", processName);
							invalidModels.put(processName, currentModel);
						}
					}
				}
			}
			
			long duration = System.currentTimeMillis() - t1;
			
			processSerializer.serializeAllProcesses(processesPath);
			
//			serializeModels(invalidModels, processesPath); // TODO: make consistent
			
			String durationLine = "Total time," + ((double)duration / 1000d) + "\n";
			FileUtils.write(new File(outPath, "other_data.csv"), durationLine);
		
			if (MiningConfig.SERIALIZE_LOGS) {
				serializeLogs();
			}

			if (MiningConfig.WRITE_ADDITIONAL_DATA) {
				writeEvalData(duration);
			}
			
		} catch (Exception e) {
			logger.error("Failed to mine a process collection using DWS.", e);
		}
		
		
	}
	
	private void serializeLogs() {
		
		try {
			MXMLibPlainLogExport exporter = new MXMLibPlainLogExport();
			File logsFolder = new File(outPath, "logs");
			if (logsFolder.exists()) {
				FileUtils.cleanDirectory(logsFolder);
			} else {
				logsFolder.mkdir();
			}
			
			for (String processName : addedLogs.keySet()) {
				LogReader l = addedLogs.get(processName);
				
				ByteArrayOutputStream outstream = new ByteArrayOutputStream();
				ProvidedObject o = new ProvidedObject("log", new Object[] {l});
				exporter.export(o, outstream);
				
				String logData = outstream.toString();
				String logName = "log_" + processName + "_" + l.getLogSummary().getNumberOfUniqueProcessInstances() + ".mxml";
				File file = new File(logsFolder, logName);
				FileUtils.write(file, logData);
			}
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
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
		
		if (!MiningConfig.SERIALIZE_INVALID_MODELS) {
			return;
		}
		
		FormattableEPCSerializer epcSerializer = new FormattableEPCSerializer();
		for (String processName : models.keySet()) {
			File modelFile = new File(outPath, "i_" + processName + ".epml");
			CPF cpf = models.get(processName);
			epcSerializer.serialize(cpf, modelFile.getAbsolutePath());
		}
	}
	
	private boolean populateNextLevelClusters(Cluster c, Queue<Cluster> unprocessedClusters) {
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

	private boolean populateNextLevelClustersInternal(Cluster c, Queue<Cluster> unprocessedClusters) {
		
		boolean populated = false;
		
		c.mineFeatures();
		c.mineClusters();
		
		List<Cluster> children = c.getChildren();
		for (Cluster child : children) {
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
}

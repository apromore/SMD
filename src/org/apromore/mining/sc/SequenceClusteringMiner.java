package org.apromore.mining.sc;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apromore.graph.JBPT.CPF;
import org.apromore.mining.ProcessComplexityChecker;
import org.apromore.mining.ProcessMiner;
import org.apromore.mining.complexity.ComplexityCalculator;
import org.apromore.mining.utils.ProcessSerializer;
import org.apromore.service.utils.EPCDeserializer;
import org.apromore.service.utils.FormattableEPCSerializer;
import org.prom5.analysis.sequenceclustering.Cluster;
import org.prom5.analysis.sequenceclustering.SCUI;
import org.prom5.framework.log.LogFile;
import org.prom5.framework.log.LogReader;
import org.prom5.framework.log.LogReaderFactory;
import org.prom5.framework.log.rfb.BufferedLogReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

public class SequenceClusteringMiner {

	private static final Logger logger = LoggerFactory.getLogger(SequenceClusteringMiner.class);
	
	@Autowired
	private ProcessMiner processMiner;
	
	@Autowired
	private ProcessComplexityChecker complexityChecker;
	
	@Autowired
	private CPFImporter cpfImporter;
	
	@Autowired
	private ProcessSerializer processSerializer;
	
	private EPCDeserializer epcDeserializer = new EPCDeserializer();
	
	private int numLevelClusters = 2;
	
	private Map<String, CPF> invalidModels = new HashMap<String, CPF>();
	
	public void mineCollectionFromFolder(String logsPath) {
		File folder = new File(logsPath);
		File[] fs = folder.listFiles();
		String logPath = fs[0].getAbsolutePath();
		mineCollection(logPath);
	}
	
	public void mineCollection(String logPath) {
		
		try {
			LogFile lf = LogFile.getInstance(logPath);
			LogReader rootLog = BufferedLogReader.createInstance(null, lf);
			
			Queue<LogReader> leafLogs = new LinkedList<LogReader>();
			leafLogs.add(rootLog);
		
			int currentModelNumber = 0;
			while (!leafLogs.isEmpty()) {

				currentModelNumber++;
				String processName = "sp_" + currentModelNumber;
				
				// mine a model from a lead log
				LogReader currentLog = leafLogs.poll();
				String epmlModel = processMiner.mineEPC(currentLog);
				
				// check the complexity of the mined model
				CPF currentModel = epcDeserializer.deserializeInputStream(IOUtils.toInputStream(epmlModel));
				if (logger.isDebugEnabled()) {
					logDetails(processName, currentModel);
				}
				
				if (complexityChecker.isComplex(currentModel)) {
					
					// mined model is complex. divide it into more log clusters.
					logger.debug("{} is complex. Adding next level clusters for the model...", processName);
					int status = populateNextLevelClusters(currentLog, leafLogs);
					if (status > 0) {
						// this model cannot be simplified. add the complex model.
						logger.debug("Complex model {} cannot be simplified. Adding the complex model to the repository...", processName);
						boolean success = cpfImporter.importModel(processName, currentModel);
						if (!success) {
							invalidModels.put(processName, currentModel);
						}
					}
					
				} else {
					
					// mined model is not complex. add it to the repository.
					// we may need to further cluster the log, if there are errors in adding the model to the
					// repository.
					logger.debug("{} is not complex. Adding it to the repository...", processName);
					boolean success = cpfImporter.importModel(processName, currentModel);
					if (!success) {
						logger.debug("{} is not valid. Adding next level clusters...", processName);
						int status = populateNextLevelClusters(currentLog, leafLogs);
						if (status > 0) {
							invalidModels.put(processName, currentModel);
						}
					}
				}
			}
			
		} catch (Exception e) {
			logger.error("Failed to mine a process collection based on the log {}.", logPath);
		}
		
		String outPath = "/home/cn/eps/bpm-demo-new1/bpm-demo/Apromore-Core/apromore-service/data/pm1/t1/sc/processes";
		processSerializer.serializeAllProcesses(outPath);
		
		logger.debug("Serializing {} invalid models...", invalidModels.size());
		String outPath2 = "/home/cn/eps/bpm-demo-new1/bpm-demo/Apromore-Core/apromore-service/data/pm1/t1/sc/invalid";
		serializeModels(invalidModels, outPath2);
	}
	
	private int populateNextLevelClusters(LogReader log, Queue<LogReader> leafLogs) throws Exception {
		
		int originalInstanceCount = log.getLogSummary().getNumberOfUniqueProcessInstances();
		if (originalInstanceCount < 2) {
			return 1;
		}
		
//		LogReader currentLog = LogReaderFactory.createInstance(null, log);
		SCUI scui = new SCUI(log, log, 0, 1.0d, numLevelClusters, false);
		List<Cluster> cs = scui.getClusters();
		for (Cluster c : cs) {
			LogReader nextLevelLog = c.getLog();
			int nextLevelInstanceCount = nextLevelLog.getLogSummary().getNumberOfUniqueProcessInstances();
			if (nextLevelInstanceCount == originalInstanceCount) {
				return 2;
			}
			if (nextLevelInstanceCount > 0) {
				leafLogs.add(nextLevelLog);
			}
		}
		return 0;
	}
	
	private void logDetails(String processName, CPF model) {
		logger.debug("Current model {} - N: {}, CNC: {}, CFC: {}", new Object[] {
				processName,
				ComplexityCalculator.getNOAJS(model),
				ComplexityCalculator.getCNC(model),
				ComplexityCalculator.getCFC(model)
		});
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
}

package org.apromore.mining;

import java.util.Map;

import org.prom5.analysis.traceclustering.algorithm.AgglomerativeHierarchicalAlgorithm.MyCluster;
import org.prom5.framework.log.LogReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

public class LogComplexityChecker {
	
	private final Logger logger = LoggerFactory.getLogger(LogComplexityChecker.class);
	
	private int noiseThreshold = MiningConfig.LOG_NOISE_FOR_TC;
	
	// private int maxEventNumber = 300; // TODO: uncomment
	private int maxEventNumber = 600;

	@Autowired
	private ProcessMiner processMiner;
	
	@Autowired
	private AggregatedLogClusterer logClusterer;
	
	public void process(MiningData data) throws Exception {
		
		logger.debug("Checking the complexity of the log {} with {} instances and {} events.", 
				new Object[] {data.getClusterIdAsString(), 
				data.getLog().getLogSummary().getNumberOfProcessInstances(),
				data.getLog().getLogSummary().getModelElements().length});
		
		if (isNoise(data.getLog())) {
			logger.debug("Log {} is noise. Abandoning the log...", data.getClusterIdAsString());
			return;
		}
		
		if (isComplex(data.getLog())) {
			
			logger.debug("Log {} is too complex. Traversing to child trace clusters.", data.getClusterIdAsString());
			Map<MyCluster, LogReader> childLogs = logClusterer.getChildren(data.getLogCluster());
			for (MyCluster logCluster : childLogs.keySet()) {
				LogReader childLog = childLogs.get(logCluster);
				MiningData childData = new MiningData(data);
				childData.setLogCluster(logCluster);
				childData.setLog(childLog);
				this.process(childData);
			}
			
		} else {
			logger.debug("Log {} is not complex. Activating the process miner for the log...", data.getClusterIdAsString());
			processMiner.process(data);
		}
	}

	private boolean isNoise(LogReader log) {
		int numInstances = log.getLogSummary().getNumberOfUniqueProcessInstances();
		if (numInstances < noiseThreshold) {
			return true;
		} else {
			return false;
		}
	}

	public boolean isComplex(LogReader logReader) {
		int numElementTypes = logReader.getLogSummary().getModelElements().length;
		int numInstances = logReader.getLogSummary().getNumberOfUniqueProcessInstances();
		// TODO: start original
		if (numElementTypes > maxEventNumber) {
			return true;
		}
		// TODO: end original
		return false;
	}
}

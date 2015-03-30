package org.apromore.mining;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.apromore.exception.RepositoryException;
import org.prom5.analysis.traceclustering.algorithm.AgglomerativeHierarchicalAlgorithm.MyCluster;
import org.prom5.framework.log.LogFile;
import org.prom5.framework.log.LogReader;
import org.prom5.framework.log.rfb.BufferedLogReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AggregatedLogClusterer {
	
	private static final Logger logger = LoggerFactory.getLogger(AggregatedLogClusterer.class);
	
	private Map<String, LogClusterer> logClusterers = new HashMap<String, LogClusterer>();
	
	public void initialize(Collection<String> logPaths) throws Exception {
		
		logger.debug("Initializing aggregated log clusterer with {} logs.", logPaths.size());
		
		long t1 = System.currentTimeMillis();
		
		for (String logPath : logPaths) {
			
			logger.debug("Reading the log file: {}", logPath);
			
			LogFile lf = LogFile.getInstance(logPath);
			LogReader log = BufferedLogReader.createInstance(null, lf);
			
			LogClusterer logClusterer = new LogClusterer();
			logClusterer.initialize(log);
			logClusterers.put(logClusterer.getId(), logClusterer);
		}
		
		long t2 = System.currentTimeMillis();
		long duration = t2 - t1;
		EvaluatorUtil.addTraceClusteringTime(duration);
	}
	
	public Map<MyCluster, LogReader> getRoots() {
		Map<MyCluster, LogReader> roots = new HashMap<MyCluster, LogReader>();
		for (String logId : logClusterers.keySet()) {
			LogClusterer logClusterer = logClusterers.get(logId);
			roots.put(logClusterer.getRoot(), logClusterer.getRootLog());
		}
		return roots;
	}
	
	public Map<MyCluster, LogReader> getChildren(MyCluster cluster) throws Exception {
		
		String logId = cluster.getGroupId();
		LogClusterer logClusterer = logClusterers.get(logId);
		
		if (logClusterer == null) {
			logger.error("LogClusterer not found for the log cluster {} with group Id: {}", 
					cluster.getID(), cluster.getGroupId());
			throw new RepositoryException("LogClusterer not found for the log cluster");
		}
		
		Map<MyCluster, LogReader> children = logClusterer.getChildren(cluster);
		return children;
	}
	
	public LogReader getLog(MyCluster cluster) throws Exception {
		
		String logId = cluster.getGroupId();
		LogClusterer logClusterer = logClusterers.get(logId);
		
		if (logClusterer == null) {
			logger.error("LogClusterer not found for the log cluster {} with group Id: {}", 
					cluster.getID(), cluster.getGroupId());
			throw new RepositoryException("LogClusterer not found for the log cluster");
		}
		
		LogReader log = logClusterer.getLog(cluster);
		return log;
	}
	
	public boolean hasChildren(MyCluster cluster) {
		if (cluster.getLeft() == null) {
			return false;
		}
		
		return true;
	}
	
	

}

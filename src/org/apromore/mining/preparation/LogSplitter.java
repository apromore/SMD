package org.apromore.mining.preparation;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

import org.apache.commons.io.FileUtils;
import org.apromore.mining.LogClusterer;
import org.prom5.analysis.traceclustering.algorithm.AgglomerativeHierarchicalAlgorithm.MyCluster;
import org.prom5.exporting.log.MXMLibPlainLogExport;
import org.prom5.framework.log.LogFile;
import org.prom5.framework.log.LogReader;
import org.prom5.framework.log.rfb.BufferedLogReader;
import org.prom5.framework.plugin.ProvidedObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class LogSplitter {
	
	private static final Logger logger = LoggerFactory.getLogger(LogSplitter.class);
	
	private int maxElementTypes = 1000;
	private int noiseThreshold = 2;
	
	private StringBuffer hierarchy = new StringBuffer();
	
	public static void main(String[] args) {
		String logPath = "/home/cn/eps/bpm-demo-new1/bpm-demo/Apromore-Core/apromore-service/data/pm1/t2/logs/clog2_filtered_min3.mxml";
		String outPath = "/home/cn/eps/bpm-demo-new1/bpm-demo/Apromore-Core/apromore-service/data/pm1/t2/splitted1";
		try {
			new LogSplitter().split(logPath, outPath);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void split(String logPath, String outPath) throws Exception {
		
		logger.debug("Cleaning the out put folder: {}", outPath);
		FileUtils.cleanDirectory(new File(outPath));
		
		logger.debug("Reading the log file: {}", logPath);
		
		LogFile lf = LogFile.getInstance(logPath);
		LogReader log = BufferedLogReader.createInstance(null, lf);
		logger.debug("Log contains {} process instances and {} event types.", 
				log.getLogSummary().getNumberOfUniqueProcessInstances(), log.getLogSummary().getModelElements().length);
		
		MXMLibPlainLogExport exporter = new MXMLibPlainLogExport();
		Map<MyCluster, LogReader> logs = split(log);
		
		File hfile = new File(outPath, "h.txt");
		logger.debug("Writting log cluster hierarchy to {}", hfile.getAbsolutePath());
		String h = getHierarchy();
		FileUtils.write(hfile, h);
		
		for (MyCluster c : logs.keySet()) {
			LogReader l = logs.get(c);
			String logName = getLogFileName(c, l);
			
			logger.debug("Writting log file: {}", logName);
			
			ByteArrayOutputStream outstream = new ByteArrayOutputStream();
			ProvidedObject o = new ProvidedObject("log", new Object[] {l});
			exporter.export(o, outstream);
			
			String logData = outstream.toString();
			File file = new File(outPath, logName);
			FileUtils.write(file, logData);
		}
	}
	
	private String getLogFileName(MyCluster c, LogReader l) {
		int numElementTypes = l.getLogSummary().getModelElements().length;
		int numInstances = l.getLogSummary().getNumberOfUniqueProcessInstances();
		String name = "log_" + c.getGroupId() + "_" + c.getID() + "_" + numInstances + "_" + numElementTypes + ".mxml";
		return name;
	}
	
	public Map<MyCluster, LogReader> split(LogReader log) throws Exception {
		
		Map<MyCluster, LogReader> logs = new HashMap<MyCluster, LogReader>();
		
		LogClusterer logClusterer = new LogClusterer();
		logClusterer.initialize(log);
		
		MyCluster rootCluster = logClusterer.getRoot();
		
		Queue<MyCluster> q = new LinkedList<MyCluster>();
		q.add(rootCluster);
		
		while (!q.isEmpty()) {
			
			MyCluster c = q.poll();
			LogReader l = logClusterer.getLog(c);
			
			if (isNoise(l)) {
				continue;
			}
			
			if (isComplex(l)) {
				Map<MyCluster, LogReader> children = logClusterer.getChildren(c);
				for (MyCluster child : children.keySet()) {
					hierarchy.append(child.getID() + " - " + c.getID() + "\n");
					q.add(child);
				}
			} else {
				logs.put(c, l);
			}
		}
		
		return logs;
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
		if (numElementTypes > maxElementTypes || numInstances > 2500) {
			return true;
		}
		return false;
	}
	
	public String getHierarchy() {
		return hierarchy.toString();
	}
}

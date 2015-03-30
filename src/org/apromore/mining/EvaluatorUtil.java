package org.apromore.mining;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.prom5.analysis.traceclustering.algorithm.AgglomerativeHierarchicalAlgorithm.MyCluster;
import org.prom5.exporting.log.MXMLibPlainLogExport;
import org.prom5.framework.log.LogReader;
import org.prom5.framework.plugin.ProvidedObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EvaluatorUtil {
	
	private static final Logger logger = LoggerFactory.getLogger(EvaluatorUtil.class);
	
	private static String outPath = "/home/cn/eps/bpm-demo-new1/bpm-demo/Apromore-Core/apromore-service/data/pm1/t1/temp";
	
	private static long miningTime = 0;
	private static long traceClusteringTime = 0;
	private static long gedMatrixTime = 0;
	private static long cloneDetectionTime = 0;
	private static long refacTime = 0;
	private static long mergingTime = 0;
	
	private static int totalMergingAttempts = 0;
	private static int mergeCacheHits = 0;
	private static int totalNewMerges = 0;
	private static int totalDiscardedMerges = 0;
	private static int totalFragmentsMerged = 0;
	
	// internal variables
	private static long gedStart = 0;
	private static long clonesStart = 0;
	private static long refacStart = 0;
	private static long mergingStart = 0;
	
	private static Map<String, MyCluster> addedLogClusters = new HashMap<String, MyCluster>();
	
	private static List<Integer[]> stepData = new ArrayList<Integer[]>();
	
	public static void init(String filePath) {
		clear();
		outPath = filePath;
	}
	
	public static void clear() {
		miningTime = 0;
		traceClusteringTime = 0;
		gedMatrixTime = 0;
		cloneDetectionTime = 0;
		refacTime = 0;
		mergingTime = 0;
		
		gedStart = 0;
		clonesStart = 0;
		refacStart = 0;
		mergingStart = 0;
		
		totalMergingAttempts = 0;
		totalDiscardedMerges = 0;
		totalFragmentsMerged = 0;
		mergeCacheHits = 0;
		totalNewMerges = 0;
		
		stepData.clear();
	}
	
	public static void addStepData(int totalProcesses, int complexProcesses, int totalExceededSize) {
		Integer[] data = new Integer[] {totalProcesses, complexProcesses, totalExceededSize};
		stepData.add(data);
	}
	
	public static void incrementMergingAttempts() {
		totalMergingAttempts++;
	}
	
	public static void incrementNewMerges() {
		totalNewMerges++;
	}
	
	public static void incrementMergeCacheHits() {
		mergeCacheHits++;
	}
	
	public static void incrementDiscardedMerges() {
		totalDiscardedMerges++;
	}
	
	public static void addMergedFragments(int n) {
		totalFragmentsMerged += n;
	}
	
	public static void writeData(AggregatedLogClusterer logClusterer, long totalTime) {
		
		File dataFile = new File(outPath, "other_data.csv");
		String outFilePath = dataFile.getAbsolutePath();
		
		File logsFolder = new File(outPath, "logs");
		try {
			if (logsFolder.exists()) {
				FileUtils.cleanDirectory(logsFolder);
			} else {
				logsFolder.mkdir();
			}
		} catch (IOException e1) {
			logger.error("Failed to clean the logs folder.");
		}
		
		logger.debug("Writing evaluation data to {}", outFilePath);
		
		MXMLibPlainLogExport exporter = new MXMLibPlainLogExport();
		StringBuffer b = new StringBuffer();
		b.append("Total time," + ((double)totalTime / 1000d) + "\n");
		b.append("Mining time," + ((double)miningTime / 1000d) + "\n");
		b.append("TC time," + ((double)traceClusteringTime / 1000d) + "\n");
		b.append("GED time," + ((double)gedMatrixTime / 1000d) + "\n");
		b.append("AP time," + ((double)cloneDetectionTime / 1000d) + "\n");
		b.append("Merge time," + ((double)mergingTime / 1000d) + "\n");
		b.append("Refac time," + ((double)refacTime / 1000d) + "\n\n");
		b.append("Merge attempts," + totalMergingAttempts + "\n");
		b.append("Merge cache hits," + mergeCacheHits + "\n");
		b.append("New merges," + totalNewMerges + "\n");
		b.append("Discarded merges," + totalDiscardedMerges + "\n");
		b.append("Total fragments merged," + totalFragmentsMerged + "\n");
		b.append("Num fragments per merge," + ((double)totalFragmentsMerged / (double)totalMergingAttempts) + "\n\n");
		
		b.append("Total processes, Complex processes, Exceeded size\n");
		for (Integer[] data : stepData) {
			b.append(data[0] + "," + data[1] + "," + data[2] + "\n");
		}
		
		b.append("\n\nLog cluster Id, Process name, Process instances, Events\n");
		for (String processName : addedLogClusters.keySet()) {
			MyCluster c = addedLogClusters.get(processName);
			try {
				LogReader l = logClusterer.getLog(c);
				if (l != null) {
					int instances = l.getLogSummary().getNumberOfUniqueProcessInstances();
					int events = l.getLogSummary().getModelElements().length;
					b.append(c.getID() + "," + processName + "," + instances + "," + events + "\n");
					
					if (MiningConfig.SERIALIZE_LOGS) {
						ByteArrayOutputStream outstream = new ByteArrayOutputStream();
						ProvidedObject o = new ProvidedObject("log", new Object[] {l});
						exporter.export(o, outstream);
						
						String logData = outstream.toString();
						File file = new File(logsFolder, getLogFileName(c, l));
						FileUtils.write(file, logData);
					}
					
				} else {
					logger.error("No log associated with the log cluster: {}", c.getID());
				}
			} catch (Exception e) {
				logger.error("Failed to get log of the log cluster {} for evaluation purpose.", c.getID());
			}
		}
		
		File outFile = new File(outFilePath);
		try {
			FileUtils.write(outFile, b.toString());
		} catch (IOException e) {
			logger.error("Failed to write evaluation data to file: {}", outFile.getAbsolutePath());
			logger.debug(b.toString());
		}
	}
	
	private static String getLogFileName(MyCluster c, LogReader l) {
		int numElementTypes = l.getLogSummary().getModelElements().length;
		int numInstances = l.getLogSummary().getNumberOfUniqueProcessInstances();
		String name = "log_" + c.getGroupId() + "_" + c.getID() + "_" + numInstances + "_" + numElementTypes + ".mxml";
		return name;
	}
	
//	public static void addClusters(Collection<MyCluster> cs) {
//		for (MyCluster c : cs) {
//			addedLogClusters.add(c);
//		}
//	}
	
	public static void addCluster(String processName, MyCluster c) {
		addedLogClusters.put(processName, c);
	}
	
	public static void removeCluster(String processName) {
		addedLogClusters.remove(processName);
	}
	
	public static long getMiningTime() {
		return miningTime;
	}
	
	public static void addMiningTime(long time) {
		miningTime += time;
	}

	public static long getTraceClusteringTime() {
		return traceClusteringTime;
	}
	
	public static void addTraceClusteringTime(long time) {
		traceClusteringTime += time;
	}

	public static long getGedMatrixTime() {
		return gedMatrixTime;
	}
	
	public static void addGEDMatrixTime(long time) {
		gedMatrixTime += time;
	}
	
	public static void gedStart() {
		gedStart = System.currentTimeMillis();
	}
	
	public static void gedEnd() {
		long gedEnd = System.currentTimeMillis();
		long duration = gedEnd - gedStart;
		gedMatrixTime += duration;
	}

	public static long getCloneDetectionTime() {
		return cloneDetectionTime;
	}
	
	public static void addCloneDetectionTime(long time) {
		cloneDetectionTime += time;
	}
	
	public static void cloneDetectionStart() {
		clonesStart = System.currentTimeMillis();
	}
	
	public static void cloneDetectionEnd() {
		long clonesEnd = System.currentTimeMillis();
		long duration = clonesEnd - clonesStart;
		cloneDetectionTime += duration;
	}

	public static long getRefacTime() {
		return refacTime;
	}
	
	public static void addRefacTime(long time) {
		refacTime += time;
	}
	
	public static void refacStart() {
		refacStart = System.currentTimeMillis();
	}
	
	public static void refacEnd() {
		long refacEnd = System.currentTimeMillis();
		long duration = refacEnd - refacStart;
		refacTime += duration;
	}
	
	public static void mergingStart() {
		mergingStart = System.currentTimeMillis();
	}
	
	public static void mergingEnd() {
		long mergingEnd = System.currentTimeMillis();
		long duration = mergingEnd - mergingStart;
		mergingTime += duration;
	}

	public static int getTotalMergingAttempts() {
		return totalMergingAttempts;
	}

	public static int getMergeCacheHits() {
		return mergeCacheHits;
	}

	public static int getTotalNewMerges() {
		return totalNewMerges;
	}

	public static int getTotalFragmentsMerged() {
		return totalFragmentsMerged;
	}

	public static long getMergingTime() {
		return mergingTime;
	}
}

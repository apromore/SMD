package org.apromore.mining.utils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.prom5.analysis.traceclustering.algorithm.AgglomerativeHierarchicalAlgorithm.MyCluster;
import org.prom5.exporting.log.MXMLibPlainLogExport;
import org.prom5.framework.log.LogReader;
import org.prom5.framework.plugin.ProvidedObject;

public class MiningUtils {
	
	public static String logsFolder = "/home/chathura/projects/qut/temp/f2";
	private static int logCount = 1;
	
	public static String getProcessName(int logClusterId) {
		return "p_" + logClusterId;
	}
	
	public static int getLogClusterId(String processName) {
		String[] parts = processName.split("_");
		int logClusterId = Integer.parseInt(parts[1]);
		return logClusterId;
	}
	
	public static void serializeLog(LogReader log, String logFileName) throws Exception {
		MXMLibPlainLogExport exporter = new MXMLibPlainLogExport();
		ByteArrayOutputStream outstream = new ByteArrayOutputStream();
		ProvidedObject o = new ProvidedObject("log", new Object[] {log});
		exporter.export(o, outstream);
		
		String logData = outstream.toString();
		File file = new File(logsFolder, logFileName);
		FileUtils.write(file, logData);
	}
	
	public static String getLogFileName(MyCluster c, LogReader l) {
		int numElementTypes = l.getLogSummary().getModelElements().length;
		int numInstances = l.getLogSummary().getNumberOfUniqueProcessInstances();
		String name = "log_id_" + logCount + "_" + c.getGroupId() + "_" + c.getID() + "_instances_" + numInstances + "_classes_" + numElementTypes + ".mxml";
		logCount++;
		return name;
	}

}

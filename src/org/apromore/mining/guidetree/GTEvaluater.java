package org.apromore.mining.guidetree;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apromore.mining.MiningConfig;
import org.deckfour.xes.info.XLogInfo;
import org.deckfour.xes.info.XLogInfoFactory;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.out.XMxmlSerializer;
import org.deckfour.xes.out.XSerializer;
import org.prom6.plugins.guidetreeminer.MineGuideTree;
import org.prom6.plugins.guidetreeminer.tree.GuideTreeNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GTEvaluater {
	
	private static final Logger logger = LoggerFactory.getLogger(GTEvaluater.class);
	
//	private static final String logsPath = "/home/cn/eps/bpm-demo-new1/bpm-demo/Apromore-Core/apromore-service/data/pm1/t1/guidetree/logs";
//	private static final String outPath = "/home/cn/eps/bpm-demo-new1/bpm-demo/Apromore-Core/apromore-service/data/pm1/t1/guidetree/gt_data.csv";
	
	private static Set<GuideTreeNode> nodes = new HashSet<GuideTreeNode>();
	
	public static void addNode(GuideTreeNode node) {
		nodes.add(node);
	}
	
	public static void writeData(MineGuideTree gt, String outPathBase, long totalTime) {
		
		logger.debug("Writing evaluation data about {} nodes to {}", nodes.size(), outPathBase);
		
		StringBuffer b = new StringBuffer();
		b.append("Total time," + ((double)totalTime / 1000d) + "\n");
		b.append("Log cluster Id, Process instances, Events\n");
		
		File logsFolder = new File(outPathBase, "logs");
		try {
			if (logsFolder.exists()) {
				FileUtils.cleanDirectory(logsFolder);
			} else {
				logsFolder.mkdir();
			}
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		
		int id = 0;
		for (GuideTreeNode node : nodes) {
			id++;
			XLog l = gt.getLog(node);
			if (l != null) {
				XLogInfo logInfo = XLogInfoFactory.createLogInfo(l);
				int instances = logInfo.getNumberOfTraces();
				int events = logInfo.getEventClasses().size();
				b.append(id + "," + instances + "," + events + "\n");
				
				if (MiningConfig.SERIALIZE_LOGS) {
					try {
						String logFileName = id + "_" + instances + ".mxml";
						File outFile = new File(logsFolder, logFileName);
						FileOutputStream out = new FileOutputStream(outFile);
						XSerializer logSerializer = new XMxmlSerializer();
						logSerializer.serialize(l, out);
						out.close();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}
		
		File outFile = new File(outPathBase, "other_data.csv");
		try {
			FileUtils.write(outFile, b.toString());
		} catch (IOException e) {
			logger.error("Failed to write evaluation data to file: {}", outFile.getAbsolutePath());
			logger.debug(b.toString());
		}
	}

}

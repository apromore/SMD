package org.apromore.mining.guidetree;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.Collection;

import org.deckfour.xes.factory.XFactory;
import org.deckfour.xes.factory.XFactoryNaiveImpl;
import org.deckfour.xes.in.XMxmlParser;
import org.deckfour.xes.in.XParser;
import org.deckfour.xes.info.XLogInfo;
import org.deckfour.xes.info.XLogInfoFactory;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.deckfour.xes.out.XMxmlSerializer;
import org.deckfour.xes.out.XSerializer;
import org.deckfour.xes.util.progress.XMonitoredInputStream;
import org.deckfour.xes.util.progress.XProgressListener;
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
import org.prom6.plugins.guidetreeminer.GuideTreeMinerInput;
import org.prom6.plugins.guidetreeminer.MineGuideTree;
import org.prom6.plugins.guidetreeminer.tree.GuideTree;
import org.prom6.plugins.guidetreeminer.tree.GuideTreeNode;
import org.prom6.plugins.guidetreeminer.types.AHCJoinType;
import org.prom6.plugins.guidetreeminer.types.DistanceMetricType;
import org.prom6.plugins.guidetreeminer.types.GTMFeatureType;
import org.prom6.plugins.guidetreeminer.types.LearningAlgorithmType;
import org.prom6.plugins.guidetreeminer.types.SimilarityDistanceMetricType;

public class PromTest1 {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			new PromTest1().work3();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void work3() throws FileNotFoundException {
		String logPath = "/home/cn/eps/bpm-demo-new1/bpm-demo/Apromore-Core/apromore-service/data/pm1/t1/logs1/log_with_system_subject2_filtered_min3.mxml";
		File logFile = new File(logPath);
		BufferedInputStream input = new BufferedInputStream(new FileInputStream(logFile));
		long fileSizeInBytes = logFile.length();
		
		XFactory factory = new XFactoryNaiveImpl();
		XParser parser = new XMxmlParser(factory);
		Collection<XLog> logs = null;
		try {
//			logs = parser.parse(new XContextMonitoredInputStream(input, fileSizeInBytes, context.getProgress()));
			
			logs = (new XMxmlParser()).parse(new XMonitoredInputStream(input, fileSizeInBytes,
					new XProgressListener() {

						public boolean isAborted() {
							return false;
						}

						public void updateProgress(int arg0, int arg1) {

						}
					}));
		} catch (Exception e) {
			logs = null;
		}
		
		XLog log = null;
		if (logs != null) {
			log = logs.iterator().next();
			System.out.println(log.size());
			XLogInfo logInfo = XLogInfoFactory.createLogInfo(log);
			System.out.println(logInfo.getEventClasses().size());
			System.out.println(logInfo.getNumberOfTraces());
		}
	}
	
	public void work2() throws Exception {
		
//		String logPath = "/home/cn/eps/prom6/svn/data/t1/logs/log_L_1_8553_1568_249.mxml";
		String logPath = "/home/cn/eps/bpm-demo-new1/bpm-demo/Apromore-Core/apromore-service/data/pm1/t1/logs1/log_with_system_subject2_filtered_min3.mxml";
		File logFile = new File(logPath);
		BufferedInputStream input = new BufferedInputStream(new FileInputStream(logFile));
		long fileSizeInBytes = logFile.length();
		
		XFactory factory = new XFactoryNaiveImpl();
		XParser parser = new XMxmlParser(factory);
		Collection<XLog> logs = null;
		try {
//			logs = parser.parse(new XContextMonitoredInputStream(input, fileSizeInBytes, context.getProgress()));
			
			logs = (new XMxmlParser()).parse(new XMonitoredInputStream(input, fileSizeInBytes,
					new XProgressListener() {

						public boolean isAborted() {
							return false;
						}

						public void updateProgress(int arg0, int arg1) {

						}
					}));
		} catch (Exception e) {
			logs = null;
		}
		
		XLog log = null;
		if (logs != null) {
			log = logs.iterator().next();
			System.out.println(log.size());
		}
		
		GuideTreeMinerInput gtinput = new GuideTreeMinerInput();
		gtinput.setFeatureType(GTMFeatureType.Sequence);
		gtinput.setMinFrequencyCountThreshold(2);
		gtinput.setMinInstancePercentageCountThreshold(2);
		gtinput.setMinAlphabetSizeThreshold(0);
		gtinput.setMaxAlphabetSizeThreshold(1000);
		gtinput.setNominalFeatureCount(true);
		gtinput.setBaseFeatures(true);
		gtinput.addFeature("Tandem Repeat");
		gtinput.addFeature("Maximal Repeat");
		gtinput.setNumberOfClusters(5);
		
		gtinput.setSimilarityDistanceMetricType(SimilarityDistanceMetricType.Distance);
		gtinput.setDistanceMetricType(DistanceMetricType.Euclidean);
		
		gtinput.setLearningAlgorithmType(LearningAlgorithmType.AHC);
		gtinput.setAhcJoinType(AHCJoinType.CompleteLinkage);
		
		
		DummyContext context = new DummyContext();
		MineGuideTree mineGuideTree = new MineGuideTree();
		Object[] results = mineGuideTree.mine(context, gtinput, log);
		
		GuideTree gt = (GuideTree) results[0];
		GuideTreeNode root = gt.getRoot();
		System.out.println("Num children: " + root.getRight().getNoChildren());
		
		XLog nl = mineGuideTree.getLog(root);
		
		String logFilePath = "/home/cn/eps/bpm-demo-new1/bpm-demo/Apromore-Core/apromore-service/data/pm1/t1/temp/temp1/l1.mxml";
		File outFile = new File(logFilePath);
		FileOutputStream out = new FileOutputStream(outFile);
		XSerializer logSerializer = new XMxmlSerializer();
		logSerializer.serialize(nl, out);
		out.close();
		
		LogFile lf = LogFile.getInstance(logFilePath);
		LogReader log1 = BufferedLogReader.createInstance(null, lf);
		
		HeuristicsMiner miner = new HeuristicsMiner();
		HeuristicsMinerParameters params = miner.getParameters();
		params.setAndThreshold(0.1);
		
		HeuristicsNetResult result = (HeuristicsNetResult) miner.mine(log1);
		HeuristicsNet net = result.getHeuriticsNet();
		
		ProvidedObject po = new ProvidedObject("hm results", new Object[] {net, log1});
		HNNetToEPCConverter converter = new HNNetToEPCConverter();
		EPCResult epcResult = (EPCResult) converter.convert(po);
		
		ConfigurableEPC epc = epcResult.getEPC();
		ProvidedObject po2 = new ProvidedObject("epc result", new Object[] {epc});
		String outFilePath = "/home/cn/eps/bpm-demo-new1/bpm-demo/Apromore-Core/apromore-service/data/pm1/t1/temp/temp1/el1.epml";
		FileOutputStream output;
		try {
			output = new FileOutputStream(new File(outFilePath));
			EpmlExport exporter = new EpmlExport();
			exporter.export(po2, output);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
//		DummyContext context = new DummyContext();
//		HeuristicsMiner hminer = new HeuristicsMiner(context, log);
//		HeuristicsNet hnet = hminer.mine();
//		System.out.println(hnet.getFitness());
		
//		Object[] pnresults = HeuristicsNetToPetriNetConverter.converter(context, hnet);
//		Petrinet pnet = (Petrinet) pnresults[0];
//		System.out.println(pnet.getNodes().size());
//		
//		ConfigurableEPC epc = EpcConversion.convert(pnet);
//		System.out.println(epc.getFunctions().size());
		
	}
	
	public void work() throws FileNotFoundException {
		
//		String logPath = "/home/cn/eps/prom6/svn/data/t1/logs/log_L_1_8553_1568_249.mxml";
		String logPath = "/home/cn/eps/prom6/svn/data/t1/logs/log_L_1_8578_238_23.mxml";
		File logFile = new File(logPath);
		BufferedInputStream input = new BufferedInputStream(new FileInputStream(logFile));
		long fileSizeInBytes = logFile.length();
		
		XFactory factory = new XFactoryNaiveImpl();
		XParser parser = new XMxmlParser(factory);
		Collection<XLog> logs = null;
		try {
//			logs = parser.parse(new XContextMonitoredInputStream(input, fileSizeInBytes, context.getProgress()));
			
			logs = (new XMxmlParser()).parse(new XMonitoredInputStream(input, fileSizeInBytes,
					new XProgressListener() {

						public boolean isAborted() {
							return false;
						}

						public void updateProgress(int arg0, int arg1) {

						}
					}));
		} catch (Exception e) {
			logs = null;
		}
		
		XLog log = null;
		if (logs != null) {
			log = logs.iterator().next();
			System.out.println(log.size());
		}
		
		GuideTreeMinerInput gtinput = new GuideTreeMinerInput();
		gtinput.setFeatureType(GTMFeatureType.Sequence);
		gtinput.setMinFrequencyCountThreshold(2);
		gtinput.setMinInstancePercentageCountThreshold(2);
		gtinput.setMinAlphabetSizeThreshold(0);
		gtinput.setMaxAlphabetSizeThreshold(1000);
		gtinput.setNominalFeatureCount(true);
		gtinput.setBaseFeatures(true);
		gtinput.addFeature("Tandem Repeat");
		gtinput.addFeature("Maximal Repeat");
		gtinput.setNumberOfClusters(5);
		
		gtinput.setSimilarityDistanceMetricType(SimilarityDistanceMetricType.Distance);
		gtinput.setDistanceMetricType(DistanceMetricType.Euclidean);
		
		gtinput.setLearningAlgorithmType(LearningAlgorithmType.AHC);
		gtinput.setAhcJoinType(AHCJoinType.CompleteLinkage);
		
		
		DummyContext context = new DummyContext();
		MineGuideTree mineGuideTree = new MineGuideTree();
		Object[] results = mineGuideTree.mine(context, gtinput, log);
		
		GuideTree gt = (GuideTree) results[0];
		GuideTreeNode root = gt.getRoot();
		System.out.println("Num children: " + root.getRight().getNoChildren());
		
		XLog nl = mineGuideTree.getLog(root.getRight());
		for (int i = 0; i < nl.size(); i++) {
			XTrace t = nl.get(i);
			String st = "";
			for (int k = 0; k < t.size(); k++) {
				XEvent e = t.get(k);
				st += e.getAttributes().get("concept:name") + " || ";
			}
			System.out.println(st);
		}
		
//		HeuristicsMiner hminer = new HeuristicsMiner(context, nl);
//		HeuristicsNet hnet = hminer.mine();
//		System.out.println(hnet.getFitness());
//		
//		Object[] pnresults = HeuristicsNetToPetriNetConverter.converter(context, hnet);
//		Petrinet pnet = (Petrinet) pnresults[0];
//		System.out.println(pnet.getNodes().size());
//		
//		ConfigurableEPC epc = EpcConversion.convert(pnet);
//		System.out.println(epc.getFunctions().size());
	}
	
	

}

/**
 * 
 */
package org.apromore.service.mining;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

import org.apromore.mining.AHCWrapper;
//import org.processmining.analysis.sequenceclustering.Cluster;
import org.prom5.analysis.dws.Cluster;
import org.prom5.analysis.dws.Feature;
import org.prom5.analysis.sequenceclustering.SCUI;
import org.prom5.analysis.traceclustering.algorithm.ClusteringInput;
import org.prom5.analysis.traceclustering.algorithm.AgglomerativeHierarchicalAlgorithm.MyCluster;
import org.prom5.analysis.traceclustering.distance.DistanceMetric;
import org.prom5.analysis.traceclustering.distance.EuclideanDistance;
import org.prom5.analysis.traceclustering.profile.ActivityProfile;
import org.prom5.analysis.traceclustering.profile.AggregateProfile;
import org.prom5.analysis.traceclustering.profile.Profile;
import org.prom5.converting.HNNetToEPCConverter;
import org.prom5.exporting.epcs.EpmlExport;
import org.prom5.exporting.log.MXMLibPlainLogExport;
import org.prom5.framework.log.LogFile;
import org.prom5.framework.log.LogReader;
import org.prom5.framework.log.LogReaderFactory;
import org.prom5.framework.log.classic.LogReaderClassic;
import org.prom5.framework.log.rfb.BufferedLogReader;
import org.prom5.framework.models.epcpack.ConfigurableEPC;
import org.prom5.framework.models.heuristics.HeuristicsNet;
import org.prom5.framework.plugin.ProvidedObject;
import org.prom5.mining.epcmining.EPCResult;
import org.prom5.mining.heuristicsmining.HeuristicsMiner;
import org.prom5.mining.heuristicsmining.HeuristicsMinerParameters;
import org.prom5.mining.heuristicsmining.HeuristicsNetResult;

/**
 * @author Chathura C. Ekanayake
 *
 */
public class PT1 {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		new PT1().test4();
	}
	
	public void test4() {
		try {
			String logFilePath = "/home/cn/eps/bpm-demo-new1/bpm-demo/Apromore-Core/apromore-service/data/pm1/t1/logs/88.mxml";
			LogFile lf = LogFile.getInstance(logFilePath);
			LogReader log1 = BufferedLogReader.createInstance(null, lf);
			
			HeuristicsMiner miner = new HeuristicsMiner();
			HeuristicsMinerParameters params = miner.getParameters();
			params.setAndThreshold(0.1);
			
			HeuristicsNetResult result = (HeuristicsNetResult) miner.mine(log1);
			HeuristicsNet net = result.getHeuriticsNet();
			
			Cluster root = new Cluster(log1, "R", 3, 0.05, 0.01, 5, 5);
			root.setHeuristicsNet(net);
			root.mineFeatures();
			root.mineClusters();
			
			List<Feature> features = root.getFeatures();
			System.out.println("Root features: " + features.size());
			for (Feature f : features) {
//				System.out.println(f.toString() + " | " + f.occurrences(log1, net));
			}
						
			List<Cluster> cs = root.getChildren();
			for (Cluster c : cs) {
				
				LogReader childLog = c.getLog();
				HeuristicsNetResult result2 = (HeuristicsNetResult) miner.mine(childLog);
				HeuristicsNet childNet = result.getHeuriticsNet();
				
				c.setHeuristicsNet(childNet);
				c.mineFeatures();
				c.mineClusters();
				List<Cluster> ccs = c.getChildren();
				System.out.println("Child: " + c.getName() + " | Features: " + c.getFeatures().size());
				System.out.println("Child clusters: " + ccs.size());
			}
			
//			System.out.println(cs.size());
			
		} catch (Exception e) {
			e.printStackTrace();
		} 
	}
	
//	public void test3() {
//		
//		String logFilePath = "/home/cn/eps/bpm-demo-new1/bpm-demo/Apromore-Core/apromore-service/data/pm1/t1/logs/88.mxml";
////		String logFilePath = "/home/cn/eps/bpm-demo-new1/bpm-demo/Apromore-Core/apromore-service/data/pm1/t1/logs/running-example.mxml";
//		String outFilePath = "/home/cn/projects/testarea/data/models/e2.epml";
//		
//		try {
//			LogFile lf = LogFile.getInstance(logFilePath);
//			LogReader log1 = BufferedLogReader.createInstance(null, lf);
//			
//			System.out.println(log1.getLogSummary().getNumberOfUniqueProcessInstances());
//			System.out.println(log1.numberOfInstances());
//			SCUI scui = new SCUI(log1, log1, 2, 1000, 10, false);
//			
//			Thread.sleep(5000);
//
//			List<Cluster> cs = scui.getClusters();
//			System.out.println("Got clusters: " + cs.size());
//			for (Cluster c : cs) {
//				int n = c.getLog().getLogSummary().getNumberOfUniqueProcessInstances();
//				System.out.println("C: " + n);
//			}
//			
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//		
//	}
	
	public void test2() {
//		String logFilePath = "/home/cn/projects/testarea/data/logs/running-example.mxml";
		String logFilePath = "/home/cn/projects/testarea/data/lcs/log_1.mxml";
		String outFilePath = "/home/cn/projects/testarea/data/models/e2.epml";
		
		LogFile lf = LogFile.getInstance(logFilePath);
		LogReader lr = LogReaderClassic.createInstance(null, lf);
		
		HeuristicsMiner miner = new HeuristicsMiner();
		HeuristicsMinerParameters params = miner.getParameters();
		params.setAndThreshold(0.1);
		
		HeuristicsNetResult result = (HeuristicsNetResult) miner.mine(lr);
		HeuristicsNet net = result.getHeuriticsNet();
		
		ProvidedObject po = new ProvidedObject("hm results", new Object[] {net, lr});
		HNNetToEPCConverter converter = new HNNetToEPCConverter();
		EPCResult epcResult = (EPCResult) converter.convert(po);
		
		ConfigurableEPC epc = epcResult.getEPC();
		ProvidedObject po2 = new ProvidedObject("epc result", new Object[] {epc});
		FileOutputStream output;
		try {
			output = new FileOutputStream(new File(outFilePath));
			EpmlExport exporter = new EpmlExport();
			exporter.export(po2, output);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void test1() {
		
		String outFolder = "/home/cn/projects/testarea/data/lcs";
		String logFilePath = "/home/cn/projects/testarea/data/logs/88.mxml";
//		String logFilePath = "/home/cn/projects/testarea/data/lcs/log_1.mxml";
		LogFile lf = LogFile.getInstance(logFilePath);
		
//		LogReader lr = LogReaderClassic.createInstance(null, lf);
		
		try {
			LogReader lr = BufferedLogReader.createInstance(null, lf);
			System.out.println(lr.getLogSummary());
			System.out.println(lr.numberOfInstances());
			
			System.out.println("Creating profiles...");
			Profile p1 = new ActivityProfile(lr);
			p1.setNormalizationMaximum(1.0d);
			AggregateProfile ap = new AggregateProfile(lr);
			ap.addProfile(p1);
			
			System.out.println("Doing the trace clustering...");
			DistanceMetric metric = new EuclideanDistance();
			ClusteringInput input = new ClusteringInput(ap, metric);
			AHCWrapper algorithm = new AHCWrapper();
			algorithm.setInput(input);
			algorithm.setClusteringMethod("Complete linkage");
			algorithm.doCluster();
			
			System.out.println("Identifying clusters with at most 30 traces...");
			List<MyCluster> cs = new ArrayList<MyCluster>();
			List<MyCluster> noise = new ArrayList<MyCluster>();
			MyCluster c1 = algorithm.getRoot();
			traverse(c1, cs, noise, 30);
			
			System.out.println("Retrieving traces of identified clusters...");
			MXMLibPlainLogExport logExporter = new MXMLibPlainLogExport();
			int clusterNumber = 0;
			for (MyCluster c : cs) {
				clusterNumber++;
				List<Integer> traceIds = getTraceIds(c);
				System.out.println(c.getID() + " - " + traceIds.size());
				
				int[] tids = new int[traceIds.size()];
				for (int i = 0; i < traceIds.size(); i++) {
					Integer tid = traceIds.get(i);
					if (tid < 88) {
						tids[i] = tid;
					}
				}
				
				LogReader clr = LogReaderFactory.createInstance(lr, tids);
				ProvidedObject clro = new ProvidedObject("cluster_traces", new Object[] {clr});
				File outFile = new File(outFolder, "log_" + clusterNumber + ".mxml");
				logExporter.export(clro, new FileOutputStream(outFile));
//				clr.getLogSummary().getModelElements()
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		} 
	}

	private void traverse(MyCluster cluster, List<MyCluster> cs, List<MyCluster> noise, int maxElements) {
		if (cluster.getCardinality() > maxElements) {
			traverse(cluster.getLeft(), cs, noise, maxElements);
			traverse(cluster.getRight(), cs, noise, maxElements);
		} else if (cluster.getCardinality() < 10) {
			noise.add(cluster);
		} else {
			cs.add(cluster);
		}
	}
	
	private List<Integer> getTraceIds(MyCluster cluster) {
		List<Integer> traceIds = new ArrayList<Integer>();
		collectTraceIds(cluster, traceIds);
		return traceIds;
	}

	private void collectTraceIds(MyCluster cluster, List<Integer> traceIds) {
		if (cluster.getLeft() == null) {
			traceIds.add(cluster.getID());
		} else {
			collectTraceIds(cluster.getLeft(), traceIds);
			collectTraceIds(cluster.getRight(), traceIds);
		}
	}
}

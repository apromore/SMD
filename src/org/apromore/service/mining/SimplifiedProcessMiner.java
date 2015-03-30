package org.apromore.service.mining;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apromore.exception.RepositoryException;
import org.apromore.mining.AHCWrapper;
import org.apromore.mining.LogClusterBasedProcessMiner;
import org.apromore.mining.MiningConfig;
import org.apromore.mining.ProcessCollectionMiner;
import org.apromore.mining.PureProcessMiner;
import org.apromore.mining.StandardizedProcessMiner;
import org.apromore.mining.dws.DWSCollectionMiner;
import org.apromore.mining.dws.pcm.DWSBasedProcessCollectionMiner;
import org.apromore.mining.guidetree.Prom5BasedMiner;
import org.apromore.mining.guidetree.pcm.GTBasedProcessCollectionMiner;
import org.apromore.mining.guidetree.plain.GuideTreeBasedMiner;
import org.apromore.mining.sc.SequenceClusteringMiner;
import org.apromore.mining.utils.CycleFixer;
import org.apromore.mining.utils.CycleRemover;
import org.apromore.mining.utils.MiningUtils;
import org.apromore.service.ClusteringService;
import org.apromore.service.FragmentService;
import org.apromore.service.RepositoryService;
import org.apromore.service.utils.IDGenerator;
import org.apromore.util.DebugUtil;
import org.jfree.util.Log;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

//@TransactionConfiguration(transactionManager = "transactionManager", defaultRollback = false)
//@Transactional
public class SimplifiedProcessMiner {
	
	private static final Logger logger = LoggerFactory.getLogger(SimplifiedProcessMiner.class);
	
	@Autowired
	ProcessCollectionMiner processCollectionMiner;

	@Autowired
	StandardizedProcessMiner standardizeMiner;
	
	@Autowired
	LogClusterBasedProcessMiner logClusterBasedProcessMiner;
	
	@Autowired
	SequenceClusteringMiner scMiner;
	
	@Autowired
	DWSCollectionMiner dwsMiner;
	
	@Autowired
	private DWSBasedProcessCollectionMiner dwsPCM;
	
	@Autowired
	PureProcessMiner pureProcessMiner;
	
	@Autowired
	private GuideTreeBasedMiner guideTreeBasedMiner;
	
	@Autowired
	private GTBasedProcessCollectionMiner gtPCM;
	
	@Autowired
	RepositoryService rsrv;
	
	@Autowired
	private ClusteringService csrv;
	
	@Autowired @Qualifier("FragmentService")
    private FragmentService fsrv;

	public void outputClusters() {
		String outPath = "/home/cn/eps/bpm-demo-new1/bpm-demo/Apromore-Core/apromore-service/data/pm1/t1/cs";
		csrv.serializeClusters(outPath);
	}
	
	public void mineCollection(String[] args) {
		
		logger.info("====== STARTING A JOB ======");
		
		if (args.length != 4) {
			logger.error("Invalid parameters.");
			return;
		}
		
		String method = args[0];
		int threshold = Integer.parseInt(args[1]);
		String inPath = args[2];
		String outPath = args[3];
		
		MiningConfig.COMPLEXITY_MATRIC_N = threshold;
		
		long t1 = System.currentTimeMillis();
		
//		String logPath = "/home/cn/eps/bpm-demo-new1/bpm-demo/Apromore-Core/apromore-service/data/pm1/t1/logs/609.mxml";
//		String logsPath = "/home/cn/eps/bpm-demo-new1/bpm-demo/Apromore-Core/apromore-service/data/pm1/t1/logs1";
		try {
			rsrv.clearRepositoryContent();
			
			if (method.equalsIgnoreCase("smd_s")) {
				processCollectionMiner.mineCollection(inPath, outPath);
				
			} else if (method.equalsIgnoreCase("s")) {
				logClusterBasedProcessMiner.mineCollection(inPath, outPath);
				
			} else if (method.equalsIgnoreCase("smd_b")) {
				gtPCM.mineCollection(inPath, outPath);
				
			} else if (method.equalsIgnoreCase("b")) {
				guideTreeBasedMiner.mineCollectionFromFolder(inPath, outPath);
				
			} else if (method.equalsIgnoreCase("smd_m")) {
				dwsPCM.mineCollection(inPath, outPath);
				
			} else if (method.equalsIgnoreCase("m")) {
				dwsMiner.mineCollectionFromFolder(inPath, outPath);
				
			}
			
		} catch (Exception e) {
			logger.error("ERROR", e);
			e.printStackTrace();
		}
		
		writeDetails();
		
		long t2 = System.currentTimeMillis();
		double duration = ((double) t2 - (double) t1) / 1000d;
		logger.info(MiningConfig.getConfig());
		logger.info("Total time: " + duration + " s");
		System.out.println("Total time: " + duration + " s");
	}
	
	private void writeDetails() {
		logger.info("Current process ID: {}", IDGenerator.generateProcessID());
		logger.info("Current fragment ID: {}", IDGenerator.generateFragmentID());
		
		System.out.println("Current process ID: " + IDGenerator.generateProcessID());
		System.out.println("Current fragment ID: " + IDGenerator.generateFragmentID());
		
		System.out.println("Attempts: " + CycleRemover.attempts);
		System.out.println("Successes: " + CycleRemover.success);
		
		// test code
		System.out.println("Attempts: " + CycleFixer.attempts);
		System.out.println("Successes: " + CycleFixer.success);
		System.out.println("Invalid models: " + DebugUtil.invalidModelsCount);
		// end of test code
	}
	
	public void serializeFragment() {
		String fid = "F_4231";
		String outFolderPath = "/home/cn/eps/bpm-demo-new1/bpm-demo/Apromore-Core/apromore-service/data/pm1/t1/temp";
		
		try {
			String epml = fsrv.getFragmentAsFormattedEPML(fid);
			FileUtils.write(new File(outFolderPath, fid + ".epml"), epml);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
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

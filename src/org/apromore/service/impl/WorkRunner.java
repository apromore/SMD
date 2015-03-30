package org.apromore.service.impl;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.activation.DataHandler;
import javax.mail.util.ByteArrayDataSource;

import nl.tue.tm.is.epc.EPC;
import nl.tue.tm.is.graph.SimpleGraph;

import org.apache.commons.io.FileUtils;
import org.apromore.TestData;
import org.apromore.dao.model.FragmentVersion;
import org.apromore.exception.ImportException;
import org.apromore.exception.RepositoryException;
import org.apromore.mining.ProcessCollectionMiner;
import org.apromore.mining.test.MiningTest1;
import org.apromore.mining.test.MiningTestP5;
import org.apromore.mining.test.ProcessAnalyzer;
import org.apromore.mining.test.ProcessStandardizerTest;
import org.apromore.mining.utils.ProcessSerializer;
import org.apromore.service.ClusterAnalyzerService;
import org.apromore.service.ClusteringService;
import org.apromore.service.FragmentService;
import org.apromore.service.ProcessService;
import org.apromore.service.RepositoryService;
import org.apromore.service.model.ClusterSettings;
import org.apromore.service.utils.ClusterHierarchyResolver;
import org.apromore.service.utils.FileUtil;
import org.apromore.service.utils.GEDBasedSharingRefiner;
import org.apromore.service.utils.OverlapResolver;
import org.apromore.toolbox.clustering.algorithms.dbscan.refinements.MaximalFragmentFilter;
import org.apromore.toolbox.clustering.algorithms.expansion.ClusterExpander;
import org.apromore.toolbox.clustering.algorithms.expansion.ExpansionFragment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

import clustering.hierarchy.GEDSimilaritySearcher;
import clustering.hierarchy.ResultFragment;

@TransactionConfiguration(transactionManager = "transactionManager", defaultRollback = false)
@Transactional
public class WorkRunner {

	@Autowired
    private ProcessService pSrv;
    
    @Autowired
    private RepositoryService rsrv;
    
	@Autowired
	private ClusteringService csrv;
	
	@Autowired
	private FragmentService fsrv;
	
	@Autowired
	private ClusterExpander ce;
	
	@Autowired
	private ClusterAnalyzerService ca;
	
	@Autowired
	private GEDSimilaritySearcher gedSearcher;
	
	@Autowired
	private GEDBasedSharingRefiner refiner;
	
	@Autowired
	private ProcessCollectionMiner processCollectionMiner;
	
	@Autowired
	private ProcessSerializer processSerializer;
	
	@Autowired @Qualifier("OverlapResolver")
	private OverlapResolver overlapResolver;
	
	@Autowired @Qualifier("ClusterHierarchyResolver")
	private ClusterHierarchyResolver chResolver;
	
	@Autowired
	private MaximalFragmentFilter maximalFragmentFilter;
	
	@Autowired
	private ProcessStandardizerTest processStandardizerTest;
	
	@Autowired
	private ProcessAnalyzer processAnalyzer;
	
	@Autowired
	private MiningTest1 miningTest1;
	
	@Autowired
	private MiningTestP5 miningTestP5;
	
	public void testClusters() {
		
		String processesPath = "/home/cn/eps/processBase_svn1_copy/processBase/data/test1/st16/dataset";
		String clustersPath =  "/home/cn/eps/processBase_svn1_copy/processBase/data/test1/st16/ic_s";
		
//		addProcesses(processesPath);
//		csrv.computeGEDMatrix();
		
		ClusterSettings cs = new ClusterSettings();
		cs.setAlgorithm("DBSCAN");
//		cs.setAlgorithm("HAC");
		cs.setMaxNeighborGraphEditDistance(0.4d);
		try {
			csrv.cluster(cs);
		} catch (RepositoryException e) {
			e.printStackTrace();
		}
		
		try {
			FileUtils.cleanDirectory(new File(clustersPath));
		} catch (IOException e) {
			e.printStackTrace();
		}
		csrv.serializeClusters(clustersPath);
	}
	
	private void addProcesses(String processesPath) {
		
		String username = "chathura";
        String domain = "General";
        String created = "12/12/2011";
        String lastUpdate = "12/12/2011";

        rsrv.clearRepositoryContent();
        System.out.println("Repository cleared.");

        long t1 = System.currentTimeMillis();
        try {
			List<String> importedProcesses = pSrv.importProcesses(username, processesPath, domain, "", created, lastUpdate);
			for (String p : importedProcesses) {
				System.out.println(p);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
        long t2 = System.currentTimeMillis();
        System.out.println("Added processes." + (t2 - t1));
	}

	public void mtest() {
		try {
//			miningTest1.mine();
			miningTestP5.mine();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void work() {
		csrv.computeGEDMatrix();
	}
	
	public void removeMaximalFragments() {
		maximalFragmentFilter.removeNonMaximalFragments();
	}
	
	public void analyzeProcess() {
		try {
			String processName = "p9";
			processAnalyzer.analyze(processName);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void testStandardizer() {
		try {
			processStandardizerTest.standardize();
		} catch (RepositoryException e) {
			e.printStackTrace();
		}
	}
	
	public void test3() {
		String logPath = "/home/cn/eps/bpm-demo-new1/bpm-demo/Apromore-Core/apromore-service/data/pm1/t1/logs/88.mxml";
		try {
			rsrv.clearRepositoryContent();
//			processCollectionMiner.mineCollection(logPath);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void serializeClusters() {
		String outpath = "/home/cn/eps/bpm-demo-new1/bpm-demo/Apromore-Core/apromore-service/data/pm1/t3/clusters2";
		csrv.serializeClusters(outpath);
	}
	
	public void expand() {
		
		String clusterId = "30";
		String outFolderPath = "/home/cn/eps/bpm-demo-new1/bpm-demo/Apromore-Core/apromore-service/data/t1/cep1_dbscan";
		
		ce.initialize();
		List<ExpansionFragment> efs = ce.getExpansionFragments(clusterId, 0, 1.0);
		
		StringBuffer buffer = new StringBuffer();
		buffer.append("FragmentID,Member FragmentID,Distance,Fragment Size,Size ratio,Clusters\n");
		for (ExpansionFragment ef : efs) {
			if (ef.getNumClusters() == 0) {
				String record = ef.getFragmentId() + "," + ef.getMemberFragmentId() + "," + ef.getDistanceToMemberFragment() + "," + ef.getFragmentSize() + "," + ef.getSizeRatio() + "," + ef.getNumClusters() + "\n";
				buffer.append(record);
				System.out.println("Fragment ID: " + ef.getFragmentId() + " | Member ID: " + ef.getMemberFragmentId() + " | Distance: " + ef.getDistanceToMemberFragment() + " | Size: " + ef.getFragmentSize());
			}
		}
		
		File cexpansions = new File(outFolderPath, clusterId + ".csv");
		try {
			FileUtils.write(cexpansions, buffer.toString());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void resolveOverlaps() {
		try {
			overlapResolver.resolveOverlaps();
		} catch (RepositoryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void serializeProcesses() {
		String outPath = "/home/cn/eps/bpm-demo-new1/bpm-demo/Apromore-Core/apromore-service/data/pm1/t1/gt";
		processSerializer.serializeAllProcesses(outPath);
	}
	
	public void search() {
		
		String outpath = "/home/cn/eps/vc-extensions/bpm-demo/Apromore-Core/apromore-service/data/t1/results1";
		String qfile = "/home/cn/eps/vc-extensions/bpm-demo/Apromore-Core/apromore-service/data/t1/qs/ff8081813987cbe5013987cbfb280083.epml";
		EPC qepc = EPC.loadEPML(qfile);
		SimpleGraph q = new SimpleGraph(qepc);
		
		String qfid = "ff8081813987cbe5013987cc916205f8";
//		String qfid = "ff8081813987cbe5013987cc21eb02b8";
		
		gedSearcher.initialize();
		gedSearcher.setDissThreshold(0.8);
		double t1 = System.currentTimeMillis();
		List<ResultFragment> results = gedSearcher.search(qfid);
		double t2 = System.currentTimeMillis();
		System.out.println("Searching complete. Time for searching: " + (t2 - t1) + " ms");
		
		for (ResultFragment f : results) {
			double d = f.getDistance();
			if (d > 0.5) {
				System.out.println(f + " : " + f.getDistance());
			}
//			try {
//				String fstring = fsrv.getFragmentAsEPML(fid);
//				String fpath = new File(outpath, fid + ".epml").getAbsolutePath();
//				FileUtil.createFile(fpath, fstring);
//			} catch (Exception e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
		}
	}
	
	public void work2() {
		String username = "chathura";
        String cpfURI = "12325335343353";
        String version = "1.0";
        String natType = "EPML";
        String domain = "General";
        String created = "12/12/2011";
        String lastUpdate = "12/12/2011";
        DataHandler stream = new DataHandler(new ByteArrayDataSource(TestData.XPDL2.getBytes(), "text/xml"));

        rsrv.clearRepositoryContent();
        System.out.println("Repository cleared.");

        long t1 = System.currentTimeMillis();
        try {
//        	String folderPath = "/home/cn/eps/bpm-demo-new1/bpm-demo/Apromore-Core/apromore-service/data/models2";
//        	String folderPath = "/home/cn/eps/bpm-demo-new1/bpm-demo/Apromore-Core/apromore-service/data/sap1";
//        	String folderPath = "/home/cn/eps/bpm-demo-new1/bpm-demo/Apromore-Core/apromore-service/data/models-suncorp1";
        	String folderPath = "/home/cn/eps/bpm-demo-new1/bpm-demo/Apromore-Core/apromore-service/data/pm1/t1/gs2";
			List<String> importedProcesses = pSrv.importProcesses(username, folderPath, domain, "", created, lastUpdate);
			for (String p : importedProcesses) {
				System.out.println(p);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
        long t2 = System.currentTimeMillis();
        System.out.println("Added processes." + (t2 - t1));
        
//        csrv.computeGEDMatrix();
        
//        ClusterSettings cs = new ClusterSettings();
//        cs.setAlgorithm("DBSCAN");
//        cs.setMaxNeighborGraphEditDistance(0.4d);
//        try {
//			csrv.cluster(cs);
//		} catch (RepositoryException e) {
//			e.printStackTrace();
//		}
	}
	
	public void resolveHierarchy() {
		chResolver.resolveHierarchy();
	}
	
	public void printExactCloneDetails() {
		Set<Set<String>> clones = refiner.getExactClones();
		System.out.println("Number of exact clones = " + clones.size());
	}
	
	public void refine() {
		refiner.refine();
	}
	
	public void cluster() {
		System.out.println("Computing GED matrix...");
		csrv.computeGEDMatrix();
		System.out.println("Clustering...");
		ClusterSettings cs = new ClusterSettings();
		cs.setAlgorithm("DBSCAN");
        cs.setMaxNeighborGraphEditDistance(0.4d);
        try {
			csrv.cluster(cs);
		} catch (RepositoryException e) {
			e.printStackTrace();
		}
	}
	
	public void writeFragment() {
		try {
			String outpath = "/home/cn/eps/bpm-demo-new1/bpm-demo/Apromore-Core/apromore-service/data/t2/fs";
			String fid = pSrv.getRootFragmentId("e24");
			String fstring = fsrv.getFragmentAsFormattedEPML(fid);
			String fpath = new File(outpath, fid + ".epml").getAbsolutePath();
			FileUtils.write(new File(fpath), fstring);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void serializeFragments() {
		
		String processName = "e24";
		String outpath = "/home/cn/eps/bpm-demo-new1/bpm-demo/Apromore-Core/apromore-service/data/t2/fs";
		
		try {
			List<FragmentVersion> fs = fsrv.getFragmentsOfProcess(processName, 0);
			for (FragmentVersion f : fs) {
				String fid = f.getFragmentVersionId();
				String fstring = fsrv.getFragmentAsFormattedEPML(fid);
				String fpath = new File(outpath, fid + ".epml").getAbsolutePath();
				FileUtils.write(new File(fpath), fstring);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} 
	}
	
	public void serializeClusterAssignments() {
		String filepath = "/home/cn/eps/bpm-demo-new1/bpm-demo/Apromore-Core/apromore-service/data/t2/cs/sap_dbscan.txt";
		try {
			ca.serializeClusterAssignments(new FileOutputStream(filepath));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	public void analyze() {
		String f1path = "/home/cn/eps/bpm-demo-new1/bpm-demo/Apromore-Core/apromore-service/data/t2/cs/sap_dbscan.txt";
		String f2path = "/home/cn/eps/bpm-demo-new1/bpm-demo/Apromore-Core/apromore-service/data/t2/cs/sap_hac.txt";
		
		Map<String, String> mappins1 = ca.getIdenticalClusters(new File(f1path), new File(f2path));
		Map<String, String> mappins2 = ca.getCoveredClusters(new File(f1path), new File(f2path));
		Map<String, String> mappins3 = ca.getCoveredClusters(new File(f2path), new File(f1path));
		Map<String, String> mappins4 = ca.getNCoveredClusters(new File(f1path), new File(f2path), 2);
		List<String> mappins5 = ca.getDisjointClusters(new File(f1path), new File(f2path));
		System.out.println("Identical: " + mappins1.size());
		System.out.println("G1 covers G2: " + mappins2.size());
		System.out.println("G2 covers G1: " + mappins3.size());
		System.out.println("G1 n covers G2: " + mappins4.size());
		System.out.println("Unique to G1: " + mappins5.size());
		
		System.out.println("\n\n======= Unique clusters of G1 ==========");
		for (String cid : mappins5) {
			System.out.println(cid);
		}
	}
}

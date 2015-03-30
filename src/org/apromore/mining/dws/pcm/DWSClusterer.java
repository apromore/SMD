package org.apromore.mining.dws.pcm;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apromore.graph.JBPT.CPF;
import org.apromore.mining.MiningConfig;
import org.apromore.service.utils.IDGenerator;
import org.prom5.analysis.dws.Cluster;
import org.prom5.analysis.dws.VectorialPoint;
import org.prom5.framework.log.LogFile;
import org.prom5.framework.log.LogReader;
import org.prom5.framework.log.rfb.BufferedLogReader;
import org.prom5.framework.models.heuristics.HeuristicsNet;
import org.prom5.mining.heuristicsmining.HeuristicsMiner;
import org.prom5.mining.heuristicsmining.HeuristicsMinerParameters;
import org.prom5.mining.heuristicsmining.HeuristicsNetResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DWSClusterer {
	
	private static final Logger logger = LoggerFactory.getLogger(DWSClusterer.class);
	
	private double sigma1 = 0.00005; // 0.00005
	private double gamma1 = 0.00001; // 0.00001
	
	private double sigma2 = 0.004; // 0.004
	private double gamma2 = 0.002; // 0.002
	
	private int k = 2; // 2
	private double sigma = sigma1; // 0.004
	private double gamma = gamma1; // 0.002
	private int l = 5; // 5
	private int maxFeatures = 100; // 5 | 100 in new tests
	
//	private int k = 2; // 2
//	private double sigma = 0.00004; // 0.004
//	private double gamma = 0.00002; // 0.002
//	private int l = 50; // 5
//	private int maxFeatures = 500; // 5
	
	private int noiseThreshold = 1; // 1
	
	private DWSNode rootNode;
	
	private HeuristicsNet hnet = null;

	public class DWSNode {
		private int ID;
		public Cluster cluster;
		public CPF cpf;
		
		public DWSNode() {
			this.ID = IDGenerator.generateGTNodeID();
		}

		public int getID() {
			return ID;
		}
		
		public int getGroupId() {
			return 1;
		}

		public LogReader getLog() {
			return cluster.getLog();
		}
	}

	public void initialize(String logPath) throws Exception {
		
		DWSEvaluatorUtil.tcStart();
		
		logger.debug("Creating the main log...");
		LogFile lf = LogFile.getInstance(logPath);
		LogReader rootLog = BufferedLogReader.createInstance(null, lf);
		
		logger.debug("Creating the root DWS cluster...");
		Cluster rootCluster = new Cluster(rootLog, "R", k, sigma, gamma, l, maxFeatures);
		
		rootNode = new DWSNode();
		rootNode.cluster = rootCluster;

//		initializeArtificialHNet();
		
		DWSEvaluatorUtil.tcEnd();
	}
	
	private void initializeArtificialHNet() throws Exception {
		
		String artificalLog = "/home/cn/eps/bpm-demo-new1/bpm-demo/Apromore-Core/apromore-service/data/pm1/t1/splitted5_windscreen_system_subject/log_L_1_8553_1568_249.mxml";
		LogFile lf = LogFile.getInstance(artificalLog);
		LogReader alog = BufferedLogReader.createInstance(null, lf);
		
		HeuristicsMiner miner = new HeuristicsMiner();
		HeuristicsMinerParameters params = miner.getParameters();
		params.setAndThreshold(0.1);
		
		HeuristicsNetResult result = (HeuristicsNetResult) miner.mine(alog);
		hnet = result.getHeuriticsNet();
		if (hnet == null) {
			logger.error("Failed to mine an artificial HNet from {}. Real node log will be used for identifying child logs.", 
					artificalLog, alog.getLogSummary().getNumberOfUniqueProcessInstances());
		}
	}
	
	public Collection<DWSNode> getChildren(DWSNode node) {
		Collection<DWSNode> childNodes = getChildrenInternal(node);
		
		if (MiningConfig.DWS_USE_ALTERNATE_CLUSTER_DIVIDING) {
			if (childNodes.isEmpty()) {
				logger.debug("Original feature projection didn't give child clusters. Using direct feature projection...");
				VectorialPoint.PROJECTION_METHOD = 1;
				childNodes = getChildrenInternal(node);
				
				VectorialPoint.PROJECTION_METHOD = 0;
			}
			
			if (childNodes.isEmpty()) {
				logger.debug("Primary parameters didn't give child clusters. Using the secondary parameters...");
				sigma = sigma2;
				gamma = gamma2;
				childNodes = getChildrenInternal(node);
				
				sigma = sigma1;
				gamma = gamma1;
			}
		}
		
		return childNodes;
	}

	public Collection<DWSNode> getChildrenInternal(DWSNode node) {
		
		Collection<DWSNode> childNodes = new ArrayList<DWSNode>();
		
//		node.cluster.setHeuristicsNet(hnet);
		
		HeuristicsNet net = node.cluster.getHeuristicsNet();
		if (net == null) {
			try {
				HeuristicsMiner miner = new HeuristicsMiner();
				HeuristicsMinerParameters params = miner.getParameters();
				params.setAndThreshold(0.1);
				
				HeuristicsNetResult result = (HeuristicsNetResult) miner.mine(node.getLog());
				net = result.getHeuriticsNet();
				if (net == null) {
					logger.error("Failed to mine a HNet from DWSNode: {} containing {} traces. Child nodes will be considered as empty.", 
							node.getID(), node.getLog().getLogSummary().getNumberOfUniqueProcessInstances());
					return childNodes;
				}
				node.cluster.setHeuristicsNet(net);
			} catch (Exception e) {
				logger.error("Failed to mine a HNet from DWSNode: {} containing {} traces. Child nodes will be considered as empty.", 
						node.getID(), node.getLog().getLogSummary().getNumberOfUniqueProcessInstances());
				return childNodes;
			}
		}
		
		node.cluster.mineFeatures();
		node.cluster.mineClusters();
		
		List<Cluster> children = node.cluster.getChildren();
		for (Cluster child : children) {
			LogReader cl = child.getLog();
			int pis = cl.getLogSummary().getNumberOfUniqueProcessInstances();
			if (pis >= noiseThreshold) {
				DWSNode childNode = new DWSNode();
				childNode.cluster = child;
				childNodes.add(childNode);
			}
		}
		return childNodes;
	}

	public DWSNode getRoot() {
		return rootNode;
	}
	
}

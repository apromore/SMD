package org.apromore.mining.guidetree.pcm;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collection;

import org.apromore.graph.JBPT.CPF;
import org.apromore.mining.guidetree.DummyContext;
import org.apromore.service.utils.IDGenerator;
import org.deckfour.xes.factory.XFactory;
import org.deckfour.xes.factory.XFactoryNaiveImpl;
import org.deckfour.xes.in.XMxmlParser;
import org.deckfour.xes.in.XParser;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.util.progress.XMonitoredInputStream;
import org.deckfour.xes.util.progress.XProgressListener;
import org.prom6.plugins.guidetreeminer.GuideTreeMinerInput;
import org.prom6.plugins.guidetreeminer.MineGuideTree;
import org.prom6.plugins.guidetreeminer.tree.GuideTree;
import org.prom6.plugins.guidetreeminer.tree.GuideTreeNode;
import org.prom6.plugins.guidetreeminer.types.AHCJoinType;
import org.prom6.plugins.guidetreeminer.types.DistanceMetricType;
import org.prom6.plugins.guidetreeminer.types.GTMFeatureType;
import org.prom6.plugins.guidetreeminer.types.LearningAlgorithmType;
import org.prom6.plugins.guidetreeminer.types.SimilarityDistanceMetricType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GTClusterer {
	
	private static final Logger logger = LoggerFactory.getLogger(GTClusterer.class);
	
	private MineGuideTree mineGuideTree;
	private GTNode rootNode;

	class GTNode {
		private int ID;
		public GuideTreeNode node;
		public XLog log;
		public CPF cpf;
		
		public GTNode() {
			this.ID = IDGenerator.generateGTNodeID();
		}

		public int getID() {
			return ID;
		}
		
		public int getGroupId() {
			return 1;
		}
	}

	public void initialize(String logPath) throws Exception {
		
		GTEvaluatorUtil.tcStart();
		
		logger.debug("Creating the main log...");
		XLog log = getLog(logPath);

		logger.debug("Creating the guide tree...");
		GuideTreeMinerInput gtinput = getGuideTreeConfig();
		DummyContext context = new DummyContext();
		mineGuideTree = new MineGuideTree();
		Object[] results = mineGuideTree.mine(context, gtinput, log);
		
		GuideTree gt = (GuideTree) results[0];
		GuideTreeNode root = gt.getRoot();
		
		rootNode = new GTNode();
		rootNode.log = log;
		rootNode.node = root;
		
		GTEvaluatorUtil.tcEnd();
	}

	public Collection<GTNode> getChildren(GTNode node) {
		
		Collection<GTNode> children = new ArrayList<GTNode>();
		
		if (node.node.getLeft() != null) {
			GTNode leftChild = new GTNode();
			leftChild.node = node.node.getLeft();
			XLog leftLog = mineGuideTree.getLog(leftChild.node);
			leftChild.log = leftLog;
			children.add(leftChild);
		}
		
		if (node.node.getRight() != null) {
			GTNode rightChild = new GTNode();
			rightChild.node = node.node.getRight();
			XLog rightLog = mineGuideTree.getLog(rightChild.node);
			rightChild.log = rightLog;
			children.add(rightChild);
		}
		
		return children;
	}

	public GTNode getRoot() {
		return rootNode;
	}
	
	private GuideTreeMinerInput getGuideTreeConfig() {
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
		return gtinput;
	}
	
	private XLog getLog(String logPath) throws FileNotFoundException {
		File logFile = new File(logPath);
		BufferedInputStream input = new BufferedInputStream(new FileInputStream(logFile));
		long fileSizeInBytes = logFile.length();
		
		XFactory factory = new XFactoryNaiveImpl();
		XParser parser = new XMxmlParser(factory);
		Collection<XLog> logs = null;
		try {
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
		}
		return log;
	}
}

package org.apromore.mining.guidetree.plain;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

import org.apache.commons.io.FileUtils;
import org.apromore.graph.JBPT.CPF;
import org.apromore.mining.MiningConfig;
import org.apromore.mining.complexity.AggregatedComplexityChecker;
import org.apromore.mining.complexity.ComplexityCalculator;
import org.apromore.mining.complexity.ComplexityEvaluatorTool;
import org.apromore.mining.guidetree.DummyContext;
import org.apromore.mining.guidetree.GTEvaluater;
import org.apromore.mining.guidetree.Prom5BasedMiner;
import org.apromore.mining.guidetree.Prom6Miner;
import org.apromore.mining.sc.CPFImporter;
import org.apromore.mining.utils.ProcessSerializer;
import org.apromore.util.DebugUtil;
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
import org.springframework.beans.factory.annotation.Autowired;

public class GuideTreeBasedMiner {
	
	private static final Logger logger = LoggerFactory.getLogger(GuideTreeBasedMiner.class);
	
	private Map<String, CPF> invalidModels = new HashMap<String, CPF>();
	
//	@Autowired
//	private Prom6Miner prom6Miner;
	
	@Autowired
	private Prom5BasedMiner prom5BasedMiner;
	
	@Autowired
	private AggregatedComplexityChecker complexityChecker;
	
	@Autowired
	private CPFImporter cpfImporter;
	
	@Autowired
	private ProcessSerializer processSerializer;
	
	String outPath = null;
	String processesPath = "processes";
	String processesDataPath = "b_processes_data.csv";
	private final String invalidModelsPath = "invalid";

	public void mineCollectionFromFolder(String inPath, String outPath) throws Exception {
		
		initializePaths(outPath);
		if (MiningConfig.WRITE_ADDITIONAL_DATA) {
			DebugUtil.initOutPath(outPath);
		}
		
		String logPath = inPath;
		mineCollection(logPath);
	}
	
	private void initializePaths(String outPath) {

		this.outPath = outPath;
		File outFolder = new File(outPath);
		try {
			FileUtils.cleanDirectory(outFolder);
		} catch (IOException e) {
			e.printStackTrace();
		}

		File processesFolder = new File(outFolder, "processes");
		processesFolder.mkdir();
		processesPath = processesFolder.getAbsolutePath();
	}
	
	public void mineCollection(String logPath) throws Exception {

		long t1 = System.currentTimeMillis();
		
		logger.debug("Mining a model collection from the log file: {} ...", logPath);
		invalidModels.clear();
		
		logger.debug("Creating the main log...");
		XLog log = getLog(logPath);

		logger.debug("Creating the guide tree...");
		GuideTreeMinerInput gtinput = getGuideTreeConfig();
		DummyContext context = new DummyContext();
		MineGuideTree mineGuideTree = new MineGuideTree();
		Object[] results = mineGuideTree.mine(context, gtinput, log);
		
		GuideTree gt = (GuideTree) results[0];
		GuideTreeNode root = gt.getRoot();

		logger.debug("Initiating the hierarchical model population...");
		Queue<GuideTreeNode> q = new LinkedList<GuideTreeNode>();
		q.add(root);
		int processNumber = 0;
		while (!q.isEmpty()) {
			processNumber++;
			String processName = "GT_" + processNumber;
			
			GuideTreeNode gtNode = q.poll();
			
			XLog currentLog = mineGuideTree.getLog(gtNode);
			CPF model = prom5BasedMiner.mineModel(currentLog);
			
			CPF modelToAdd = null;
			if (complexityChecker.isComplexWithBuffer(model)) {
				
				if (logger.isDebugEnabled()) {
					int n = ComplexityCalculator.getNOAJS(model);
					logger.debug("Process model: {} with N: {} is complex. Adding next level models...", processName, n);
				}
				
				if (gtNode.getLeft() == null && gtNode.getRight() == null) {
					// this model cannot be further simplified
					logger.debug("Process model: {} cannot be further simplified. Adding the complex model...", processName);
					modelToAdd = model;
				}
				
				if (gtNode.getLeft() != null) {
					q.add(gtNode.getLeft());
				}
				
				if (gtNode.getRight() != null) {
					q.add(gtNode.getRight());
				}
				
			} else {
				// current model is not complex. we can add it to the repository.
				modelToAdd = model;
			}
			
			if (modelToAdd != null) {
				if (logger.isDebugEnabled()) {
					int n = ComplexityCalculator.getNOAJS(model);
					logger.debug("Adding process {} with N: {}", processName, n);
				}
				
				boolean success = cpfImporter.importModel(processName, modelToAdd);
				if (!success) {
					logger.debug("{} is not valid. Adding next level clusters...", processName);
					
					if (gtNode.getLeft() == null && gtNode.getRight() == null) {
						// this invalid model cannot be further simplified
						invalidModels.put(processName, modelToAdd);
//						GTEvaluater.addNode(gtNode);
					}
					
					if (gtNode.getLeft() != null) {
						q.add(gtNode.getLeft());
					}
					
					if (gtNode.getRight() != null) {
						q.add(gtNode.getRight());
					}
				} else {
					GTEvaluater.addNode(gtNode);
				}
			}
		}
		
		logger.debug("GuideTree based mining is complete.");

		long duration = System.currentTimeMillis() - t1;
		
		processSerializer.serializeAllProcesses(processesPath);

		if (MiningConfig.WRITE_ADDITIONAL_DATA) {
			GTEvaluater.writeData(mineGuideTree, outPath, duration);
			for (String pname : invalidModels.keySet()) {
				CPF faultyModel = invalidModels.get(pname);
				DebugUtil.writeModel(pname, faultyModel);
			}
			writeEvalData(duration);
		}
	}
	
	private void writeEvalData(long duration) {
		ComplexityEvaluatorTool evaluatorTool = new ComplexityEvaluatorTool();
		evaluatorTool.writeComplexities(processesPath, processesDataPath, duration);
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

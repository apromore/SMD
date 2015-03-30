package org.prom6.plugins.guidetreeminer.ui;

import java.io.File;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import org.deckfour.uitopia.api.event.TaskListener.InteractionResult;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.prom6.plugins.guidetreeminer.GuideTreeMinerInput;
import org.prom6.plugins.guidetreeminer.settings.GuideTreeMinerSettingsListener;
import org.prom6.plugins.guidetreeminer.types.AHCJoinType;
import org.prom6.plugins.guidetreeminer.types.DistanceMetricType;
import org.prom6.plugins.guidetreeminer.types.GTMFeatureType;
import org.prom6.plugins.guidetreeminer.types.LearningAlgorithmType;
import org.prom6.plugins.guidetreeminer.types.SimilarityDistanceMetricType;
import org.prom6.plugins.guidetreeminer.types.SimilarityMetricType;
import org.prom6.plugins.guidetreeminer.util.FileIO;

/**
 * @author R.P. Jagadeesh Chandra 'JC' Bose
 * @date 08 July 2010 
 * @since 01 June 2010
 * @version 1.0
 * @email j.c.b.rantham.prabhakara@tue.nl
 * @copyright R.P. Jagadeesh Chandra 'JC' Bose
 * 			  Architecture of Information Systems Group (AIS) 
 * 			  Department of Mathematics and Computer Science
 * 			  University of Technology, Eindhoven, The Netherlands
 */

public class GuideTreeMinerUI implements GuideTreeMinerSettingsListener{
	private UIPluginContext context;
	
	private int introductionStep;
	private int featureConfigurationStep;
	private int similarityDistanceConfigurationStep;
	private int algorithmConfigurationStep;
	
	int noSteps;
	int currentStep;
	private myStep[] mySteps;
	
	GTMFeatureType featureType;
	GuideTreeMinerInput input;
	
	public GuideTreeMinerUI(UIPluginContext context){
		this.context = context;
	}
	
	public GuideTreeMinerInput mineTree(XLog log){
		input = new GuideTreeMinerInput();
		InteractionResult result = InteractionResult.NEXT;

		Set<String> activitySet = new HashSet<String>();
		for(XTrace trace : log){
			for(XEvent event : trace)
				if(event.getAttributes() != null && event.getAttributes().containsKey("concept:name"))
					activitySet.add(event.getAttributes().get("concept:name").toString());
		}

		noSteps = 0;
		introductionStep = noSteps++;
		featureConfigurationStep = noSteps++;
		similarityDistanceConfigurationStep = noSteps++;
		algorithmConfigurationStep = noSteps++;
		
		mySteps = new myStep[noSteps];

		mySteps[introductionStep] = new IntroductionStep();
		mySteps[introductionStep].setListener(this);
		
		mySteps[featureConfigurationStep] = new FeatureConfigurationStep();
		mySteps[featureConfigurationStep].setListener(this);
		
		mySteps[similarityDistanceConfigurationStep] = new SimilarityDistanceConfigurationStep();
		mySteps[similarityDistanceConfigurationStep].setListener(this);
		
		mySteps[algorithmConfigurationStep] = new AlgorithmConfigurationStep();
		mySteps[algorithmConfigurationStep].setListener(this);
		
		while (true) {
			if (currentStep < 0) {
				currentStep = 0;
			}
			if (currentStep >= noSteps) {
				currentStep = noSteps - 1;
			}
			context.log("Current step: " + currentStep);
			result = context.showWizard("Guide Tree Miner Plugin", currentStep == 0, currentStep == noSteps - 1, mySteps[currentStep]);
			
			switch (result) {
			case NEXT:
				go(1);
				break;
			case PREV:
				go(-1);
				break;
			case FINISHED:
				readSettings();
				if(!checkGEDScores(activitySet)){
					JOptionPane.showMessageDialog(new JFrame(), "<HTML>Generic Edit Distance has been chosen as the distance metric type. <BR>There is a problem in scoring files provided as input.<BR> Either the files are missing/corrupt <BR>or the content of the file doesn't comply with the format <BR>or the set of activities in the file does not match with the activities in the log file <BR></HTML>");
				}
//				return new GuideTreeFrame(context, input, log);
				return input;
				
//				MineGuideTree mineGuideTree = new MineGuideTree();
//				Object[] mineGuideTreeOutput = mineGuideTree.mine(context, input, log);
				
//				return new Object[]{null, null};
			default:
				context.getFutureResult(0).cancel(true);
				context.getFutureResult(1).cancel(true);
				return null;
			}
		}
	}
	
	private int go(int direction) {
		currentStep += direction;
		if (currentStep >= 0 && currentStep < noSteps) {
			if (mySteps[currentStep].precondition()) {
				return currentStep;
			} else {
				return go(direction);
			}
		}
		return currentStep;
	}
	
	private void readSettings(){
		for(int currentStep = 1; currentStep < noSteps; currentStep++)
			mySteps[currentStep].readSettings();
	}
	
	private boolean checkGEDScores(Set<String> activitySet){
		if(input.getDistanceMetricType() == DistanceMetricType.GenericEditDistance){
			if(!input.isDeriveSubstitutionScore()){
				Map<String, Integer> substituionScoreMap = readSubstitutionScoreMap();
				if(substituionScoreMap == null)
					return false;
				String[] activityPairSplit;
				for(String activityPair : substituionScoreMap.keySet()){
					activityPairSplit = activityPair.split(" @ ");
					if(!activitySet.contains(activityPairSplit[0].trim()) || !activitySet.contains(activityPairSplit[1].trim()))
						return false;
				}
			}
			if(!input.isDeriveIndelScore()){
				Map<String, Integer> indelScoreMap = readIndelScoreMap();
				if(indelScoreMap == null)
					return false;
				String[] activityPairSplit;
				for(String activityPair : indelScoreMap.keySet()){
					activityPairSplit = activityPair.split(" @ ");
					if(!activitySet.contains(activityPairSplit[0].trim()) || !activitySet.contains(activityPairSplit[1].trim()))
						return false;
				}
				input.setIndelScoreMap(indelScoreMap);
			}
			if(input.isDeriveSubstitutionScore() || input.isDeriveIndelScore()){
				return true;
			}
			return true;
		}
		return true;
	}
	
	private Map<String, Integer> readSubstitutionScoreMap(){
		String delim = " @ ";
		Map<String, Integer> substitutionScoreMap = null;
		FileIO io = new FileIO();
		String inputDir = new File(input.getSubstitutionScoreFileName()).getParent();
		String fileName = new File(input.getSubstitutionScoreFileName()).getName();
		if(inputDir != null && fileName != null)
			substitutionScoreMap = io.readMapStringIntegerFromFile(inputDir, fileName, delim);
		return substitutionScoreMap;
	}
	
	private Map<String, Integer> readIndelScoreMap(){
		String delim = " @ ";
		Map<String, Integer> indelScoreMap = null;
		FileIO io = new FileIO();
		String inputDir = new File(input.getIndelScoreFileName()).getParent();
		String fileName = new File(input.getIndelScoreFileName()).getName();
		if(inputDir != null && fileName != null)
			indelScoreMap = io.readMapStringIntegerFromFile(inputDir, fileName, delim);
		return indelScoreMap;
	}
	
	public void setFeatureType(GTMFeatureType featureType){
		this.featureType = featureType;
//		((SimilarityDistanceConfigurationStep)mySteps[similarityDistanceConfigurationStep]).setFeatureType(featureType);
		input.setFeatureType(featureType);
	}
	
	public void setFeatureTypeOption(GTMFeatureType featureType){
		this.featureType = featureType;
		((SimilarityDistanceConfigurationStep)mySteps[similarityDistanceConfigurationStep]).setFeatureType(featureType);
		input.setFeatureType(featureType);
	}
	
	
	public void clearFeatureSelection(){
		input.removeAllFeatures();
	}

	public void featureSelectionChanged(String featureString, boolean isSelected){
		if(isSelected){
			input.addFeature(featureString);
		}else{
			input.removeFeature(featureString);
		}
	}
	public void setKGramValue(int kGramValue){
		input.setKGramValue(kGramValue);
	}

	public void setFrequencyCount(boolean isNominalCount){
		input.setNominalFeatureCount(isNominalCount);
	}

	public void setLearningAlgorithmType(LearningAlgorithmType learningAlgorithmType){
		input.setLearningAlgorithmType(learningAlgorithmType);
	}	
	
	public void setAHCJoinType(AHCJoinType ahcJoinType){
		input.setAhcJoinType(ahcJoinType);
	}
	
	public void setBaseFeatures(boolean isBaseFeatures){
		input.setBaseFeatures(isBaseFeatures);
	}
	
	public void setNominalFeatureCount(boolean isNominalCount){
		input.setNominalFeatureCount(isNominalCount);
	}
	
	public void setMininumFrequencyCountThreshold(int minFrequencyCountThreshold){
		input.setMinFrequencyCountThreshold(minFrequencyCountThreshold);
	}
	
	public void setMininumInstancePercentageCountThreshold(int minInstancePercentageCountThreshold){
		input.setMinInstancePercentageCountThreshold(minInstancePercentageCountThreshold);
	}
	
	public void setMinimumAlphabetSizeThreshold(int minAlphabetSizeThreshold){
		input.setMinAlphabetSizeThreshold(minAlphabetSizeThreshold);
	}

	public void setMaximumAlphabetSizeThreshold(int maxAlphabetSizeThreshold){
		input.setMaxAlphabetSizeThreshold(maxAlphabetSizeThreshold);
	}
	
	public void setSimilarityDistanceType(SimilarityDistanceMetricType metricType){
		input.setSimilarityDistanceMetricType(metricType);
	}
	
	public void setSimilarityMetricType(SimilarityMetricType similarityMetricType){
		input.setSimilarityMetricType(similarityMetricType);
	}
	
	public void setDistanceMetricType(DistanceMetricType distanceMetricType){
		input.setDistanceMetricType(distanceMetricType);
	}
	
	public void setIsDeriveSubstitutionScores(boolean isDeriveSubstitutionScores){
		input.setIsDeriveSubstitutionScores(isDeriveSubstitutionScores);
	}
	
	public void setIsDeriveIndelScores(boolean isDeriveIndelScores){
		input.setIsDeriveIndelScores(isDeriveIndelScores);
	}
	
	public void setSubstitutionScoreFileName(String substitutionScoreFileName){
		input.setSubstitutionScoreFileName(substitutionScoreFileName);
	}
	
	public void setIndelScoreFileName(String indelScoreFileName){
		input.setIndelScoreFileName(indelScoreFileName);
	}
	
	public void setOutputClusterLogs(boolean isOutputClusterLogs){
		input.setIsOutputClusterLogs(isOutputClusterLogs);
	}
	
	public void setNumberOfClusters(int noClusters){
		input.setNumberOfClusters(noClusters);
	}
}

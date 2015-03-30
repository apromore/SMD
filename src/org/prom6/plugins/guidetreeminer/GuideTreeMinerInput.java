package org.prom6.plugins.guidetreeminer;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.prom6.plugins.guidetreeminer.types.AHCJoinType;
import org.prom6.plugins.guidetreeminer.types.DistanceMetricType;
import org.prom6.plugins.guidetreeminer.types.GTMFeature;
import org.prom6.plugins.guidetreeminer.types.GTMFeatureType;
import org.prom6.plugins.guidetreeminer.types.LearningAlgorithmType;
import org.prom6.plugins.guidetreeminer.types.SimilarityDistanceMetricType;
import org.prom6.plugins.guidetreeminer.types.SimilarityMetricType;

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
public class GuideTreeMinerInput {
	Map<String, GTMFeature> featureStringValueFeatureMap;
	GTMFeatureType featureType;
	GTMFeature feature;
	Set<GTMFeature> selectedFeatureSet;
	boolean isBaseFeatures;
	boolean isNominalFeatureCount;
	boolean isDeriveSubstitutionScore, isDeriveIndelScore;
	String substitutionScoreFileName, indelScoreFileName;
	int kGramValue;
	SimilarityDistanceMetricType similarityDistanceMetricType;
	SimilarityMetricType similarityMetricType;
	DistanceMetricType distanceMetricType;
	int minFrequencyCountThreshold, minInstancePercentageCountThreshold, minAlphabetSizeThreshold, maxAlphabetSizeThreshold;
	
	LearningAlgorithmType learningAlgorithmType;
	AHCJoinType ahcJoinType;
	
	Map<String, Integer> substitutionScoreMap;
	Map<String, Integer> indelScoreMap;
	
	int noClusters;
	boolean isOutputClusterLogs;
	
	public GuideTreeMinerInput(){
		this.selectedFeatureSet = new HashSet<GTMFeature>();
		createFeatureStringValueFeatureMap();
	}
	
	private void createFeatureStringValueFeatureMap(){
		featureStringValueFeatureMap = new HashMap<String, GTMFeature>();
		featureStringValueFeatureMap.put("Individual Event", GTMFeature.IE);
		featureStringValueFeatureMap.put("K-Gram", GTMFeature.KGram);
		featureStringValueFeatureMap.put("Tandem Repeat", GTMFeature.TR);
		featureStringValueFeatureMap.put("Maximal Repeat", GTMFeature.MR);
		featureStringValueFeatureMap.put("Super Maximal Repeat", GTMFeature.SMR);
		featureStringValueFeatureMap.put("Near Super Maximal Repeat", GTMFeature.NSMR);
		featureStringValueFeatureMap.put("Tandem Repeat Alphabet", GTMFeature.TRA);
		featureStringValueFeatureMap.put("Maximal Repeat Alphabet", GTMFeature.MRA);
		featureStringValueFeatureMap.put("Super Maximal Repeat Alphabet", GTMFeature.SMRA);
		featureStringValueFeatureMap.put("Near Super Maximal Repeat Alphabet", GTMFeature.NSMRA);
	}

	public GTMFeatureType getFeatureType() {
		return featureType;
	}

	public void setFeatureType(GTMFeatureType featureType) {
		this.featureType = featureType;
	}

	public GTMFeature getFeature() {
		return feature;
	}

	public void setFeature(GTMFeature feature) {
		this.feature = feature;
	}

	public SimilarityDistanceMetricType getSimilarityDistanceMetricType() {
		return similarityDistanceMetricType;
	}

	public void setSimilarityDistanceMetricType(
			SimilarityDistanceMetricType similarityDistanceMetricType) {
		this.similarityDistanceMetricType = similarityDistanceMetricType;
	}

	public SimilarityMetricType getSimilarityMetricType() {
		return similarityMetricType;
	}

	public void setSimilarityMetricType(SimilarityMetricType similarityMetricType) {
		this.similarityMetricType = similarityMetricType;
	}

	public DistanceMetricType getDistanceMetricType() {
		return distanceMetricType;
	}

	public void setDistanceMetricType(DistanceMetricType distanceMetricType) {
		this.distanceMetricType = distanceMetricType;
	}

	public int getMinFrequencyCountThreshold() {
		return minFrequencyCountThreshold;
	}

	public void setMinFrequencyCountThreshold(int minFrequencyCountThreshold) {
		this.minFrequencyCountThreshold = minFrequencyCountThreshold;
	}

	public int getMinInstancePercentageCountThreshold() {
		return minInstancePercentageCountThreshold;
	}

	public void setMinInstancePercentageCountThreshold(
			int minInstancePercentageCountThreshold) {
		this.minInstancePercentageCountThreshold = minInstancePercentageCountThreshold;
	}

	public int getMinAlphabetSizeThreshold() {
		return minAlphabetSizeThreshold;
	}

	public void setMinAlphabetSizeThreshold(int minAlphabetSizeThreshold) {
		this.minAlphabetSizeThreshold = minAlphabetSizeThreshold;
	}

	public int getMaxAlphabetSizeThreshold() {
		return maxAlphabetSizeThreshold;
	}

	public void setMaxAlphabetSizeThreshold(int maxAlphabetSizeThreshold) {
		this.maxAlphabetSizeThreshold = maxAlphabetSizeThreshold;
	}
	
	public void setNominalFeatureCount(boolean isNominalCount){
		this.isNominalFeatureCount = isNominalCount;
	}
	
	public boolean isNominalFeatureCount(){
		return isNominalFeatureCount;
	}
	
	public void setBaseFeatures(boolean isBaseFeatures){
		this.isBaseFeatures = isBaseFeatures;
	}
	
	public boolean isBaseFeatures(){
		return isBaseFeatures;
	}

	public LearningAlgorithmType getLearningAlgorithmType() {
		return learningAlgorithmType;
	}

	public void setLearningAlgorithmType(LearningAlgorithmType learningAlgorithmType) {
		this.learningAlgorithmType = learningAlgorithmType;
	}

	public AHCJoinType getAhcJoinType() {
		return ahcJoinType;
	}

	public void setAhcJoinType(AHCJoinType ahcJoinType) {
		this.ahcJoinType = ahcJoinType;
	}
	
	public int getKGramValue(){
		return kGramValue;
	}
	
	public void setKGramValue(int kGramValue){
		this.kGramValue= kGramValue;
	}
	
	public void removeAllFeatures(){
		selectedFeatureSet.clear();
	}
	
	public void addFeature(GTMFeature feature){
		selectedFeatureSet.add(feature);
	}
	
	public void addFeature(String featureString){
		selectedFeatureSet.add(featureStringValueFeatureMap.get(featureString));
	}
	
	public void removeFeature(String featureString){
		selectedFeatureSet.remove(featureStringValueFeatureMap.get(featureString));
	}

	public Set<GTMFeature> getSelectedFeatureSet() {
		return selectedFeatureSet;
	}
	
	public void setIsDeriveSubstitutionScores(boolean isDeriveSubstitutionScores){
		this.isDeriveSubstitutionScore = isDeriveSubstitutionScores;
	}
	
	public void setIsDeriveIndelScores(boolean isDeriveIndelScores){
		this.isDeriveIndelScore = isDeriveIndelScores;
	}
	
	public void setSubstitutionScoreFileName(String substitutionScoreFileName){
		this.substitutionScoreFileName = substitutionScoreFileName;
	}

	public void setIndelScoreFileName(String indelScoreFileName){
		this.indelScoreFileName = indelScoreFileName;
	}

	public boolean isDeriveSubstitutionScore() {
		return isDeriveSubstitutionScore;
	}

	public boolean isDeriveIndelScore() {
		return isDeriveIndelScore;
	}

	public String getSubstitutionScoreFileName() {
		return substitutionScoreFileName;
	}

	public String getIndelScoreFileName() {
		return indelScoreFileName;
	}
	
	public void setSubstitutionScoreMap(Map<String, Integer> substitutionScoreMap){
		this.substitutionScoreMap = substitutionScoreMap;
	}
	
	public void setIndelScoreMap(Map<String, Integer> indelScoreMap){
		this.indelScoreMap = indelScoreMap;
	}
	
	public Map<String, Integer> getSubstitutionScoreMap(){
		return  substitutionScoreMap;
	}
	
	public Map<String, Integer> getIndelScoreMap(){
		return indelScoreMap;
	}
	
	public void setIsOutputClusterLogs(boolean isOutputClusterLogs){
		this.isOutputClusterLogs = isOutputClusterLogs;
	}
	
	public void setNumberOfClusters(int noClusters){
		this.noClusters = noClusters;
	}
	
	public int getNoClusters(){
		return noClusters;
	}
	
}

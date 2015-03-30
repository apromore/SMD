package org.prom6.plugins.guidetreeminer.settings;

import org.prom6.plugins.guidetreeminer.types.AHCJoinType;
import org.prom6.plugins.guidetreeminer.types.DistanceMetricType;
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

public interface GuideTreeMinerSettingsListener {
	public void clearFeatureSelection();
	public void featureSelectionChanged(String featureString, boolean isSelected);
	public void setKGramValue(int kGramValue);
	public void setFrequencyCount(boolean isNominalCount);
	public void setFeatureType(GTMFeatureType featureType);
	public void setFeatureTypeOption(GTMFeatureType featureType);
	public void setLearningAlgorithmType(LearningAlgorithmType learningAlgorithmType);
	public void setAHCJoinType(AHCJoinType ahcJoinType);
	public void setBaseFeatures(boolean isBaseFeatures);
	public void setNominalFeatureCount(boolean isNominalCount);
	public void setSimilarityDistanceType(SimilarityDistanceMetricType metricType);
	public void setSimilarityMetricType(SimilarityMetricType similarityMetricType);
	public void setDistanceMetricType(DistanceMetricType distanceMetricType);
	public void setMininumFrequencyCountThreshold(int minFrequencyCountThreshold);
	public void setMininumInstancePercentageCountThreshold(int minInstancePercentageCountThreshold);
	public void setMinimumAlphabetSizeThreshold(int minAlphabetSizeThreshold);
	public void setMaximumAlphabetSizeThreshold(int maxAlphabetSizeThreshold);
	public void setIsDeriveSubstitutionScores(boolean isDeriveSubstitutionScores);
	public void setIsDeriveIndelScores(boolean isDeriveIndelScores);
	public void setSubstitutionScoreFileName(String substitutionScoreFileName);
	public void setIndelScoreFileName(String indelScoreFileName);
	public void setOutputClusterLogs(boolean isOutputClusterLogs);
	public void setNumberOfClusters(int noClusters);
}

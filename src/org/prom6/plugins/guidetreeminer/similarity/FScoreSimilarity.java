package org.prom6.plugins.guidetreeminer.similarity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.prom6.plugins.guidetreeminer.util.UkkonenSuffixTree;

public class FScoreSimilarity {
	int encodingLength;
	float[][] similarityMatrix;
	List<String> charStreamList;

	public FScoreSimilarity(int encodingLength) {
		this.encodingLength = encodingLength;
	}

	public float[][] getFScore(Set<String> sequenceFeatureSet, Collection<String> charStreamCollection) {
		/*
		 * Convert the charStreamCollection to a list to maintain the order
		 */
		charStreamList = new ArrayList<String>();
		for (String charStream : charStreamCollection) {
			charStreamList.add(charStream);
		}

		int noCharStreams = charStreamList.size();
		similarityMatrix = new float[noCharStreams][noCharStreams];

		UkkonenSuffixTree suffixTree;

		int iCount, jCount, featureCount;

		List<Map<String, Integer>> featureCountMapList = new ArrayList<Map<String, Integer>>();
		Map<String, Integer> featureCountMap;

		for (String charStream : charStreamList) {
			suffixTree = new UkkonenSuffixTree(encodingLength, charStream);
			featureCountMap = new HashMap<String, Integer>();
			for (String feature : sequenceFeatureSet) {
				featureCount = suffixTree.noMatches(feature);
				if (featureCount > 0) {
					featureCountMap.put(feature, featureCount);
				}
			}
			featureCountMapList.add(featureCountMap);
			suffixTree = null;
		}

		Map<String, Integer> featureCountMapTraceI, featureCountMapTraceJ;
		Set<String> commonFeatureSet = new HashSet<String>();

		int sumCount, totalCountTraceI, totalCountTraceJ;
		for (int i = 0; i < noCharStreams - 1; i++) {
			similarityMatrix[i][i] = (float) 1.0;
			featureCountMapTraceI = featureCountMapList.get(i);

			totalCountTraceI = 0;
			for (String feature : featureCountMapTraceI.keySet()) {
				totalCountTraceI += featureCountMapTraceI.get(feature);
			}

			for (int j = i + 1; j < noCharStreams; j++) {
				featureCountMapTraceJ = featureCountMapList.get(j);

				totalCountTraceJ = 0;
				for (String feature : featureCountMapTraceJ.keySet()) {
					totalCountTraceJ += featureCountMapTraceJ.get(feature);
				}

				commonFeatureSet.clear();
				commonFeatureSet.addAll(featureCountMapTraceI.keySet());
				commonFeatureSet.retainAll(featureCountMapTraceJ.keySet());
				/*
				 * Scan through the feature set and get the count of the feature
				 * in traceI and traceJ
				 */
				sumCount = 0;
				for (String feature : commonFeatureSet) {
					iCount = featureCountMapTraceI.get(feature);
					jCount = featureCountMapTraceJ.get(feature);

					sumCount += Math.min(iCount, jCount);
				}

				if ((totalCountTraceI != 0) || (totalCountTraceJ != 0)) {
					similarityMatrix[i][j] = similarityMatrix[j][i] = ((float) sumCount)
							/ Math.min(totalCountTraceI, totalCountTraceJ);
				} else {
					System.out.println(i + " ZERO");
					similarityMatrix[i][j] = similarityMatrix[j][i] = 0;
				}
			}
		}
		similarityMatrix[noCharStreams - 1][noCharStreams - 1] = (float) 1.0;

		return similarityMatrix;
	}

	public float[][] getFScore(int[][] featureMatrix) {
		int noCharStreams = featureMatrix.length;
		similarityMatrix = new float[noCharStreams][noCharStreams];

		int[] featureVectorTraceI, featureVectorTraceJ;
		int noFeatures;
		int sumCount, totalCountTraceI, totalCountTraceJ;
		for (int i = 0; i < noCharStreams; i++) {
			similarityMatrix[i][i] = (float) 1.0;

			featureVectorTraceI = featureMatrix[i];
			totalCountTraceI = 0;
			for (int featureCountTraceI : featureVectorTraceI) {
				totalCountTraceI += featureCountTraceI;
			}

			for (int j = i + 1; j < noCharStreams; j++) {
				featureVectorTraceJ = featureMatrix[j];

				totalCountTraceJ = 0;
				for (int featureCountTraceJ : featureVectorTraceJ) {
					totalCountTraceJ += featureCountTraceJ;
				}

				sumCount = 0;
				noFeatures = featureVectorTraceI.length;

				for (int k = 0; k < noFeatures; k++) {
					sumCount += Math.min(featureVectorTraceI[k], featureVectorTraceJ[k]);
				}
				if ((totalCountTraceI != 0) || (totalCountTraceJ != 0)) {
					//					similarityMatrix[i][j] = similarityMatrix[j][i] = ((float)sumCount)/Math.min(totalCountTraceI, totalCountTraceJ);
					similarityMatrix[i][j] = similarityMatrix[j][i] = ((float) sumCount)
							/ ((totalCountTraceI + totalCountTraceJ) / 2);
				} else {
					similarityMatrix[i][j] = similarityMatrix[j][i] = Integer.MIN_VALUE;
				}
			}
		}

		return similarityMatrix;
	}

	public float[][] getF2Score(int[][] featureMatrix) {
		int noCharStreams = featureMatrix.length;
		similarityMatrix = new float[noCharStreams][noCharStreams];

		int[] featureVectorTraceI, featureVectorTraceJ;
		int noFeatures;
		int sumCount, totalCountTraceI, totalCountTraceJ, diff1Count, diff2Count;
		for (int i = 0; i < noCharStreams; i++) {
			similarityMatrix[i][i] = (float) 1.0;

			featureVectorTraceI = featureMatrix[i];
			totalCountTraceI = 0;
			for (int featureCountTraceI : featureVectorTraceI) {
				totalCountTraceI += featureCountTraceI;
			}

			for (int j = i + 1; j < noCharStreams; j++) {
				featureVectorTraceJ = featureMatrix[j];

				totalCountTraceJ = 0;
				for (int featureCountTraceJ : featureVectorTraceJ) {
					totalCountTraceJ += featureCountTraceJ;
				}

				sumCount = 0;
				diff1Count = 0;
				diff2Count = 0;
				noFeatures = featureVectorTraceI.length;

				for (int k = 0; k < noFeatures; k++) {
					sumCount += Math.min(featureVectorTraceI[k], featureVectorTraceJ[k]);
					if ((featureVectorTraceI[k] == 0) && (featureVectorTraceJ[k] != 0)) {
						diff2Count += 1;
					} else if ((featureVectorTraceI[k] != 0) && (featureVectorTraceJ[k] == 0)) {
						diff1Count += 1;
					}

				}
				if ((totalCountTraceI != 0) || (totalCountTraceJ != 0)) {
					similarityMatrix[i][j] = similarityMatrix[j][i] = ((float) sumCount)
							/ Math.min(totalCountTraceI, totalCountTraceJ) - ((float) diff1Count * diff2Count)
							/ (totalCountTraceI * totalCountTraceJ);
				} else {
					similarityMatrix[i][j] = similarityMatrix[j][i] = Integer.MIN_VALUE;
				}
			}
		}
		return similarityMatrix;
	}
}	

package org.prom6.plugins.guidetreeminer.distance;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.prom6.plugins.guidetreeminer.types.Normalization;
import org.prom6.plugins.guidetreeminer.util.UkkonenSuffixTree;

/**
 * @author R.P. Jagadeesh Chandra 'JC' Bose
 * @date 08 July 2010 
 * @since 01 June 2009
 * @version 1.0
 * @email j.c.b.rantham.prabhakara@tue.nl
 * @copyright R.P. Jagadeesh Chandra 'JC' Bose
 * 			  Architecture of Information Systems Group (AIS) 
 * 			  Department of Mathematics and Computer Science
 * 			  University of Technology, Eindhoven, The Netherlands
 */

public class EuclideanDistance {
	float[][] distanceMatrix;
	int encodingLength;

	List<Map<String, Integer>> featureFeatureCountMapTracesList;
	List<Map<Set<String>, Integer>> alphabetFeatureFeatureCountMapTracesList;

	/*
	 * Since the distance matrix should be indexed, we need a list of
	 * charStreams (though it is assumed that there are no duplicates in the
	 * list) This constructor is to be used for sequence based features
	 */
	public EuclideanDistance(int encodingLength, List<String> charStreamList, Set<String> featureSet,
			Normalization normalizationType) {
		this.encodingLength = encodingLength;

		/*
		 * Get the count of each feature in the traces
		 */
		featureFeatureCountMapTracesList = new ArrayList<Map<String, Integer>>();
		Map<String, Integer> featureFeatureCountMap;
		UkkonenSuffixTree suffixTree;
		int count;
		for (String charStream : charStreamList) {
			suffixTree = new UkkonenSuffixTree(encodingLength, charStream);
			featureFeatureCountMap = new HashMap<String, Integer>();
			for (String feature : featureSet) {
				count = suffixTree.noMatches(feature);
				if (count > 0) {
					featureFeatureCountMap.put(feature, count);
				}
			}
			featureFeatureCountMapTracesList.add(featureFeatureCountMap);
		}

		distanceMatrix = new float[charStreamList.size()][charStreamList.size()];

		int noTraces = charStreamList.size();
		double countI, countJ;
		double sum;
		Map<String, Integer> featureFeatureCountMapI, featureFeatureCountMapJ;
		Set<String> unionFeaturesSet = new HashSet<String>();
		double[] euclideanLengthArray, traceLengthArray;

		traceLengthArray = new double[noTraces];
		euclideanLengthArray = new double[noTraces];

		int traceLength;
		for (int i = 0; i < noTraces; i++) {
			featureFeatureCountMapI = featureFeatureCountMapTracesList.get(i);
			traceLength = 0;
			sum = 0;
			for (String feature : featureFeatureCountMapI.keySet()) {
				traceLength += featureFeatureCountMapI.get(feature);
				sum += Math.pow(featureFeatureCountMapI.get(feature), 2);
			}

			traceLengthArray[i] = traceLength;
			euclideanLengthArray[i] = Math.sqrt(sum);
		}

		for (int i = 0; i < noTraces - 1; i++) {
			distanceMatrix[i][i] = 0;
			featureFeatureCountMapI = featureFeatureCountMapTracesList.get(i);

			unionFeaturesSet.clear();
			unionFeaturesSet.addAll(featureFeatureCountMapI.keySet());

			for (int j = i + 1; j < noTraces; j++) {
				featureFeatureCountMapJ = featureFeatureCountMapTracesList.get(j);

				unionFeaturesSet.addAll(featureFeatureCountMapJ.keySet());

				sum = 0.0;
				for (String featureIJ : unionFeaturesSet) {
					countI = countJ = 0;
					if (featureFeatureCountMapI.containsKey(featureIJ)) {
						countI = featureFeatureCountMapI.get(featureIJ);
					}

					if (featureFeatureCountMapJ.containsKey(featureIJ)) {
						countJ = featureFeatureCountMapJ.get(featureIJ);
					}

					switch (normalizationType) {
						case TRACE_LENGTH :
							if (traceLengthArray[i] != 0) {
								countI /= traceLengthArray[i];
							}
							if (traceLengthArray[j] != 0) {
								countJ /= traceLengthArray[j];
							}
							break;
						case EUCLIDEAN_LENGTH :
							if (euclideanLengthArray[i] != 0) {
								countI /= euclideanLengthArray[i];
							}
							if (euclideanLengthArray[j] != 0) {
								countJ /= euclideanLengthArray[j];
							}
							break;
						default :
							break;
					}

					sum += Math.pow((countI - countJ), 2);
				}

				if ((traceLengthArray[i] != 0) || (traceLengthArray[j] != 0)) {
					distanceMatrix[i][j] = distanceMatrix[j][i] = (float) Math.sqrt(sum);
				} else {
					distanceMatrix[i][j] = distanceMatrix[j][i] = Integer.MAX_VALUE;
				}
			}
		}
	}

	/*
	 * This constructor is used for alphabet based features
	 */
	public EuclideanDistance(int encodingLength, List<String> charStreamList,
			Map<Set<String>, Set<String>> alphabetFeatureSetMap, Normalization normalizationType) {
		this.encodingLength = encodingLength;

		alphabetFeatureFeatureCountMapTracesList = new ArrayList<Map<Set<String>, Integer>>();
		Map<Set<String>, Integer> alphabetFeatureFeatureCountMap;
		Set<String> alphabetEquivalenceClassFeatureSet;
		UkkonenSuffixTree suffixTree;
		int count;
		for (String charStream : charStreamList) {
			suffixTree = new UkkonenSuffixTree(encodingLength, charStream);
			alphabetFeatureFeatureCountMap = new HashMap<Set<String>, Integer>();
			for (Set<String> alphabetFeature : alphabetFeatureSetMap.keySet()) {
				count = 0;
				alphabetEquivalenceClassFeatureSet = alphabetFeatureSetMap.get(alphabetFeature);
				for (String feature : alphabetEquivalenceClassFeatureSet) {
					count += suffixTree.noMatches(feature);
				}
				if (count > 0) {
					alphabetFeatureFeatureCountMap.put(alphabetFeature, count);
				}
			}
			alphabetFeatureFeatureCountMapTracesList.add(alphabetFeatureFeatureCountMap);
		}

		distanceMatrix = new float[charStreamList.size()][charStreamList.size()];

		int noTraces = charStreamList.size();
		double countI, countJ;
		double sum;

		Map<Set<String>, Integer> alphabetFeatureFeatureCountMapI, alphabetFeatureFeatureCountMapJ;
		Set<Set<String>> unionAlphabetFeaturesSet = new HashSet<Set<String>>();

		double[] euclideanLengthArray, traceLengthArray;

		traceLengthArray = new double[noTraces];
		euclideanLengthArray = new double[noTraces];

		int traceLength;
		for (int i = 0; i < noTraces; i++) {
			alphabetFeatureFeatureCountMapI = alphabetFeatureFeatureCountMapTracesList.get(i);
			traceLength = 0;
			sum = 0;
			for (Set<String> alphabetFeature : alphabetFeatureFeatureCountMapI.keySet()) {
				traceLength += alphabetFeatureFeatureCountMapI.get(alphabetFeature);
				sum += Math.pow(alphabetFeatureFeatureCountMapI.get(alphabetFeature), 2);
			}

			traceLengthArray[i] = traceLength;
			euclideanLengthArray[i] = Math.sqrt(sum);
		}

		for (int i = 0; i < noTraces - 1; i++) {
			distanceMatrix[i][i] = 0;
			alphabetFeatureFeatureCountMapI = alphabetFeatureFeatureCountMapTracesList.get(i);

			unionAlphabetFeaturesSet.clear();
			unionAlphabetFeaturesSet.addAll(alphabetFeatureFeatureCountMapI.keySet());

			for (int j = i + 1; j < noTraces; j++) {
				alphabetFeatureFeatureCountMapJ = alphabetFeatureFeatureCountMapTracesList.get(j);

				unionAlphabetFeaturesSet.addAll(alphabetFeatureFeatureCountMapJ.keySet());

				sum = 0.0;
				for (Set<String> alphabetFeatureIJ : unionAlphabetFeaturesSet) {
					countI = countJ = 0;
					if (alphabetFeatureFeatureCountMapI.containsKey(alphabetFeatureIJ)) {
						countI = alphabetFeatureFeatureCountMapI.get(alphabetFeatureIJ);
					}

					if (alphabetFeatureFeatureCountMapJ.containsKey(alphabetFeatureIJ)) {
						countJ = alphabetFeatureFeatureCountMapJ.get(alphabetFeatureIJ);
					}

					switch (normalizationType) {
						case TRACE_LENGTH :
							if (traceLengthArray[i] != 0) {
								countI /= traceLengthArray[i];
							}
							if (traceLengthArray[j] != 0) {
								countJ /= traceLengthArray[j];
							}
							break;
						case EUCLIDEAN_LENGTH :
							if (euclideanLengthArray[i] != 0) {
								countI /= euclideanLengthArray[i];
							}
							if (euclideanLengthArray[j] != 0) {
								countJ /= euclideanLengthArray[j];
							}
							break;
						default :
							break;
					}

					sum += Math.pow((countI - countJ), 2);
				}

				if ((traceLengthArray[i] != 0) || (traceLengthArray[j] != 0)) {
					distanceMatrix[i][j] = distanceMatrix[j][i] = (float) Math.sqrt(sum);
				} else {
					distanceMatrix[i][j] = distanceMatrix[j][i] = Integer.MAX_VALUE;
				}
			}
		}
	}

	/**
	 * This constructor is used when a feature matrix is provided
	 * 
	 * @param featureMatrix
	 */
	public EuclideanDistance(int[][] featureMatrix, Normalization normalizationType) {
		int noTraces = featureMatrix.length;
		int noFeatures = featureMatrix[0].length;
		double sum = 0;
		double countI, countJ;
		double[] traceLengthArray, euclideanLengthArray;

		traceLengthArray = new double[noTraces];
		euclideanLengthArray = new double[noTraces];

		float traceLength;
		for (int i = 0; i < noTraces; i++) {
			traceLength = 0;
			sum = 0;
			for (int j = 0; j < noFeatures; j++) {
				traceLength += featureMatrix[i][j];
				sum += Math.pow(featureMatrix[i][j], 2);
			}
			traceLengthArray[i] = traceLength;
			euclideanLengthArray[i] = Math.sqrt(sum);
		}

		distanceMatrix = new float[noTraces][noTraces];
		for (int i = 0; i < noTraces - 1; i++) {
			distanceMatrix[i][i] = 0;
//			long currentTime = System.currentTimeMillis(); // TODO: remove
//			System.out.println("In loop: " + i + " out of " + noTraces + " (Elapsed time: " + Math.floor((currentTime - startTime)/1000) + ")"); // TODO: remove
			System.out.println("In loop: " + i + " out of " + noTraces); // TODO: remove
			for (int j = i + 1; j < noTraces; j++) {
				sum = 0.0;
				for (int k = 0; k < noFeatures; k++) {
					countI = featureMatrix[i][k];
					countJ = featureMatrix[j][k];

					switch (normalizationType) {
						case TRACE_LENGTH :
							if (traceLengthArray[i] != 0) {
								countI /= traceLengthArray[i];
							}
							if (traceLengthArray[j] != 0) {
								countJ /= traceLengthArray[j];
							}
							break;
						case EUCLIDEAN_LENGTH :
							if (euclideanLengthArray[i] != 0) {
								countI /= euclideanLengthArray[i];
							}
							if (euclideanLengthArray[j] != 0) {
								countJ /= euclideanLengthArray[j];
							}
							break;
						default :
					}

					sum += Math.pow((countI - countJ), 2);
				}
				if ((traceLengthArray[i] != 0) || (traceLengthArray[j] != 0)) {
					distanceMatrix[i][j] = distanceMatrix[j][i] = (float) Math.sqrt(sum);
				} else {
					distanceMatrix[i][j] = distanceMatrix[j][i] = Integer.MAX_VALUE;
				}
			}
		}
	}

	public float[][] getDistanceMatrix() {
		return distanceMatrix;
	}

	public List<Map<String, Integer>> getFeatureFeatureCountMapTracesList() {
		return featureFeatureCountMapTracesList;
	}

	public List<Map<Set<String>, Integer>> getAlphabetFeatureFeatureCountMapTracesList() {
		return alphabetFeatureFeatureCountMapTracesList;
	}
}

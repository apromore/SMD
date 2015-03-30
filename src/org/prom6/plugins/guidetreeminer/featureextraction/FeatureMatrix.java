package org.prom6.plugins.guidetreeminer.featureextraction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.prom6.plugins.guidetreeminer.types.FilterType;
import org.prom6.plugins.guidetreeminer.util.EquivalenceClass;
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

public class FeatureMatrix {
	int[][] featureMatrix;
	List<String> sequenceFeatureList;
	List<Set<String>> alphabetFeatureList;

	public FeatureMatrix(int encodingLength, List<String> charStreamList, Set<String> sequenceFeatureSet) {
		int noTraces = charStreamList.size();
		int noFeatures = sequenceFeatureSet.size();

		featureMatrix = new int[noTraces][noFeatures];

		/*
		 * To maintain the order of the features, we first need to convert the
		 * set of features into a list of features
		 */
		sequenceFeatureList = new ArrayList<String>();
		for (String feature : sequenceFeatureSet) {
			sequenceFeatureList.add(feature);
		}

		sequenceFeatureSet = null;

		UkkonenSuffixTree suffixTree;
		int count;
		for (int i = 0; i < noTraces; i++) {
			suffixTree = new UkkonenSuffixTree(encodingLength, charStreamList.get(i));
			for (int j = 0; j < noFeatures; j++) {
				count = suffixTree.noMatches(sequenceFeatureList.get(j));
				featureMatrix[i][j] = count;
			}
		}
	}

	/**
	 * Cater to feature matrix generation with consideration of non-overlaps
	 * 
	 * @param encodingLength
	 * @param charStreamList
	 * @param sequenceFeatureSet
	 */
	public FeatureMatrix(int encodingLength, List<String> charStreamList, Set<String> sequenceFeatureSet,
			FilterType filterType) {
		int noTraces = charStreamList.size();
		int noFeatures = sequenceFeatureSet.size();

		featureMatrix = new int[noTraces][noFeatures];

		/*
		 * To maintain the order of the features, we first need to convert the
		 * set of features into a list of features
		 */
		sequenceFeatureList = new ArrayList<String>();
		for (String feature : sequenceFeatureSet) {
			sequenceFeatureList.add(feature);
		}

		EquivalenceClass equivalenceClass = new EquivalenceClass();
		Map<String, Set<String>> startAlphabetEquivalenceClassMap = equivalenceClass.getStartSymbolEquivalenceClassMap(
				encodingLength, sequenceFeatureSet);
		Set<String> startAlphabetEquivalenceSet, overlapStartAlphabetEquivalenceSet;

		sequenceFeatureSet = null;

		Map<String, Integer> featureFeatureCountMap = new HashMap<String, Integer>();
		int charStreamLength, noMatches;
		String charStream, startSymbol, overlapStartSymbol;
		Pattern pattern, overlapPattern;
		Matcher matcher, overlapMatcher;
		boolean foundOverlapMatch;
		for (int i = 0; i < noTraces; i++) {
			charStream = charStreamList.get(i);
			charStreamLength = charStream.length() / encodingLength;
			featureFeatureCountMap.clear();

			for (int j = 0; j < charStreamLength; j++) {
				startSymbol = charStream.substring(j * encodingLength, (j + 1) * encodingLength);
				if (startAlphabetEquivalenceClassMap.containsKey(startSymbol)) {
					startAlphabetEquivalenceSet = startAlphabetEquivalenceClassMap.get(startSymbol);
					for (String feature : startAlphabetEquivalenceSet) {
						pattern = Pattern.compile("(" + feature + "){1,}");
						matcher = pattern.matcher(charStream);
						if (matcher.find(j * encodingLength) && (matcher.start() == j * encodingLength)) {
							noMatches = 0;
							//check for overlap matches
							foundOverlapMatch = false;
							for (int k = j + 1; k < matcher.end() / encodingLength; k++) {
								overlapStartSymbol = charStream.substring(k * encodingLength, (k + 1) * encodingLength);
								if (startAlphabetEquivalenceClassMap.containsKey(overlapStartSymbol)) {
									overlapStartAlphabetEquivalenceSet = startAlphabetEquivalenceClassMap
											.get(overlapStartSymbol);
									for (String overlapFeature : overlapStartAlphabetEquivalenceSet) {
										if (overlapFeature.length() < feature.length()) {
											//since the current feature's length under inspection for overlap is smaller than that of the outer feature, no point in looking further; 
											break;
										}
										overlapPattern = Pattern.compile("(" + overlapFeature + "){1,}");
										overlapMatcher = overlapPattern.matcher(charStream);
										if (overlapMatcher.find(k * encodingLength)
												&& (overlapMatcher.start() == k * encodingLength)) {
											//found an overlap match
											foundOverlapMatch = true;
											j = k - 1;
											noMatches = (k - j) / encodingLength;
											break;
										}
									}
									if (foundOverlapMatch) {
										break;
									}
								}
							}
							if (!foundOverlapMatch) {
								j = matcher.end() / encodingLength - 1;
								noMatches = (matcher.end() - matcher.start()) / encodingLength;
							}
							featureFeatureCountMap.put(feature, noMatches);
							break;
						}
					}
				}
			}

			for (int j = 0; j < noFeatures; j++) {
				if (featureFeatureCountMap.containsKey(sequenceFeatureList.get(j))) {
					featureMatrix[i][j] = featureFeatureCountMap.get(sequenceFeatureList.get(j));
				} else {
					featureMatrix[i][j] = 0;
				}

			}
		}
	}

	public FeatureMatrix(int encodingLength, List<String> charStreamList,
			Map<Set<String>, Set<String>> alphabetFeatureSetMap) {
		int noTraces = charStreamList.size();
		int noFeatures = alphabetFeatureSetMap.size();

		/*
		 * To maintain the order of the attributes/features, we first convert
		 * the keySet of alphabets to a list of alphabets
		 */
		alphabetFeatureList = new ArrayList<Set<String>>();
		for (Set<String> alphabet : alphabetFeatureSetMap.keySet()) {
			alphabetFeatureList.add(alphabet);
		}

		featureMatrix = new int[noTraces][noFeatures];
		Set<String> alphabet;
		Set<String> alphabetEquivalenceFeatureSet;
		int count;

		UkkonenSuffixTree suffixTree;
		for (int i = 0; i < noTraces; i++) {
			suffixTree = new UkkonenSuffixTree(encodingLength, charStreamList.get(i));

			for (int j = 0; j < noFeatures; j++) {
				count = 0;
				alphabet = alphabetFeatureList.get(j);
				if (alphabetFeatureSetMap.containsKey(alphabet)) {
					alphabetEquivalenceFeatureSet = alphabetFeatureSetMap.get(alphabet);
					for (String feature : alphabetEquivalenceFeatureSet) {
						count += suffixTree.noMatches(feature);
					}
				}
				featureMatrix[i][j] = count;
			}
		}
	}

	public int[][] getFeatureMatrix() {
		return featureMatrix;
	}

	public List<String> getSequenceFeatureList() {
		return sequenceFeatureList;
	}

	public List<Set<String>> getAlphabetFeatureList() {
		return alphabetFeatureList;
	}
}

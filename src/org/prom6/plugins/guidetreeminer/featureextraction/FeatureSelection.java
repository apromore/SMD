package org.prom6.plugins.guidetreeminer.featureextraction;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JFrame;

import org.prom6.plugins.guidetreeminer.encoding.ActivityOverFlowException;
import org.prom6.plugins.guidetreeminer.encoding.EncodeActivitySet;
import org.prom6.plugins.guidetreeminer.swingx.ErrorDialog;
import org.prom6.plugins.guidetreeminer.types.FilterType;
import org.prom6.plugins.guidetreeminer.util.EquivalenceClass;
import org.prom6.plugins.guidetreeminer.util.FileIO;
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
public class FeatureSelection {
	int encodingLength;
	Set<String> charStreamSet;

	public FeatureSelection(int encodingLength) {
		this.encodingLength = encodingLength;
	}

	public FeatureSelection(int encodingLength, Set<String> charStreamSet) {
		this.encodingLength = encodingLength;
		this.charStreamSet = charStreamSet;
	}

	public FeatureSelection(int encodingLength, List<String> charStreamList) {
		this.encodingLength = encodingLength;
		charStreamSet = new HashSet<String>();
		charStreamSet.addAll(charStreamList);
	}

	/*
	 * This method is to be invoked to filter repeat sequences (sequence
	 * features) based on non-overlap criteria; The repeat definition does not
	 * take care of the overlapping repeats; for e.g,. consider the traces
	 * abcdabxabcd and xycda In these two traces, the maximal repeats are abcd,
	 * cda, ab and x; Now the repeat cda in the first trace is overlapping with
	 * abcd Filtering repeats with non-overlap criteria will get all repeats
	 * that exist at least once without any overlap with other repeats
	 */
	public Set<String> getFilteredSequenceFeatureSet(Set<String> featureSet, FilterType filterType) {
		switch (filterType) {
			case NONOVERLAP :
				return filterSequenceFeatureSetNonOverlap(featureSet);
			default :
				ErrorDialog.showErrorDialog(new JFrame(),"<HTML> Wrong Method Invoked in FeatureSelection; <BR>This Method is defined only for non-overlap filter <BR>Use Other Methods of FeatureSelection</HTML>");
				System.exit(0);
				return null;
		}
	}

	/*
	 * This method is to be invoked to filter repeat sequences that are
	 * below/above a certain length; For e.g., ignore all repeats that are
	 * individual alphabets themselves (length = 1)
	 * 
	 * Input: Set of all sequence features Output: Set of all sequence features
	 * whose lengths are above/below a given threshold
	 */
	public Set<String> getFilteredSequenceFeatureSet(Set<String> featureSet, FilterType filterType, int minThreshold,
			int maxThreshold) {
		switch (filterType) {
			case LENGTH :
				return filterSequenceFeatureSetLength(featureSet, minThreshold, maxThreshold);
			default :
				ErrorDialog.showErrorDialog(new JFrame(),"<HTML> Wrong Method Invoked in FeatureSelection; <BR>This Method is defined only for length filter <BR>Use Other Methods of FeatureSelection</HTML>");
				System.exit(0);
				return null;
		}
	}

	/*
	 * This method is to be invoked to filter sequence features that occur in a
	 * specified range of number of instances or that occur in a specified
	 * number of times
	 */
	public Map<String, Integer> getFilteredSequenceFeatureSet(Set<String> featureSet, FilterType filterType,
			float minInstanceFrequencyCountPercentageThreshold, float maxInstanceFrequencyCountPercentageThreshold) {
		switch (filterType) {
			case INSTANCE :
				return filterSequenceFeatureSetInstanceCount(featureSet, minInstanceFrequencyCountPercentageThreshold,
						maxInstanceFrequencyCountPercentageThreshold);
			case FREQUENCY :
				return filterSequenceFeatureSetFrequencyCount(featureSet, minInstanceFrequencyCountPercentageThreshold,
						maxInstanceFrequencyCountPercentageThreshold);
			default :
				return null;
		}
	}

	/*
	 * This method is to be invoked to filter alphabet features based on the
	 * size of the alphabet; For e.g., ignore all alphabets involving a single
	 * activity, involving 10 activities
	 * 
	 * Input: the alphabet feature equivalence map Output: filtered alphabet
	 * feature equivalence map
	 */
	public Set<Set<String>> getFilteredAlphabetFeatureSet(Map<Set<String>, Set<String>> alphabetFeatureSetMap,
			FilterType filterType, int minThreshold, int maxThreshold) {
		switch (filterType) {
			case LENGTH :
				return filterAlphabetFeatureSetLength(alphabetFeatureSetMap.keySet(), minThreshold, maxThreshold);
			default :
				ErrorDialog.showErrorDialog(new JFrame(),"<HTML> Wrong Method Invoked in FeatureSelection; <BR>This Method is defined only for length count filter <BR>Use Other Methods of FeatureSelection</HTML>");
				System.exit(0);
				return null;
		}
	}

	/*
	 * this method is to be invoked to filter alphabet features based on the
	 * number of instances in which they occur or based on the number of times
	 * they occur in the log For alphabet based features, for the instance
	 * count, we consider all traces where any repeat sequence in the
	 * equivalence class of the alphabet is present For frequency count, we sum
	 * the count of occurrences of all repeats under the equivalence class of
	 * the alphabet feature
	 */
	public Map<Set<String>, Integer> getFilteredAlphabetFeatureSet(Map<Set<String>, Set<String>> alphabetFeatureSetMap,
			FilterType filterType, float minInstanceFrequencyCountPercentageThreshold,
			float maxInstanceFrequencyCountPercentageThreshold) {
		switch (filterType) {
			case INSTANCE :
				return filterAlphabetFeatureSetInstanceCount(alphabetFeatureSetMap,
						minInstanceFrequencyCountPercentageThreshold, maxInstanceFrequencyCountPercentageThreshold);
			case FREQUENCY :
				return filterAlphabetFeatureSetFrequencyCount(alphabetFeatureSetMap,
						minInstanceFrequencyCountPercentageThreshold, maxInstanceFrequencyCountPercentageThreshold);
			default :
				return null;
		}
	}

	private Map<String, Integer> filterSequenceFeatureSetInstanceCount(Set<String> featureSet,
			float minInstanceCountPercentageThreshold, float maxInstanceCountPercentageThrehold) {
		Map<String, Integer> featureInstanceCountMap = new HashMap<String, Integer>();

		int count, noInstances;
		double instancePercentage;

		noInstances = charStreamSet.size();
		for (String feature : featureSet) {
			count = 0;
			for (String charStream : charStreamSet) {
				if (charStream.contains(feature)) {
					count++;
				}
			}
			instancePercentage = ((double) count) / noInstances;
			if ((instancePercentage >= minInstanceCountPercentageThreshold)
					&& (instancePercentage <= maxInstanceCountPercentageThrehold)) {
				featureInstanceCountMap.put(feature, count);
			}
		}
		return featureInstanceCountMap;
	}

	private Map<Set<String>, Integer> filterAlphabetFeatureSetInstanceCount(
			Map<Set<String>, Set<String>> alphabetFeatureSetMap, float minInstanceCountPercentageThreshold,
			float maxInstanceCountPercentageThreshold) {
		Map<Set<String>, Integer> alphabetFeatureInstanceCountMap = new HashMap<Set<String>, Integer>();

		int count, noInstances;
		double instancePercentage;

		noInstances = charStreamSet.size();

		Set<Set<String>> alphabetFeatureSet = alphabetFeatureSetMap.keySet();
		Set<String> alphabetFeatureRepeatEquivalanceClassSet;
		for (Set<String> alphabetFeature : alphabetFeatureSet) {
			count = 0;
			alphabetFeatureRepeatEquivalanceClassSet = alphabetFeatureSetMap.get(alphabetFeature);
			for (String charStream : charStreamSet) {
				for (String repeat : alphabetFeatureRepeatEquivalanceClassSet) {
					if (charStream.contains(repeat)) {
						count++;
						break;
					}
				}
			}
			instancePercentage = ((float) count) / noInstances;
			if ((instancePercentage >= minInstanceCountPercentageThreshold)
					&& (instancePercentage <= maxInstanceCountPercentageThreshold)) {
				alphabetFeatureInstanceCountMap.put(alphabetFeature, count);
			}
		}

		return alphabetFeatureInstanceCountMap;
	}

	private Map<String, Integer> filterSequenceFeatureSetFrequencyCount(Set<String> featureSet,
			float minFrequencyCountThreshold, float maxFrequencyCountThrehold) {
		Map<String, Integer> featureFrequencyCountMap = new HashMap<String, Integer>();

		int count;
		UkkonenSuffixTree suffixTree;

		for (String charStream : charStreamSet) {
			suffixTree = new UkkonenSuffixTree(encodingLength, charStream);
			for (String feature : featureSet) {
				count = suffixTree.noMatches(feature);
				if (featureFrequencyCountMap.containsKey(feature)) {
					count += featureFrequencyCountMap.get(feature);
				}
				featureFrequencyCountMap.put(feature, count);
			}
			suffixTree = null;
		}

		Set<String> featuresToFilterSet = new HashSet<String>();
		for (String feature : featureSet) {
			if (featureFrequencyCountMap.containsKey(feature)) {
				count = featureFrequencyCountMap.get(feature);
				if ((count < minFrequencyCountThreshold) || (count > maxFrequencyCountThrehold)) {
					featuresToFilterSet.add(feature);
				}
			}
		}

		for (String feature : featuresToFilterSet) {
			featureFrequencyCountMap.remove(feature);
		}

		return featureFrequencyCountMap;
	}

	private Map<Set<String>, Integer> filterAlphabetFeatureSetFrequencyCount(
			Map<Set<String>, Set<String>> alphabetFeatureSetMap, float minFrequencyCountThreshold,
			float maxFrequencyCountThrehold) {
		Map<Set<String>, Integer> alphabetFeatureFrequencyCountMap = new HashMap<Set<String>, Integer>();
		int count;
		UkkonenSuffixTree suffixTree;

		Set<Set<String>> alphabetFeatureSet = alphabetFeatureSetMap.keySet();
		Set<String> alphabetFeatureEquivalenceClassSet;

		for (String charStream : charStreamSet) {
			suffixTree = new UkkonenSuffixTree(encodingLength, charStream);
			for (Set<String> alphabetFeature : alphabetFeatureSet) {
				count = 0;

				alphabetFeatureEquivalenceClassSet = alphabetFeatureSetMap.get(alphabetFeature);
				for (String repeat : alphabetFeatureEquivalenceClassSet) {
					count += suffixTree.noMatches(repeat);
				}
				if (alphabetFeatureFrequencyCountMap.containsKey(alphabetFeature)) {
					count += alphabetFeatureFrequencyCountMap.get(alphabetFeature);
				}
				alphabetFeatureFrequencyCountMap.put(alphabetFeature, count);
			}
			suffixTree = null;
		}

		Set<Set<String>> alphabetFeaturesToFilterSet = new HashSet<Set<String>>();
		for (Set<String> alphabetFeature : alphabetFeatureSet) {
			if (alphabetFeatureFrequencyCountMap.containsKey(alphabetFeature)) {
				count = alphabetFeatureFrequencyCountMap.get(alphabetFeature);
				if ((count < minFrequencyCountThreshold) || (count > maxFrequencyCountThrehold)) {
					alphabetFeaturesToFilterSet.add(alphabetFeature);
				}
			}
		}

		for (Set<String> alphabetFeature : alphabetFeaturesToFilterSet) {
			alphabetFeatureFrequencyCountMap.remove(alphabetFeature);
		}

		return alphabetFeatureFrequencyCountMap;
	}

	private Set<String> filterSequenceFeatureSetLength(Set<String> featureSet, int minFeatureLengthThreshold,
			int maxFeatureLengthThreshold) {
		Set<String> filteredFeatureSet = new HashSet<String>();

		for (String feature : featureSet) {
			if ((feature.length() / encodingLength >= minFeatureLengthThreshold)
					&& (feature.length() / encodingLength <= maxFeatureLengthThreshold)) {
				filteredFeatureSet.add(feature);
			}
		}
		return filteredFeatureSet;
	}

	private Set<Set<String>> filterAlphabetFeatureSetLength(Set<Set<String>> alphabetFeatureSet,
			int minAlphabetSizeThreshold, int maxAlphabetSizeThreshold) {
		Set<Set<String>> filteredAlphabetFeatureSet = new HashSet<Set<String>>();

		for (Set<String> alphabetFeature : alphabetFeatureSet) {
			if ((alphabetFeature.size() >= minAlphabetSizeThreshold)
					&& (alphabetFeature.size() <= maxAlphabetSizeThreshold)) {
				filteredAlphabetFeatureSet.add(alphabetFeature);
			}
		}

		return filteredAlphabetFeatureSet;
	}

	private Set<String> filterSequenceFeatureSetNonOverlap(Set<String> featureSet) {
		Set<String> filteredFeatureSet;

		/*
		 * Store the set of all features that start with the symbol (provided as
		 * key); The longest feature is to be provided first in the set
		 */
		EquivalenceClass equivalenceClass = new EquivalenceClass();
		Map<String, Set<String>> startSymbolEquivalenceClassMap = equivalenceClass.getStartSymbolEquivalenceClassMap(
				encodingLength, featureSet);

		equivalenceClass = null;

		filteredFeatureSet = new HashSet<String>();
		int charStreamLength;
		String startSymbol;
		Set<String> startSymbolEquivalenceClassSet;
		Pattern pattern;
		Matcher matcher;
		for (String charStream : charStreamSet) {
			charStreamLength = charStream.length() / encodingLength;
			for (int i = 0; i < charStreamLength; i++) {
				startSymbol = charStream.substring(i * encodingLength, (i + 1) * encodingLength);
				if (startSymbolEquivalenceClassMap.containsKey(startSymbol)) {
					startSymbolEquivalenceClassSet = startSymbolEquivalenceClassMap.get(startSymbol);
					for (String startSymbolFeature : startSymbolEquivalenceClassSet) {
						pattern = Pattern.compile("(" + startSymbolFeature + "){1,}");
						matcher = pattern.matcher(charStream);
						if (matcher.find(i * encodingLength) && (matcher.start() == i * encodingLength)) {
							//TODO
							/*
							 * Currently, the assumption is that we do not need
							 * to consider overlap repeats seriously The reason
							 * being we are interested just in finding all
							 * repeats that occur at least once For e.g., abc
							 * and bca can be identified as repeats and it could
							 * be the case that bca always overlaps with abc;
							 * so, we don't want to consider bca
							 */
							filteredFeatureSet.add(startSymbolFeature);
							i = matcher.end() / encodingLength - 1;
							break;
						}
					}
				}
			}
		}

		return filteredFeatureSet;
	}

	public Map<Set<String>, Set<String>> getBaseFeatureAlphabetFeatureSetMap(Map<Set<String>, Set<String>> alphabetFeatureFeatureSetMap){
		Set<String> allFeatureSet = new HashSet<String>();
		for(Set<String> alphabet : alphabetFeatureFeatureSetMap.keySet()){
			allFeatureSet.addAll(alphabetFeatureFeatureSetMap.get(alphabet));
		}
		
		Set<String> baseFeatureSet = getBaseFeatureSet(allFeatureSet);
		EquivalenceClass equivalenceClass= new EquivalenceClass();

		return equivalenceClass.getAlphabetEquivalenceClassMap(encodingLength, baseFeatureSet);
	}
	
	/**
	 * 
	 */
	public Set<String> getBaseFeatureSet(Set<String> featureSet) {
		Set<String> baseFeatureSet = new HashSet<String>();

		EquivalenceClass equivalenceClass = new EquivalenceClass();
		Map<Set<String>, Set<String>> alphabetEquivalenceClassFeatureSetMap = equivalenceClass
				.getAlphabetEquivalenceClassMap(encodingLength, featureSet);

		Set<String> alphabetEquivalenceClassFeatureSet;
		Set<String> leftOutFeatureSet = new HashSet<String>();
		boolean alphabetHasBaseFeature;
		for (Set<String> alphabet : alphabetEquivalenceClassFeatureSetMap.keySet()) {
			alphabetEquivalenceClassFeatureSet = alphabetEquivalenceClassFeatureSetMap.get(alphabet);
			leftOutFeatureSet.clear();

			alphabetHasBaseFeature = false;
			for (String feature : alphabetEquivalenceClassFeatureSet) {
				if (feature.length() / encodingLength == alphabet.size()) {
					alphabetHasBaseFeature = true;
					baseFeatureSet.add(feature);
				} else {
					leftOutFeatureSet.add(feature);
				}
			}
			/*
			 * None of the repeats under this alphabet is of the same length as
			 * that of the size of the alphabet It could be the case that all
			 * the repeats are longer and that there exists repeats within this
			 * repeat Try to find the base feature within these repeats such
			 * that the base feature alphabet is the same as that of this
			 * alphabet or that the base feature length is the same as that of
			 * the size of the alphabet of the base feature
			 * 
			 * It could be made a recursive call to this function
			 */
			if (!alphabetHasBaseFeature || (leftOutFeatureSet.size() > 0)) {
				Set<String> repeatSet;
				if (!alphabetHasBaseFeature) {
					repeatSet = getRepeats(alphabetEquivalenceClassFeatureSet);
				} else {
					repeatSet = getRepeats(leftOutFeatureSet);
				}
				if (repeatSet.size() > 0) {
					baseFeatureSet.addAll(getBaseFeatureSet(repeatSet));
				}
			}
		}

		return baseFeatureSet;
	}

	private Set<String> getRepeats(Set<String> charStreamSet) {
		Set<String> repeatSet = new HashSet<String>();

		Set<String> activitySet = new HashSet<String>();
		int index = 0;
		int charStreamLength;
		for (String charStream : charStreamSet) {
			charStreamLength = charStream.length() / encodingLength;
			for (int i = 0; i < charStreamLength; i++) {
				activitySet.add(charStream.substring(i * encodingLength, (i + 1) * encodingLength));
			}
			activitySet.add("Trace" + index);
			index++;
		}

		try {
			EncodeActivitySet encodeActivitySet = new EncodeActivitySet(activitySet);
			int extendedEncodingLength = encodeActivitySet.getEncodingLength();
			Map<String, String> activityCharMap = encodeActivitySet.getActivityCharMap();
			Map<String, String> charActivityMap = encodeActivitySet.getCharActivityMap();
			index = 0;
			StringBuilder extendedCombinedCharStreamBuilder = new StringBuilder();
			for (String charStream : charStreamSet) {
				charStreamLength = charStream.length() / encodingLength;
				extendedCombinedCharStreamBuilder.setLength(0);
				for (int i = 0; i < charStreamLength; i++) {
					extendedCombinedCharStreamBuilder.append(activityCharMap.get(charStream.subSequence(i
							* encodingLength, (i + 1) * encodingLength)));
				}
				extendedCombinedCharStreamBuilder.append(activityCharMap.get("Trace" + index));
				index++;
			}

			UkkonenSuffixTree suffixTree = new UkkonenSuffixTree(extendedEncodingLength,
					extendedCombinedCharStreamBuilder.toString());
			suffixTree.findLeftDiverseNodes();
			Set<String> extendedRepeatSet = suffixTree.getMaximalRepeats();
			// Remove the super maximal Repeats
			extendedRepeatSet.removeAll(suffixTree.getSuperMaximalRepeats());

			int extendedRepeatLength;
			StringBuilder repeatStringBuilder = new StringBuilder();
			for (String extendedRepeat : extendedRepeatSet) {
				repeatStringBuilder.setLength(0);
				extendedRepeatLength = extendedRepeat.length() / extendedEncodingLength;
				for (int j = 0; j < extendedRepeatLength; j++) {
					repeatStringBuilder.append(charActivityMap.get(extendedRepeat.substring(j * extendedEncodingLength,
							(j + 1) * extendedEncodingLength)));
				}
				repeatSet.add(repeatStringBuilder.toString());
			}

		} catch (ActivityOverFlowException e) {
			e.printStackTrace();
		}

		return repeatSet;
	}

	public static void main(String[] args) {
		String inputDir = "D:\\JC\\JCTemp\\PatternAbstraction";
		String repeatFileName = "mRepeat.txt";
		FileIO io = new FileIO();
		int encodingLength = 1;
		FeatureSelection featureSelection = new FeatureSelection(encodingLength);
		Set<String> originalRepeatSet = io.readFileAsSet(inputDir, repeatFileName);
		Set<String> baseFeatureSet = featureSelection.getBaseFeatureSet(originalRepeatSet);
		EquivalenceClass equivalenceClass = new EquivalenceClass();
		io.writeToFile(inputDir, "baseAlphabetEquivalenceClassFeatureSetMap.txt", equivalenceClass
				.getAlphabetEquivalenceClassMap(encodingLength, baseFeatureSet), "@");
	}
}
